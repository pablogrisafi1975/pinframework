package com.pinframework.handler;

import com.pinframework.PinExchange;
import com.pinframework.PinHandler;
import com.pinframework.PinResponse;
import com.pinframework.PinResponses;
import com.pinframework.requestmatcher.PinExternalFileRequestMatcher;
import java.io.FileInputStream;
import java.io.InputStream;

public class PinExternalFileHandler implements PinHandler {

  @Override
  public PinResponse handle(PinExchange pinExchange) throws Exception {
    String fileName = pinExchange.pathParams().get(PinExternalFileRequestMatcher.FILE_NAME);
    String externalFileName =
        pinExchange.pathParams().get(PinExternalFileRequestMatcher.EXTERNAL_FILE_NAME);
    InputStream inputStream = new FileInputStream(externalFileName);
    return PinResponses.okFile(inputStream, fileName);
  }

}
