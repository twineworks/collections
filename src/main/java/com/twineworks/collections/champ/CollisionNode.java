package com.twineworks.collections.champ;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

final class CollisionNode<K, V> implements ChampNode<K, V> {

  private final K[] keys;
  private final V[] vals;
  private final int hash;
  private final AtomicBoolean mutable;
  private HashMap<String, String> foo;

  CollisionNode(AtomicBoolean mutable, final int hash, final K[] keys, final V[] vals) {
    this.mutable = mutable;
    this.keys = keys;
    this.vals = vals;
    this.hash = hash;
  }

  boolean isMutable() {
    return mutable != null && mutable.get();
  }

  @Override
  public boolean containsKey(final K key, final int keyHash, final int shift) {
    if (this.hash == keyHash) {
      for (K k : keys) {
        if (key.equals(k)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public V findByKey(final K key, final int keyHash, final int shift) {
    for (int i = 0; i < keys.length; i++) {
      final K _key = keys[i];
      if (key.equals(_key)) {
        return vals[i];
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ChampNode<K, V> update(final AtomicBoolean mutable, final K key, final V val,
                                final int keyHash, final int shift, final UpdateResult<K, V> ur) {

    for (int idx = 0; idx < keys.length; idx++) {
      if (key.equals(keys[idx])) {
        final V currentVal = vals[idx];

        if (val.equals(currentVal)) {
          return this;
        } else {

          if (isMutable()) {
            vals[idx] = val;
            return this;
          }

          // replace value array
          final V[] src = this.vals;
          final V[] dst = (V[]) new Object[src.length];

          // copy 'src' and set 1 element(s) at position 'idx'
          System.arraycopy(src, 0, dst, 0, src.length);
          dst[idx] = val;

          final ChampNode<K, V> thisNew = new CollisionNode<>(mutable, this.hash, this.keys, dst);

          ur.updated(currentVal);
          return thisNew;
        }
      }
    }

    final K[] keysNew = (K[]) new Object[this.keys.length + 1];

    // copy 'this.keys' and insert 1 element(s) at position 'keys.length'

    System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
    keysNew[keys.length] = key;

    final V[] valsNew = (V[]) new Object[this.vals.length + 1];

    // copy 'this.vals' and insert 1 element(s) at position 'vals.length'
    System.arraycopy(this.vals, 0, valsNew, 0, vals.length);
    valsNew[vals.length] = val;

    ur.modified();
    return new CollisionNode<>(mutable, keyHash, keysNew, valsNew);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ChampNode<K, V> remove(AtomicBoolean mutable, K key, int keyHash, int shift, UpdateResult<K, V> ur) {
    for (int idx = 0; idx < keys.length; idx++) {
      if (key.equals(keys[idx])) {
        final V currentVal = vals[idx];
        ur.updated(currentVal);

        if (this.arity() == 1) {
          return new CompactBitmapNode<>(mutable, 0, 0, new Object[0]);
        } else if (this.arity() == 2) {

          final K theOtherKey = (idx == 0) ? keys[1] : keys[0];
          final V theOtherVal = (idx == 0) ? vals[1] : vals[0];
          return new CompactBitmapNode<K, V>(mutable, 0, 0, new Object[0])
              .update(mutable, theOtherKey, theOtherVal, keyHash, 0, ur);
        } else {
          final K[] keysNew = (K[]) new Object[this.keys.length - 1];

          // copy 'this.keys' and remove 1 element(s) at position
          // 'idx'
          System.arraycopy(this.keys, 0, keysNew, 0, idx);
          System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx - 1);

          final V[] valsNew = (V[]) new Object[this.vals.length - 1];

          // copy 'this.vals' and remove 1 element(s) at position
          // 'idx'
          System.arraycopy(this.vals, 0, valsNew, 0, idx);
          System.arraycopy(this.vals, idx + 1, valsNew, idx, this.vals.length - idx - 1);

          return new CollisionNode<>(mutable, keyHash, keysNew, valsNew);
        }
      }
    }
    return this;

  }

  @Override
  public byte sizePredicate() {
    return SizePredicate.MORE_THAN_ONE;
  }

  @Override
  public ChampNode<K, V> dup(AtomicBoolean mutable) {
    return new CollisionNode<>(mutable, hash, Arrays.copyOf(keys, keys.length), Arrays.copyOf(vals, vals.length));
  }

  int arity() {
    return keys.length;
  }

  @Override
  public boolean hasPayload() {
    return true;
  }

  @Override
  public boolean hasNodes() {
    return false;
  }

  @Override
  public int payloadArity() {
    return keys.length;
  }

  @Override
  public int nodeArity() {
    return 0;
  }

  @Override
  public ChampNode<K, V> getNode(int index) {
    throw new AssertionError("no nodes in collision node");
  }

  @Override
  public K getKey(final int index) {
    return keys[index];
  }

  @Override
  public V getValue(final int index) {
    return vals[index];
  }

  @Override
  public Map.Entry<K, V> getKeyValueEntry(int index) {
    return new AbstractMap.SimpleEntry<>(keys[index], vals[index]);
  }

  @Override
  public ChampEntry<K, V> getChampEntry(int index) {
    return new ChampEntry<>(keys[index], vals[index]);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 0;
    result = prime * result + hash;
    result = prime * result + Arrays.hashCode(keys);
    result = prime * result + Arrays.hashCode(vals);
    return result;
  }

  @Override
  public boolean equals(final Object other) {
    if (null == other) {
      return false;
    }
    if (this == other) {
      return true;
    }
    if (getClass() != other.getClass()) {
      return false;
    }

    CollisionNode<?, ?> that = (CollisionNode<?, ?>) other;

    if (hash != that.hash) {
      return false;
    }

    if (arity() != that.arity()) {
      return false;
    }

    /*
     * Linear scan for each key, because of arbitrary element order.
     */
    outerLoop:
    for (int i = 0; i < that.arity(); i++) {
      final Object otherKey = that.getKey(i);
      final Object otherVal = that.getValue(i);

      for (int j = 0; j < keys.length; j++) {
        final K key = keys[j];
        final V val = vals[j];

        if (key.equals(otherKey) && val.equals(otherVal)) {
          continue outerLoop;
        }
      }
      return false;
    }

    return true;
  }

}
