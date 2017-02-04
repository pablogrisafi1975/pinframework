package com.pinframework.upload;

import com.pinframework.PinUtils;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class FileParam {
  private final String name;
  private final String contentType;
  private final Long size;
  private final InputStream inputStream;

  private byte[] content;

  /**
   * Constructs a File parameter with the given values.
   * 
   * @param name The name of file
   * @param contentType The automatically extracted content type
   * @param size The size of the file
   * @param inputStream The content of the file
   */
  public FileParam(String name, String contentType, Long size, InputStream inputStream) {
    this.name = name;
    this.contentType = contentType;
    this.size = size;
    this.inputStream = inputStream;
  }

  /**
   * This is NOT a getter. Content will be extracted on first call.
   * 
   * @return The full content of the file
   */
  public synchronized byte[] content() {
    if (content == null) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PinUtils.copy(inputStream, out);
      content = out.toByteArray();
    }
    return content;
  }

  public String getContentType() {
    return contentType;
  }

  public Long getSize() {
    return size;
  }

  public String getName() {
    return name;
  }

  public InputStream getInputStream() {
    return inputStream;
  }



}
