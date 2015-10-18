package io.twineworks.collections.shapemap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;

@State(Scope.Thread)
public class SmallMapWithCollisionsBenchmark {

  HashMap<String, String> hashMap;
  ShapeMap<String> shapeMap;

  ShapeMap.Accessor<String> a1, a2, fa;
  ShapeKey k1, k2;

  @Setup
  public void setup() {

    // sets up a ShapeMap and a HashMap with 2 keys each, the keys have the same hashCode
    // and are expected to give inferior performance in regular HashMaps
    //
    // {
    //   :FB -> v1
    //   :Ea -> v2
    // }
    //

    // ShapeMap
    k1 = ShapeKey.get("FB");
    k2 = ShapeKey.get("Ea");

    shapeMap = new ShapeMap<>(k1, k2);

    a1 = ShapeMap.accessor(k1);
    a2 = ShapeMap.accessor(k2);

    fa = Util.fallbackAccessor(k2);

    a1.put(shapeMap, "v1");
    a2.put(shapeMap, "v2");


    // HashMap
    hashMap = new HashMap<>(16);
    hashMap.put("FB", "v1");
    hashMap.put("Ea", "v2");

    if (k1.hashCode() != k2.hashCode()) throw new AssertionError("Keys are expected to have same hash codes");
    if ("FB".hashCode() != "Ea".hashCode()) throw new AssertionError("Keys are expected to have same hash codes");

  }

  @Benchmark
  public String readHashMap() {
    return hashMap.get("Ea");
  }

  @Benchmark
  public String readShapeMap(){
    return shapeMap.get(k2);
  }

  @Benchmark
  public String readShapeMapAccessor(){
    return a2.get(shapeMap);
  }

  @Benchmark
  public String readShapeMapAccessorFallback(){
    return fa.get(shapeMap);
  }


  @Benchmark
  public String putHashMap() {
    return hashMap.put("Ea", "vn1");
  }

  @Benchmark
  public String putShapeMap(){
    return shapeMap.put(k2, "vn1");
  }

  @Benchmark
  public String putShapeMapAccessor(){
    return a2.put(shapeMap, "vn1");
  }

  @Benchmark
  public Object putShapeMapAccessorSetOnly(){
    a2.set(shapeMap, "vn1");
    return a2;
  }

}

