package com.pinframework;

import java.util.HashMap;
import java.util.Map;

public class PinMimeType {


    private static final Map<String, String> MAP = new HashMap<>();

    private PinMimeType() {

    }

    static {
        MAP.put("au", "audio/basic");
        MAP.put("avi", "video/msvideo,video/avi,video/x-msvideo");
        MAP.put("bmp", "image/bmp");
        MAP.put("bz2", "application/x-bzip2");
        MAP.put("css", "text/css");
        MAP.put("dtd", "application/xml-dtd");
        MAP.put("doc", "application/msword");
        MAP.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MAP.put("dotx", "application/vnd.openxmlformats-officedocument.wordprocessingml.template");
        MAP.put("eot", "application/vnd.ms-fontobject");
        MAP.put("es", "application/ecmascript");
        MAP.put("exe", "application/octet-stream");
        MAP.put("gif", "image/gif");
        MAP.put("gz", "application/x-gzip");
        MAP.put("hqx", "application/mac-binhex40");
        MAP.put("html", "text/html; charset=utf-8");
        MAP.put("jar", "application/java-archive");
        MAP.put("jpg", "image/jpeg");
        MAP.put("js", "application/javascript");
        MAP.put("midi", "audio/x-midi");
        MAP.put("mp3", "audio/mpeg");
        MAP.put("mpeg", "video/mpeg");
        MAP.put("ogg", "audio/vorbis,application/ogg");
        MAP.put("otf", "application/font-otf");
        MAP.put("pdf", "application/pdf");
        MAP.put("pl", "application/x-perl");
        MAP.put("png", "image/png");
        MAP.put("potx", "application/vnd.openxmlformats-officedocument.presentationml.template");
        MAP.put("ppsx", "application/vnd.openxmlformats-officedocument.presentationml.slideshow");
        MAP.put("ppt", "application/vnd.ms-powerpointtd");
        MAP.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        MAP.put("ps", "application/postscript");
        MAP.put("qt", "video/quicktime");
        MAP.put("ra", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        MAP.put("rar", "application/x-rar-compressed");
        MAP.put("ram", "audio/x-pn-realaudio,audio/vnd.rn-realaudio");
        MAP.put("rdf", "application/rdf,application/rdf+xml");
        MAP.put("rtf", "application/rtf");
        MAP.put("sgml", "text/sgml");
        MAP.put("sit", "application/x-stuffit");
        MAP.put("sldx", "application/vnd.openxmlformats-officedocument.presentationml.slide");
        MAP.put("svg", "image/svg+xml");
        MAP.put("swf", "application/x-shockwave-flash");
        MAP.put("tgz", "application/x-tar");
        MAP.put("tiff", "image/tiff");
        MAP.put("tsv", "text/tab-separated-values");
        MAP.put("ttf", "application/font-ttf");
        MAP.put("txt", "text/plain");
        MAP.put("wav", "audio/wav,audio/x-wav");
        MAP.put("woff", "application/font-woff");
        MAP.put("woff2", "application/font-woff2");
        MAP.put("xlam", "application/vnd.ms-excel.addin.macroEnabled.12");
        MAP.put("xls", "application/vnd.ms-excel");
        MAP.put("xlsb", "application/vnd.ms-excel.sheet.binary.macroEnabled.12");
        MAP.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MAP.put("xltx", "application/vnd.openxmlformats-officedocument.spreadsheetml.template");
        MAP.put("xml", "application/xml");
        MAP.put("zip", "application/zip,application/x-compressed-zip");
    }

    public static Map<String, String> cloneMap() {
        return new HashMap<>(MAP);
    }
}
