package com.twineworks.collections.champ;

import org.junit.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class TransientChampMapTest {

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
    for (char c = 1;  c < 128; c++){
      for (char d = 1; d < 128; d++){
        for (char e = 1; d < 128; d++) {
          strKeys.add((String.valueOf(c) + d) + e);
        }
      }
    }
  }

  @Test
  public void empty_has_zero_size() {
    assertThat(new TransientChampMap<>().size()).isEqualTo(0);
  }

  @Test
  public void insert_into_empty() {

    TransientChampMap<String, Long> map = new TransientChampMap<>();

    map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(1L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void reset_in_singleton() {

    TransientChampMap<String, Long> map = new TransientChampMap<>();

    map.set("a",1L);
    map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(1L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void replace_in_singleton() {

    TransientChampMap<String, Long> map = new TransientChampMap<>();

    map.set("a",1L);
    map.set("a",2L);
    assertThat(map.size()).isEqualTo(1);

    assertThat(map.containsKey("a")).isTrue();
    assertThat(map.get("a")).isEqualTo(2L);

    assertThat(map.containsKey("b")).isFalse();
    assertThat(map.get("b")).isNull();
  }

  @Test
  public void split_common_prefix_insert() {

    TransientChampMap<Long, Long> map = new TransientChampMap<>();

    map.set(1L, 1L);
    map.set(1025L, 2L);
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

    TransientChampMap<Collider, Long> map = new TransientChampMap<>();

    Collider a = new Collider("a");
    Collider b = new Collider("b");
    Collider c = new Collider("c");

    assertThat(a.hashCode()).isEqualTo(b.hashCode());

    map.set(a, 1L);
    map.set(b, 2L);
    assertThat(map.size()).isEqualTo(2);

    assertThat(map.containsKey(a)).isTrue();
    assertThat(map.get(a)).isEqualTo(1L);

    assertThat(map.containsKey(b)).isTrue();
    assertThat(map.get(b)).isEqualTo(2L);

    assertThat(map.containsKey(c)).isFalse();
    assertThat(map.get(c)).isNull();
  }

  @Test
  public void insert_and_remove_many() {

    TransientChampMap<String, String> map = new TransientChampMap<>();

    for(String k: strKeys){
      map.set(k, k);
      assertThat(map.containsKey(k)).isTrue();
      assertThat(map.get(k)).isEqualTo(k);
    }

    assertThat(map.size()).isEqualTo(strKeys.size());

    int size = map.size();
    for (String s : strKeys) {

      map.remove(s);
      size--;

      assertThat(map.containsKey(s)).isFalse();
      assertThat(map.get(s)).isNull();
      assertThat(map.size()).isEqualTo(size);
    }

  }

  @Test
  public void insert_into_and_remove_from_empty() {

    TransientChampMap<String, Long> map = new TransientChampMap<>();

    map.set("a",1L);
    assertThat(map.size()).isEqualTo(1);
    map.remove("a");

    assertThat(map.size()).isEqualTo(0);
    assertThat(map.containsKey("a")).isFalse();
    assertThat(map.get("a")).isNull();
  }

  @Test
  public void freezes_after_inserts() {

    TransientChampMap<Long, Long> map = new TransientChampMap<>();

    map.set(0L, 0L);
    map.set(1L, 1L);
    map.set(2L, 2L);
    map.set(3L, 3L);
    map.set(64L, 64L);
    map.set(1025L, 1025L);

    ChampMap<Long, Long> p = map.freeze();
    Iterator<Map.Entry<Long, Long>> iter = p.entryIterator();

    ArrayList<Long> ks = new ArrayList<>();
    ArrayList<Long> vs = new ArrayList<>();

    while(iter.hasNext()){
      Map.Entry<Long, Long> entry = iter.next();
      ks.add(entry.getKey());
      vs.add(entry.getValue());
    }

    assertThat(ks).isEqualTo(vs);

    ks.sort(Comparator.comparingLong((x) -> x));

    assertThat(ks.get(0)).isEqualTo(0L);
    assertThat(ks.get(1)).isEqualTo(1L);
    assertThat(ks.get(2)).isEqualTo(2L);
    assertThat(ks.get(3)).isEqualTo(3L);
    assertThat(ks.get(4)).isEqualTo(64L);
    assertThat(ks.get(5)).isEqualTo(1025L);

  }

  @Test
  public void compares_as_equal_after_inserts() {

    TransientChampMap<Long, Long> map = new TransientChampMap<>();
    map.set(0L, 0L);
    map.set(1L, 1L);
    map.set(2L, 2L);
    map.set(3L, 3L);
    map.set(64L, 64L);
    map.set(1025L, 1025L);
    ChampMap<Long, Long> p = map.freeze();

    ChampMap<Long, Long> p2 = ChampMap.empty();
    p2 = p2.set(0L, 0L);
    p2 = p2.set(1L, 1L);
    p2 = p2.set(2L, 2L);
    p2 = p2.set(3L, 3L);
    p2 = p2.set(64L, 64L);
    p2 = p2.set(1025L, 1025L);

    assertThat(p).isEqualTo(p2);

  }

  @Test
  public void compares_as_equal_after_insert_and_remove_many() {

    TransientChampMap<String, String> map = new TransientChampMap<>();
    ChampMap<String, String> p2 = ChampMap.empty();

    for(String k: strKeys){
      map.set(k, k);
      p2 = p2.set(k, k);
    }

    for (String s : strKeys) {
      map.remove(s);
      p2 = p2.remove(s);
    }
    ChampMap<String, String> p1 = map.freeze();

    assertThat(p1).isEqualTo(p2);

  }

  @Test
  public void compares_as_equal_after_insert_many() {

    TransientChampMap<String, String> map = new TransientChampMap<>();
    ChampMap<String, String> p2 = ChampMap.empty();

    for(String k: strKeys){
      map.set(k, k);
      p2 = p2.set(k, k);
    }

    ChampMap<String, String> p1 = map.freeze();
    assertThat(p1).isEqualTo(p2);

  }

}
