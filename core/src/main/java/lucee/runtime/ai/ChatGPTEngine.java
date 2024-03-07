package lucee.runtime.ai;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import lucee.print;
import lucee.commons.io.CharsetUtil;
import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ResourcesImpl;
import lucee.commons.net.HTTPUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;
import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.commons.net.http.httpclient.HeaderImpl;
import lucee.loader.util.Util;
import lucee.runtime.converter.JSONConverter;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.SerializationSettings;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class ChatGPTEngine implements AIEngine {
	private static final URL DEFAULT_URL;
	private static final int DEFAULT_TIMEOUT = 0;
	private static final String DEFAULT_CHARSET = null;
	private static final String DEFAULT_MIMETYPE = null;
	private static final String DEFAULT_USERAGENT = "Lucee (AI Request)";
	private static final String DEFAULT_MODEL = "gpt-3.5-turbo";

	static {
		URL tmp = null;
		try {
			tmp = new URL("https://api.openai.com/v1/chat/completions");
		}
		catch (MalformedURLException e) {
			log(e);

		}
		DEFAULT_URL = tmp;
	}
	private Struct properties;
	private URL url;
	private String secretKey;
	private int timeout;
	private String charset;
	private String mimetype;
	private ProxyData proxy = null;// TODO
	private Map<String, String> formfields = null;
	private String model;

	@Override
	public AIEngine init(Struct properties) throws PageException {
		this.properties = properties;

		// URL
		String str = Caster.toString(properties.get(KeyConstants._URL, null), null);
		if (!Util.isEmpty(str, true)) {
			try {
				url = HTTPUtil.toURL(str.trim(), HTTPUtil.ENCODED_AUTO);
			}
			catch (Exception e) {
				url = DEFAULT_URL;
			}
		}
		else url = DEFAULT_URL;

		// secret key
		str = Caster.toString(properties.get(KeyConstants._secretKey, null), null);
		if (Util.isEmpty(str, true)) {
			throw new ApplicationException("the property [secretKey] is required for the AI Engine ChatGPT!");
		}
		secretKey = str.trim();

		// timeout
		timeout = Caster.toIntValue(properties.get(KeyConstants._timeout, null), DEFAULT_TIMEOUT);
		// charset
		charset = Caster.toString(properties.get(KeyConstants._charset, null), DEFAULT_CHARSET);
		if (Util.isEmpty(charset, true)) charset = null;
		// mimetype
		mimetype = Caster.toString(properties.get(KeyConstants._mimetype, null), DEFAULT_MIMETYPE);
		if (Util.isEmpty(mimetype, true)) mimetype = null;
		// model
		model = Caster.toString(properties.get(KeyConstants._model, DEFAULT_MODEL), DEFAULT_MODEL);
		return this;
	}

	@Override
	public Response invoke(Request req) throws PageException {
		Struct sct = new StructImpl();
		sct.set(KeyConstants._model, model);
		Array arr = new ArrayImpl();
		Struct msg;
		for (String q: req.getQuestions()) {
			msg = new StructImpl();
			msg.set(KeyConstants._role, "user");
			msg.set(KeyConstants._content, q);
			arr.append(msg);
		}
		sct.set(KeyConstants._messages, arr);

		try {
			JSONConverter json = new JSONConverter(true, CharsetUtil.UTF8);// TODO better charset
			String str = json.serialize(null, sct, SerializationSettings.SERIALIZE_AS_COLUMN, null);
			print.e(str);

			HTTPResponse rsp = HTTPEngine4Impl.post(url, null, null, timeout, false, mimetype, charset, DEFAULT_USERAGENT, proxy, new Header[] {

					new HeaderImpl("Authorization", "Bearer " + secretKey)

					, new HeaderImpl("Content-Type", "application/json")

					, new HeaderImpl("Content-Type", "application/json")

			}, formfields, str);

			print.e(rsp.getContentAsString(charset));

		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}

		return null;
	}

	private static void log(MalformedURLException e) {
		LogUtil.log("ai", e); // TODO optimize for external usage
	}

	public static void main(String[] args) throws PageException, IOException {
		Struct props = new StructImpl();
		props.set(KeyConstants._secretKey, "");
		AIEngine ai = new ChatGPTEngine().init(props);

		String code = IOUtil.toString(ResourcesImpl.getFileResourceProvider().getResource("/Users/mic/Test/test-cfconfig/webapps/ROOT/test3.cfm"), CharsetUtil.UTF8);

		Request req = new Request(new String[] { "Please analyze the following Lucee (CFML) code for best practices, performance, and security improvements",
				" give me suggestions for doc comments", "The code is intended for Lucee version 5.4.4.42.", "keep it as short as possible", "Here is the code:", code });

		ai.invoke(req);
	}
}
