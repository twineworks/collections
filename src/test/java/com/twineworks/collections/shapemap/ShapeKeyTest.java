/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
  public void get_all_provides_a_set_of_keys_for_strings() throws Exception {
    Set<ShapeKey> keys = ShapeKey.getAll("foo", "bar", "baz");
    assertThat(keys).isNotNull();
    assertThat(keys.size()).isEqualTo(3);
    assertThat(keys).contains(
      ShapeKey.get("foo"),
      ShapeKey.get("bar"),
      ShapeKey.get("baz")
    );
  }

  @Test
  public void get_all_provides_a_set_of_keys_for_keys() throws Exception {
    Set<ShapeKey> keys = ShapeKey.getAll(ShapeKey.get("foo"), ShapeKey.get("bar"), ShapeKey.get("baz"));
    assertThat(keys).isNotNull();
    assertThat(keys.size()).isEqualTo(3);
    assertThat(keys).contains(
      ShapeKey.get("foo"),
      ShapeKey.get("bar"),
      ShapeKey.get("baz")
    );
  }

}