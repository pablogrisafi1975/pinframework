package com.pinframework.upload;

import com.pinframework.exception.PinFileUploadRuntimeException;
import com.pinframework.exception.PinIORuntimeException;
import com.pinframework.exception.PinUnsupportedEncodingRuntimeException;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class PinMutipartParamsParserImpl implements PinMutipartParamsParser {

  @Override
  public MultipartParams parse(HttpExchange httpExchange) {
    RequestContext requestContext = new PinHttpHandlerRequestContext(httpExchange);
    DiskFileItemFactory diskItemFactory = new DiskFileItemFactory();
    ServletFileUpload up = new ServletFileUpload(diskItemFactory);
    try {
      List<FileItem> fileItems = up.parseRequest(requestContext);
      Map<String, FileParam> fileParams =
          Collections.unmodifiableMap(fileItems.stream().filter(fi -> !fi.isFormField())
              .collect(Collectors.toMap(fi -> fi.getFieldName(), fi -> createFileParam(fi))));
      Map<String, Object> postParams =
          Collections.unmodifiableMap(fileItems.stream().filter(fi -> fi.isFormField())
              .collect(Collectors.toMap(fi -> fi.getFieldName(), fi -> utf8Value(fi))));
      return new MultipartParams(fileParams, postParams);
    } catch (FileUploadException ex) {
      throw new PinFileUploadRuntimeException(ex);
    }
  }

  private String utf8Value(FileItem fi) {
    try {
      return fi.getString(StandardCharsets.UTF_8.name());
    } catch (UnsupportedEncodingException ex) {
      throw new PinUnsupportedEncodingRuntimeException(ex);
    }
  }

  private FileParam createFileParam(FileItem fi) {
    try {
      return new FileParam(fi.getName(), fi.getContentType(), fi.getSize(), fi.getInputStream());
    } catch (IOException ex) {
      throw new PinIORuntimeException(ex);
    }
  }



}
