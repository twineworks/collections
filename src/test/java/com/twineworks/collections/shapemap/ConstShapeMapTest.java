package com.twineworks.collections.shapemap;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class ConstShapeMapTest {

  private static final ShapeKey a = ShapeKey.get("a");
  private static final ShapeKey b = ShapeKey.get("b");
  private static final ShapeKey c = ShapeKey.get("c");
  private static final ShapeKey d = ShapeKey.get("d");

  private static final Set<ShapeKey> shape = ShapeKey.getAll(a, b, c, d);

  @Test
  public void put_via_string() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.puts("a", "prev");
    String prev = m.puts("a", "foo");
    assertThat(prev).isEqualTo("prev");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void put_via_accessor() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    m.puta(a_a, "prev");
    String prev = m.puta(a_a, "foo");
    assertThat(prev).isEqualTo("prev");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void set_via_string() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.sets("a", "foo");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void set_via_accessor() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    m.seta(a_a, "foo");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void remove_via_string() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    m.removes("a");
    assertThat(m.get(a)).isNull();
    assertThat(m.containsKey(a)).isTrue();
  }

  @Test
  public void get_miss() throws Exception {
    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    ConstShapeMap<String> m = new ConstShapeMap<>();
    assertThat(a_a.get(m)).isNull();
  }

  @Test
  public void get_find() throws Exception {

    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    a_a.set(m, "foo");
    assertThat(a_a.get(m)).isEqualTo("foo");

  }

  @Test
  public void get_via_string_find() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    assertThat(m.gets("a")).isEqualTo("foo");

  }

  @Test
  public void get_via_accessor_miss() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>();
    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    assertThat(m.geta(a_a)).isNull();
  }

  @Test
  public void get_via_accessor_find() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    assertThat(m.geta(a_a)).isEqualTo("foo");

  }

  @Test
  public void contains_keys_of_entire_shape() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    assertThat(m.containsStrKey("a")).isTrue();
    // unset, but present
    assertThat(m.containsStrKey("b")).isTrue();
    assertThat(m.containsStrKey("c")).isTrue();
    assertThat(m.containsStrKey("d")).isTrue();
    // not present in shape
    assertThat(m.containsStrKey("e")).isFalse();
  }

  @Test
  public void get_find_multiple() throws Exception {

    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    ConstShapeMap.Accessor<String> a_b = ConstShapeMap.accessor(b);
    ConstShapeMap.Accessor<String> a_c = ConstShapeMap.accessor(c);
    ConstShapeMap.Accessor<String> a_d = ConstShapeMap.accessor(d);

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    a_a.set(m, "foo");
    a_b.set(m, "bar");
    a_c.set(m, "baz");
    a_d.set(m, "que");

    assertThat(a_a.get(m)).isEqualTo("foo");
    assertThat(a_b.get(m)).isEqualTo("bar");
    assertThat(a_c.get(m)).isEqualTo("baz");
    assertThat(a_d.get(m)).isEqualTo("que");

  }

  @Test
  public void get_find_multiple_and_miss_multiple() throws Exception {

    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    ConstShapeMap.Accessor<String> a_b = ConstShapeMap.accessor(b);
    ConstShapeMap.Accessor<String> a_c = ConstShapeMap.accessor(c);
    ConstShapeMap.Accessor<String> a_d = ConstShapeMap.accessor(d);
    ConstShapeMap.Accessor<String> a_m1 = ConstShapeMap.accessor(ShapeKey.get("missing_1"));
    ConstShapeMap.Accessor<String> a_m2 = ConstShapeMap.accessor(ShapeKey.get("missing_2"));

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    a_a.set(m, "foo");
    a_b.set(m, "bar");
    a_c.set(m, "baz");
    a_d.set(m, "que");

    assertThat(a_a.get(m)).isEqualTo("foo");
    assertThat(a_b.get(m)).isEqualTo("bar");
    assertThat(a_c.get(m)).isEqualTo("baz");
    assertThat(a_d.get(m)).isEqualTo("que");

    assertThat(a_m1.get(m)).isNull();
    assertThat(a_m2.get(m)).isNull();

  }

  @Test
  public void put_returns_previous_value() throws Exception {

    ConstShapeMap.Accessor<String> a_a = ConstShapeMap.accessor(a);
    ConstShapeMap.Accessor<String> a_b = ConstShapeMap.accessor(b);
    ConstShapeMap.Accessor<String> a_c = ConstShapeMap.accessor(c);

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    a_a.set(m, "foo");
    a_b.set(m, "bar");

    assertThat(a_a.put(m, "a")).isEqualTo("foo");
    assertThat(a_b.put(m, "b")).isEqualTo("bar");

    // no previous value for c
    assertThat(a_c.put(m, "c")).isNull();

  }


  /*
    tests for reference cleanup upon mapping removal
  */

  @Test
  public void removing_mapping_by_remove_nulls_storage_and_retains_key() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.remove(a);
    assertThat(m.storage[idx]).isNull();
    assertThat(m.keySet()).contains(a);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void removing_mapping_by_keyset_not_supported() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // try to remove a key via keyset
    m.keySet().remove(a);

  }

  @Test(expected = UnsupportedOperationException.class)
  public void removing_mapping_by_keyset_iterator_remove_not_supported() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // try to remove a key via keyset iterator
    Iterator<ShapeKey> iterator = m.keySet().iterator();
    iterator.next();
    iterator.remove();

  }

  @Test(expected = UnsupportedOperationException.class)
  public void removing_mapping_by_entry_set_remove_not_supported() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // try to remove key via entryset
    m.entrySet().remove(new AbstractMap.SimpleEntry<ShapeKey, String>(a, "foo"));

  }

  @Test(expected = UnsupportedOperationException.class)
  public void removing_mapping_by_entry_set_iterator_remove_not_supported() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // try to remove key via entryset iterator
    Iterator<Map.Entry<ShapeKey, String>> iterator = m.entrySet().iterator();
    iterator.next();
    iterator.remove();

  }

  @Test
  public void removing_mapping_by_value_collection_remove_nulls_storage_and_retains_keyset() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.values().remove("foo");
    assertThat(m.storage[idx]).isNull();
    assertThat(m.keySet()).isEqualTo(shape);

  }

  @Test
  public void removing_mapping_by_value_collection_iterator_remove_nulls_storage_and_retains_keyset() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    Iterator<String> iterator = m.values().iterator();

    while(iterator.hasNext()){
      String v = iterator.next();
      if ("foo".equals(v)){
        iterator.remove();
      }
    }

    assertThat(m.storage[idx]).isNull();
    assertThat(m.keySet()).isEqualTo(shape);

  }

  @Test
  public void clear_nulls_storage_and_retains_shape() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(shape);
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // clear, verify storage is null
    m.clear();
    assertThat(m.storage[idx]).isNull();
    assertThat(m.keySet()).isEqualTo(shape);

  }

  @Test
  public void converts_a_string_keyed_map() throws Exception {

    Map<String, Object> srcMap = new HashMap<>();
    srcMap.put("k1", "foo");
    srcMap.put("k2", "bar");
    srcMap.put("k3", "baz");

    ConstShapeMap<Object> m = new ConstShapeMap<>(srcMap);
    assertThat(m.get(ShapeKey.get("k1"))).isEqualTo("foo");
    assertThat(m.get(ShapeKey.get("k2"))).isEqualTo("bar");
    assertThat(m.get(ShapeKey.get("k3"))).isEqualTo("baz");
    assertThat(m.size()).isEqualTo(3);

  }

  @Test
  public void allows_var_arg_construction() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(String.class, a, "Hello", b, "World");
    assertThat(m.get(a)).isEqualTo("Hello");
    assertThat(m.get(b)).isEqualTo("World");
    assertThat(m.get(c)).isNull();

  }

  @Test
  public void allows_var_arg_construction_with_string_keys() throws Exception {
    ConstShapeMap<String> m = new ConstShapeMap<>(String.class, "a", "Hello", "b", "World");
    assertThat(m.get(a)).isEqualTo("Hello");
    assertThat(m.get(b)).isEqualTo("World");
    assertThat(m.get(c)).isNull();
  }


  @Test
  public void remove_then_add() throws Exception {

    ConstShapeMap<String> m = new ConstShapeMap<>(String.class,
      "a", "a",
      "b", "b");

    assertThat(m.keySet().contains(ShapeKey.get("a"))).isTrue();
    assertThat(m.keySet().contains(ShapeKey.get("b"))).isTrue();

    m.removes("a");
    assertThat(m.containsValue("a")).isFalse();
    m.removes("b");
    assertThat(m.containsValue("b")).isFalse();

    m.sets("a", "a");
    m.puts("b", "b");

    assertThat(m.keySet().contains(ShapeKey.get("a"))).isTrue();
    assertThat(m.keySet().contains(ShapeKey.get("b"))).isTrue();

    assertThat(m.gets("a")).isEqualTo("a");
    assertThat(m.gets("b")).isEqualTo("b");


  }

}