package io.twineworks.collections.shapemap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;

@State(Scope.Thread)
public class BigMapBenchmark {

  HashMap<String, String> hashMap;
  ShapeMap<String> shapeMap;

  ShapeMap.Accessor<String> a, fa;
  ShapeKey k;

  @Setup
  public void setup() {

    // sets up a ShapeMap and a HashMap with 1_000_000 keys each
    //
    // {
    //   :k0 -> v0
    //   :k0 -> v1
    //   ...
    //   :k999999 -> v999999
    // }
    //
    // Benchmarks access key 123456


    // HashMap
    hashMap = Util.hashMapRange(1_000_000);

    // ShapeMap
    shapeMap = new ShapeMap<>(hashMap);

    k = ShapeKey.get("k123456");
    a = ShapeMap.accessor(k);

    // learn the location once
    a.get(shapeMap);

    // over-trained accessor
    fa = Util.fallbackAccessor(k);

  }

  @Benchmark
  public String readHashMap() {
    return hashMap.get("k123456");
  }

  @Benchmark
  public String readShapeMap(){
    return shapeMap.get(k);
  }

  @Benchmark
  public String readShapeMapAccessor(){
    return a.get(shapeMap);
  }

  @Benchmark
  public String readShapeMapAccessorFallback(){
    return fa.get(shapeMap);
  }

  @Benchmark
  public String putHashMap() {
    return hashMap.put("k123456", "vn1");
  }

  @Benchmark
  public String putShapeMap(){
    return shapeMap.put(k, "vn1");
  }

  @Benchmark
  public String putShapeMapAccessor(){
    return a.put(shapeMap, "vn1");
  }

  @Benchmark
  public Object putShapeMapAccessorSetOnly(){
    a.set(shapeMap, "vn1");
    return a;
  }
  
}

