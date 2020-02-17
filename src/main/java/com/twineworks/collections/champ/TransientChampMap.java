package com.twineworks.collections.champ;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TransientChampMap<K, V> {

  final private AtomicBoolean mutable;
  private ChampNode<K, V> rootNode;
  private int cachedHashCode;
  private int cachedSize;

  public TransientChampMap() {
    this.mutable = new AtomicBoolean(true);
    ChampMap<K, V> src = ChampMap.empty();
    this.rootNode = src.rootNode;
    this.cachedHashCode = src.cachedHashCode;
    this.cachedSize = src.cachedSize;
  }

  public TransientChampMap(TransientChampMap<K, V> src) {
    this.mutable = new AtomicBoolean(true);
    this.rootNode = src.rootNode.dup(this.mutable);
    this.cachedHashCode = src.cachedHashCode;
    this.cachedSize = src.cachedSize;
  }

  public TransientChampMap(ChampMap<K, V> src) {
    this.mutable = new AtomicBoolean(true);
    this.rootNode = src.rootNode;
    this.cachedHashCode = src.cachedHashCode;
    this.cachedSize = src.cachedSize;
  }

  public TransientChampMap<K, V> dup() {
    return new TransientChampMap<>(this);
  }

  public V get(K key) {
    return rootNode.findByKey(key, key.hashCode(), 0);
  }

  public boolean containsKey(K key) {
    return rootNode.containsKey(key, key.hashCode(), 0);
  }

  public int size() {
    return cachedSize;
  }

  public void set(final K key, final V val) {
    if (!mutable.get()) {
      throw new IllegalStateException("Transient already frozen.");
    }

    final int keyHash = key.hashCode();
    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    final ChampNode<K, V> newRootNode = rootNode.update(mutable, key, val, keyHash, 0, ur);

    if (ur.isModified()) {
      if (ur.hasReplacedValue()) {
        final V old = ur.getReplacedValue();

        final int valHashOld = old.hashCode();
        final int valHashNew = val.hashCode();

        rootNode = newRootNode;
        cachedHashCode = cachedHashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
      } else {
        final int valHashNew = val.hashCode();
        rootNode = newRootNode;
        cachedHashCode += (keyHash ^ valHashNew);
        cachedSize += 1;
      }
    }

  }

  public void setAll(final Iterator<Map.Entry<K, V>> iter) {

    if (!mutable.get()) {
      throw new IllegalStateException("Transient already frozen.");
    }

    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    while (iter.hasNext()) {
      final Map.Entry<K, V> entry = iter.next();

      K key = entry.getKey();
      V val = entry.getValue();

      final int keyHash = key.hashCode();

      final ChampNode<K, V> newRootNode = rootNode.update(mutable, key, val, keyHash, 0, ur);

      if (ur.isModified()) {
        if (ur.hasReplacedValue()) {
          final V old = ur.getReplacedValue();

          final int valHashOld = old.hashCode();
          final int valHashNew = val.hashCode();

          rootNode = newRootNode;
          cachedHashCode = cachedHashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
        } else {
          final int valHashNew = val.hashCode();
          rootNode = newRootNode;
          cachedHashCode += (keyHash ^ valHashNew);
          cachedSize += 1;
        }
        ur.reset();
      }

    }

  }

  public void setAll(Iterable<Map.Entry<K, V>> entries) {
    setAll(entries.iterator());
  }

  public void setAll(Map<K, V> src) {
    setAll(src.entrySet());
  }

  public void setAll(K[] keys, V[] values){
    if (!mutable.get()) {
      throw new IllegalStateException("Transient already frozen.");
    }

    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    for (int i = 0; i < keys.length; i++) {
      K key = keys[i];
      V val = values[i];

      final int keyHash = key.hashCode();

      final ChampNode<K, V> newRootNode = rootNode.update(mutable, key, val, keyHash, 0, ur);

      if (ur.isModified()) {
        if (ur.hasReplacedValue()) {
          final V old = ur.getReplacedValue();

          final int valHashOld = old.hashCode();
          final int valHashNew = val.hashCode();

          rootNode = newRootNode;
          cachedHashCode = cachedHashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
        } else {
          final int valHashNew = val.hashCode();
          rootNode = newRootNode;
          cachedHashCode += (keyHash ^ valHashNew);
          cachedSize += 1;
        }
        ur.reset();
      }
    }

  }

  public void setAll(ChampMap<K, V> src) {

    if (!mutable.get()) {
      throw new IllegalStateException("Transient already frozen.");
    }

    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    final Iterator<ChampEntry<K, V>> iter = src.champEntryIterator();
    while (iter.hasNext()) {

      final ChampEntry<K, V> entry = iter.next();

      K key = entry.key;
      V val = entry.value;

      final int keyHash = key.hashCode();

      final ChampNode<K, V> newRootNode = rootNode.update(mutable, key, val, keyHash, 0, ur);

      if (ur.isModified()) {
        if (ur.hasReplacedValue()) {
          final V old = ur.getReplacedValue();

          final int valHashOld = old.hashCode();
          final int valHashNew = val.hashCode();

          rootNode = newRootNode;
          cachedHashCode = cachedHashCode + (keyHash ^ valHashNew) - (keyHash ^ valHashOld);
        } else {
          final int valHashNew = val.hashCode();
          rootNode = newRootNode;
          cachedHashCode += (keyHash ^ valHashNew);
          cachedSize += 1;
        }
        ur.reset();
      }

    }

  }

  public void remove(K key) {

    final int keyHash = key.hashCode();
    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    final ChampNode<K, V> newRootNode = rootNode.remove(mutable, key, keyHash, 0, ur);

    if (ur.isModified()) {
      final int valHash = ur.getReplacedValue().hashCode();
      rootNode = newRootNode;
      cachedHashCode -= (keyHash ^ valHash);
      cachedSize--;
    }

  }

  public void removeAll(Collection<K> keys){

    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    for (K key : keys) {
      final int keyHash = key.hashCode();

      final ChampNode<K, V> newRootNode = rootNode.remove(mutable, key, keyHash, 0, ur);

      if (ur.isModified()) {
        final int valHash = ur.getReplacedValue().hashCode();
        rootNode = newRootNode;
        cachedHashCode -= (keyHash ^ valHash);
        cachedSize--;
        ur.reset();
      }

    }

  }

  public ChampMap<K, V> freeze() {
    if (!mutable.get()) {
      throw new IllegalStateException("Transient already frozen.");
    }

    mutable.set(false);
    return new ChampMap<K, V>(rootNode, cachedHashCode, cachedSize);

  }

}
