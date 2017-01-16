package com.pinframework.render;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.google.gson.Gson;
import com.pinframework.PinGson;
import com.pinframework.PinMimeType;
import com.pinframework.PinRender;
import com.pinframework.PinServer;
import com.pinframework.PinUtils;

public class PinRenderUt implements PinRender {
	
	public static final ScriptEngine NASHORN = new ScriptEngineManager().getEngineByName("nashorn");
	private final String templateContent;

	public PinRenderUt(String template) {
		InputStream is = PinServer.class.getClassLoader().getResourceAsStream("dynamic/" + template);
		this.templateContent = PinUtils.asString(is);
	}

	@Override
	public void render(Object obj, OutputStream outputStream) throws IOException {
		try {
//			NASHORN.eval("function ff(a){return '-->' + a + '<--';}");
//			NASHORN.eval("var ff = new Function('a', '{return \"-->\" + a + \"<--\";}')");
			NASHORN.eval(new InputStreamReader(PinServer.class.getClassLoader().getResourceAsStream("jsut.js"), StandardCharsets.UTF_8));
			Invocable invocable = (Invocable) NASHORN;
			String evaluatedTemplate = (String)  invocable.invokeFunction("tmpl", templateContent, PinGson.getInstance().toJson(obj));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
			pw.write(evaluatedTemplate);
			pw.flush();
			pw.close();

		} catch (ScriptException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void changeHeaders(Map<String, List<String>> responseHeaders) {
		PinUtils.put(responseHeaders, PinMimeType.CONTENT_TYPE, "text/html; charset=UTF-8");
	}

}
