# Special purpose collections for Java

[![Java 7+](https://img.shields.io/badge/java-7+-4c7e9f.svg)](http://java.oracle.com)
[![License](https://img.shields.io/badge/license-MIT-4c7e9f.svg)](https://raw.githubusercontent.com/twineworks/collections/master/LICENSE.txt)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.twineworks/collections/badge.svg)](http://search.maven.org/#search|gav|1|g:"com.twineworks"%20AND%20a:"collections")
[![Travis Build Status](https://travis-ci.org/twineworks/collections.svg?branch=master)](https://travis-ci.org/twineworks/collections)
[![AppVeyor Build status](https://ci.appveyor.com/api/projects/status/ab9agcx66pw4rjt2/branch/master?svg=true)](https://ci.appveyor.com/project/slawo-ch/collections/branch/master)

## Getting the jar
You can grab the jar from the [relases page](https://github.com/twineworks/collections/releases), or [maven central](http://search.maven.org/#search|gav|1|g:"com.twineworks"%20AND%20a:"collections"). 

## ShapeMaps
ShapeMaps are high performance maps similar to HashMaps.

 * They implement the standard Map interface of Java collections.
 * They _do not_ allow `null` keys.
 * They _do_ allow `null` values.
 * Like HashMaps, they are not thread-safe. You need to provide your own synchronization.
 * Compatible with Java 7 and above

ShapeMaps implement a combination of the following performance optimization techniques:

 * [Interned symbols](https://en.wikipedia.org/wiki/String_interning) as keys. Your keys must have a string representation.
 * [Perfect hashing](https://en.wikipedia.org/wiki/Perfect_hash_function) for collision-free storage.
 * Uses ideas behind [Polymorphic Inline Caching](https://en.wikipedia.org/wiki/Inline_caching) to provide the ability to access a set of keys
    many times, without paying the potential collision resolution cost each time.

These techniques are particularly effective if:

   * You need to access certain keys many times
   * Your frequent access keys tend to hash-collide
   * Your maps have a limited number of different key sets (shapes)

## Usage

ShapeMaps work with a fixed key type: `ShapeKey`. Convert any string to a key like so:

```java
ShapeKey k1 = ShapeKey.get("k1");
```

ShapeMaps can be constructed and handled like regular maps. The type of their keys
is fixed to ShapeKey. The value type is generic. They implement `java.util.Map<ShapeKey, T>`.

```java
ShapeMap<String> m = new ShapeMap<>();
ShapeKey k1 = ShapeKey.get("k1");

m.put(k1, "my_value");
String v1 = m.get(k1);
```

### High-performance access

To take advantage of performance benefits, you use a `ShapeMap.Accessor` which learns where map shapes store
their keys, and subsequent accesses are faster. The accessor implements the Polymorphic Inline Cache. The inline cache
learns up to four map shapes before falling back to generic lookup.

```java
ShapeKey k = ShapeKey.get("k");
ShapeMap.Accessor<String> ak = ShapeMap.accessor(k);

ShapeMap<String> m = new ShapeMap<>();
ak.set(m, "my_value");
String v = ak.get(m);
```
A small additional performance tweak is a `set()` method in addition to the regular `put()`. The set method does not return any previous value.

It is perfectly safe to use the same accessor for different maps. The accessor is maximally effective if it is used on maps with up to four distinct shapes.

```java
ShapeKey k = ShapeKey.get("k");
ShapeMap.Accessor<String> ak = ShapeMap.accessor(k);

ShapeMap<String> m1 = new ShapeMap<>();
ShapeMap<String> m2 = new ShapeMap<>();

ak.set(m1, "my_value");
ak.set(m2, "my_value");

String v1 = ak.get(m1);
String v2 = ak.get(m2);
```

### Shapes

ShapeMaps work best if they are given the whole set of their potential keys up front. They make up the
map's `shape`. A shape reserves space in the underlying storage for its keys. Think of a shape as a potential key set.

When working with ShapeMaps you do not interact with shapes directly. Shapes are transparently maintained as an implementation
detail. You need to know how they work though, so you can reap their performance benefits. So here's the definitions and mechanics:

  * Every time a ShapeMap is initialized with a set of keys, these keys are used to form its shape.
  * If you use the same set of keys to initialize multiple ShapeMaps, they will share the same shape. Shapes used in map initialization are interned.
  * Shapes are immutable. A map can transition to a new shape, but it cannot mutate a shape.
  * If you put a key into a ShapeMap that is not part of its current shape, a transition link from its current shape to
    the new shape is maintained. If another ShapeMap of the same original shape must transition to a new shape, it checks
    whether there is already a transition in place and it uses the existing shape if that is the case.
  * Accessors remember where their key is in up to four different shapes, before having to fall back to un-optimized lookup.
  * Removing a key from a ShapeMap does not change its current shape.


#### Initializing ShapeMaps with a potential key set

There is a ShapeMap constructor allowing you to initialize the map with a potential key set:
```java
Set<ShapeKey> keys = ShapeKey.getAll("key_1", "key_2", "key_3");
ShapeMap<String> m = new ShapeMap<>(keys);
```

Or if you already have another map String -> T, you can convert it to a ShapeMap using a convenience constructor. The keys
in the source map are the initial shape of the ShapeMap.

```java
Map<String, Object> srcMap = new HashMap<>();
srcMap.put("k1", "foo");
srcMap.put("k2", "bar");
srcMap.put("k3", "baz");

ShapeMap<Object> m = new ShapeMap<>(srcMap);

ShapeKey k1 = ShapeKey.get("k1");
assertThat(m.get(k1)).isEqualTo("foo");
```

#### Changing shape
Maps transition to a new shape each time you `put` an unknown key into them.
However, you can add keys a bunch at a time, allowing for fewer shape transitions as opposed to adding them one by one.
You can use the following methods to do that:

 * `a.putAll(b) // ensures all keys in b are in a's shape`
 * `a.extendShape(ShapeKey.getAll("k1","k2")) // ensures "k1" and "k2" are part of a's shape`

Please note that the order in which keys are added is important:

`[] -> [k1] -> [k1, k2]` is not the same shape as `[] -> [k2] -> [k2, k1]`. The key sets are identical, but in the indexes
 of storage are different.

## ConstShapeMaps

ConstShapeMaps are like ShapeMaps whose shape can never change after construction. They are
useful as records, when the record structure is fixed, but only known at runtime. 

ConstShapeMaps differ from regular ShapeMaps in the following ways:
  - Attempts to change the shape in any way throw runtime exceptions.
  - All shape keys are present as keys. There is no separate tracking of shape and keyset.
  - If a key is not set, its value is null.
  - There is no insertion order of keys, therefore key order is not defined.
  - They do not implement the java.util.Map interface.
  
## Batches

Batches are array-backed non-synchronized high performance FIFO queues. They are useful for passing batches of
records between consumers and producers that must be processed in order, producers and consumers are giving it
a single pass to fill and consume the whole batch.

## License
This project uses the business friendly [MIT](https://opensource.org/licenses/MIT) license.

## Support
Open source does not mean you're on your own. This project is developed by [Twineworks GmbH](http://twineworks.com). Twineworks offers commercial support and consulting services. [Contact us](mailto:hi@twineworks.com) if you'd like us to help with a project.
