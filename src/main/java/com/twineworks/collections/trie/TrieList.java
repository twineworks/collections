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

import java.util.*;

public final class TrieList implements Iterable {

  private static final int NODE_SIZE = 64;    // 2^6
  private static final int CAPACITY_BITS = 6; // how many addressing bits per level
  private static final int BITS_MASK = 0x03f; // least significant 6 bits set

  private int hash = 0;

  private static class TrieNode {

    public final Object[] array;

    public TrieNode(Object[] array){
      this.array = array;
    }

    TrieNode(){
      this.array = new Object[NODE_SIZE];
    }
  }

  private final static TrieNode EMPTY_TRIE_NODE = new TrieNode();

  // amount of items in list
  private final int size;

  // total addressable bits at the top level
  private final int levelBits;

  // root trie of list
  private final TrieNode rootNode;

  // Extra ragged trie node that only enters tree hierarchy when full.
  // Useful to minimize computationally more involved operations when
  // growing or shrinking the list from the back, using it like a stack.
  private final Object[] tail;

  // empty list
  private final static TrieList EMPTY_LIST = new TrieList(0, CAPACITY_BITS, EMPTY_TRIE_NODE, new Object[]{});

  public static TrieList empty(){
    return EMPTY_LIST;
  }

  public static TrieList singleTon(Object item){
    return new TrieList(1, CAPACITY_BITS, EMPTY_TRIE_NODE, new Object[]{item});
  }

  private TrieList(int size, int levelBits, TrieNode rootNode, Object[] tail){
    this.size = size;
    this.levelBits = levelBits;
    this.rootNode = rootNode;
    this.tail = tail;
  }

  /**
   * @return the first index served by tail.
   * In other words, the list index of any list item at tail[0].
   * There need not be an actual item in the specified position.
   */
  private int tailOffset(){

    // all items live in tail
    if(size < NODE_SIZE){
      return 0;
    }

    // divide last item index by number of items per node, discarding remainder
    // this is how many bottom level value nodes are in the trie
    // multiply by number of items per node to arrive at the number of items all nodes can
    // hold in total, which is the index of the first item outside their capacity
    // this is the first index in tail
    return ((size - 1) >>> CAPACITY_BITS) << CAPACITY_BITS;
  }

  /**
   * Returns the node array that holds the list index in question.
   * The corresponding item will be at index i & BITS_MASK.
   *
   * @param i the list index to find the array for
   * @return The node array
   */
  private Object[] storageFor(int i){
    if (i < 0 || i >= size) throw new IndexOutOfBoundsException();

    // index in tail?
    if(i >= tailOffset()){
      return tail;
    }

    // descend to the node holding the index in question
    // use the appropriate subset of bits for addressing on each level
    TrieNode trieNode = rootNode;
    for(int level = levelBits; level > 0; level -= CAPACITY_BITS){
      int idx = (i >>> level) & BITS_MASK;
      trieNode = (TrieNode) trieNode.array[idx];
    }

    return trieNode.array;
  }

  public Object get(int i){
    Object[] node = storageFor(i);
    return node[i & BITS_MASK];
  }

  public TrieList set(int i, Object val){
    if (i < 0 || i >= size) throw new IndexOutOfBoundsException();

    // if the index is held in tail
    // create a copy of the tail with the item put in place
    if(i >= tailOffset()) {
      Object[] newTail = new Object[tail.length];
      System.arraycopy(tail, 0, newTail, 0, tail.length);
      newTail[i & BITS_MASK] = val;
      return new TrieList(size, levelBits, rootNode, newTail);
    }

    // index is held in tree trie nodes
    // replace root node with updated version pathing down
    // to the updated data trie
    return new TrieList(size, levelBits, place(levelBits, rootNode, i, val), tail);

  }

  public TrieList remove(int i){
    if (i < 0 || i >= size) throw new IndexOutOfBoundsException();

    // remove from end
    if (i == size-1) return pop();

    // slice out
    TrieList left = slice(0, i);
    TrieList right = slice(i+1, size);
    return left.addAll(right);

  }

