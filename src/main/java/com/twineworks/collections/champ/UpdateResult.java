package com.twineworks.collections.champ;

class UpdateResult<K, V> {

  private V replacedValue;
  private boolean isModified;
  private boolean isReplaced;

  private UpdateResult() {
  }

  public static <K, V> UpdateResult<K, V> unchanged() {
    return new UpdateResult<>();
  }

  public void modified() {
    this.isModified = true;
  }

  public void updated(V replacedValue) {
    this.replacedValue = replacedValue;
    this.isModified = true;
    this.isReplaced = true;
  }

  public void reset() {
    replacedValue = null;
    isModified = false;
    isReplaced = false;
  }

  public boolean isModified() {
    return isModified;
  }

  public boolean hasReplacedValue() {
    return isReplaced;
  }

  public V getReplacedValue() {
    return replacedValue;
  }

}

