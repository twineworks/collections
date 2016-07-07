package com.twineworks.collections.shapemap;

import org.junit.Test;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ShapeMapTest {

  private static ShapeKey a = ShapeKey.get("a");
  private static ShapeKey b = ShapeKey.get("b");
  private static ShapeKey c = ShapeKey.get("c");
  private static ShapeKey d = ShapeKey.get("d");

  @Test
  public void put_via_string() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    m.puts("a", "prev");
    String prev = m.puts("a", "foo");
    assertThat(prev).isEqualTo("prev");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void put_via_accessor() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    m.puta(a_a, "prev");
    String prev = m.puta(a_a, "foo");
    assertThat(prev).isEqualTo("prev");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void set_via_string() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    m.sets("a", "foo");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void set_via_accessor() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    m.seta(a_a, "foo");
    assertThat(m.get(a)).isEqualTo("foo");
    assertThat(m.gets("a")).isEqualTo("foo");
    assertThat(m.get(b)).isNull();
  }

  @Test
  public void remove_via_string() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    m.removes("a");
    assertThat(m.get(a)).isNull();
  }

  @Test
  public void get_miss() throws Exception {
    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap<String> m = new ShapeMap<>();
    assertThat(a_a.get(m)).isNull();
  }

  @Test
  public void get_find() throws Exception {

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap<String> m = new ShapeMap<>();
    a_a.set(m, "foo");
    assertThat(a_a.get(m)).isEqualTo("foo");

  }

  @Test
  public void get_via_string_find() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    assertThat(m.gets("a")).isEqualTo("foo");

  }

  @Test
  public void get_via_accessor_miss() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    assertThat(m.geta(a_a)).isNull();
  }

  @Test
  public void get_via_accessor_find() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    assertThat(m.geta(a_a)).isEqualTo("foo");

  }

  @Test
  public void contains_key_via_string() throws Exception {
    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    assertThat(m.containsStrKey("a")).isTrue();
    assertThat(m.containsStrKey("b")).isFalse();
  }

  @Test
  public void get_find_multiple() throws Exception {

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap.Accessor<String> a_b = ShapeMap.accessor(b);
    ShapeMap.Accessor<String> a_c = ShapeMap.accessor(c);
    ShapeMap.Accessor<String> a_d = ShapeMap.accessor(d);

    ShapeMap<String> m = new ShapeMap<>();
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

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap.Accessor<String> a_b = ShapeMap.accessor(b);
    ShapeMap.Accessor<String> a_c = ShapeMap.accessor(c);
    ShapeMap.Accessor<String> a_d = ShapeMap.accessor(d);
    ShapeMap.Accessor<String> a_m1 = ShapeMap.accessor(ShapeKey.get("missing_1"));
    ShapeMap.Accessor<String> a_m2 = ShapeMap.accessor(ShapeKey.get("missing_2"));

    ShapeMap<String> m = new ShapeMap<>();
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

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap.Accessor<String> a_b = ShapeMap.accessor(b);
    ShapeMap.Accessor<String> a_c = ShapeMap.accessor(c);

    ShapeMap<String> m = new ShapeMap<>();
    a_a.set(m, "foo");
    a_b.set(m, "bar");

    assertThat(a_a.put(m, "a")).isEqualTo("foo");
    assertThat(a_b.put(m, "b")).isEqualTo("bar");

    // no previous value for c
    assertThat(a_c.put(m, "c")).isNull();

  }

  @Test
  public void set_extends_to_5_shapes() throws Exception {

    ShapeMap.Accessor<String> a_1 = ShapeMap.accessor(ShapeKey.get("1"));
    ShapeMap.Accessor<String> a_2 = ShapeMap.accessor(ShapeKey.get("2"));
    ShapeMap.Accessor<String> a_3 = ShapeMap.accessor(ShapeKey.get("3"));
    ShapeMap.Accessor<String> a_4 = ShapeMap.accessor(ShapeKey.get("4"));
    ShapeMap.Accessor<String> a_5 = ShapeMap.accessor(ShapeKey.get("5"));

    ShapeMap.Accessor<String> a_6 = ShapeMap.accessor(ShapeKey.get("6"));

    ShapeMap<String> m = new ShapeMap<>();
    Shape s_0 = m.shape;

    a_1.set(m, "one");
    Shape s_1 = m.shape;

    a_2.set(m, "two");
    Shape s_2 = m.shape;

    a_3.set(m, "three");
    Shape s_3 = m.shape;

    a_4.set(m, "four");
    Shape s_4 = m.shape;

    a_5.set(m, "five");
    Shape s_5 = m.shape;

    // shapes are all different (note: complete verification requires n*(n+1)/2 comparisons)
    // sticking to simpler heuristic, verifying a transition took place on each set
    assertThat(s_0 == s_1).isFalse();
    assertThat(s_1 == s_2).isFalse();
    assertThat(s_2 == s_3).isFalse();
    assertThat(s_3 == s_4).isFalse();
    assertThat(s_4 == s_5).isFalse();

    // verify integrity of map
    assertThat(a_1.put(m, "1")).isEqualTo("one");
    assertThat(a_2.put(m, "2")).isEqualTo("two");
    assertThat(a_3.put(m, "3")).isEqualTo("three");
    assertThat(a_4.put(m, "4")).isEqualTo("four");
    assertThat(a_5.put(m, "5")).isEqualTo("five");

    // a_6 never had a value
    assertThat(a_6.put(m, "6")).isNull();


  }

  @Test
  public void shapes_transition_on_same_extension_paths() throws Exception {

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap.Accessor<String> a_b = ShapeMap.accessor(b);
    ShapeMap.Accessor<String> a_c = ShapeMap.accessor(c);

    // make m1 create some shapes
    ShapeMap<String> m1 = new ShapeMap<>();
    Shape s1_0 = m1.shape;

    a_a.set(m1, "one");
    Shape s1_a = m1.shape;

    a_b.set(m1, "two");
    Shape s1_ab = m1.shape;

    a_c.set(m1, "three");
    Shape s1_abc = m1.shape;

    // make m2 follow the transition pointers
    ShapeMap<String> m2 = new ShapeMap<>();
    Shape s2_0 = m2.shape;

    a_a.set(m2, "one");
    Shape s2_a = m2.shape;

    a_b.set(m2, "two");
    Shape s2_ab = m2.shape;

    a_c.set(m2, "three");
    Shape s2_abc = m2.shape;

    // verify instance equality of created shapes
    assertThat(s1_0 == s2_0).isTrue();
    assertThat(s1_a == s2_a).isTrue();
    assertThat(s1_ab == s2_ab).isTrue();
    assertThat(s1_abc == s2_abc).isTrue();


    // verify integrity of maps
    assertThat(a_a.put(m1, "1")).isEqualTo("one");
    assertThat(a_b.put(m1, "2")).isEqualTo("two");
    assertThat(a_c.put(m1, "3")).isEqualTo("three");
    assertThat(a_a.put(m2, "1")).isEqualTo("one");
    assertThat(a_b.put(m2, "2")).isEqualTo("two");
    assertThat(a_c.put(m2, "3")).isEqualTo("three");


  }

  @Test
  public void shapes_do_not_transition_on_different_extension_paths() throws Exception {

    ShapeMap.Accessor<String> a_a = ShapeMap.accessor(a);
    ShapeMap.Accessor<String> a_b = ShapeMap.accessor(b);
    ShapeMap.Accessor<String> a_c = ShapeMap.accessor(c);

    // make m1 create some shapes
    ShapeMap<String> m1 = new ShapeMap<>();
    Shape s1_0 = m1.shape;

    a_a.set(m1, "one");
    Shape s1_a = m1.shape;

    a_b.set(m1, "two");
    Shape s1_ab = m1.shape;

    a_c.set(m1, "three");
    Shape s1_abc = m1.shape;

    // make m2 follow the transition pointers {} -> :a then break by adding :c, :b
    ShapeMap<String> m2 = new ShapeMap<>();
    Shape s2_0 = m2.shape;

    a_a.set(m2, "one");
    Shape s2_a = m2.shape;

    a_c.set(m2, "three");
    Shape s2_ac = m2.shape;

    a_b.set(m2, "two");
    Shape s2_acb = m2.shape;

    // verify instance equality of created shapes
    assertThat(s1_0 == s2_0).isTrue();
    assertThat(s1_a == s2_a).isTrue(); // followed from {} -> :a
    assertThat(s1_ab != s2_ac).isTrue();   // extended :a by :b or :c respectively
    assertThat(s1_abc != s2_acb).isTrue(); // further drift

    // verify integrity of maps
    assertThat(a_a.put(m1, "1")).isEqualTo("one");
    assertThat(a_b.put(m1, "2")).isEqualTo("two");
    assertThat(a_c.put(m1, "3")).isEqualTo("three");
    assertThat(a_a.put(m2, "1")).isEqualTo("one");
    assertThat(a_b.put(m2, "2")).isEqualTo("two");
    assertThat(a_c.put(m2, "3")).isEqualTo("three");

  }

  /*
    tests for reference cleanup upon mapping removal
  */

  @Test
  public void removing_mapping_by_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.remove(a);
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_keyset_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.keySet().remove(a);
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_keyset_iterator_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    Iterator<ShapeKey> iterator = m.keySet().iterator();
    iterator.next();
    iterator.remove();
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_entry_set_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.entrySet().remove(new AbstractMap.SimpleEntry<ShapeKey, String>(a, "foo"));
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_entry_set_iterator_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    Iterator<Map.Entry<ShapeKey, String>> iterator = m.entrySet().iterator();
    iterator.next();
    iterator.remove();
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_value_collection_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    m.values().remove("foo");
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void removing_mapping_by_value_collection_iterator_remove_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // remove, verify storage is null
    Iterator<String> iterator = m.values().iterator();
    iterator.next();
    iterator.remove();
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void clear_nulls_storage() throws Exception {

    ShapeMap<String> m = new ShapeMap<>();
    m.put(a, "foo");
    int idx = m.shape.idxFor(a);
    assertThat(m.storage[idx]).isEqualTo("foo");

    // clear, verify storage is null
    m.clear();
    assertThat(m.storage[idx]).isNull();

  }

  @Test
  public void converts_a_string_keyed_map() throws Exception {

    Map<String, Object> srcMap = new HashMap<>();
    srcMap.put("k1", "foo");
    srcMap.put("k2", "bar");
    srcMap.put("k3", "baz");

    ShapeMap<Object> m = new ShapeMap<>(srcMap);
    assertThat(m.get(ShapeKey.get("k1"))).isEqualTo("foo");
    assertThat(m.get(ShapeKey.get("k2"))).isEqualTo("bar");
    assertThat(m.get(ShapeKey.get("k3"))).isEqualTo("baz");
    assertThat(m.size()).isEqualTo(3);

  }

  @Test
  public void allows_var_arg_construction() throws Exception {
    ShapeMap<String> m = new ShapeMap<>(String.class, a, "Hello", b, "World");
    assertThat(m.get(a)).isEqualTo("Hello");
    assertThat(m.get(b)).isEqualTo("World");
    assertThat(m.get(c)).isNull();

  }

  @Test
  public void allows_var_arg_construction_with_string_keys() throws Exception {
    ShapeMap<String> m = new ShapeMap<>(String.class, "a", "Hello", "b", "World");
    assertThat(m.get(a)).isEqualTo("Hello");
    assertThat(m.get(b)).isEqualTo("World");
    assertThat(m.get(c)).isNull();

  }
}