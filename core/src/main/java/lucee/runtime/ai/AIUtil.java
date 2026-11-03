package lucee.runtime.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import lucee.commons.digest.Base64Encoder;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.ContentType;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ClassUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.net.http.HTTPResponse;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.PageContext;
import lucee.runtime.coder.CoderException;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.proxy.ProxyDataImpl;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.ArrayImpl;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Query;
import lucee.runtime.type.QueryImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.util.AI;
import lucee.transformer.library.ClassDefinitionImpl;

public final class AIUtil {

	private static final Key CREATED_AT = KeyImpl.init("createdAt");
	private static final Key STATUS_DETAILS = KeyImpl.init("statusDetails");
	public static final AI INSTANCE = new AIImpl();

	/**
	 * @return true when the listener asked to cancel the inquiry, a null listener or one that can not
	 *         cancel never does
	 */
	public static boolean isCancelled(AIResponseListener listener) {
		return listener instanceof CancelableAIResponseListener && ((CancelableAIResponseListener) listener).isCancelled();
	}

	public static PageException toException(AIEngine engine, String msg, String type, String code, int statusCode) {
		String appendix = "";
		if ("model_not_found".equals(code) || msg.equals("invalid_model") || msg.indexOf("models") != -1) {

			String models = AIUtil.getModelNamesAsStringList(engine);
			if (!StringUtil.isEmpty(models, true)) {
				appendix = " Available model names are [" + models + "]";
			}
		}

		PageException ae = new ApplicationException(msg + appendix + "; type:" + type + "; code:" + code + "; status-code:" + statusCode);
		ae.setErrorCode(code);
		return ae;
	}

	public static void valdate(AIEngine aie, int connectTimeout, int socketTimeout) throws PageException {
		AISession session = aie.createSession("keep the answer short", null, -1, -1D, connectTimeout <= 0 ? aie.getConnectTimeout() : connectTimeout,
				socketTimeout <= 0 ? aie.getSocketTimeout() : socketTimeout);
		session.inquiry("ping");
	}

	public static List<String> getModelNames(AIEngine aie) {
		List<AIModel> models = aie.getModels(null);
		List<String> names = new ArrayList<>();
		if (models != null) {
			for (AIModel m: models) {
				names.add(m.getName());
			}
			Collections.sort(names);
		}
		return names;
	}

	public static String findModelName(AIEngine aie, String name) {
		List<AIModel> models = aie.getModels(null);
		if (models != null) {
			for (AIModel m: models) {
				if (m.getName().equalsIgnoreCase(name)) return m.getName();
				if (m.getName().equalsIgnoreCase(name + ":latest")) return m.getName();
				if (m.getLabel().equalsIgnoreCase(name)) return m.getName();
			}
		}
		return null;
	}

	public static String getModelNamesAsStringList(AIEngine aie) {
		return getModelNamesAsStringList(getModelNames(aie));
	}

