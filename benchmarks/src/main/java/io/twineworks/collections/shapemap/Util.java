package io.twineworks.collections.shapemap;

import java.util.HashMap;

public class Util {

  public static HashMap<String, String> hashMapRange(int upTo){
    HashMap<String, String> h = new HashMap<>(upTo);
    for (int i = 0; i < upTo; i++) {
      h.put("k"+i, "v"+i);
    }
    return h;
  }

  public static ShapeMap.Accessor<String> fallbackAccessor(ShapeKey forKey){
    ShapeMap<String> m = new ShapeMap<>();

    ShapeMap.Accessor<String> a = ShapeMap.accessor(forKey);

    // learn map shape #1
    m.put(ShapeKey.get(forKey.toString() + "_1"), "");
    a.put(m, "");

    // learn map shape #2
    m.put(ShapeKey.get(forKey.toString() + "_2"), "");
    a.put(m, "");

    // learn map shape #3
    m.put(ShapeKey.get(forKey.toString() + "_3"), "");
    a.put(m, "");

    // learn map shape #4
    m.put(ShapeKey.get(forKey.toString() + "_4"), "");
    a.put(m, "");

    // over-trained on map shape #5
    m.put(ShapeKey.get(forKey.toString() + "_5"), "");
    a.put(m, "");

    // over trained, accessor falls back to lookup now
    return a;

  }


}
