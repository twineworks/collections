package com.twineworks.collections.champ;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

final class CompactBitmapNode<K, V> implements ChampNode<K, V> {

  static final int HASH_CODE_LENGTH = 32;
  static final int TUPLE_LENGTH = 2; // K/V
  static final int BIT_PARTITION_SIZE = 5;
  static final int BIT_PARTITION_MASK = 0b11111;

  final AtomicBoolean mutable;
  final int nodeMap;
  final int dataMap;
  final Object[] nodes;

  static int mask(final int keyHash, final int shift) {
    return (keyHash >>> shift) & BIT_PARTITION_MASK;
  }

  static int bitpos(final int mask) {
    return 1 << mask;
  }

  CompactBitmapNode(final AtomicBoolean mutable, final int nodeMap, final int dataMap, final Object[] nodes) {
    this.mutable = mutable;
    this.nodeMap = nodeMap;
    this.dataMap = dataMap;
    this.nodes = nodes;
  }

  boolean isMutable() {
    return mutable != null && mutable.get();
  }

  @Override
  @SuppressWarnings("unchecked")
  public K getKey(final int index) {
    return (K) nodes[TUPLE_LENGTH * index];
  }

  @Override
  @SuppressWarnings("unchecked")
  public V getValue(final int index) {
    return (V) nodes[TUPLE_LENGTH * index + 1];
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map.Entry<K, V> getKeyValueEntry(int index) {
    return new AbstractMap.SimpleEntry<K, V>(
        (K) nodes[TUPLE_LENGTH * index],
        (V) nodes[TUPLE_LENGTH * index + 1]
    );
  }

  @SuppressWarnings("unchecked")
  @Override
  public ChampEntry<K, V> getChampEntry(final int index) {
    final int b = TUPLE_LENGTH * index;
    return new ChampEntry<>(
        (K) nodes[b],
        (V) nodes[b + 1]
    );
  }

  @Override
  @SuppressWarnings("unchecked")
  public ChampNode<K, V> getNode(final int index) {
    return (ChampNode<K, V>) nodes[nodes.length - 1 - index];
  }

  int dataIndex(final int bitpos) {
    return Integer.bitCount(dataMap & (bitpos - 1));
  }

  int nodeIndex(final int bitpos) {
    return Integer.bitCount(nodeMap & (bitpos - 1));
  }

  ChampNode<K, V> nodeAt(final int bitpos) {
    return getNode(nodeIndex(bitpos));
  }

  @Override
  public boolean containsKey(final K key, final int keyHash, final int shift) {
    final int mask = mask(keyHash, shift);
    final int bitpos = bitpos(mask);

    if ((dataMap & bitpos) != 0) { // inplace value
      final int index = dataIndex(bitpos);
      return key.equals(getKey(index));
    }

    if ((nodeMap & bitpos) != 0) { // node (not value)
      final ChampNode<K, V> subNode = nodeAt(bitpos);
      return subNode.containsKey(key, keyHash, shift + BIT_PARTITION_SIZE);
    }

    return false;
  }

  @Override
  public V findByKey(final K key, final int keyHash, final int shift) {
    final int mask = mask(keyHash, shift);
    final int bitpos = bitpos(mask);

    if ((dataMap & bitpos) != 0) { // inplace value
      final int index = dataIndex(bitpos);
      if (key.equals(getKey(index))) {
        return getValue(index);
      }

      return null;
    }

    if ((nodeMap & bitpos) != 0) { // node (not value)
      final ChampNode<K, V> subNode = nodeAt(bitpos);
      return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
    }

    return null;
  }

  @Override
  public ChampNode<K, V> update(final AtomicBoolean mutable, final K key, final V val, int keyHash, int shift, final UpdateResult<K, V> ur) {

    final int mask = mask(keyHash, shift);
    final int bitpos = bitpos(mask);

    if ((dataMap & bitpos) != 0) { // in-place value
      final int dataIndex = dataIndex(bitpos);
      final K currentKey = getKey(dataIndex);
      final V currentVal = getValue(dataIndex);

      if (currentKey.equals(key)) {
        // refuse to update to an equal value
        if (currentVal.equals(val)) {
          return this;
        }
        // update mapping
        ur.updated(currentVal);
        return copyAndSetValue(mutable, bitpos, val);
      } else {
        final ChampNode<K, V> subNodeNew = mergeTwoKeyValPairs(mutable, currentKey, currentVal, currentKey.hashCode(), key, val, keyHash, shift + BIT_PARTITION_SIZE);
        ur.modified();
        return copyAndMigrateFromInlineToNode(mutable, bitpos, subNodeNew);
      }
    } else if ((nodeMap & bitpos) != 0) { // node (not value)
      final ChampNode<K, V> subNode = nodeAt(bitpos);
      final ChampNode<K, V> subNodeNew = subNode.update(mutable, key, val, keyHash, shift + BIT_PARTITION_SIZE, ur);

      if (ur.isModified()) {
        return copyAndSetNode(mutable, bitpos, subNodeNew);
      } else {
        return this;
      }
    } else {
      // no value
      ur.modified();
      return copyAndInsertValue(mutable, bitpos, key, val);
    }

  }

  @Override
  public ChampNode<K, V> remove(final AtomicBoolean mutable, final K key, final int keyHash, final int shift, final UpdateResult<K, V> ur) {

    final int mask = mask(keyHash, shift);
    final int bitpos = bitpos(mask);

    if ((dataMap & bitpos) != 0) { // inplace value
      final int dataIndex = dataIndex(bitpos);

      if (key.equals(getKey(dataIndex))) {
        final V currentVal = getValue(dataIndex);
        ur.updated(currentVal);

        if (this.payloadArity() == 2 && this.nodeArity() == 0) {
          final int newDataMap = (shift == 0) ? (dataMap ^ bitpos) : bitpos(mask(keyHash, 0));

          if (dataIndex == 0) {
            return new CompactBitmapNode<>(mutable, 0, newDataMap, new Object[]{getKey(1), getValue(1)});
          } else {
            return new CompactBitmapNode<>(mutable, 0, newDataMap, new Object[]{getKey(0), getValue(0)});
          }
        } else {
          return copyAndRemoveValue(mutable, bitpos);
        }
      } else {
        return this;
      }
    } else if ((nodeMap & bitpos) != 0) { // node (not value)

      final ChampNode<K, V> subNode = nodeAt(bitpos);
      final ChampNode<K, V> subNodeNew = subNode.remove(mutable, key, keyHash, shift + BIT_PARTITION_SIZE, ur);

      if (!ur.isModified()) {
        return this;
      }

      switch (subNodeNew.sizePredicate()) {
        case 0: {
          throw new IllegalStateException("Sub-node must have at least one element.");
        }
        case 1: {
          if (this.payloadArity() == 0 && this.nodeArity() == 1) {
            // escalate (singleton or empty) result
            return subNodeNew;
          } else {
            // inline value (move to front)
            return copyAndMigrateFromNodeToInline(mutable, bitpos, subNodeNew);
          }
        }
        default: {
          // modify current node (set replacement node)
          return copyAndSetNode(mutable, bitpos, subNodeNew);
        }
      }
    }

    return this;
  }

  ChampNode<K, V> copyAndSetNode(final AtomicBoolean mutable, final int bitpos, final ChampNode<K, V> node) {

    final int idx = this.nodes.length - 1 - nodeIndex(bitpos);

    if (isMutable()) {
      // no copying if editable
      this.nodes[idx] = node;
      return this;
    } else {
      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length];

      // copy 'src' and set 1 element(s) at position 'idx'
      System.arraycopy(src, 0, dst, 0, src.length);
      dst[idx] = node;

      return new CompactBitmapNode<>(mutable, nodeMap, dataMap, dst);
    }
  }