  public TrieList padTo(int length, Object withValue){
    if (size >= length) return this;
    Object[] padding = new Object[length - size];
    Arrays.fill(padding, withValue);
    return addAll(padding);
  }

  public TrieList insert(int idx, Object val){

    if (idx < 0 || idx > size) throw new IndexOutOfBoundsException("cannot insert at index: "+idx);

    // append
    if (idx == size) return add(val);

    // prepend
    if (idx == 0) return singleTon(val).addAll(this);

    // splice in
    TrieList left = slice(0, idx).add(val);
    TrieList right = slice(idx, size);

    return left.addAll(right);
  }

  private static TrieNode place(int levelBits, TrieNode trieNode, int i, Object val){

    // the new trie node replacing the existing one
    TrieNode ret = new TrieNode(trieNode.array.clone());

    if(levelBits == 0) {
      // arrived at data level, set the value in the trie node
      ret.array[i & BITS_MASK] = val;
    } else {
      // need to push down to next level replacing the trie node one level down
      int subIndex = (i >>> levelBits) & BITS_MASK;
      ret.array[subIndex] = place(levelBits - CAPACITY_BITS, (TrieNode) trieNode.array[subIndex], i, val);
    }
    return ret;
  }

  public int size(){
    return size;
  }

  public boolean isEmpty() {
    return size == 0;
  }

  public TrieList addAll(Object[] items){

    TrieList ret = this;
    int at = 0;

    while (at < items.length){
      // tail can accommodate more items?
      int tailSize = ret.size - ret.tailOffset();
      if (tailSize < NODE_SIZE){
        int availableSpace = NODE_SIZE - tailSize;
        int neededSpace = items.length-at;
        int addedSpace = Math.min(neededSpace, availableSpace);

        Object[] newTail = new Object[tailSize + addedSpace];
        System.arraycopy(ret.tail, 0, newTail, 0, ret.tail.length);
        System.arraycopy(items, at, newTail, ret.tail.length, addedSpace);

        ret = new TrieList(ret.size + addedSpace, ret.levelBits, ret.rootNode, newTail);
        at += addedSpace;
      }
      else{
        // just add one item to merge tail into trie tree
        ret = ret.add(items[at]);
        at+=1;
      }

    }

    return ret;
  }

  public TrieList addAll(TrieList items){

    TrieList ret = this;

    int i = 0;
    int end = items.size;

    while (i < end){

      Object[] array = items.storageFor(i);

      int startIdx = i & BITS_MASK;
      int endIdx = Math.min(array.length, end-i);

      if (startIdx == 0 && endIdx == array.length){
        ret = ret.addAll(array);
      }
      else{
        ret = ret.addAll(Arrays.copyOfRange(array, startIdx, endIdx));
      }

      i += (endIdx - startIdx);

    }

    return ret;
  }

  public TrieList add(Object val){

    // tail can be made bigger?
    if(size - tailOffset() < NODE_SIZE) {
      Object[] newTail = new Object[tail.length + 1];
      System.arraycopy(tail, 0, newTail, 0, tail.length);
      newTail[tail.length] = val;
      return new TrieList(size + 1, levelBits, rootNode, newTail);
    }
    // tail is at capacity and needs to start acting as a node in the tree
    TrieNode newRoot;
    TrieNode tailNode = new TrieNode(tail);
    int newLevelBits = levelBits;

    // overflowing root node capacity?
    if((size >>> CAPACITY_BITS) > (1 << levelBits)) {
      // need another level of nodes
      newRoot = new TrieNode();
      newRoot.array[0] = rootNode;
      newRoot.array[1] = carve(levelBits, tailNode);
      newLevelBits += CAPACITY_BITS;
    }
    else {
      // tail can take its place at the data level in the existing structure
      newRoot = mergeTail(levelBits, rootNode, tailNode);
    }

    // new item ends up in new tail
    return new TrieList(size + 1, newLevelBits, newRoot, new Object[]{val});
  }

