package com.twineworks.collections.champ;

final public class ChampEntry<K, V> {

  final public K key;
  final public V value;

  public ChampEntry(K key, V value) {
    this.key = key;
    this.value = value;
  }

}
