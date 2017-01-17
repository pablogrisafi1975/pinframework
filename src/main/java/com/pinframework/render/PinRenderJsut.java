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

import com.pinframework.PinContentType;
import com.pinframework.PinGson;
import com.pinframework.PinRender;
import com.pinframework.PinServer;
import com.pinframework.PinUtils;
import com.pinframework.exception.PinFileRenderRuntimeException;
import com.pinframework.exception.PinInitializationException;

public class PinRenderJsut implements PinRender {

	public static class Input {

		public Input(Object object, String template) {
			this.object = object;
			this.template = template;
		}

		public final String template;
		public final Object object;
	}

	public static final PinRender INSTANCE = new PinRenderJsut();
	private final Invocable invocable;

	public PinRenderJsut() {
		ScriptEngine nashornEngine = new ScriptEngineManager().getEngineByName("nashorn");
		try {
			nashornEngine.eval(new InputStreamReader(PinServer.class.getClassLoader().getResourceAsStream("jsut.js"),
					StandardCharsets.UTF_8));
		} catch (ScriptException e) {
			throw new PinInitializationException("Can not initialize jsut templates", e);
		}
		this.invocable = (Invocable) nashornEngine;
	}

	@Override
	public void render(Object obj, OutputStream outputStream) throws IOException {
		Input input = (Input) obj;
		String jsonObject = PinGson.getInstance().toJson(input.object);
		try {
			InputStream is = PinServer.class.getClassLoader().getResourceAsStream("dynamic/" + input.template);
			if(is == null){
				throw new PinFileRenderRuntimeException("Error load template '" + input.template + "'" +
						". Templates should be in folder in src/main/resources/dynamic");
			}
			String templateContent = PinUtils.asString(is);
			String evaluatedTemplate = (String) invocable.invokeFunction("tmpl", templateContent,
					jsonObject);
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), false);
			pw.write(evaluatedTemplate);
			pw.flush();
			pw.close();

		} catch (ScriptException | NoSuchMethodException e) {
			throw new PinFileRenderRuntimeException("Error rendering template '" + input.template + "' with object :" + jsonObject +
					". Remember to use <%= name %> to show the content of name (the = is vital!)",  e);
		}
	}

	@Override
	public void changeHeaders(Map<String, List<String>> responseHeaders) {
		PinUtils.put(responseHeaders, PinContentType.CONTENT_TYPE, PinContentType.TEXT_HTML_UTF8);
	}

}
