package io.twineworks.collections.shapemap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.util.HashMap;

@State(Scope.Thread)
public class SmallMapBenchmark {

  HashMap<String, String> hashMap;
  ShapeMap<String> shapeMap;

  ShapeMap.Accessor<String> a1, fa;
  ShapeKey k0;

  @Setup
  public void setup() {

    // sets up a ShapeMap and a HashMap with 2 keys each
    //
    // {
    //   :k0 -> v0
    //   :k1 -> v1
    // }
    //

    hashMap = Util.hashMapRange(2);
    shapeMap = new ShapeMap<>(hashMap);

    k0 = ShapeKey.get("k0");
    a1 = ShapeMap.accessor(k0);

    // train accessor
    a1.get(shapeMap);

    // get over-trained fallback accessor
    fa = Util.fallbackAccessor(k0);

  }

  @Benchmark
  public String readHashMap() {
    return hashMap.get("k0");
  }

  @Benchmark
  public String readShapeMap(){
    return shapeMap.get(k0);
  }

  @Benchmark
  public String readShapeMapAccessor(){
    return a1.get(shapeMap);
  }

  @Benchmark
  public String readShapeMapAccessorFallback(){
    return fa.get(shapeMap);
  }

  @Benchmark
  public String putHashMap() {
    return hashMap.put("k0", "vn1");
  }

  @Benchmark
  public String putShapeMap(){
    return shapeMap.put(k0, "vn1");
  }

  @Benchmark
  public String putShapeMapAccessor(){
    return a1.put(shapeMap, "vn1");
  }

  @Benchmark
  public Object putShapeMapAccessorSetOnly(){
    a1.set(shapeMap, "vn1");
    return a1;
  }

}