	public static String getModelNamesAsStringList(List<String> models) {
		StringBuilder sb = new StringBuilder();
		for (String name: models) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(name);
		}
		return sb.toString();
	}

	/**
	 * returns the models available for the given engine as a query with the columns name, label, description and custom
	 */
	public static Query getModelsAsQuery(AIEngine aie) throws PageException {
		List<AIModel> models = aie.getModels();
		Query qry = new QueryImpl(new Key[] { KeyConstants._name, KeyConstants._label, KeyConstants._description, KeyConstants._custom }, models.size(), "models");
		int row = 0;
		for (AIModel m: models) {
			row++;
			qry.setAt(KeyConstants._name, row, m.getName());
			qry.setAt(KeyConstants._label, row, m.getLabel());
			qry.setAt(KeyConstants._description, row, m.getDescription());
			qry.setAt(KeyConstants._custom, row, m.asStruct());
		}
		return qry;
	}

	public static Struct getMetaData(AIEngine aie, boolean addModelsInfo, boolean addFilesInfo) throws PageException {

		Struct meta = new StructImpl();

		meta.set(KeyConstants._label, aie.getLabel());
		meta.set(KeyConstants._model, aie.getModel());
		meta.set(KeyConstants._id, aie.getId());
		if (aie.getTemperature() != null) meta.set(KeyConstants._temperature, aie.getTemperature());
		meta.set("connectTimeout", aie.getConnectTimeout());
		meta.set("conversationSizeLimit", aie.getConversationSizeLimit());
		meta.set(KeyConstants._name, aie.getName());
		AITokenLimits limits = AITokenLimits.get(aie);
		if (!limits.isEmpty()) meta.set("tokenLimits", limits.toStruct());

		// models
		if (addModelsInfo) {
			meta.set(KeyConstants._models, getModelsAsQuery(aie));
		}

		// files
		if (addFilesInfo && aie instanceof AIEngineFile) {
			AIEngineFile aief = (AIEngineFile) aie;
			List<AIFile> files = aief.listFiles();

			// String status, String statusDetails
			Query qry = new QueryImpl(new Key[] { KeyConstants._object, KeyConstants._id, KeyConstants._purpose, KeyConstants._filename, KeyConstants._bytes, CREATED_AT,
					KeyConstants._status, STATUS_DETAILS }, files.size(), "files");
			int row = 0;
			for (AIFile f: files) {
				row++;
				qry.setAt(KeyConstants._object, row, f.getObject());
				qry.setAt(KeyConstants._id, row, f.getId());
				qry.setAt(KeyConstants._purpose, row, f.getPurpose());
				qry.setAt(KeyConstants._filename, row, f.getFilename());
				qry.setAt(KeyConstants._bytes, row, f.getBytes());
				qry.setAt(CREATED_AT, row, f.getCreatedAt());
				qry.setAt(KeyConstants._status, row, f.getStatus());
				qry.setAt(STATUS_DETAILS, row, f.getStatusDetails());
			}
			meta.set(KeyConstants._files, qry);
		}
		return meta;
	}

	private static final String getCharset(ContentType ct) {
		String charset = null;
		if (ct != null) charset = ct.getCharset();
		if (!StringUtil.isEmpty(charset)) return charset;

		PageContext pc = ThreadLocalPageContext.get();
		if (pc != null) return pc.getWebCharset().name();
		return "ISO-8859-1";
	}

	public static void addConversation(AISession session, List<Conversation> history, Conversation conversation) {
		history.add(conversation);
		while (history.size() > session.getConversationSizeLimit()) {
			history.remove(0);
		}
	}

	/**
	 * same as {@link #addConversation(AISession, List, Conversation)}, but also adds the call to the
	 * usage of the session and writes the usage line to the ai log (LDEV-6527)
	 * 
	 * @param provider the name of the provider, using the OpenTelemetry GenAI naming (openai, anthropic,
	 *            gcp.gemini)
	 * @param requestModel the model that was requested
	 * @param startNanos System.nanoTime() when the call started
	 * @param streamed was the answer streamed
	 */
	public static void addConversation(AISession session, List<Conversation> history, Conversation conversation, String provider, String requestModel, long startNanos,
			boolean streamed) {
		addConversation(session, history, conversation);
		AISessionSupport sessionSupport = session instanceof AISessionSupport ? (AISessionSupport) session : null;
		if (sessionSupport == null) return;

		Response rsp = conversation.getResponse();
		AIUsage usage;
		String responseModel = null;
		if (rsp instanceof AIUsageProvider) {
			usage = ((AIUsageProvider) rsp).getUsage();
			responseModel = ((AIUsageProvider) rsp).getResponseModel();
		}
		else usage = AIUsage.fromTotal(rsp.getTotalTokenUsed());
		if (StringUtil.isEmpty(responseModel, true)) responseModel = null;

		long durationMs = (System.nanoTime() - startNanos) / 1000000L;
		AIUsageRecord record = new AIUsageRecord(usage, responseModel != null ? responseModel : requestModel, durationMs, streamed);
		sessionSupport.addUsage(record);

		StringBuilder sb = new StringBuilder("usage");
		appendLogValue(sb, "gen_ai.operation.name", "chat");
		appendLogValue(sb, "gen_ai.provider.name", provider);
		appendLogValue(sb, "gen_ai.request.model", requestModel);
		appendLogValue(sb, "gen_ai.response.model", responseModel);
		if (usage != null) {
			appendLogValue(sb, "gen_ai.usage.input_tokens", usage.input);
			appendLogValue(sb, "gen_ai.usage.output_tokens", usage.output);
			appendLogValue(sb, "gen_ai.usage.total_tokens", usage.total);
			appendLogValue(sb, "gen_ai.usage.cache_read.input_tokens", usage.cached);
			appendLogValue(sb, "gen_ai.usage.reasoning.output_tokens", usage.reasoning);
		}
		else appendLogValue(sb, "gen_ai.usage.reported", "false");
		appendLogValue(sb, "gen_ai.conversation.id", session.getId());
		appendLogValue(sb, "gen_ai.request.stream", String.valueOf(streamed));
		appendLogValue(sb, "duration_ms", durationMs);
		appendLogValue(sb, "ai.connection", session.getEngine().getName());
		LogUtil.logx(null, Log.LEVEL_INFO, "ai", sb.toString(), "ai", "application");
	}

	/**
	 * the session as AISessionSupport (the only type that tracks usage), null for third party sessions
	 */
	public static AISessionSupport toSessionSupport(AISession session) {
		if (session instanceof AISessionMultipartWrapper) session = ((AISessionMultipartWrapper) session).getDelegate();
		return session instanceof AISessionSupport ? (AISessionSupport) session : null;
	}

	/**
	 * attaches the listener (a component or struct with the hooks) to the session (LDEV-6540)
	 */
	public static void setListener(PageContext pc, AISession session, Object listener) throws PageException {
		if (listener == null) return;
		AISessionSupport support = toSessionSupport(session);
		if (support == null) throw new ApplicationException("the AI session [" + session.getClass().getName() + "] does not support a listener");
		support.setListener(AISessionListener.getInstance(pc, listener));
	}

	/**
	 * a call that failed or was aborted, written as WARN to the ai log (LDEV-6527)
	 */
	public static void addFailedInquiry(AISession session, String provider, String requestModel, long startNanos, boolean streamed, Throwable t) {
		if (session instanceof AISessionSupport) ((AISessionSupport) session).addFailedRequest();

		StringBuilder sb = new StringBuilder("usage");
		appendLogValue(sb, "gen_ai.operation.name", "chat");
		appendLogValue(sb, "gen_ai.provider.name", provider);
		appendLogValue(sb, "gen_ai.request.model", requestModel);
		appendLogValue(sb, "error.type", t.getClass().getName());
		appendLogValue(sb, "gen_ai.conversation.id", session.getId());
		appendLogValue(sb, "gen_ai.request.stream", String.valueOf(streamed));
		appendLogValue(sb, "duration_ms", (System.nanoTime() - startNanos) / 1000000L);
		appendLogValue(sb, "ai.connection", session.getEngine().getName());
		appendLogValue(sb, "status", "failed");
		LogUtil.logx(null, Log.LEVEL_WARN, "ai", sb.toString(), "ai", "application");
	}

	/**
	 * a token limit of the connection was reached or crossed (LDEV-6528), written as WARN to the ai log
	 * 
	 * @param limit the name of the setting
	 * @param phase before or after the call
	 */
	public static void logTokenLimit(AISession session, String limit, long max, long used, String action, String phase) {
		StringBuilder sb = new StringBuilder("limit");
		appendLogValue(sb, "ai.limit", limit);
		appendLogValue(sb, "ai.limit.max", max);
		appendLogValue(sb, "ai.limit.used", used);
		appendLogValue(sb, "ai.limit.action", action);
		appendLogValue(sb, "ai.limit.phase", phase);
		appendLogValue(sb, "gen_ai.conversation.id", session.getId());
		appendLogValue(sb, "ai.connection", session.getEngine().getName());
		LogUtil.logx(null, Log.LEVEL_WARN, "ai", sb.toString(), "ai", "application");
	}

	private static void appendLogValue(StringBuilder sb, String key, long value) {
		if (value < 0L) return;
		sb.append(' ').append(key).append('=').append(value);
	}

	private static void appendLogValue(StringBuilder sb, String key, String value) {
		if (StringUtil.isEmpty(value, true)) return;
		sb.append(' ').append(key).append('=');
		boolean quote = false;
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (c <= ' ' || c == '"' || c == '=') {
				quote = true;
				break;
			}
		}
		if (!quote) sb.append(value);
		else {
			sb.append('"');
			for (int i = 0; i < value.length(); i++) {
				char c = value.charAt(i);
				if (c == '"' || c == '\\') sb.append('\\').append(c);
				else if (c < ' ') sb.append(' ');
				else sb.append(c);
			}
			sb.append('"');
		}
	}

	public static String createJsonContentType(String charset) {
		if (StringUtil.isEmpty(charset, true)) return "application/json";
		return "application/json; charset=" + charset.trim();
	}

	public static int getStatusCode(HTTPResponse response) {
		if (response != null) {
			return response.getStatusCode();
		}
		return -1;
	}

	public static String extractStringAnswer(Response rsp) {
		List<Part> parts = rsp.getAnswers();
		if (parts.isEmpty()) return rsp.getAnswer();

		StringBuilder sb = new StringBuilder();
		String a;
		for (Part rp: parts) {
			if (rp.isStructured() || !rp.isText()) continue;
			a = rp.getAsString();
			if (a != null) sb.append(a);
		}
		return sb.length() > 0 ? sb.toString() : rsp.getAnswer();
	}

	public static List<Part> getAnswersFromAnswer(Response rsp) {
		List<Part> parts = new ArrayList<>();
		parts.add(new PartImpl(rsp.getAnswer(), 0));
		return parts;
	}

	private static class AIImpl implements AI {

		@Override
		public void valdate(AIEngine aie, int connectTimeout, int socketTimeout) throws PageException {
			AIUtil.valdate(aie, connectTimeout, socketTimeout);
		}

		@Override
		public List<String> getModelNames(AIEngine aie) {
			return AIUtil.getModelNames(aie);
		}

		@Override
		public String findModelName(AIEngine aie, String name) {
			return AIUtil.findModelName(aie, name);
		}

		@Override
		public String getModelNamesAsStringList(AIEngine aie) {
			return AIUtil.getModelNamesAsStringList(aie);
		}

		@Override
		public Struct getMetaData(AIEngine aie, boolean addModelsInfo, boolean addFilesInfo) throws PageException {
			return AIUtil.getMetaData(aie, addModelsInfo, addFilesInfo);
		}

		@Override
		public String extractStringAnswer(Response rsp) {
			return AIUtil.extractStringAnswer(rsp);
		}

		@Override
		public List<Part> getAnswersFromAnswer(Response rsp) {
			return AIUtil.getAnswersFromAnswer(rsp);
		}

	}

	public static String serialize(AISession ais, int maxLength, boolean condense) throws PageException {
		Struct data = new StructImpl(StructImpl.TYPE_LINKED);

		data.setEL(KeyConstants._temperature, ais.getTemperature());
		data.setEL(KeyConstants._limit, ais.getConversationSizeLimit());
		data.setEL(KeyConstants._connectionTimeout, ais.getConnectTimeout());
		data.setEL("socketTimeout", ais.getSocketTimeout());
		data.setEL("systemMessage", ais.getSystemMessage());
		Request req;
		Response rsp;
		Struct sct;
		Array arr = CFMLEngineFactory.getInstance().getCreationUtil().createArray();
		Conversation[] history = ais.getHistory();
		Conversation c;
		for (int i = maxLength == -1 || maxLength >= history.length ? 0 : (history.length - maxLength); i < history.length; i++) {
			c = history[i];
			sct = new StructImpl(StructImpl.TYPE_LINKED);
			arr.append(sct);

			// request
			req = c.getRequest();
			if (req.isMultiPart()) {
				Array _arr = new ArrayImpl();
				appendParts(_arr, req.getQuestions());
				sct.set(KeyConstants._question, _arr);
			}
			else {
				sct.set(KeyConstants._question, req.getQuestion());
			}

			// response
			rsp = c.getResponse();
			if (serializeAnswerAsParts(rsp)) {
				Array _arr = new ArrayImpl();
				appendParts(_arr, rsp.getAnswers());
				sct.set(KeyConstants._answer, _arr);
			}
			else {
				sct.set(KeyConstants._answer, rsp.getAnswer());
			}
		}
		data.setEL(KeyConstants._history, arr);

		AISessionSupport support = toSessionSupport(ais);
		if (support != null) data.setEL("usage", support.getUsage());

		if (condense) {

			String systemMessage = "You are analyzing a conversation history between a human and an AI. \n"

					+ "Your task is to create a concise summary of the earlier parts of this conversation that:\n"

					+ "1. Reduce it to the absolute minimum\n"

					+ "2. Only preserves essential information, key facts, and critical context\n"

					+ "3. Maintains the flow and logical progression of important topics\n"

					+ "4. Captures any decisions or conclusions reached\n"

					+ "5. Omits all pleasantries, repetitive content, and non-essential text\n"

					+ "6. remove all conversation you know the answer";

			String message = "Here is the conversation history to summarize:\n"

					+ "\n"

					+ "CONVERSATION HISTORY:\n"

					+ CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data, true)

					+ "\n\n"

					+ "FORMAT YOUR RESPONSE AS:\n"

					+ "[Condensed representation of the conversation with only the most important in the exact same json format as provided with no leading ``` or anything]\n"

					+ "\";";

			// validate
			// TODO iterate in case the result is not good with further instructions for the AI
			try {
				AISession session = ais.getEngine().createSession(systemMessage, null, -1, -1D, -1, -1);
				rsp = session.inquiry(message);
				Struct condensed = CFMLEngineFactory.getInstance().getCastUtil().fromJsonStringToStruct(rsp.getAnswer());
				Array tmp = Caster.toArray(condensed.get(KeyConstants._history));
				// test if we can load it
				toConversations(tmp);
				data.setEL(KeyConstants._history, tmp);
			}
			catch (Exception e) {
				LogUtil.log("ai", e);
			}
		}

		return CFMLEngineFactory.getInstance().getCastUtil().fromStructToJsonString(data, false);
	}

	public static Conversation[] toConversations(Array raw) throws PageException {
		Iterator<Object> it = raw.valueIterator();
		Struct sct;
		Conversation[] conversations = new Conversation[raw.size()];
		int index = 0;
		while (it.hasNext()) {
			sct = Caster.toStruct(it.next());

			Object obj = sct.get(KeyConstants._question);
			RequestSupport req;
			if (obj instanceof CharSequence) {
				req = new RequestSupport(Caster.toString(obj));
			}
			else {
				req = new RequestSupport(toParts(Caster.toArray(obj)));
			}

			conversations[index++] = new ConversationImpl(

					req,

					toResponse(sct.get(KeyConstants._answer))

			);
		}
		return conversations;
	}

	private static Response toResponse(Object obj) throws PageException {
		if (obj == null) return new SimpleResponse(null);
		if (obj instanceof CharSequence) return new SimpleResponse(Caster.toString(obj));
		return new PartsResponse(toParts(Caster.toArray(obj)));
	}

	private static boolean serializeAnswerAsParts(Response rsp) {
		if (rsp.isMultiPart()) return true;
		List<Part> parts = rsp.getAnswers();
		if (parts.isEmpty()) return false;
		if (parts.size() > 1) return true;
		Part part = parts.get(0);
		return part.isStructured() || !part.isText();
	}

	private static void appendParts(Array arr, List<Part> parts) throws PageException {
		Struct _sct;
		for (Part p: parts) {
			_sct = new StructImpl();
			arr.append(_sct);
			_sct.set(KeyConstants._contenttype, p.getContentType());
			if (p.isStructured()) {
				_sct.set(KeyConstants._type, "struct");
				_sct.set(KeyConstants._content, p.getAsStruct());
			}
			else if (p.isText()) {
				_sct.set(KeyConstants._type, "text");
				_sct.set(KeyConstants._content, p.getAsString());
			}
			else {
				_sct.set(KeyConstants._type, "binary");
				_sct.set(KeyConstants._content, Caster.toBase64(p.getAsBinary()));
			}
		}
	}

	private static List<Part> toParts(Array array) throws PageException {
		List<Part> list = new ArrayList<>();
		Iterator<Object> it = array.valueIterator();
		Struct sct;
		String contenttype, type;
		while (it.hasNext()) {
			sct = Caster.toStruct(it.next());
			type = Caster.toString(sct.get(KeyConstants._type));
			contenttype = Caster.toString(sct.get(KeyConstants._contenttype));

			if ("struct".equals(type)) {
				list.add(new PartImpl(null, list.size(), Caster.toStruct(sct.get(KeyConstants._content)), contenttype));
			}
			// text
			else if ("text".equals(type)) {
				list.add(new PartImpl(null, list.size(), Caster.toString(sct.get(KeyConstants._content)), contenttype));
			}
			else {
				try {
					list.add(new PartImpl(null, list.size(), Base64Encoder.decode(Caster.toString(sct.get(KeyConstants._content)), false), contenttype));
				}
				catch (CoderException ce) {
					throw Caster.toPageException(ce);
				}
			}
		}
		return list;
	}

	public static AIEngine getEngine(Class<? extends AIEngine> clazz, Struct properties) throws ClassException, PageException {
		return ((AIEngine) ClassUtil.loadInstance(clazz)).init(new ClassDefinitionImpl<>(clazz), properties, null, null, null);
	}

	public static AISession createSession(AIEngine aie) throws PageException {
		return aie.createSession(null, null, -1, -1D, aie.getConnectTimeout(), aie.getSocketTimeout());
	}

	public static String extractTextFromParts(List<Part> parts) {
		if (parts == null) return "";

		StringBuilder sb = new StringBuilder();
		for (Part part: parts) {
			if (part.isText()) {
				String text = part.getAsString();
				if (!StringUtil.isEmpty(text)) {
					if (sb.length() > 0) sb.append(" ");
					sb.append(text);
				}
			}
		}
		return sb.toString();
	}

	public static boolean isTextOnly(List<Part> answers) {
		if (answers == null || answers.isEmpty()) return true;
		for (Part a: answers) {
			if (a.isStructured() || !a.isText()) return false;
		}
		return true;
	}

	/**
	 * the configured server proxy for the given host (LDEV-6580), null if there is none or the host is
	 * excluded
	 *
	 * @param host host of the target URL, used to check the proxy exclusion list
	 */
	public static ProxyData getProxyData(String host) {
		Config config = ThreadLocalPageContext.getConfig();
		if (config == null) return null;
		return ProxyDataImpl.validate(config.getProxyData(), host);
	}
}