  private TrieNode mergeTail(int levelBits, TrieNode parent, TrieNode tailNode){
    int targetIndex = ((size - 1) >>> levelBits) & BITS_MASK;
    TrieNode ret = new TrieNode(parent.array.clone());
    TrieNode trieNodeToInsert;
    // parent is just above data level?
    // just place the tail at the correct index
    if(levelBits == CAPACITY_BITS) {
      trieNodeToInsert = tailNode;
    } else {
      // parent holds non-data nodes?
      // push tail down to data level
      TrieNode child = (TrieNode) parent.array[targetIndex];
      trieNodeToInsert = (child != null)
          ? mergeTail(levelBits-CAPACITY_BITS, child, tailNode)
          : carve(levelBits-CAPACITY_BITS, tailNode);
    }
    ret.array[targetIndex] = trieNodeToInsert;
    return ret;
  }

  private static TrieNode carve(int levelBits, TrieNode trieNode){
    if(levelBits == 0){
      return trieNode;
    }
    TrieNode ret = new TrieNode();
    ret.array[0] = carve(levelBits - CAPACITY_BITS, trieNode);
    return ret;
  }

  public Iterator iterator(){
    return sliceIterator(0, size);
  }

  public Iterator sliceIterator(final int start, final int end){

    return new Iterator(){

      int i = start;
      int base = start - (start % NODE_SIZE);

      Object[] array = (size > start) ? storageFor(i) : null;

      public boolean hasNext(){
        return i < end;
      }

      public Object next(){
        if(i < end) {
          if(i-base == NODE_SIZE){
            array = storageFor(i);
            base += NODE_SIZE;
          }
          return array[i++ & BITS_MASK];
        } else {
          throw new NoSuchElementException();
        }
      }

      public void remove(){
        throw new UnsupportedOperationException();
      }
    };
  }

  public Iterator reverseSliceIterator(final int start, final int end){

    return new Iterator(){

      int i = end-1;
      int base = i - (i % NODE_SIZE);

      Object[] array = (size > i) ? storageFor(i) : null;

      public boolean hasNext(){
        return i >= start;
      }

      public Object next(){
        if(i >= start) {
          if(i<base){
            array = storageFor(i);
            base -= NODE_SIZE;
          }
          return array[i-- & BITS_MASK];
        } else {
          throw new NoSuchElementException();
        }
      }

      public void remove(){
        throw new UnsupportedOperationException();
      }
    };
  }

  public Iterator reverseIterator() {
    return reverseSliceIterator(0, size);
  }

  public TrieList slice(int start, int end){
    if (start > end) throw new IllegalArgumentException("start must be <= end");
    if (start < 0 || start > size || end > size) throw new IndexOutOfBoundsException();

    TrieList ret = empty();

    int i = start;

    while (i < end){

      Object[] storage = storageFor(i);

      int startIdx = i & BITS_MASK;
      int endIdx = Math.min(storage.length, startIdx+end-i);

      if (startIdx == 0 && endIdx == storage.length){
        ret = ret.addAll(storage);
      }
      else{
        ret = ret.addAll(Arrays.copyOfRange(storage, startIdx, endIdx));
      }

      i += (endIdx - startIdx);

    }

    return ret;

  }

  public TrieList pop(){

    if(size == 0){
      throw new IllegalStateException("Can't pop from empty list");
    }

    if(size == 1){
      return EMPTY_LIST;
    }

    // can remove from end of tail?
    if(size - tailOffset() > 1) {
      Object[] newTail = new Object[tail.length - 1];
      System.arraycopy(tail, 0, newTail, 0, newTail.length);
      return new TrieList(size - 1, levelBits, rootNode, newTail);
    }
    // tail only has last item, need to convert last trie node
    // to new tail
    Object[] newTail = storageFor(size - 2);

    TrieNode newRoot = dropTail(levelBits, rootNode);
    int newLevelBits = levelBits;

    // only existing data trie got transformed to tail
    // need to have a distinct array backing the root trie
    if(newRoot == null) {
      newRoot = EMPTY_TRIE_NODE;
    }

    // the tree may need to collapse by one level
    if(levelBits > CAPACITY_BITS && newRoot.array[1] == null) {
      newRoot = (TrieNode) newRoot.array[0];
      newLevelBits -= CAPACITY_BITS;
    }
    return new TrieList(size - 1, newLevelBits, newRoot, newTail);
  }

