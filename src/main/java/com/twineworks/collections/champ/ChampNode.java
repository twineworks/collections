package com.twineworks.collections.champ;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public interface ChampNode<K, V> {

  boolean hasPayload();

  boolean hasNodes();

  int payloadArity();

  int nodeArity();

  ChampNode<K, V> getNode(final int index);

  K getKey(final int index);

  V getValue(final int index);

  Map.Entry<K, V> getKeyValueEntry(final int index);

  ChampEntry<K, V> getChampEntry(final int index);

  boolean containsKey(final K key, final int keyHash, final int shift);

  V findByKey(final K key, final int keyHash, final int shift);

  ChampNode<K, V> update(final AtomicBoolean mutable, final K key, final V val, final int keyHash, final int shift, final UpdateResult<K, V> ur);

  ChampNode<K, V> remove(final AtomicBoolean mutable, final K key, final int keyHash, final int shift, final UpdateResult<K, V> ur);

  byte sizePredicate();

  ChampNode<K, V> dup(final AtomicBoolean mutable);

}
