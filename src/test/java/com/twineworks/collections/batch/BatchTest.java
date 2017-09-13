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

package com.twineworks.collections.batch;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BatchTest {

  @Test
  public void is_initially_empty() throws Exception {
    Batch<?> batch = new Batch<>(2);
    assertThat(batch.isEmpty()).isTrue();
  }

  @Test
  public void reports_as_full_on_capacity() throws Exception {
    Batch<Object> batch = new Batch<>(3);
    assertThat(batch.isEmpty()).isTrue();
    assertThat(batch.isFull()).isFalse();

    batch.add(0L);
    assertThat(batch.isFull()).isFalse();
    assertThat(batch.isEmpty()).isFalse();

    batch.add(1L);
    assertThat(batch.isFull()).isFalse();
    assertThat(batch.isEmpty()).isFalse();

    batch.add(2L);
    assertThat(batch.isFull()).isTrue();
    assertThat(batch.isEmpty()).isFalse();
  }

  @Test(expected = ArrayIndexOutOfBoundsException.class)
  public void throws_adding_over_capacity() throws Exception {
    Batch<Object> batch = new Batch<>(1);
    batch.add(0L);
    batch.add(1L); // throws here
  }

  @Test
  public void removes_items_in_insertion_order() throws Exception {
    Batch<Object> batch = new Batch<>(3);
    batch.add(0L);
    batch.add(1L);
    batch.add(2L);

    assertThat(batch.remove()).isEqualTo(0L);
    assertThat(batch.remove()).isEqualTo(1L);
    assertThat(batch.remove()).isEqualTo(2L);
  }


  @Test
  public void does_not_throw_when_removing_from_empty() throws Exception {
    Batch<Object> batch = new Batch<>(2);
    batch.add(0L);
    batch.add(1L);

    batch.remove(); // 0L
    batch.remove(); // 1L
    batch.remove(); // usage error does not throw, you must check for isEmpty yourself

  }


  @Test
  public void can_interleave_add_and_remove() throws Exception {
    Batch<Object> batch = new Batch<>(3);
    batch.add(0L);
    assertThat(batch.isEmpty()).isFalse();

    batch.add(1L);
    batch.add(2L);


    assertThat(batch.remove()).isEqualTo(0L);
    assertThat(batch.remove()).isEqualTo(1L);
    assertThat(batch.isEmpty()).isFalse();

    assertThat(batch.remove()).isEqualTo(2L);
    assertThat(batch.isEmpty()).isTrue();

    batch.add(0L);
    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.remove()).isEqualTo(0L);
    assertThat(batch.isEmpty()).isTrue();

    batch.add(1L);
    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.remove()).isEqualTo(1L);
    assertThat(batch.isEmpty()).isTrue();

    batch.add(2L);
    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.remove()).isEqualTo(2L);
    assertThat(batch.isEmpty()).isTrue();

    batch.add(0L);
    batch.add(1L);
    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.remove()).isEqualTo(0L);
    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.remove()).isEqualTo(1L);
    assertThat(batch.isEmpty()).isTrue();

  }

  @Test
  public void can_clear() throws Exception {
    Batch<Object> batch = new Batch<>(3);
    batch.add(0L);
    batch.add(1L);
    batch.add(2L);

    assertThat(batch.isEmpty()).isFalse();
    assertThat(batch.isFull()).isTrue();

    batch.clear();
    assertThat(batch.isEmpty()).isTrue();
    assertThat(batch.isFull()).isFalse();

  }

}