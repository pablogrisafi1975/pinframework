package com.pinframework.handler;

import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinResponse;
import com.pinframework.PinResponses;
import com.pinframework.PinServer;
import com.pinframework.requestmatcher.PinInternalFileRequestMatcher;

import java.io.InputStream;

public class PinInternalFileHandler implements PinHandler {

  @Override
  public PinResponse handle(PinExchange pinExchange) throws Exception {
    String fileName = pinExchange.pathParams().get(PinInternalFileRequestMatcher.FILE_NAME);
    String internalResourceName =
        pinExchange.pathParams().get(PinInternalFileRequestMatcher.INTERNAL_RESOURCE_NAME);
    InputStream inputStream =
        PinServer.class.getClassLoader().getResourceAsStream(internalResourceName);
    return PinResponses.okFile(inputStream, fileName);
  }

}
