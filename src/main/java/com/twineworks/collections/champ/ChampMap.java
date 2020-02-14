package com.twineworks.collections.champ;

import java.util.*;

public class ChampMap<K, V> {

  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final ChampNode EMPTY_NODE = new CompactBitmapNode(null, 0, 0, new Object[0]);
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static final ChampMap EMPTY_MAP = new ChampMap(EMPTY_NODE, 0, 0);
  final ChampNode<K, V> rootNode;
  final int cachedHashCode;
  final int cachedSize;

  public ChampMap(ChampNode<K, V> rootNode, int cachedHashCode, int cachedSize) {
    this.rootNode = rootNode;
    this.cachedHashCode = cachedHashCode;
    this.cachedSize = cachedSize;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> ChampMap<K, V> empty() {
    return ChampMap.EMPTY_MAP;
  }

  public ChampMap<K, V> set(K key, V value) {
    final int keyHash = key.hashCode();
    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    final ChampNode<K, V> newRootNode = rootNode.update(null, key, value, keyHash, 0, ur);

    if (ur.isModified()) {
      if (ur.hasReplacedValue()) {
        final int valHashOld = ur.getReplacedValue().hashCode();
        final int valHashNew = value.hashCode();

        return new ChampMap<>(newRootNode,
            cachedHashCode + ((keyHash ^ valHashNew)) - ((keyHash ^ valHashOld)), cachedSize);
      }

      final int valHash = value.hashCode();
      return new ChampMap<>(newRootNode, cachedHashCode + ((keyHash ^ valHash)), cachedSize + 1);
    }

    return this;
  }

  public ChampMap<K, V> setAll(ChampMap<K, V> m) {
    TransientChampMap<K, V> t = new TransientChampMap<>(this);
    t.setAll(m);
    return t.freeze();
  }

  public ChampMap<K, V> setAll(Map<K, V> m) {
    TransientChampMap<K, V> t = new TransientChampMap<>(this);
    t.setAll(m.entrySet().iterator());
    return t.freeze();
  }

  public V get(K key) {
    return rootNode.findByKey(key, key.hashCode(), 0);
  }

  @SuppressWarnings("unchecked")
  public boolean containsKey(Object key) {
    return rootNode.containsKey((K) key, key.hashCode(), 0);
  }

  public ChampMap<K, V> remove(K key) {

    final int keyHash = key.hashCode();
    final UpdateResult<K, V> ur = UpdateResult.unchanged();

    final ChampNode<K, V> newRootNode = rootNode.remove(null, key,
        keyHash, 0, ur);

    if (ur.isModified()) {
      final int valHash = ur.getReplacedValue().hashCode();
      return new ChampMap<K, V>(newRootNode, cachedHashCode - ((keyHash ^ valHash)),
          cachedSize - 1);
    }

    return this;
  }

  public ChampMap<K, V> removeAll(Collection<K> keys){

    ChampNode<K, V> newRootNode = rootNode;
    int cachedHashCode = this.cachedHashCode;
    int cachedSize = this.cachedSize;
    UpdateResult<K, V> ur = UpdateResult.unchanged();

    for (K key : keys) {
      final int keyHash = key.hashCode();

      newRootNode = rootNode.remove(null, key, keyHash, 0, ur);

      if (ur.isModified()) {
        final int valHash = ur.getReplacedValue().hashCode();
        cachedHashCode -= (keyHash ^ valHash);
        cachedSize--;
        ur.reset();
      }
    }

    if (cachedSize == this.cachedSize){
      return this;
    }

    return new ChampMap<>(newRootNode, cachedHashCode, cachedSize);

  }

  public int size() {
    return cachedSize;
  }

  @Override
  public int hashCode() {
    return cachedHashCode;
  }

  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object other) {
    if (other == null) return false;
    if (this == other) return true;
    if (other.getClass() != this.getClass()) return false;
    ChampMap otherMap = (ChampMap) other;
    if (cachedSize != otherMap.cachedSize) return false;
    if (cachedHashCode != otherMap.cachedHashCode) return false;
    return rootNode.equals(otherMap.rootNode);
  }

  public boolean containsValue(final Object o) {
    for (Iterator<V> iterator = valueIterator(); iterator.hasNext(); ) {
      if (iterator.next().equals(o)) {
        return true;
      }
    }
    return false;
  }

  public boolean isEmpty() {
    return cachedSize == 0;
  }

  public Iterator<K> keyIterator() {
    return new MapKeyIterator<>(rootNode);
  }

  public Iterator<V> valueIterator() {
    return new MapValueIterator<>(rootNode);
  }

  public Iterator<Map.Entry<K, V>> entryIterator() {
    return new MapEntryIterator<>(rootNode);
  }

  public Iterator<ChampEntry<K, V>> champEntryIterator() {
    return new ChampEntryIterator<>(rootNode);
  }

