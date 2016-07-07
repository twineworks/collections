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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ShapeKey {

  // keeps interned weak references of keys
  private static final ConcurrentHashMap<String, Reference<ShapeKey>> table = new ConcurrentHashMap<>();
  private static final ReferenceQueue<ShapeKey> rq = new ReferenceQueue<>();

  // cached hashcode of the key
  public final int hashCode;

  // string the key is for
  public final String sym;

  private static ShapeKey intern(String sym){

    Reference<ShapeKey> existingRef = table.get(sym);

    if(existingRef == null) {
      purgeCache();

      ShapeKey k = new ShapeKey(sym);
      existingRef = table.putIfAbsent(sym, new WeakReference<>(k, rq));

      if(existingRef == null)
        return k;

    }

    ShapeKey existingKey = existingRef.get();

    if(existingKey != null)
      return existingKey;

    // entry was gc'd in the interim, retry
    table.remove(sym, existingRef);
    return intern(sym);
  }

  public static void purgeCache(){
    WeakRefCacheUtil.clear(rq, table);
  }

  public static ShapeKey get(String symbol){
    return intern(symbol);
  }

  public static Set<ShapeKey> getAll(String ... symbols){
    HashSet<ShapeKey> keys = new HashSet<>(symbols.length);
    for (String sym : symbols) {
      keys.add(get(sym));
    }

    return keys;
  }

  public static Set<ShapeKey> getAll(ShapeKey ... inputs){
    HashSet<ShapeKey> keys = new HashSet<>(inputs.length);
    for (ShapeKey k : inputs) {
      keys.add(k);
    }
    return keys;
  }

  private ShapeKey(String symbol){
    this.sym = symbol;
    this.hashCode = symbol.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    // all instances are interned, using object identity
    return this == obj;
  }

  @Override
  public final int hashCode(){
    return hashCode;
  }

  @Override
  public String toString(){
    return sym;
  }

}
