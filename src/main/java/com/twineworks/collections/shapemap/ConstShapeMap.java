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


package com.twineworks.collections.shapemap;

import java.util.*;

public class ConstShapeMap<T> implements Cloneable {

  public Shape shape;
  public Object[] storage = null;

  public ConstShapeMap(){
    shape = Shapes.forKeySet(Collections.<ShapeKey>emptySet());
    shape.initConst(this);
  }

  @SuppressWarnings("unchecked")
  public ConstShapeMap(ConstShapeMap input){
    shape = input.shape;
    Object[] src = input.storage;
//    this.storage = Arrays.copyOf(src, src.length);
    this.storage = new Object[src.length];
    System.arraycopy(src, 0, this.storage, 0, src.length);
  }

  public ConstShapeMap(Collection<ShapeKey> keys){
    shape = Shapes.forKeys(keys);
    shape.initConst(this);
  }

  public ConstShapeMap(Map<String, ? extends T> map){
    Set<String> strKeys = map.keySet();
    Set<ShapeKey> keys = ShapeKey.getAll(strKeys);
    shape = Shapes.forKeySet(keys);
    shape.initConst(this);

    for (ShapeKey key : keys) {
      int idx = shape.idxFor(key);
      storage[idx] = map.get(key.toString());
    }

  }

  public ConstShapeMap(ShapeKey... keys){
    shape = Shapes.forKeySet(ShapeKey.getAll(keys));
    shape.initConst(this);

  }

  // convenience constructor useful for tests
  // expects k1, T1, k2, T2, etc.. arguments
  // clazz argument is used to cast the Tns ensuring
  // compile-time type safety
  public ConstShapeMap(Class<T> clazz, Object ... keysAndValues){

    if (keysAndValues.length % 2 != 0){
      throw new IllegalArgumentException("cannot initialize map: keys and values must come in pairs");
    }

    ArrayList<ShapeKey> keys = new ArrayList<>();
    ArrayList<T> values = new ArrayList<>();

    for (int i = 0; i < keysAndValues.length; i+=2) {
      Object oKey = keysAndValues[i];
      Object oVal = keysAndValues[i + 1];

      // turn key to shapeKey
      if (oKey == null) {
        throw new NullPointerException("Keys cannot be null");
      }

      ShapeKey k;
      if (oKey instanceof ShapeKey) {
        k = (ShapeKey) oKey;
      } else {
        k = ShapeKey.get(oKey.toString());
      }

      keys.add(k);
      values.add(clazz.cast(oVal));

    }

    HashSet<ShapeKey> keySet = new HashSet<>();
    keySet.addAll(keys);
    shape = Shapes.forKeySet(keySet);
    shape.initConst(this);

    for (int i = 0; i < keys.size(); i++) {
      ShapeKey key = keys.get(i);
      int idx = shape.idxFor(key);
      storage[idx] = values.get(i);
    }

  }

  public static <V> ConstShapeMap.Accessor<V> accessor(ShapeKey k){
    Objects.requireNonNull(k);
    return new PolymorphicAccessor<>(k);
  }

  // convenience method
  public static <V> ConstShapeMap.Accessor<V> accessor(String k){
    Objects.requireNonNull(k);
    return new PolymorphicAccessor<>(ShapeKey.get(k));
  }

  public int size() {
    return shape.size();
  }

  public boolean isEmpty() {
    return shape.size() == 0;
  }

  public boolean containsKey(Object key) {
    return key instanceof ShapeKey && shape.keySet().contains(key);
  }

  // convenience method when performance is not important
  // converts given key to ShapeKey and calls containsKey
  public boolean containsStrKey(String key) {
    return containsKey(ShapeKey.get(key));
  }

  public boolean containsValue(Object value) {

    for (ShapeKey key : shape.keySet()) {
      int idx = shape.idxFor(key);
      Object v = storage[idx];
      if (Objects.equals(value, v)) return true;
    }

    return false;

  }

  @SuppressWarnings("unchecked")
  public T get(Object key) {
    int idx = shape.idxFor((ShapeKey)key);
    return (T) storage[idx];
  }

  // convenience method if performance is not an issue
  // converts given key to ShapeKey and calls get
  public T gets(String key){
    return get(ShapeKey.get(key));
  }

  // convenience method to
  // allow code in the shape of map.geta(accessor) instead of accessor.get(map)
  public T geta(ConstShapeMap.Accessor<T> accessor){
    return accessor.get(this);
  }

