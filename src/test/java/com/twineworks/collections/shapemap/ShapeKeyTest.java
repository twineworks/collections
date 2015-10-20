package com.twineworks.collections.shapemap;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ShapeKeyTest {

  @Test
  public void get_provides_a_key() throws Exception {
    ShapeKey foo = ShapeKey.get("foo");
    assertThat(foo).isNotNull();
    assertThat(foo.toString()).isEqualTo("foo");
  }

  @Test
  public void get_interns_a_key() throws Exception {
    ShapeKey foo1 = ShapeKey.get("foo");
    assertThat(foo1).isNotNull();
    ShapeKey foo2 = ShapeKey.get("foo");
    assertThat(foo1).isSameAs(foo2);
  }

  @Test
  public void get_all_provides_a_set_of_keys() throws Exception {
    Set<ShapeKey> keys = ShapeKey.getAll("foo", "bar", "baz");
    assertThat(keys).isNotNull();
    assertThat(keys.size()).isEqualTo(3);
    assertThat(keys).contains(
      ShapeKey.get("foo"),
      ShapeKey.get("bar"),
      ShapeKey.get("baz")
    );
  }

}