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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class Shapes {

  private static final ConcurrentHashMap<Set<ShapeKey>, Reference<Shape>> table = new ConcurrentHashMap<>();
  private static final ReferenceQueue<Shape> rq = new ReferenceQueue<>();

  private static Shape intern(Set<ShapeKey> keys){

    Reference<Shape> existingRef = table.get(keys);

    if(existingRef == null) {
      WeakRefCacheUtil.clear(rq, table);

      Shape s = createShapeForKeys(keys);
      existingRef = table.putIfAbsent(keys, new WeakReference<>(s, rq));

      if(existingRef == null)
        return s;

    }

    Shape existingShape = existingRef.get();

    if(existingShape != null)
      return existingShape;

    // entry was gc'd in the interim
    table.remove(keys, existingRef);
    return intern(keys);
  }

  private static Shape createShapeForKeys(Set<ShapeKey> keys){
    return new ShapeN(keys);
  }

  public static Shape forKeySet(Set<ShapeKey> keys){
    return intern(keys);
  }

  public static Shape extendBy(Shape s, Set<ShapeKey> keys){
    return s.extendBy(keys);
  }

  public static Shape extendBy(Shape s, ShapeKey key){
    Set<ShapeKey> changeSet = Collections.singleton(key);
    return extendBy(s, changeSet);
  }


}
