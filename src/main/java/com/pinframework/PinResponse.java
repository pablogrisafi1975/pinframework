package com.pinframework;

public class PinResponse {

  //TODO: hacer una interface y una basic response. Eliminar las implementaciones triviales.
  private final int status;
  private final Object obj;
  private final PinRender render;

  /**
   * Creates a PinResponse.
   * @param status If you don't have nice constants around, use java.net.HttpURLConnection.
   * @param obj The object to render
   * @param render The renderer that will render the object
   */
  public PinResponse(int status, Object obj, PinRender render) {
    this.status = status;
    this.obj = obj;
    this.render = render;
  }

  public int getStatus() {
    return status;
  }

  public Object getObj() {
    return obj;
  }

  public PinRender getTransformer() {
    return render;
  }

  public boolean keepResponseOpen() {
    return false;
  }

}