  public Set<K> keySet() {
    return new AbstractSet<K>() {
      @Override
      public Iterator<K> iterator() {
        return ChampMap.this.keyIterator();
      }

      @Override
      public int size() {
        return ChampMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return ChampMap.this.isEmpty();
      }

      @Override
      public void clear() {
        throw new UnsupportedOperationException();
      }

      @SuppressWarnings("unchecked")
      @Override
      public boolean contains(Object k) {
        return ChampMap.this.containsKey(k);
      }
    };
  }

  public Collection<V> values() {

    return new AbstractCollection<V>() {
      @Override
      public Iterator<V> iterator() {
        return ChampMap.this.valueIterator();
      }

      @Override
      public int size() {
        return ChampMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return ChampMap.this.isEmpty();
      }

      @Override
      public void clear() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean contains(Object v) {
        return ChampMap.this.containsValue(v);
      }
    };

  }

  public Set<Map.Entry<K, V>> entrySet() {
    return new AbstractSet<Map.Entry<K, V>>() {
      @Override
      public Iterator<Map.Entry<K, V>> iterator() {
        return new Iterator<Map.Entry<K, V>>() {
          private final Iterator<Map.Entry<K, V>> i = entryIterator();

          @Override
          public boolean hasNext() {
            return i.hasNext();
          }

          @Override
          public Map.Entry<K, V> next() {
            return i.next();
          }

          @Override
          public void remove() {
            i.remove();
          }
        };
      }

      @Override
      public int size() {
        return ChampMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return ChampMap.this.isEmpty();
      }

      @Override
      public void clear() {
        throw new UnsupportedOperationException();
      }

      @Override
      public boolean contains(Object k) {
        throw new UnsupportedOperationException();
      }
    };

  }


  private static abstract class BaseMapIterator<K, V> {

    private static final int MAX_DEPTH = 7;
    private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];
    protected int currentValueCursor;
    protected int currentValueLength;
    protected ChampNode<K, V> currentValueNode;
    @SuppressWarnings("rawtypes")
    ChampNode[] nodes = new ChampNode[MAX_DEPTH];
    private int currentStackLevel = -1;

    BaseMapIterator(ChampNode<K, V> rootNode) {
      if (rootNode.hasNodes()) {
        currentStackLevel = 0;

        nodes[0] = rootNode;
        nodeCursorsAndLengths[0] = 0;
        nodeCursorsAndLengths[1] = rootNode.nodeArity();
      }

      if (rootNode.hasPayload()) {
        currentValueNode = rootNode;
        currentValueCursor = 0;
        currentValueLength = rootNode.payloadArity();
      }
    }

    /*
     * search for next node that contains values
     */
    @SuppressWarnings("unchecked")
    private boolean searchNextValueNode() {
      while (currentStackLevel >= 0) {
        final int currentCursorIndex = currentStackLevel * 2;
        final int currentLengthIndex = currentCursorIndex + 1;

        final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
        final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

        if (nodeCursor < nodeLength) {
          final ChampNode<K, V> nextNode = nodes[currentStackLevel].getNode(nodeCursor);
          nodeCursorsAndLengths[currentCursorIndex]++;

          if (nextNode.hasNodes()) {
            /*
             * put node on next stack level for depth-first traversal
             */
            final int nextStackLevel = ++currentStackLevel;
            final int nextCursorIndex = nextStackLevel * 2;
            final int nextLengthIndex = nextCursorIndex + 1;

            nodes[nextStackLevel] = nextNode;
            nodeCursorsAndLengths[nextCursorIndex] = 0;
            nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
          }

          if (nextNode.hasPayload()) {
            /*
             * found next node that contains values
             */
            currentValueNode = nextNode;
            currentValueCursor = 0;
            currentValueLength = nextNode.payloadArity();
            return true;
          }
        } else {
          currentStackLevel--;
        }
      }

      return false;
    }

    public boolean hasNext() {
      if (currentValueCursor < currentValueLength) {
        return true;
      } else {
        return searchNextValueNode();
      }
    }

    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  protected static class MapKeyIterator<K, V> extends BaseMapIterator<K, V>
      implements Iterator<K> {

    MapKeyIterator(ChampNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public K next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getKey(currentValueCursor++);
      }
    }

  }

  protected static class MapValueIterator<K, V> extends BaseMapIterator<K, V>
      implements Iterator<V> {

    MapValueIterator(ChampNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public V next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getValue(currentValueCursor++);
      }
    }

  }

  protected static class MapEntryIterator<K, V> extends BaseMapIterator<K, V>
      implements Iterator<Map.Entry<K, V>> {

    MapEntryIterator(ChampNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public Map.Entry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getKeyValueEntry(currentValueCursor++);
      }
    }

  }

  protected static class ChampEntryIterator<K, V> extends BaseMapIterator<K, V>
      implements Iterator<ChampEntry<K, V>> {

    ChampEntryIterator(ChampNode<K, V> rootNode) {
      super(rootNode);
    }

    @Override
    public ChampEntry<K, V> next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      } else {
        return currentValueNode.getChampEntry(currentValueCursor++);
      }
    }

  }

}
