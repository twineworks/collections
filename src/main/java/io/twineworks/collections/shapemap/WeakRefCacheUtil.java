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


package io.twineworks.collections.shapemap;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class WeakRefCacheUtil {

  static <K, T> void clear(ReferenceQueue<K> rq, ConcurrentHashMap<T, Reference<K>> cache){
    //cleanup any dead entries
    if(rq.poll() != null) { // there are weak-only-refs , i.e. only in cache map

      //noinspection StatementWithEmptyBody
      while(rq.poll() != null) // kick them all out, causing them to be gc'd
        ;

      // clear gc'd weak references from the table
      for(Map.Entry<T, Reference<K>> e : cache.entrySet()) {
        Reference<K> val = e.getValue();
        if(val != null && val.get() == null)
          cache.remove(e.getKey(), val);
      }
    }
  }

}
