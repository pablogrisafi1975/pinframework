package com.pinframework;

public class PinResponse {

	private final int status;
	private final Object obj;
	private final PinRender transformer;

	/**
	 * @param status. If you don't have nice constants around, use java.net.HttpURLConnection
	 * @param obj
	 * @param transformer
	 */
	public PinResponse(int status, Object obj, PinRender transformer) {
		this.status = status;
		this.obj = obj;
		this.transformer = transformer;
	}

	public int getStatus() {
		return status;
	}

	public Object getObj() {
		return obj;
	}

	public PinRender getTransformer() {
		return transformer;
	}
	
	public boolean keepResponseOpen(){
		return false;
	}

}
