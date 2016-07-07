package com.twineworks.collections.shapemap;

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
  public String hashMap_get() {
    return hashMap.get("k123456");
  }

  @Benchmark
  public String shapeMap_get(){
    return shapeMap.get(k);
  }

  @Benchmark
  public String accessor_get(){
    return a.get(shapeMap);
  }

  @Benchmark
  public String shapeMap_geta(){
    return a.get(shapeMap);
  }

  @Benchmark
  public String fallback_accessor_get(){
    return fa.get(shapeMap);
  }


  @Benchmark
  public String hashMap_put() {
    return hashMap.put("k123456", "vn1");
  }

  @Benchmark
  public String shapeMap_put(){
    return shapeMap.put(k, "vn1");
  }

  @Benchmark
  public String acessor_put(){
    return a.put(shapeMap, "vn1");
  }

  @Benchmark
  public String shapeMap_puta(){
    return shapeMap.puta(a, "vn1");
  }

  @Benchmark
  public Object accessor_set(){
    a.set(shapeMap, "vn1");
    return a;
  }

  @Benchmark
  public Object shapeMap_seta(){
    shapeMap.seta(a, "vn1");
    return a;
  }

}

