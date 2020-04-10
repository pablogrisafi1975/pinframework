package com.pinframework;

public class PinResponse {

    private final int status;
    private final Object obj;
    private final PinRender render;

    /**
     * @param status      If you don't have nice constants around, use java.net.HttpURLConnection
     * @param obj         the object to be rendered
     * @param render the render that will render the object
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

    public PinRender getRender() {
        return render;
    }

    public boolean keepResponseOpen() {
        return false;
    }

}
