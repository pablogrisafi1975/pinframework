package com.pinframework.response;

import com.pinframework.PinRender;
import com.pinframework.PinResponse;

public class PinBaseResponse implements PinResponse {

  private final int status;
  private final Object obj;
  private final PinRender render;

  /**
   * Creates a PinResponse.
   * 
   * @param status If you don't have nice constants around, use java.net.HttpURLConnection.
   * @param obj The object to render
   * @param render The renderer that will render the object
   */
  public PinBaseResponse(int status, Object obj, PinRender render) {
    this.status = status;
    this.obj = obj;
    this.render = render;
  }

  @Override
  public int getStatus() {
    return status;
  }

  @Override
  public Object getObj() {
    return obj;
  }

  @Override
  public PinRender getRender() {
    return render;
  }

}