  ChampNode<K, V> copyAndSetValue(final AtomicBoolean mutable, final int bitpos, final V val) {

    final int idx = TUPLE_LENGTH * dataIndex(bitpos) + 1;

    if (isMutable()) {
      // no copying if editable
      this.nodes[idx] = val;
      return this;
    } else {
      final Object[] src = this.nodes;
      final Object[] dst = new Object[src.length];

      // copy 'src' and set 1 element(s) at position 'idx'
      System.arraycopy(src, 0, dst, 0, src.length);
      dst[idx] = val;

      return new CompactBitmapNode<>(mutable, nodeMap, dataMap, dst);
    }
  }

  ChampNode<K, V> copyAndInsertValue(final AtomicBoolean mutable, final int bitpos, final K key, final V val) {

    final int idx = TUPLE_LENGTH * dataIndex(bitpos);

    final Object[] src = this.nodes;
    final Object[] dst = new Object[src.length + 2];

    // copy 'src' and insert 2 element(s) at position 'idx'
    System.arraycopy(src, 0, dst, 0, idx);
    dst[idx] = key;
    dst[idx + 1] = val;
    System.arraycopy(src, idx, dst, idx + 2, src.length - idx);

    return new CompactBitmapNode<K, V>(mutable, nodeMap, dataMap | bitpos, dst);
  }

  @SuppressWarnings("unchecked")
  ChampNode<K, V> mergeTwoKeyValPairs(final AtomicBoolean mutable, final K key0, final V val0, final int keyHash0, final K key1, final V val1, final int keyHash1, final int shift) {

    if (shift >= HASH_CODE_LENGTH) {
      return new CollisionNode<>(mutable, keyHash0, (K[]) new Object[]{key0, key1}, (V[]) new Object[]{val0, val1});
    }

    final int mask0 = mask(keyHash0, shift);
    final int mask1 = mask(keyHash1, shift);

    if (mask0 != mask1) {
      // both nodes fit on same level
      final int dataMap = bitpos(mask0) | bitpos(mask1);

      if (mask0 < mask1) {
        return new CompactBitmapNode<>(mutable, (0), dataMap, new Object[]{key0, val0, key1, val1});
      } else {
        return new CompactBitmapNode<>(mutable, (0), dataMap, new Object[]{key1, val1, key0, val0});
      }
    } else {
      final ChampNode<K, V> node = mergeTwoKeyValPairs(mutable, key0, val0, keyHash0, key1, val1, keyHash1, shift + BIT_PARTITION_SIZE);
      // values fit on next level
      final int nodeMap = bitpos(mask0);
      return new CompactBitmapNode<>(mutable, nodeMap, (0), new Object[]{node});
    }
  }

