package com.twineworks.collections.batch;

import java.util.Arrays;

/* Highly specialized array-backed non-synchronized high performance FIFO queue.
 *
 * The following potentially dangerous trade-offs have been made:
 *
 * Calling remove() on empty queue yields undefined behavior, always ensure !isEmpty() before calling.
 * The safety check is not done for performance reasons. Users call isEmpty() before remove() anyway,
 * and double checking that !isEmpty() in remove() is redundant and only costs cycles.
 *
 * Also, remove()'d items are still referenced by the queue, and will be overwritten by subsequent calls to add().
 * Call clear to remove all references at an opportune moment.
 *
 * See test for intended usage pattern.
 * */

public class Batch<E> {

  public final Object[] data;
  private int idx;
  private int consumeIdx;

  public Batch(int capacity) {
    data = new Object[capacity];
    idx = 0;
    consumeIdx = 0;
  }

  public void add(E element) {
    data[idx++] = element;
  }

  public boolean isFull() {
    return idx == data.length;
  }

  @SuppressWarnings("unchecked")
  public E remove() {
    E returnValue = (E) data[consumeIdx++];
    if (consumeIdx == idx) {
      idx = 0;
      consumeIdx = 0;
    }
    return returnValue;
  }

  public boolean isEmpty() {
    return idx == 0 && consumeIdx == 0;
  }

  public void clear(){
    Arrays.fill(data, null);
    idx = 0;
    consumeIdx = 0;
  }

}