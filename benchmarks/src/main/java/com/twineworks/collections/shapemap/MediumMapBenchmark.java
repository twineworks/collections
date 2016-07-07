package com.twineworks.collections.shapemap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;

@State(Scope.Thread)
public class MediumMapBenchmark {

  HashMap<String, String> hashMap;
  ShapeMap<String> shapeMap;

  ShapeMap.Accessor<String> a42, fa;
  ShapeKey k42;

  @Setup
  public void setup() {

    // sets up a ShapeMap and a HashMap with 100 keys each
    //
    // {
    //   :k0 -> v0
    //   :k0 -> v1
    //   ...
    //   :k99 -> v99
    // }
    //
    // Benchmarks access key 42


    // HashMap
    hashMap = Util.hashMapRange(100);

    // ShapeMap
    shapeMap = new ShapeMap<>(hashMap);

    k42 = ShapeKey.get("k42");
    a42 = ShapeMap.accessor(k42);

    // learn the location once
    a42.get(shapeMap);

    // over-trained accessor
    fa = Util.fallbackAccessor(k42);

  }

  @Benchmark
  public String hashMap_get() {
    return hashMap.get("k42");
  }

  @Benchmark
  public String shapeMap_get(){
    return shapeMap.get(k42);
  }

  @Benchmark
  public String accessor_get(){
    return a42.get(shapeMap);
  }

  @Benchmark
  public String shapeMap_geta(){
    return a42.get(shapeMap);
  }

  @Benchmark
  public String fallback_accessor_get(){
    return fa.get(shapeMap);
  }


  @Benchmark
  public String hashMap_put() {
    return hashMap.put("k42", "vn1");
  }

  @Benchmark
  public String shapeMap_put(){
    return shapeMap.put(k42, "vn1");
  }

  @Benchmark
  public String acessor_put(){
    return a42.put(shapeMap, "vn1");
  }

  @Benchmark
  public String shapeMap_puta(){
    return shapeMap.puta(a42, "vn1");
  }

  @Benchmark
  public Object accessor_set(){
    a42.set(shapeMap, "vn1");
    return a42;
  }

  @Benchmark
  public Object shapeMap_seta(){
    shapeMap.seta(a42, "vn1");
    return 42;
  }

}

