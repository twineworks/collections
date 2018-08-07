/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2018 Twineworks GmbH
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

package com.twineworks.collections.trie;

import org.junit.Test;

import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unchecked")
public class TrieListTest {

  private TrieList list0 = TrieList.empty();
  private TrieList list1;
  private TrieList list10;
  private TrieList listFullTail;
  private TrieList listSpillTail;
  private TrieList listFullLevel;
  private TrieList listSpillLevel;

  public TrieListTest() {

    TrieList list = list0;

    for (int i=0;i<4096+65;i++){
      list = list.add(i);
      if (i == 0){
        list1 = list;
      }
      if (i == 9){
        list10 = list;
      }
      if (i == 63){
        listFullTail = list;
      }
      if (i == 64){
        listSpillTail = list;
      }
      if (i == 4096+63){
        listFullLevel = list;
      }
      if (i == 4096+64){
        listSpillLevel = list;
      }
    }

  }

  @Test
  public void empty_has_zero_size() {
    assertThat(list0.size()).isEqualTo(0);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void throws_getting_bad_index() {
    list0.get(1);
  }

  @Test
  public void adds_to_empty() {
    TrieList list = list0.add("foo");
    assertThat(list.size()).isEqualTo(1);
    assertThat(list.get(0)).isEqualTo("foo");
  }

  @Test
  public void pops_from_singleton() {
    TrieList list = list1.pop();
    assertThat(list.size()).isEqualTo(0);
  }

  @Test
  public void pops_crossing_level_boundary() {

    TrieList list = listSpillLevel;
    for(int i=listSpillLevel.size();i>0;i--){
      assertThat(list.size()).isEqualTo(i);
      assertThat(list.get(list.size()-1)).isEqualTo(i-1);
      list = list.pop();
    }

  }

  @Test
  public void iterates_crossing_level_boundary() {
    TrieList list = listSpillLevel;
    int i=0;
    for (Object o : list) {
      assertThat(o).isEqualTo(i);
      i++;
    }

    assertThat(i).isEqualTo(list.size());
  }

  @Test
  public void updates_values() {

    TrieList list = listSpillLevel;
    for (int i=0; i<listSpillLevel.size(); i++){
      list = list.set(i, "foo_"+i);
    }

    int j=0;
    for (Object o : list) {
      assertThat(o).isEqualTo("foo_"+j);
      j++;
    }
    assertThat(j).isEqualTo(list.size());

  }

  @Test
  public void adds_multiple() {

    Object[] items = new Object[50000];
    for (int i=0;i<items.length;i++){
      items[i] = "foo_"+i;
    }

    TrieList list = list0.addAll(items);
    int j=0;
    for (Object o : list) {
      assertThat(o).isEqualTo("foo_"+j);
      j++;
    }

    assertThat(j).isEqualTo(50000);

  }

  @Test
  public void extracts_empty_slice() {
    TrieList list = listSpillLevel;
    TrieList slice = list.slice(42, 42);
    assertThat(slice.size()).isEqualTo(0);
  }

  @Test
  public void extracts_empty_slice_from_empty() {
    TrieList list = list0;
    TrieList slice = list.slice(0, 0);
    assertThat(slice.size()).isEqualTo(0);
  }

  @Test
  public void extracts_middle_slice() {
    TrieList list = listSpillLevel;
    TrieList slice = list.slice(1000, 2000);
    int j=0;
    for (Object o : slice) {
      assertThat(o).isEqualTo(j+1000);
      j++;
    }
    assertThat(j).isEqualTo(1000);
  }

  @Test
  public void extracts_leading_slice() {
    TrieList list = listSpillLevel;
    TrieList slice = list.slice(0, 1000);
    int j=0;
    for (Object o : slice) {
      assertThat(o).isEqualTo(j);
      j++;
    }
    assertThat(j).isEqualTo(1000);
  }

  @Test
  public void extracts_trailing_slice() {
    TrieList list = listSpillLevel;
    TrieList slice = list.slice(1000, list.size());
    int j=1000;
    for (Object o : slice) {
      assertThat(o).isEqualTo(j);
      j++;
    }
    assertThat(j).isEqualTo(list.size());
  }


  @Test
  public void converts_empty_to_array() {
    TrieList list = list0;
    assertThat(list.toArray()).isEqualTo(new Object[]{});
  }

  @Test
  public void converts_to_array() {
    TrieList list = listSpillLevel;
    Object[] array = list.toArray();
    assertThat(list.size()).isEqualTo(array.length);
    for (int i = 0; i < array.length; i++) {
      Object o = array[i];
      assertThat(o).isEqualTo(list.get(i));
    }
  }

  @Test
  public void iterates_over_list() {
    TrieList list = listSpillLevel;
    int i=0;
    for (Object o: list) {
      assertThat(o).isEqualTo(list.get(i));
      i++;
    }
    assertThat(i).isEqualTo(list.size());
  }

  @Test
  public void iterates_over_list_in_reverse() {
    TrieList list = listSpillLevel;
    int i=list.size()-1;
    for (Iterator iterator=list.reverseIterator();iterator.hasNext();i--) {
      assertThat(iterator.next()).isEqualTo(list.get(i));
    }
    assertThat(i).isEqualTo(-1);
  }


  @Test
  public void iterates_over_slice() {
    TrieList list = listSpillLevel;
    int i=100;
    for (Iterator iterator = list.sliceIterator(100, 1000); iterator.hasNext(); i++) {
      assertThat(iterator.next()).isEqualTo(list.get(i));
    }
    assertThat(i).isEqualTo(1000);

  }

  @Test
  public void iterates_over_slice_in_reverse() {
    TrieList list = listSpillLevel;
    int i=999;
    for (Iterator iterator = list.reverseSliceIterator(100, 1000); iterator.hasNext(); i--) {
      assertThat(iterator.next()).isEqualTo(list.get(i));
    }
    assertThat(i).isEqualTo(99);
  }

  @Test
  public void finds_index_of_item() {
    TrieList list = listSpillLevel;
    assertThat(list.indexOf(999)).isEqualTo(999);
    assertThat(list.indexOf(null)).isEqualTo(-1);
    assertThat(list.indexOf(0)).isEqualTo(0);
    assertThat(list.indexOf(list.size()-1)).isEqualTo(list.size()-1);
  }

  @Test
  public void finds_index_of_item_from_starting_point() {
    TrieList list = listSpillLevel;
    assertThat(list.indexOf(999, 999)).isEqualTo(999);
    assertThat(list.indexOf(null, 0)).isEqualTo(-1);
    assertThat(list.indexOf(0, 0)).isEqualTo(0);
    assertThat(list.indexOf(0, 1)).isEqualTo(-1);
    assertThat(list.indexOf(list.size()-1, 333)).isEqualTo(list.size()-1);
  }

  @Test
  public void finds_last_index_of_item() {

    TrieList list = listSpillLevel;
    assertThat(list.lastIndexOf(999)).isEqualTo(999);
    assertThat(list.lastIndexOf(null)).isEqualTo(-1);
    assertThat(list.lastIndexOf(0)).isEqualTo(0);
    assertThat(list.lastIndexOf(list.size()-1)).isEqualTo(list.size()-1);

    TrieList dup = TrieList.empty().add(1).add(2).add(2).add(3);
    assertThat(dup.lastIndexOf(2)).isEqualTo(2);
    assertThat(dup.indexOf(2)).isEqualTo(1);

  }

  @Test
  public void finds_last_index_of_item_from_starting_point() {

    TrieList list = listSpillLevel;
    assertThat(list.lastIndexOf(999, 999)).isEqualTo(999);
    assertThat(list.lastIndexOf(999, 998)).isEqualTo(-1);
    assertThat(list.lastIndexOf(null)).isEqualTo(-1);
    assertThat(list.lastIndexOf(0)).isEqualTo(0);
    assertThat(list.lastIndexOf(list.size()-1)).isEqualTo(list.size()-1);
    assertThat(list.lastIndexOf(list.size()-1, 0)).isEqualTo(-1);

    TrieList dup = TrieList.empty().add(1).add(2).add(2).add(3);
    assertThat(dup.lastIndexOf(2)).isEqualTo(2);
    assertThat(dup.lastIndexOf(2, 1)).isEqualTo(1);
    assertThat(dup.lastIndexOf(2, 0)).isEqualTo(-1);
  }

  @Test
  public void reports_contained_items() {

    TrieList list = listSpillLevel;
    assertThat(list.contains(0)).isTrue();
    assertThat(list.contains(999)).isTrue();
    assertThat(list.contains(null)).isFalse();
    assertThat(list.contains(list.size()-1)).isTrue();
    assertThat(list.contains(list.size())).isFalse();
  }

  @Test
  public void reports_being_empty() {
    assertThat(list0.isEmpty()).isTrue();
    assertThat(list1.isEmpty()).isFalse();
  }

  @Test
  public void pads_to_length() {

    // no padding required
    assertThat(list0.padTo(0, 0)).isSameAs(list0);
    assertThat(list1.padTo(0, 0)).isSameAs(list1);
    assertThat(list1.padTo(1, 0)).isSameAs(list1);

    // full padding required
    TrieList foos = list0.padTo(10, "foo");
    assertThat(foos.size()).isEqualTo(10);
    for (Object foo : foos) {
      assertThat(foo).isEqualTo("foo");
    }

    // some padding required
    TrieList bars = list10.padTo(20, "bar");
    assertThat(bars.size()).isEqualTo(20);
    for (int i=0;i<20;i++){
      if(i < 10){
        assertThat(bars.get(i)).isEqualTo(i);
      }
      else {
        assertThat(bars.get(i)).isEqualTo("bar");
      }
    }

  }

  @Test
  public void inserts_at_beginning(){
    assertThat(list0.insert(0, 0)).isEqualTo(list1);

    TrieList list = list10.insert(0, "foo");
    assertThat(list.size()).isEqualTo(11);
    assertThat(list.get(0)).isEqualTo("foo");
    assertThat(list.get(1)).isEqualTo(0);
    assertThat(list.get(10)).isEqualTo(9);

  }

  @Test
  public void inserts_at_end(){
    assertThat(list0.insert(0, 0)).isEqualTo(list1);

    TrieList list = list10.insert(list10.size(), "foo");
    assertThat(list.size()).isEqualTo(11);
    assertThat(list.get(0)).isEqualTo(0);
    assertThat(list.get(10)).isEqualTo("foo");
  }

  @Test
  public void inserts_in_middle(){

    TrieList list = list10.insert(5, "foo");
    assertThat(list.size()).isEqualTo(11);
    assertThat(list.get(0)).isEqualTo(0);
    assertThat(list.get(1)).isEqualTo(1);
    assertThat(list.get(2)).isEqualTo(2);
    assertThat(list.get(3)).isEqualTo(3);
    assertThat(list.get(4)).isEqualTo(4);
    assertThat(list.get(5)).isEqualTo("foo");
    assertThat(list.get(6)).isEqualTo(5);
    assertThat(list.get(7)).isEqualTo(6);
    assertThat(list.get(8)).isEqualTo(7);
    assertThat(list.get(9)).isEqualTo(8);
    assertThat(list.get(10)).isEqualTo(9);
  }

  @Test
  public void removes_from_beginning() {
    assertThat(list1.remove(0).isEmpty()).isTrue();

    TrieList list = listSpillLevel.remove(0);

    assertThat(list.size()).isEqualTo(listSpillLevel.size()-1);

    for (int i=0;i<list.size();i++){
      assertThat(list.get(i)).isEqualTo(listSpillLevel.get(i+1));
    }

  }

  @Test
  public void removes_from_end() {
    assertThat(list1.remove(0).isEmpty()).isTrue();

    TrieList list = listSpillLevel.remove(listSpillLevel.size()-1);
    assertThat(list).isEqualTo(listSpillLevel.pop());

  }

  @Test
  public void removes_from_middle() {

    TrieList list = listSpillLevel.remove(listSpillLevel.size()/2);

    assertThat(list.size()).isEqualTo(listSpillLevel.size()-1);
    for (int i=0;i<list.size();i++){
      if (i<listSpillLevel.size()/2){
        assertThat(list.get(i)).isEqualTo(listSpillLevel.get(i));
      }
      else {
        assertThat(list.get(i)).isEqualTo(listSpillLevel.get(i+1));
      }
    }

  }
}