  public int indexOf(Object item){
    int i=0;
    for (Iterator iterator = this.iterator(); iterator.hasNext(); i++) {
      Object o = iterator.next();
      if (Objects.equals(o, item)){
        return i;
      }
    }
    return -1;
  }

  public int indexOf(Object item, int startAt){
    int i=startAt;
    for (Iterator iterator = this.sliceIterator(startAt, size); iterator.hasNext(); i++) {
      Object o = iterator.next();
      if (Objects.equals(o, item)){
        return i;
      }
    }
    return -1;
  }

  public int lastIndexOf(Object item){
    int i=size-1;
    for (Iterator iterator = this.reverseIterator(); iterator.hasNext(); i--) {
      Object o = iterator.next();
      if (Objects.equals(o, item)){
        return i;
      }
    }
    return -1;
  }

  public int lastIndexOf(Object item, int startAt){
    int i=startAt;
    for (Iterator iterator = this.reverseSliceIterator(0, startAt+1); iterator.hasNext(); i--) {
      Object o = iterator.next();
      if (Objects.equals(o, item)){
        return i;
      }
    }
    return -1;
  }

  public boolean contains(Object item){
    return this.indexOf(item) > -1;
  }

  private TrieNode dropTail(int levelBits, TrieNode trieNode){
    int subIndex = ((size -2) >>> levelBits) & BITS_MASK;
    if(levelBits > CAPACITY_BITS) {
      TrieNode newChild = dropTail(levelBits - CAPACITY_BITS, (TrieNode) trieNode.array[subIndex]);
      if(newChild == null && subIndex == 0){
        return null;
      } else {
        TrieNode ret = new TrieNode(trieNode.array.clone());
        ret.array[subIndex] = newChild;
        return ret;
      }
    }
    else if(subIndex == 0){
      return null;
    } else {
      TrieNode ret = new TrieNode(trieNode.array.clone());
      ret.array[subIndex] = null;
      return ret;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    if (o instanceof TrieList){
      TrieList oTrieList = (TrieList) o;
      if (oTrieList.size != size) return false;
      if (oTrieList.hashCode() != hashCode()) return false;
      Iterator iterator = iterator();
      for (Object oItem : oTrieList){
        Object item = iterator.next();
        if (!Objects.equals(oItem, item)) return false;
      }
      return true;
    }
    else if (o instanceof List){
      List oList = (List) o;
      if (oList.size() != size) return false;
      if (oList.hashCode() != hashCode()) return false;
      Iterator iterator = iterator();
      for (Object oItem : oList){
        Object item = iterator.next();
        if (!Objects.equals(oItem, item)) return false;
      }
      return true;
    }
    else {
      return false;
    }

  }

  @Override
  public int hashCode() {

    int hash = this.hash;
    if (hash == 0) {
      hash = 1;
      for(Object item: this){
        hash = 31 * hash + (item == null ? 0 : item.hashCode());
      }
      this.hash = hash;
    }
    return hash;

  }

  public Object[] toArray(){

    Object[] array = new Object[size()];

    int i = 0;
    int end = size;

    while (i < end){

      Object[] storage = storageFor(i);

      int startIdx = i & BITS_MASK;
      int endIdx = Math.min(storage.length, end-i);

      System.arraycopy(storage, startIdx, array, i, endIdx);

      i += (endIdx - startIdx);

    }

    return array;
  }
}