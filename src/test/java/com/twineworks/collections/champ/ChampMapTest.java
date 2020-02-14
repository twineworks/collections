package com.twineworks.collections.champ;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class ChampMapTest {

  private static class Collider{
    private final String name;

    private Collider(String name) {
      this.name = name;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Collider collider = (Collider) o;
      return Objects.equals(name, collider.name);
    }

    @Override
    public int hashCode() {
      // always collide
      return 42;
    }
  }

  private static ArrayList<String> strKeys = new ArrayList<>();

  static {
    for (char c = 10;  c < 128; c++){
      for (char d = 10; d < 128; d++){
        strKeys.add(String.valueOf(c) + d);
      }
    }
  }

  @Test
  public void empty_has_zero_size() {
    assertThat(ChampMap.empty().size()).isEqualTo(0);
  }

  @Test
  public void insert_into_empty() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(1L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void insert_and_remove_many() {

    ChampMap<String, String> map = ChampMap.empty();

    for(String k: strKeys){
      map = map.set(k, k);
      assertThat(map.containsKey(k)).isTrue();
      assertThat(map.get(k)).isEqualTo(k);
    }

    assertThat(map.size()).isEqualTo(strKeys.size());

    int size = map.size();
    for (String s : map.keySet()) {

      map = map.remove(s);
      size--;

      assertThat(map.containsKey(s)).isFalse();
      assertThat(map.get(s)).isNull();
      assertThat(map.size()).isEqualTo(size);
    }

  }

  @Test
  public void insert_into_and_remove_from_empty() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);
    map = map.remove("a");

    assertThat(map.containsKey("a")).isFalse();
    assertThat(map.get("a")).isNull();
  }

  @Test
  public void reset_in_singleton() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(1L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void replace_in_singleton() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("a",2L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(2L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void split_common_prefix_insert() {

    ChampMap<Long, Long> map = ChampMap.empty();

    map = map.set(1L, 1L);
    map = map.set(1025L, 2L);
    assertThat(map.size()).isEqualTo(2);

    assertThat(map.containsKey(1L)).isTrue();
    assertThat(map.get(1L)).isEqualTo(1L);

    assertThat(map.containsKey(1025L)).isTrue();
    assertThat(map.get(1025L)).isEqualTo(2L);

    assertThat(map.containsKey(2L)).isFalse();
    assertThat(map.get(2L)).isNull();
  }


  @Test
  public void hash_collision_insert() {

    ChampMap<Collider, Long> map = ChampMap.empty();

    Collider a = new Collider("a");
    Collider b = new Collider("b");
    Collider c = new Collider("c");

    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    map = map.set(a, 1L);
    map = map.set(b, 2L);
    assertThat(map.size()).isEqualTo(2);

    assertThat(map.containsKey(a)).isTrue();
    assertThat(map.get(a)).isEqualTo(1L);

    assertThat(map.containsKey(b)).isTrue();
    assertThat(map.get(b)).isEqualTo(2L);

    assertThat(map.containsKey(c)).isFalse();
    assertThat(map.get(c)).isNull();
  }

  @Test
  public void iterates_over_keys() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("b",2L);
    map = map.set("c",3L);
    assertThat(map.size()).isEqualTo(3);

    ArrayList<String> keys = new ArrayList<>();
    Iterator<String> ki = map.keyIterator();
    while(ki.hasNext()){
      keys.add(ki.next());
    }

    assertThat(keys).containsExactly("a", "b", "c");

  }

  @Test
  public void key_set() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("b",2L);
    map = map.set("c",3L);
    assertThat(map.size()).isEqualTo(3);

    assertThat(map.keySet()).containsExactly("a", "b", "c");

  }

  @Test
  public void values() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("b",2L);
    map = map.set("c",3L);
    assertThat(map.size()).isEqualTo(3);

    assertThat(map.values()).containsExactly(1L, 2L, 3L);

  }

  @Test
  public void iterates_over_values() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("b",2L);
    map = map.set("c",3L);
    assertThat(map.size()).isEqualTo(3);

    ArrayList<Long> values = new ArrayList<>();
    Iterator<Long> vi = map.valueIterator();
    while(vi.hasNext()){
      values.add(vi.next());
    }

    assertThat(values).containsExactly(1L, 2L, 3L);

  }

  @Test
  public void iterates_over_entries() {

    ChampMap<String, Long> map = ChampMap.empty();

    map = map.set("a",1L);
    map = map.set("b",2L);
    map = map.set("c",3L);
    assertThat(map.size()).isEqualTo(3);

    ArrayList<String> keys = new ArrayList<>();
    ArrayList<Long> values = new ArrayList<>();
    Iterator<Map.Entry<String, Long>> vi = map.entryIterator();
    while(vi.hasNext()){
      Map.Entry<String, Long> n = vi.next();
      keys.add(n.getKey());
      values.add(n.getValue());
      // a=1, b=2 etc.
      assertThat(n.getKey().charAt(0)).isEqualTo((char)('a'+n.getValue()-1));
    }

    assertThat(keys).containsExactly("a", "b", "c");
    assertThat(values).containsExactly(1L, 2L, 3L);

  }

}
