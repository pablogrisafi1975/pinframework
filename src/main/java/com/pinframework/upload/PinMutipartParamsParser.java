package com.pinframework.upload;

import com.pinframework.exception.PinInitializationException;
import com.sun.net.httpserver.HttpExchange;

public interface PinMutipartParamsParser {

  MultipartParams parse(HttpExchange httpExchange);

  /**
   * Tries to create the default implementations.
   * 
   * @return default implementation of PinMutipartParamsParser
   * @throws PinInitializationException on any problem, typically missing commons-upload on
   *         classpath
   */
  static PinMutipartParamsParser createImpl() {

    try {
      return (PinMutipartParamsParser) Class
          .forName("com.pinframework.upload.PinMutipartParamsParserImpl").newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
      throw new PinInitializationException(ex);
    }
  }

}
