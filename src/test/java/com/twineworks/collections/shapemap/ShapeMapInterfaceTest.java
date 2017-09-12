/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 Twineworks GmbH
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.twineworks.collections.shapemap;

/*
  Adapted version of guava MapInterfaceTest
  https://github.com/google/guava/blob/master/guava-testlib/src/com/google/common/collect/testing/MapInterfaceTest.java
 */

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class ShapeMapInterfaceTest {

  private ShapeMap<String> makeEmptyMap(){
    return new ShapeMap<>();
  }

  private ShapeMap<String> makePopulatedMap(){

    ShapeKey k1 = ShapeKey.get("k1");
    ShapeKey k2 = ShapeKey.get("k2");
    ShapeKey k3 = ShapeKey.get("k3");
    ShapeKey k4 = ShapeKey.get("k4");

    Set<ShapeKey> keys = new HashSet<>();
    keys.add(k1);
    keys.add(k2);
    keys.add(k3);
    keys.add(k4);

    ShapeMap<String> map = new ShapeMap<>(keys);
    map.put(k1, "v1");
    map.put(k2, "v2");
    map.put(k3, "v3");
    map.put(k4, "v4");

    return map;
  }

  private ShapeKey getKeyNotInPopulatedMap(){
    return ShapeKey.get("missing key");
  }

  private String getValueNotInPopulatedMap(){
    return "missing value";
  }

  static <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
    return new AbstractMap.SimpleEntry<>(key, value);
  }

  static <T> Set<T> singleton(T value){
    HashSet<T> set = new HashSet<>();
    set.add(value);
    return set;
  }

  /**
   * Checks all the properties that should always hold of a map.
   */
  protected final void assertInvariants(ShapeMap<String> map) {
    Set<ShapeKey> keySet = map.keySet();
    Collection<String> valueCollection = map.values();
    Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    assertEquals(map.size() == 0, map.isEmpty());
    assertEquals(map.size(), keySet.size());
    assertEquals(keySet.size() == 0, keySet.isEmpty());
    assertEquals(!keySet.isEmpty(), keySet.iterator().hasNext());

    int expectedKeySetHash = 0;
    for (ShapeKey key : keySet) {
      String value = map.get(key);
      expectedKeySetHash += key != null ? key.hashCode() : 0;
      assertTrue(map.containsKey(key));
      assertTrue(map.containsValue(value));
      assertTrue(valueCollection.contains(value));
      assertTrue(valueCollection.containsAll(Collections.singleton(value)));
      assertTrue(entrySet.contains(mapEntry(key, value)));
      assertTrue((key != null));
    }
    assertEquals(expectedKeySetHash, keySet.hashCode());

    assertEquals(map.size(), valueCollection.size());
    assertEquals(valueCollection.size() == 0, valueCollection.isEmpty());
    assertEquals(!valueCollection.isEmpty(), valueCollection.iterator().hasNext());
    for (String value : valueCollection) {
      assertTrue(map.containsValue(value));
    }

    assertEquals(map.size(), entrySet.size());
    assertEquals(entrySet.size() == 0, entrySet.isEmpty());
    assertEquals(!entrySet.isEmpty(), entrySet.iterator().hasNext());

    //noinspection SuspiciousMethodCalls
    assertFalse(entrySet.contains("foo"));

    int expectedEntrySetHash = 0;
    for (Map.Entry<ShapeKey, String> entry : entrySet) {
      assertTrue(map.containsKey(entry.getKey()));
      assertTrue(map.containsValue(entry.getValue()));
      int expectedHash =
        (entry.getKey() == null ? 0 : entry.getKey().hashCode()) ^
          (entry.getValue() == null ? 0 : entry.getValue().hashCode());
      assertEquals(expectedHash, entry.hashCode());
      expectedEntrySetHash += expectedHash;
    }
    assertEquals(expectedEntrySetHash, entrySet.hashCode());
    assertTrue(entrySet.containsAll(new HashSet<>(entrySet)));
    assertTrue(entrySet.equals(new HashSet<>(entrySet)));

    Object[] entrySetToArray1 = entrySet.toArray();
    assertEquals(map.size(), entrySetToArray1.length);
    assertTrue(Arrays.asList(entrySetToArray1).containsAll(entrySet));

    Map.Entry<?, ?>[] entrySetToArray2 = new Map.Entry<?, ?>[map.size() + 2];
    entrySetToArray2[map.size()] = mapEntry("foo", 1);
    assertSame(entrySetToArray2, entrySet.toArray(entrySetToArray2));
    assertNull(entrySetToArray2[map.size()]);
    assertTrue(Arrays.asList(entrySetToArray2).containsAll(entrySet));

    Object[] valuesToArray1 = valueCollection.toArray();
    assertEquals(map.size(), valuesToArray1.length);
    assertTrue(Arrays.asList(valuesToArray1).containsAll(valueCollection));

    Object[] valuesToArray2 = new Object[map.size() + 2];
    valuesToArray2[map.size()] = "foo";
    assertSame(valuesToArray2, valueCollection.toArray(valuesToArray2));
    assertNull(valuesToArray2[map.size()]);
    assertTrue(Arrays.asList(valuesToArray2).containsAll(valueCollection));

    int expectedHash = 0;
    for (Map.Entry<ShapeKey, String> entry : entrySet) {
      expectedHash += entry.hashCode();
    }
    assertEquals(expectedHash, map.hashCode());

  }

  @Test
  public void testClear() {
    final ShapeMap<String> map = makePopulatedMap();
    map.clear();
    assertTrue(map.isEmpty());
    assertInvariants(map);
  }

  @Test
  public void testContainsKey() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey unmappedKey = getKeyNotInPopulatedMap();

    assertFalse(map.containsKey(unmappedKey));
    //noinspection SuspiciousMethodCalls
    assertFalse(map.containsKey("Non-Shape-Key-Type"));
    assertTrue(map.containsKey(map.keySet().iterator().next()));
    assertFalse(map.containsKey(null));

    assertInvariants(map);
  }

  @Test
  public void testContainsValue() {
    final ShapeMap<String> map = makePopulatedMap();
    final String unmappedValue = getValueNotInPopulatedMap();

    assertFalse(map.containsValue(unmappedValue));
    assertTrue(map.containsValue(map.values().iterator().next()));

    assertFalse(map.containsValue(null));

    assertInvariants(map);
  }

  @Test
  public void testEntrySet() {
    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet;

    assertInvariants(map);

    entrySet = map.entrySet();
    final ShapeKey unmappedKey = getKeyNotInPopulatedMap();
    final String unmappedValue = getValueNotInPopulatedMap();

    for (Map.Entry<ShapeKey, String> entry : entrySet) {
      assertFalse(unmappedKey.equals(entry.getKey()));
      assertFalse(unmappedValue.equals(entry.getValue()));
    }

    assertInvariants(map);
  }

  @Test
  public void testEntrySetForEmptyMap() {
    final ShapeMap<String> map = makeEmptyMap();
    assertInvariants(map);
  }

  @Test
  public void testEntrySetContainsEntryIncompatibleKey() {
    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet;

    assertInvariants(map);

    entrySet = map.entrySet();
    final String unmappedValue = getValueNotInPopulatedMap();

    Map.Entry<String, String> entry = mapEntry("Incompatible Key Type", unmappedValue);
    //noinspection SuspiciousMethodCalls
    assertFalse(entrySet.contains(entry));

  }

  @Test
  public void testEntrySetIteratorRemove() {
    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    Iterator<Map.Entry<ShapeKey, String>> iterator = entrySet.iterator();

    int initialSize = map.size();
    Map.Entry<ShapeKey, String> entry = iterator.next();
    Map.Entry<ShapeKey, String> entryCopy = mapEntry(entry.getKey(), entry.getValue());

    iterator.remove();
    assertEquals(initialSize - 1, map.size());

    // Use "entryCopy" instead of "entry" because "entry" might be invalidated after
    // iterator.remove().
    assertFalse(entrySet.contains(entryCopy));
    assertInvariants(map);
    try {
      iterator.remove();
      fail("Expected IllegalStateException.");
    } catch (IllegalStateException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testEntrySetRemove() {
    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    int initialSize = map.size();
    boolean didRemove = entrySet.remove(entrySet.iterator().next());
    assertTrue(didRemove);
    assertEquals(initialSize - 1, map.size());

    assertInvariants(map);
  }

  @Test
  public void testEntrySetRemoveMissingKey() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    final ShapeKey key = getKeyNotInPopulatedMap();

    Map.Entry<ShapeKey, String> entry = mapEntry(key, getValueNotInPopulatedMap());
    int initialSize = map.size();
    boolean didRemove = entrySet.remove(entry);
    assertFalse(didRemove);

    assertEquals(initialSize, map.size());
    assertFalse(map.containsKey(key));
    assertInvariants(map);
  }

  @Test
  public void testEntrySetRemoveDifferentValue() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    ShapeKey key = map.keySet().iterator().next();
    Map.Entry<ShapeKey, String> entry = mapEntry(key, getValueNotInPopulatedMap());
    int initialSize = map.size();

    boolean didRemove = entrySet.remove(entry);
    assertFalse(didRemove);

    assertEquals(initialSize, map.size());
    assertTrue(map.containsKey(key));
    assertInvariants(map);
  }

  @Test
  public void testEntrySetRemoveAll() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    Map.Entry<ShapeKey, String> entryToRemove = entrySet.iterator().next();
    Set<Map.Entry<ShapeKey, String>> entriesToRemove = new HashSet<>();
    entriesToRemove.add(entryToRemove);

    // We use a copy of "entryToRemove" in the assertion because "entryToRemove" might be
    // invalidated and have undefined behavior after entrySet.removeAll(entriesToRemove),
    // for example entryToRemove.getValue() might be null.
    Map.Entry<ShapeKey, String> entryToRemoveCopy = mapEntry(entryToRemove.getKey(), entryToRemove.getValue());
    assertTrue(entrySet.contains(entryToRemoveCopy));

    int initialSize = map.size();
    boolean didRemove = entrySet.removeAll(entriesToRemove);
    assertTrue(didRemove);
    assertEquals(initialSize - entriesToRemove.size(), map.size());

    // Use "entryToRemoveCopy" instead of "entryToRemove" because it might be invalidated and
    // have undefined behavior after entrySet.removeAll(entriesToRemove),
    assertFalse(entrySet.contains(entryToRemoveCopy));

    assertInvariants(map);
  }

  @Test
  public void testEntrySetRemoveAllNullFromEmpty() {
    final ShapeMap<String> map = makeEmptyMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    try {
      //noinspection ConstantConditions
      entrySet.removeAll(null);
      fail("Expected NullPointerException.");
    } catch (NullPointerException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testEntrySetRetainAll() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    Set<Map.Entry<ShapeKey, String>> entriesToRetain = singleton(entrySet.iterator().next());

    boolean shouldRemove = (entrySet.size() > entriesToRetain.size());
    boolean didRemove = entrySet.retainAll(entriesToRetain);
    assertEquals(shouldRemove, didRemove);
    assertEquals(entriesToRetain.size(), map.size());

    for (Map.Entry<ShapeKey, String> entry : entriesToRetain) {
      assertTrue(entrySet.contains(entry));
    }
    assertInvariants(map);

  }

  @Test
  public void testEntrySetRetainAllNullFromEmpty() {
    final ShapeMap<String> map = makeEmptyMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    try {
      //noinspection ConstantConditions
      entrySet.retainAll(null);
      fail("NPE Expected");
    } catch (NullPointerException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testEntrySetClear() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    entrySet.clear();
    assertTrue(entrySet.isEmpty());
    assertTrue(map.isEmpty());

    assertInvariants(map);
  }

  @Test
  public void testEntrySetAddAndAddAll() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    final Map.Entry<ShapeKey, String> entryToAdd = mapEntry(null, null);
    try {
      entrySet.add(entryToAdd);
      fail("Expected UnsupportedOperationException or NullPointerException.");
    } catch (UnsupportedOperationException | NullPointerException e) {
      // Expected.
    }
    assertInvariants(map);

    try {
      entrySet.addAll(singleton(entryToAdd));
      fail("Expected UnsupportedOperationException or NullPointerException.");
    } catch (UnsupportedOperationException | NullPointerException e) {
      // Expected.
    }
    assertInvariants(map);
  }

  @Test
  public void testEntrySetSetValue() {

    final ShapeMap<String> map = makePopulatedMap();
    final String valueToSet = getValueNotInPopulatedMap();

    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    Map.Entry<ShapeKey, String> entry = entrySet.iterator().next();

    final String oldValue = entry.getValue();
    final String returnedValue = entry.setValue(valueToSet);
    assertEquals(oldValue, returnedValue);
    assertTrue(entrySet.contains(mapEntry(entry.getKey(), valueToSet)));
    assertEquals(valueToSet, map.get(entry.getKey()));
    assertInvariants(map);

  }

  @Test
  public void testEntrySetSetValueSameValue() {

    final ShapeMap<String> map = makePopulatedMap();
    final Set<Map.Entry<ShapeKey, String>> entrySet = map.entrySet();

    Map.Entry<ShapeKey, String> entry = entrySet.iterator().next();

    final String oldValue = entry.getValue();
    final String returnedValue = entry.setValue(oldValue);

    assertEquals(oldValue, returnedValue);
    assertTrue(entrySet.contains(mapEntry(entry.getKey(), oldValue)));
    assertEquals(oldValue, map.get(entry.getKey()));

    assertInvariants(map);
  }

  @Test
  public void testEqualsForEqualMap() {
    final ShapeMap<String> map = makePopulatedMap();

    assertEquals(map, map);
    assertEquals(makePopulatedMap(), map);
    //noinspection EqualsBetweenInconvertibleTypes
    assertFalse(map.equals(Collections.emptyMap()));
    //noinspection ObjectEqualsNull
    assertFalse(map.equals(null));
  }

  @Test
  public void testEqualsForLargerMap() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeMap<String> largerMap = makePopulatedMap();
    largerMap.put(getKeyNotInPopulatedMap(), getValueNotInPopulatedMap());

    assertFalse(map.equals(largerMap));
  }

  @Test
  public void testEqualsForSmallerMap() {

    final ShapeMap<String> map = makePopulatedMap();
    final ShapeMap<String> smallerMap = makePopulatedMap();
    smallerMap.remove(smallerMap.keySet().iterator().next());

    assertFalse(map.equals(smallerMap));
  }

  @Test
  public void testEqualsForEmptyMap() {
    final ShapeMap<String> map = makeEmptyMap();

    assertEquals(map, map);
    assertEquals(makeEmptyMap(), map);
    //noinspection AssertEqualsBetweenInconvertibleTypes
    assertEquals(Collections.emptyMap(), map);
    assertFalse(map.equals(Collections.emptySet()));
    //noinspection ObjectEqualsNull
    assertFalse(map.equals(null));
  }

  @Test
  public void testGet() {
    final ShapeMap<String> map = makePopulatedMap();

    for (Map.Entry<ShapeKey, String> entry : map.entrySet()) {
      assertEquals(entry.getValue(), map.get(entry.getKey()));
    }

    ShapeKey unmappedKey = getKeyNotInPopulatedMap();
    assertNull(map.get(unmappedKey));
    assertInvariants(map);
  }

  @Test
  public void testGetForEmptyMap() {
    final ShapeMap<String> map = makeEmptyMap();

    ShapeKey unmappedKey = getKeyNotInPopulatedMap();
    assertNull(map.get(unmappedKey));
    assertInvariants(map);
  }

  @Test
  public void testGetNull() {
    final ShapeMap<String> map = makeEmptyMap();
    assertNull(map.get(null));
    assertInvariants(map);
  }

  @Test
  public void testHashCode() {
    assertInvariants(makePopulatedMap());
  }

  @Test
  public void testHashCodeForEmptyMap() {
    assertInvariants(makeEmptyMap());
  }

  @Test
  public void testPutNewKey() {
    final ShapeMap<String> map = makeEmptyMap();
    final ShapeKey keyToPut = getKeyNotInPopulatedMap();
    final String valueToPut = getValueNotInPopulatedMap();

    int initialSize = map.size();
    String oldValue = map.put(keyToPut, valueToPut);
    assertEquals(valueToPut, map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(valueToPut));
    assertEquals(initialSize + 1, map.size());
    assertNull(oldValue);

    assertInvariants(map);
  }

  @Test
  public void testPutExistingKey() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToPut = map.keySet().iterator().next();
    final String valueToPut = getValueNotInPopulatedMap();

    int initialSize = map.size();
    map.put(keyToPut, valueToPut);
    assertEquals(valueToPut, map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(valueToPut));
    assertEquals(initialSize, map.size());

    assertInvariants(map);
  }

  @Test
  public void testPutNullKey() {

    final ShapeMap<String> map = makePopulatedMap();
    final String valueToPut = getValueNotInPopulatedMap();

    try {
      map.put(null, valueToPut);
      fail("Expected RuntimeException");
    } catch (RuntimeException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testPutNullValue() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToPut = getKeyNotInPopulatedMap();

    int initialSize = map.size();
    final String oldValue = map.get(keyToPut);
    final String returnedValue = map.put(keyToPut, null);
    assertEquals(oldValue, returnedValue);
    assertNull(map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(null));
    assertEquals(initialSize + 1, map.size());
    assertInvariants(map);

  }

  @Test
  public void testPutNullValueForExistingKey() {

    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToPut = map.keySet().iterator().next();


    int initialSize = map.size();
    final String oldValue = map.get(keyToPut);
    final String returnedValue = map.put(keyToPut, null);
    assertEquals(oldValue, returnedValue);
    assertNull(map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(null));
    assertEquals(initialSize, map.size());

    assertInvariants(map);
  }

  @Test
  public void testPutAllNewKey() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToPut = getKeyNotInPopulatedMap();
    final String valueToPut = getValueNotInPopulatedMap();

    final Map<ShapeKey, String> mapToPut = Collections.singletonMap(keyToPut, valueToPut);

    int initialSize = map.size();
    map.putAll(mapToPut);
    assertEquals(valueToPut, map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(valueToPut));
    assertEquals(initialSize + 1, map.size());

    assertInvariants(map);
  }

  @Test
  public void testPutAllExistingKey() {

    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToPut = map.keySet().iterator().next();
    final String valueToPut = getValueNotInPopulatedMap();

    final Map<ShapeKey, String> mapToPut = Collections.singletonMap(keyToPut, valueToPut);
    int initialSize = map.size();

    map.putAll(mapToPut);
    assertEquals(valueToPut, map.get(keyToPut));
    assertTrue(map.containsKey(keyToPut));
    assertTrue(map.containsValue(valueToPut));

    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  @Test
  public void testRemove() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToRemove = map.keySet().iterator().next();

    int initialSize = map.size();
    String expectedValue = map.get(keyToRemove);
    String oldValue = map.remove(keyToRemove);
    assertEquals(expectedValue, oldValue);
    assertFalse(map.containsKey(keyToRemove));
    assertEquals(initialSize - 1, map.size());

    assertInvariants(map);
  }

  @Test
  public void testRemoveMissingKey() {
    final ShapeMap<String> map = makePopulatedMap();
    final ShapeKey keyToRemove = getKeyNotInPopulatedMap();

    int initialSize = map.size();
    assertNull(map.remove(keyToRemove));
    assertEquals(initialSize, map.size());

    assertInvariants(map);
  }

  @Test
  public void testSize() {
    assertInvariants(makePopulatedMap());
    assertInvariants(makeEmptyMap());
  }

  @Test
  public void testKeySetRemove() {

    final ShapeMap<String> map = makePopulatedMap();

    Set<ShapeKey> keys = map.keySet();
    ShapeKey key = keys.iterator().next();

    int initialSize = map.size();
    keys.remove(key);
    assertEquals(initialSize - 1, map.size());
    assertFalse(map.containsKey(key));

    assertInvariants(map);
  }

  @Test
  public void testKeySetRemoveAll() {
    final ShapeMap<String> map = makePopulatedMap();

    Set<ShapeKey> keys = map.keySet();
    ShapeKey key = keys.iterator().next();

    int initialSize = map.size();
    assertTrue(keys.removeAll(Collections.singleton(key)));
    assertEquals(initialSize - 1, map.size());
    assertFalse(map.containsKey(key));

    assertInvariants(map);
  }

  @Test
  public void testKeySetRetainAll() {
    final ShapeMap<String> map = makePopulatedMap();

    Set<ShapeKey> keys = map.keySet();
    ShapeKey key = keys.iterator().next();

    keys.retainAll(Collections.singleton(key));
    assertEquals(1, map.size());
    assertTrue(map.containsKey(key));

    assertInvariants(map);
  }

  @Test
  public void testKeySetClear() {

    final ShapeMap<String> map = makePopulatedMap();

    Set<ShapeKey> keys = map.keySet();

    keys.clear();
    assertTrue(keys.isEmpty());
    assertTrue(map.isEmpty());

    assertInvariants(map);
  }

  @Test
  public void testKeySetRemoveAllNullFromEmpty() {

    final ShapeMap<String> map = makeEmptyMap();

    Set<ShapeKey> keySet = map.keySet();

    try {
      //noinspection ConstantConditions
      keySet.removeAll(null);
      fail("Expected NullPointerException.");
    } catch (NullPointerException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testKeySetRetainAllNullFromEmpty() {

    final ShapeMap<String> map = makeEmptyMap();
    Set<ShapeKey> keySet = map.keySet();

    try {
      //noinspection ConstantConditions
      keySet.retainAll(null);
      fail("NPE expected");
    } catch (NullPointerException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testValues() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    final String unmappedValue = getValueNotInPopulatedMap();

    for (String value : valueCollection) {
      assertFalse(unmappedValue.equals(value));
    }

    assertInvariants(map);
  }

  @Test
  public void testValuesIteratorRemove() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    Iterator<String> iterator = valueCollection.iterator();

    int initialSize = map.size();
    iterator.next();
    iterator.remove();
    assertEquals(initialSize - 1, map.size());
    // (We can't assert that the values collection no longer contains the
    // removed value, because the underlying map can have multiple mappings
    // to the same value.)
    assertInvariants(map);
    try {
      iterator.remove();
      fail("Expected IllegalStateException.");
    } catch (IllegalStateException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testValuesRemove() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    int initialSize = map.size();
    valueCollection.remove(valueCollection.iterator().next());
    assertEquals(initialSize - 1, map.size());
    // (We can't assert that the values collection no longer contains the
    // removed value, because the underlying map can have multiple mappings
    // to the same value.)

    assertInvariants(map);
  }

  @Test
  public void testValuesRemoveMissing() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    final String valueToRemove = getValueNotInPopulatedMap();

    int initialSize = map.size();
    assertFalse(valueCollection.remove(valueToRemove));
    assertEquals(initialSize, map.size());
    assertInvariants(map);
  }

  @Test
  public void testValuesRemoveAll() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    Set<String> valuesToRemove = singleton(valueCollection.iterator().next());

    valueCollection.removeAll(valuesToRemove);

    for (String value : valuesToRemove) {
      assertFalse(valueCollection.contains(value));
    }

    for (String value : valueCollection) {
      assertFalse(valuesToRemove.contains(value));
    }

    assertInvariants(map);
  }

  @Test
  public void testValuesRemoveAllNullFromEmpty() {

    final ShapeMap<String> map = makeEmptyMap();
    final Collection<String> valueCollection = map.values();

    try {
      //noinspection ConstantConditions
      valueCollection.removeAll(null);
      fail("NPE expected");
    } catch (NullPointerException e) {
      // Expected.
    }

    assertInvariants(map);
  }

  @Test
  public void testValuesRetainAll() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    Set<String> valuesToRetain = singleton(valueCollection.iterator().next());

    valueCollection.retainAll(valuesToRetain);

    for (String value : valuesToRetain) {
      assertTrue(valueCollection.contains(value));
    }
    for (String value : valueCollection) {
      assertTrue(valuesToRetain.contains(value));
    }

    assertInvariants(map);
  }

  @Test
  public void testValuesRetainAllNullFromEmpty() {

    final ShapeMap<String> map = makeEmptyMap();
    final Collection<String> values = map.values();

      try {
        //noinspection ConstantConditions
        values.retainAll(null);
        fail("NPE expected");
      } catch (NullPointerException e) {
        // Expected.
      }

    assertInvariants(map);
  }

  @Test
  public void testValuesClear() {

    final ShapeMap<String> map = makePopulatedMap();
    final Collection<String> valueCollection = map.values();

    valueCollection.clear();
    assertTrue(valueCollection.isEmpty());
    assertTrue(map.isEmpty());

    assertInvariants(map);
  }

}
