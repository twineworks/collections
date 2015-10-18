/*
The MIT License (MIT)

Copyright (c) 2015 Twineworks GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/


package io.twineworks.collections.shapemap;

import java.util.*;

public class ShapeMap<T> implements Map<ShapeKey, T> {

  public Shape shape;
  public Object[] storage = null;
  public final Set<ShapeKey> keys = new HashSet<>();

  public ShapeMap(){
    shape = Shapes.forKeySet(Collections.<ShapeKey>emptySet());
    shape.init(this);
  }

  public ShapeMap(Set<ShapeKey> keys){
    shape = Shapes.forKeySet(keys);
    this.keys.addAll(keys);
    shape.init(this);
  }

  public ShapeMap(Map<String, ? extends T> map){
    Set<String> strKeys = map.keySet();
    for (String strKey : strKeys) {
      this.keys.add(ShapeKey.get(strKey));
    }

    shape = Shapes.forKeySet(keys);
    shape.init(this);

    for (ShapeKey key : keys) {
      storage[shape.idxFor(key)] = map.get(key.toString());
    }

  }

  public ShapeMap(ShapeKey... keys){

    Collections.addAll(this.keys, keys);

    shape = Shapes.forKeySet(this.keys);
    shape.init(this);
  }

  public static <V> ShapeMap.Accessor<V> accessor(ShapeKey k){
    Objects.requireNonNull(k);
    return new PolymorphicAccessor<>(k);
  }

  @Override
  public int size() {
    return keys.size();
  }

  @Override
  public boolean isEmpty() {
    return keys.size() == 0;
  }

  @Override
  public boolean containsKey(Object key) {
    return key instanceof ShapeKey && keys.contains(key);
  }

  @Override
  public boolean containsValue(Object value) {

    for (ShapeKey key : keys) {
      int idx = shape.idxFor(key);
      Object v = storage[idx];
      if (Objects.equals(value, v)) return true;
    }

    return false;

  }

  @Override
  @SuppressWarnings("unchecked")
  public T get(Object key) {
    return (T) storage[shape.idxFor((ShapeKey)key)];
  }

  @Override
  @SuppressWarnings("unchecked")
  public T put(ShapeKey key, T value) {

    Objects.requireNonNull(key);

    int idx = shape.idxFor(key);
    if (idx > 0){
      T prev = (T) storage[idx];
      storage[idx] = value;
      return prev;
    }
    else{
      shape = Shapes.extendBy(shape, key);
      shape.ensureCapacity(this);
      keys.add(key);
      idx = shape.idxFor(key);
      storage[idx] = value;
      return null;
    }

  }

  @Override
  @SuppressWarnings("unchecked")
  public T remove(Object key) {

    // removes the key from keyset, does not change the shape
    ShapeKey k = (ShapeKey) key;
    if (keys.remove(k)){
      int idx = shape.idxFor(k);
      T v = (T) storage[idx];
      storage[idx] = null;
      return v;
    }

    return null;

  }

  private void clearKeyData(ShapeKey k){
    int idx = shape.idxFor(k);
    storage[idx] = null;
  }

  @Override
  public void putAll(Map<? extends ShapeKey, ? extends T> m) {

    HashSet<ShapeKey> newKeys = new HashSet<>(m.keySet());
    keys.addAll(newKeys);
    shape = shape.extendBy(newKeys);
    shape.ensureCapacity(this);

    for (ShapeKey key : newKeys) {
      Objects.requireNonNull(key);
      storage[shape.idxFor(key)] = m.get(key);
    }

  }

  public void extendShape(Set<ShapeKey> newKeys){
    shape = shape.extendBy(newKeys);
    shape.ensureCapacity(this);
  }

  @Override
  public void clear() {
    // just clears the keys and values, does not change the shape
    Arrays.fill(storage, null);
    keys.clear();

  }

  @Override
  public Set<ShapeKey> keySet() {
    return new AbstractSet<ShapeKey>() {

      @Override
      public Iterator<ShapeKey> iterator() {
        return new Iterator<ShapeKey>() {

          private Iterator<ShapeKey> keyIterator = ShapeMap.this.keys.iterator();
          private ShapeKey k = null;

          @Override
          public boolean hasNext() {
            return keyIterator.hasNext();
          }

          @Override
          public ShapeKey next() {
            k = keyIterator.next();
            return k;
          }

          @Override
          public void remove() {
            if (k == null){
                throw new IllegalStateException("No item to remove. You did not call .next() or you've called .remove() more than once");
            }
            keyIterator.remove();
            ShapeMap.this.clearKeyData(k);
            k = null;
          }
        };
      }

      @Override
      public int size() {
          return ShapeMap.this.keys.size();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return super.removeAll(c);
      }

      @Override
      public boolean addAll(Collection<? extends ShapeKey> c) {
        Objects.requireNonNull(c);
        return super.addAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return super.retainAll(c);
      }

      @Override
      public boolean contains(Object o) {
        return ShapeMap.this.keys.contains(o);
      }
    };
  }

  @Override
  public Collection<T> values() {

    return new AbstractCollection<T>() {

      @Override
      public Iterator<T> iterator() {

        return new Iterator<T>() {

          private Iterator<ShapeKey> keyIterator = ShapeMap.this.keys.iterator();
          private ShapeKey k = null;

          @Override
          public boolean hasNext() {
            return keyIterator.hasNext();
          }

          @Override
          public T next() {
            k = keyIterator.next();
            return ShapeMap.this.get(k);
          }

          @Override
          public void remove() {

            if (k == null){
              throw new IllegalStateException("No item to remove. You did not call .next() or you've called .remove() more than once");
            }

            keyIterator.remove();
            ShapeMap.this.clearKeyData(k);
            k = null;
          }
        };

      }

      @Override
      public int size() {
        return ShapeMap.this.size();
      }

        @Override
        public boolean retainAll(Collection<?> c) {
            Objects.requireNonNull(c);
            return super.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            return super.removeAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            Objects.requireNonNull(c);
            return super.addAll(c);
        }
    };
  }

  @Override
  public Set<Entry<ShapeKey, T>> entrySet() {

    return new AbstractSet<Entry<ShapeKey, T>>(){
      @Override
      public int size() {
        return ShapeMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return ShapeMap.this.size() == 0;
      }

      @Override
      public boolean contains(Object o) {

        if (o == null) return false;
        if (!(o instanceof Map.Entry)) return false;

        Map.Entry e = (Map.Entry) o;
        if (e.getKey() instanceof ShapeKey){
          ShapeKey k = (ShapeKey) e.getKey();
          return ShapeMap.this.keys.contains(k) && Objects.equals(ShapeMap.this.get(k), e.getValue());
        }
        else{
          return false;
        }

      }

      @Override
      public Iterator<Entry<ShapeKey, T>> iterator() {
        return new Iterator<Entry<ShapeKey, T>>() {

          private Iterator<ShapeKey> keyIterator = ShapeMap.this.keys.iterator();
          private ShapeKey k = null;

          @Override
          public boolean hasNext() {
            return keyIterator.hasNext();
          }

          @Override
          public Entry<ShapeKey, T> next() {
            k = keyIterator.next();
            return new MapEntry<>(ShapeMap.this, k, ShapeMap.this.get(k));
          }

          @Override
          public void remove() {
            if (k == null){
              throw new IllegalStateException("No item to remove. You did not call .next() or you've called .remove() more than once");
            }
            keyIterator.remove();
            ShapeMap.this.clearKeyData(k);
            k = null;
          }
        };
      }

      @Override
      public boolean add(Entry<ShapeKey, T> keyObjectEntry) {
        ShapeKey k = keyObjectEntry.getKey();

        if (k == null) throw new UnsupportedOperationException("No null keys allowed");
        T v = keyObjectEntry.getValue();
        // key already present?
        if (ShapeMap.this.containsKey(k)){
          // with same value already mapped?
          Object existingValue = ShapeMap.this.get(k);
          if (Objects.equals(existingValue, v)){
            return false;
          }
        }

        // note: may overwrite an entry thus not strictly "adding" but replacing an entry
        ShapeMap.this.put(k, v);
        return true;

      }

      @Override
      public boolean remove(Object o) {
        if (contains(o)){
          Map.Entry e = (Entry) o;
          ShapeKey k = (ShapeKey) e.getKey();
          ShapeMap.this.remove(k);
          return true;
        }

        return false;
      }

      @Override
      public boolean removeAll(Collection<?> c) {
          Objects.requireNonNull(c);
          return super.removeAll(c);
      }

      @Override
      public boolean retainAll(Collection<?> c) {
          Objects.requireNonNull(c);
          return super.retainAll(c);
      }

      @Override
      public void clear() {
        ShapeMap.this.clear();
      }
    };

  }

  public boolean equals(Object o) {
    if (o == this)
      return true;

    if (!(o instanceof Map))
      return false;
    Map<?,?> m = (Map<?,?>) o;
    if (m.size() != size())
      return false;

    try {
      Iterator<Entry<ShapeKey,T>> i = entrySet().iterator();
      while (i.hasNext()) {
        Map.Entry<ShapeKey,T> e = i.next();
        ShapeKey key = e.getKey();
        T value = e.getValue();
        if (value == null) {
          if (!(m.get(key)==null && m.containsKey(key)))
            return false;
        } else {
          if (!value.equals(m.get(key)))
            return false;
        }
      }
    } catch (ClassCastException unused) {
      return false;
    } catch (NullPointerException unused) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int h = 0;
    Iterator<Entry<ShapeKey,T>> i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }

  public static interface Accessor<T> {
    T get(ShapeMap<? extends T> shapeMap);
    T put(ShapeMap<? super T> shapeMap, T v);
    void set(ShapeMap<? super T> shapeMap, T v);
  }

  private static class PolymorphicAccessor<T> implements ShapeMap.Accessor<T> {

    private final ShapeKey k;
    private Shape shape1;
    private Shape shape2;
    private Shape shape3;
    private Shape shape4;

    private int idx1 = 0;
    private int idx2 = 0;
    private int idx3 = 0;
    private int idx4 = 0;

    public PolymorphicAccessor(ShapeKey k) {
      this.k = k;
    }

    @SuppressWarnings("unchecked")
    public T get(ShapeMap<? extends T> shapeMap){

      final Shape s = shapeMap.shape;
      final Object[] storage = shapeMap.storage;

      if (s == shape1){
        return (T) storage[idx1];
      }
      else if (s == shape2){
        return (T) storage[idx2];
      }
      else if (s == shape3){
        return (T) storage[idx3];
      }
      else if (s == shape4){
        return (T) storage[idx4];
      }
      else if (shape1 == null){
        shape1 = s;
        idx1 = s.idxFor(k);
        return (T) storage[idx1];
      }
      else if (shape2 == null){
        shape2 = s;
        idx2 = s.idxFor(k);
        return (T) storage[idx2];
      }
      else if (shape3 == null){
        shape3 = s;
        idx3 = s.idxFor(k);
        return (T) storage[idx3];
      }
      else if (shape4 == null){
        shape4 = s;
        idx4 = s.idxFor(k);
        return (T) storage[idx4];
      }
      else{
        return (T) storage[s.idxFor(k)];
      }

    }

    @SuppressWarnings("unchecked")
    private Shape extendAssocBy(ShapeMap m, ShapeKey k){
      Shape newShape = Shapes.extendBy(m.shape, k);
      m.shape = newShape;
      m.keys.add(k);
      newShape.ensureCapacity(m);
      return newShape;
    }

    @SuppressWarnings("unchecked")
    public T put(ShapeMap<? super T> shapeMap, T v){

      final Shape s = shapeMap.shape;
      final Object[] storage = shapeMap.storage;

      if (s == shape1){
        T o = (T) storage[idx1];
        storage[idx1] = v;
        return o;
      }

      if (s == shape2){
        T o = (T) storage[idx2];
        storage[idx2] = v;
        return o;
      }

      if (s == shape3){
        T o = (T) storage[idx3];
        storage[idx3] = v;
        return o;
      }

      if (s == shape4){
        T o = (T) storage[idx4];
        storage[idx4] = v;
        return o;
      }

      if (shape1 == null){
        shape1 = extendAssocBy(shapeMap, k);

        idx1 = shape1.idxFor(k);
        T o = (T) shapeMap.storage[idx1];
        shapeMap.storage[idx1] = v;
        return o;
      }

      if (shape2 == null){
        shape2 = extendAssocBy(shapeMap, k);

        idx2 = shape2.idxFor(k);
        T o = (T) shapeMap.storage[idx2];
        shapeMap.storage[idx2] = v;
        return o;
      }

      if (shape3 == null){
        shape3 = extendAssocBy(shapeMap, k);

        idx3 = shape3.idxFor(k);
        T o = (T) shapeMap.storage[idx3];
        shapeMap.storage[idx3] = v;
        return o;
      }

      if (shape4 == null){
        shape4 = extendAssocBy(shapeMap, k);

        idx4 = shape4.idxFor(k);
        T o = (T) shapeMap.storage[idx4];
        shapeMap.storage[idx4] = v;
        return o;
      }

      int idx = shapeMap.shape.idxFor(k);
      // key present?
      if (idx > 0) {
        T o = (T) storage[idx];
        shapeMap.storage[idx] = v;
        return o;
      }
      // shape needs extending
      else{
        Shape newShape = extendAssocBy(shapeMap, k);
        idx = newShape.idxFor(k);
        T o = (T) shapeMap.storage[idx];
        shapeMap.storage[idx] = v;
        return o;
      }

    }

    public void set(ShapeMap<? super T> shapeMap, T v){

      final Shape s = shapeMap.shape;
      final Object[] storage = shapeMap.storage;

      if (s == shape1){
        storage[idx1] = v;
        return;
      }

      if (s == shape2){
        storage[idx2] = v;
        return;
      }

      if (s == shape3){
        storage[idx3] = v;
        return;
      }

      if (s == shape4){
        storage[idx4] = v;
        return;
      }

      if (shape1 == null){
        shape1 = extendAssocBy(shapeMap, k);

        idx1 = shape1.idxFor(k);
        shapeMap.storage[idx1] = v;
        return;
      }

      if (shape2 == null){
        shape2 = extendAssocBy(shapeMap, k);

        idx2 = shape2.idxFor(k);
        shapeMap.storage[idx2] = v;
        return;
      }

      if (shape3 == null){
        shape3 = extendAssocBy(shapeMap, k);
        idx3 = shape3.idxFor(k);
        shapeMap.storage[idx3] = v;
        return;
      }

      if (shape4 == null){
        shape4 = extendAssocBy(shapeMap, k);

        idx4 = shape4.idxFor(k);
        shapeMap.storage[idx4] = v;
        return;
      }

      int idx = shapeMap.shape.idxFor(k);
      // key present?
      if (idx > 0) {
        shapeMap.storage[idx] = v;
      }
      // shape needs extending
      else{
        Shape newShape = extendAssocBy(shapeMap, k);
        shapeMap.storage[newShape.idxFor(k)] = v;
      }


    }

  }

  private static class MapEntry<T> implements Map.Entry<ShapeKey, T> {

    private final ShapeKey key;
    private T value;
    private final ShapeMap<T> shapeMap;

    public MapEntry(ShapeMap<T> shapeMap, ShapeKey key, T value) {
      this.key = key;
      this.shapeMap = shapeMap;
      this.value = value;
    }

    @Override
    public ShapeKey getKey() {
      return key;
    }

    @Override
    public T getValue() {
      return value;
    }

    @Override
    public T setValue(T value) {
      T oldValue = this.value;
      shapeMap.put(key, value);
      this.value = value;
      return oldValue;
    }

    public int hashCode() {
      return (key   == null ? 0 :   key.hashCode()) ^
        (value == null ? 0 : value.hashCode());
    }

    private static boolean eq(Object o1, Object o2) {
      return o1 == null ? o2 == null : o1.equals(o2);
    }

    public boolean equals(Object o) {
      if (!(o instanceof MapEntry))
        return false;
      @SuppressWarnings("unchecked")
      MapEntry<T> e = (MapEntry<T>)o;
      return eq(key, e.getKey()) && eq(value, e.getValue());
    }

  }


}
