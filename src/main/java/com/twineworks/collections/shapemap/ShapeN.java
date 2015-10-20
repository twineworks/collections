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

package com.twineworks.collections.shapemap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class ShapeN implements Shape {

  // keys in shape
  public final HashSet<ShapeKey> keys;

  // which indexes they are stored in
  public final HashMap<ShapeKey, Integer> keyIntMap;

  // which other shapes have been derived from this shape
  public final HashMap<HashSet<ShapeKey>, Shape> transitions = new HashMap<>(32, 0.65f);

  private ShapeN(HashSet<ShapeKey> keys, HashMap<ShapeKey, Integer> keyIntMap){
    this.keys = keys;
    this.keyIntMap = keyIntMap;
  }

  public ShapeN(Set<ShapeKey> keys) {
    this.keys = new HashSet<>(keys);
    keyIntMap = new HashMap<>(Math.max(keys.size() * 2, 16), 0.65f);

    int i = 1;
    for (ShapeKey key : keys) {
      keyIntMap.put(key, i);
      i+=1;
    }

  }

  @Override
  public int idxFor(ShapeKey k) {
    Integer val = keyIntMap.get(k);
    if (val == null) return 0;
    return val;
  }

  @Override
  public void init(ShapeMap m) {
    m.storage = new Object[(keys.size()+1)*2];
  }

  @Override
  public void ensureCapacity(ShapeMap m) {

    int targetLen = keys.size()+1;
    int currentLen = m.storage.length;
    Object[] s = m.storage;

    if (currentLen < targetLen){
      Object[] a = new Object[targetLen*2];
      System.arraycopy(s, 0, a, 0, s.length);
      m.storage = a;
    }

  }

  @Override
  public int size() {
    return keys.size();
  }

  @Override
  @SuppressWarnings("unchecked")
  public synchronized Shape extendBy(Set<ShapeKey> byKeys) {

    // byKeys -> potentially new keys to add to existing
    // toKeys -> new set of all keys in shape

    HashSet<ShapeKey> toKeys = (HashSet<ShapeKey>) keys.clone();
    toKeys.addAll(byKeys);

    // existing transition available?
    Shape shape = transitions.get(toKeys);
    if (shape != null) return shape;

    // no existing transition available, copy shape and add keys to the copy,
    // keeping the existing indexes as they are
    HashMap<ShapeKey, Integer> newKeyIntMap = (HashMap<ShapeKey, Integer>) keyIntMap.clone();

    int idx = keys.size()+1;
    for(ShapeKey newKey : byKeys){
      if (!newKeyIntMap.containsKey(newKey)){
        newKeyIntMap.put(newKey, idx);
        idx += 1;
      }
    }

    Shape newShape = new ShapeN(toKeys, newKeyIntMap);
    transitions.put(toKeys, newShape);
    return newShape;

  }

  @Override
  public Set<ShapeKey> keySet() {
    return keys;
  }

}
