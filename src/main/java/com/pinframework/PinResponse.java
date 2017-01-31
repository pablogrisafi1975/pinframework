package com.pinframework;

public interface PinResponse {
  int getStatus();

  Object getObj();

  PinRender getRender();

  default boolean keepOpen() {
    return false;
  }

}
