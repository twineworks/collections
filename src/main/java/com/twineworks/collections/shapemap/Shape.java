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

import java.util.Set;

interface Shape {

  // initialize storage
  void init(ShapeMap m);
  // increase storage if necessary
  void ensureCapacity(ShapeMap m);

  // returns the index of key k in this shape
  int idxFor(ShapeKey k);

  // create another shape by adding given keys
  Shape extendBy(Set<ShapeKey> byKeys);

  // create another shape by removing given keys
  Shape shrinkBy(Set<ShapeKey> byKeys);

  // returns which keys are in the shape
  Set<ShapeKey> keySet();

  // the amount of keys the shape holds
  int size();

}