  ChampNode<K, V> copyAndMigrateFromInlineToNode(final AtomicBoolean mutable, final int bitpos, final ChampNode<K, V> node) {

    final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
    final int idxNew = this.nodes.length - TUPLE_LENGTH - nodeIndex(bitpos);

    final Object[] src = this.nodes;
    final Object[] dst = new Object[src.length - 2 + 1];

    // copy 'src' and remove 2 element(s) at position 'idxOld' and
    // insert 1 element(s) at position 'idxNew'

    System.arraycopy(src, 0, dst, 0, idxOld);
    System.arraycopy(src, idxOld + 2, dst, idxOld, idxNew - idxOld);
    dst[idxNew] = node;
    System.arraycopy(src, idxNew + 2, dst, idxNew + 1, src.length - idxNew - 2);

    return new CompactBitmapNode<>(mutable, nodeMap | bitpos, dataMap ^ bitpos, dst);
  }

  ChampNode<K, V> copyAndMigrateFromNodeToInline(final AtomicBoolean mutable, final int bitpos, final ChampNode<K, V> node) {

    final int idxOld = this.nodes.length - 1 - nodeIndex(bitpos);
    final int idxNew = TUPLE_LENGTH * dataIndex(bitpos);

    final Object[] src = this.nodes;
    final Object[] dst = new Object[src.length - 1 + 2];

    // copy 'src' and remove 1 element(s) at position 'idxOld' and
    // insert 2 element(s) at position 'idxNew'
    assert idxOld >= idxNew;
    System.arraycopy(src, 0, dst, 0, idxNew);
    dst[idxNew] = node.getKey(0);
    dst[idxNew + 1] = node.getValue(0);
    System.arraycopy(src, idxNew, dst, idxNew + 2, idxOld - idxNew);
    System.arraycopy(src, idxOld + 1, dst, idxOld + 2, src.length - idxOld - 1);

    return new CompactBitmapNode<>(mutable, nodeMap ^ bitpos, dataMap | bitpos, dst);
  }

  ChampNode<K, V> copyAndRemoveValue(final AtomicBoolean mutable, final int bitpos) {
    final int idx = TUPLE_LENGTH * dataIndex(bitpos);

    final Object[] src = this.nodes;
    final Object[] dst = new Object[src.length - 2];

    // copy 'src' and remove 2 element(s) at position 'idx'
    System.arraycopy(src, 0, dst, 0, idx);
    System.arraycopy(src, idx + 2, dst, idx, src.length - idx - 2);

    return new CompactBitmapNode<>(mutable, nodeMap, dataMap ^ bitpos, dst);
  }

  @Override
  public int payloadArity() {
    return Integer.bitCount(dataMap);
  }

  @Override
  public int nodeArity() {
    return Integer.bitCount(nodeMap);
  }

  int slotArity() {
    return nodes.length;
  }

  @Override
  public byte sizePredicate() {
    if (nodeArity() == 0) {
      switch (payloadArity()) {
        case 0:
          return SizePredicate.EMPTY;
        case 1:
          return SizePredicate.ONE;
        default:
          return SizePredicate.MORE_THAN_ONE;
      }
    } else {
      return SizePredicate.MORE_THAN_ONE;
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ChampNode<K, V> dup(AtomicBoolean mutable) {

    // useful only on transient nodes, to create template transients with pre-allocated memory structures
    // which can be copied once, then their values are set.

    final Object[] src = this.nodes;
    final Object[] dst = new Object[src.length];

    int firstNodeAt = TUPLE_LENGTH * payloadArity();

    // copy all values
    System.arraycopy(src, 0, dst, 0, firstNodeAt);

    // dup all nodes
    for (int i = firstNodeAt; i < dst.length; i++) {
      dst[i] = ((ChampNode<K, V>) src[i]).dup(mutable);
    }

    return new CompactBitmapNode<>(mutable, nodeMap, dataMap, dst);

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
    CompactBitmapNode<?, ?> that = (CompactBitmapNode<?, ?>) other;
    if (nodeMap != that.nodeMap) {
      return false;
    }
    if (dataMap != that.dataMap) {
      return false;
    }

    return Arrays.equals(this.nodes, that.nodes);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 0;
    result = prime * result + nodeMap;
    result = prime * result + dataMap;
    result = prime * result + Arrays.hashCode(nodes);
    return result;
  }

  @Override
  public boolean hasPayload() {
    return dataMap != 0;
  }

  @Override
  public boolean hasNodes() {
    return nodeMap != 0;
  }

}