  // convenience method to
  // allow code in the shape of map.puta(accessor, value) instead of accessor.put(map, value)
  public T puta(ConstShapeMap.Accessor<T> accessor, T value){
    return accessor.put(this, value);
  }

  // convenience method to
  // allow code in the shape of map.seta(accessor, value) instead of accessor.set(map, value)
  public void seta(ConstShapeMap.Accessor<T> accessor, T value){
    accessor.set(this, value);
  }


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
      throw new IllegalArgumentException("Can only access present fields");
    }

  }

  // convenience method if performance is not an issue
  // converts given key to ShapeKey and calls put
  public T puts(String key, T value){
    return put(ShapeKey.get(key), value);
  }

  @SuppressWarnings("unchecked")
  public T remove(Object key) {

    ShapeKey k = (ShapeKey) key;
    int idx = shape.idxFor(k);
    T v = (T) storage[idx];
    storage[idx] = null;
    return v;

  }
  // a version of put that does not return the previous value
  @SuppressWarnings("unchecked")
  public void set(ShapeKey key, T value) {

    Objects.requireNonNull(key);

    int idx = shape.idxFor(key);
    if (idx > 0){
      storage[idx] = value;
    }
    else{
      throw new IllegalArgumentException("Can only access present fields");
    }


  }

  // convenience method if performance is not an issue
  // converts given key to ShapeKey and calls set
  public void sets(String key, T value){
    set(ShapeKey.get(key), value);
  }
  // convenience method if performance is not an issue
  // converts given key to ShapeKey and calls remove
  public T removes(String key){
    return remove(ShapeKey.get(key));
  }

  private void clearKeyData(ShapeKey k){
    int idx = shape.idxFor(k);
    storage[idx] = null;
  }

  public void clear() {
    // just clears the keys and values, does not change the shape
    Arrays.fill(storage, null);

  }

  public Set<ShapeKey> keySet() {
    return new AbstractSet<ShapeKey>() {

      @Override
      public Iterator<ShapeKey> iterator() {
        return new Iterator<ShapeKey>() {

          private Iterator<ShapeKey> keyIterator = ConstShapeMap.this.shape.keySet().iterator();
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
            ConstShapeMap.this.clearKeyData(k);
            k = null;
          }
        };
      }

      @Override
      public int size() {
          return ConstShapeMap.this.keySet().size();
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
        return ConstShapeMap.this.keySet().contains(o);
      }
    };
  }

  public Collection<T> values() {
    return new AbstractCollection<T>() {

      @Override
      public Iterator<T> iterator() {

        return new Iterator<T>() {

          private Iterator<ShapeKey> keyIterator = ConstShapeMap.this.keySet().iterator();
          private ShapeKey k = null;

          @Override
          public boolean hasNext() {
            return keyIterator.hasNext();
          }

          @Override
          public T next() {
            k = keyIterator.next();
            return ConstShapeMap.this.get(k);
          }

          @Override
          public void remove() {

            if (k == null){
              throw new IllegalStateException("No item to remove. You did not call .next() or you've called .remove() more than once");
            }

            keyIterator.remove();
            ConstShapeMap.this.clearKeyData(k);
            k = null;
          }
        };

      }

      @Override
      public int size() {
        return ConstShapeMap.this.size();
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

  public Set<Map.Entry<ShapeKey, T>> entrySet() {

    return new AbstractSet<Map.Entry<ShapeKey, T>>(){
      @Override
      public int size() {
        return ConstShapeMap.this.size();
      }

      @Override
      public boolean isEmpty() {
        return ConstShapeMap.this.size() == 0;
      }

      @Override
      public boolean contains(Object o) {

        if (o == null) return false;
        if (!(o instanceof Map.Entry)) return false;

        Map.Entry e = (Map.Entry) o;
        if (e.getKey() instanceof ShapeKey){
          ShapeKey k = (ShapeKey) e.getKey();
          return ConstShapeMap.this.shape.keySet().contains(k) && Objects.equals(ConstShapeMap.this.get(k), e.getValue());
        }
        else{
          return false;
        }

      }

      @Override
      public Iterator<Map.Entry<ShapeKey, T>> iterator() {
        return new Iterator<Map.Entry<ShapeKey, T>>() {

          private Iterator<ShapeKey> keyIterator = ConstShapeMap.this.keySet().iterator();
          private ShapeKey k = null;

          @Override
          public boolean hasNext() {
            return keyIterator.hasNext();
          }

          @Override
          public Map.Entry<ShapeKey, T> next() {
            k = keyIterator.next();
            return new MapEntry<>(ConstShapeMap.this, k, ConstShapeMap.this.get(k));
          }

          @Override
          public void remove() {
            if (k == null){
              throw new IllegalStateException("No item to remove. You did not call .next() or you've called .remove() more than once");
            }
            keyIterator.remove();
            ConstShapeMap.this.clearKeyData(k);
            k = null;
          }
        };
      }

      @Override
      public boolean add(Map.Entry<ShapeKey, T> keyObjectEntry) {
        ShapeKey k = keyObjectEntry.getKey();

        if (k == null) throw new UnsupportedOperationException("No null keys allowed");
        T v = keyObjectEntry.getValue();
        // key already present?
        if (ConstShapeMap.this.containsKey(k)){
          // with same value already mapped?
          Object existingValue = ConstShapeMap.this.get(k);
          if (Objects.equals(existingValue, v)){
            return false;
          }
        }

        // note: may overwrite an entry thus not strictly "adding" but replacing an entry
        ConstShapeMap.this.put(k, v);
        return true;

      }

      @Override
      public boolean remove(Object o) {
        if (contains(o)){
          Map.Entry e = (Map.Entry) o;
          ShapeKey k = (ShapeKey) e.getKey();
          ConstShapeMap.this.remove(k);
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
        ConstShapeMap.this.clear();
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
      Iterator<Map.Entry<ShapeKey,T>> i = entrySet().iterator();
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
    } catch (ClassCastException | NullPointerException unused) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int h = 0;
    Iterator<Map.Entry<ShapeKey,T>> i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }

  public interface Accessor<T> {
    T get(ConstShapeMap<? extends T> shapeMap);
    T put(ConstShapeMap<? super T> shapeMap, T v);
    void set(ConstShapeMap<? super T> shapeMap, T v);
  }

  private static class PolymorphicAccessor<T> implements ConstShapeMap.Accessor<T> {

    private final ShapeKey k;
    private Shape shape1;
    private Shape shape2;

    private int idx1 = 0;
    private int idx2 = 0;

    public PolymorphicAccessor(ShapeKey k) {
      this.k = k;
    }

    @SuppressWarnings("unchecked")
    public T get(ConstShapeMap<? extends T> shapeMap){

      final Shape s = shapeMap.shape;
      final Object[] storage = shapeMap.storage;

      if (s == shape1){
        return (T) storage[idx1];
      }
      else if (s == shape2){
        return (T) storage[idx2];
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
      else{
        return (T) storage[s.idxFor(k)];
      }

    }

    @SuppressWarnings("unchecked")
    public T put(ConstShapeMap<? super T> shapeMap, T v){

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

      if (shape1 == null){
        shape1 = shapeMap.shape;
        idx1 = shape1.idxFor(k);
        T o = (T) shapeMap.storage[idx1];
        shapeMap.storage[idx1] = v;
        return o;
      }

      if (shape2 == null){
        shape2 = shapeMap.shape;
        idx2 = shape2.idxFor(k);
        T o = (T) shapeMap.storage[idx2];
        shapeMap.storage[idx2] = v;
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
        throw new IllegalArgumentException("Can only access existing fields");
      }

    }

    public void set(ConstShapeMap<? super T> shapeMap, T v){

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

      if (shape1 == null){
        shape1 = shapeMap.shape;
        idx1 = shape1.idxFor(k);
        shapeMap.storage[idx1] = v;
        return;
      }

      if (shape2 == null){
        shape2 = shapeMap.shape;
        idx2 = shape2.idxFor(k);
        shapeMap.storage[idx2] = v;
        return;
      }

      int idx = shapeMap.shape.idxFor(k);
      // key present?
      if (idx > 0) {
        shapeMap.storage[idx] = v;
      }
      // shape needs extending
      else{
        throw new IllegalArgumentException("Can only access existing fields");
      }


    }

  }

  private static class MapEntry<T> implements Map.Entry<ShapeKey, T> {

    private final ShapeKey key;
    private T value;
    private final ConstShapeMap<T> shapeMap;

    public MapEntry(ConstShapeMap<T> shapeMap, ShapeKey key, T value) {
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
