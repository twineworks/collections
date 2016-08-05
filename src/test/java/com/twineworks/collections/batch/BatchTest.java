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
    batch.add(1L);
    batch.add(2L);

    assertThat(batch.remove()).isEqualTo(0L);
    assertThat(batch.remove()).isEqualTo(1L);
    assertThat(batch.remove()).isEqualTo(2L);

    batch.add(0L);
    assertThat(batch.remove()).isEqualTo(0L);

    batch.add(1L);
    assertThat(batch.remove()).isEqualTo(1L);

    batch.add(2L);
    assertThat(batch.remove()).isEqualTo(2L);

    batch.add(0L);
    batch.add(1L);
    assertThat(batch.remove()).isEqualTo(0L);
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