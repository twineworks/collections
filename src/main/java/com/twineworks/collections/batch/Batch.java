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

package com.twineworks.collections.batch;

import java.util.Arrays;

/*
 * Highly specialized array-backed non-synchronized high performance FIFO queue.
 *
 * The following potentially dangerous trade-offs have been made:
 *
 * Calling remove() on empty queue yields undefined behavior, always ensure !isEmpty() before calling.
 * The safety check is not done for performance reasons. Users call isEmpty() before remove() anyway,
 * and double checking that !isEmpty() in remove() is redundant and potentially costs cycles.
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