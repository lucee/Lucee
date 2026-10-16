package lucee.runtime.lsp;

import java.nio.charset.StandardCharsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.ServletException;
import lucee.aprint;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.loader.util.Util;
import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.functions.conversion.DeserializeJSON;
import lucee.runtime.functions.conversion.SerializeJSON;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public final class LSPEndpointFactory implements MessageProcessor {
	public static final int DEFAULT_LSP_PORT = 2089;
	public static final String DEFAULT_COMPONENT = "org.lucee.cfml.lsp.LSPEndpoint";
	public static final long TIMEOUT = 3000;
	public static final String DEFAULT_LOG = "application";
	public static final Key JSONRPC;

	private ServerSocket serverSocket;
	private ExecutorService executor;
	private volatile boolean running = true;
	private CFMLEngine engine;
	private int port;
	private String cfcPath;
	private Log log;
	private boolean stateless;
	private Component cfc;
	private static LSPEndpointFactory instance;

	private ConcurrentHashMap<String, OutputStream> clientOutputStreams = new ConcurrentHashMap<>();
	private AtomicInteger clientCounter = new AtomicInteger(0);
	private final AtomicInteger requestId = new AtomicInteger(0);

	static {
		JSONRPC = KeyImpl.init("jsonrpc");
	}

	private LSPEndpointFactory(Config config) {
		// setup config and utils
		engine = CFMLEngineFactory.getInstance();
		log = LSPUtil.getLog(config);
		port = engine.getCastUtil().toIntValue(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.port", null), DEFAULT_LSP_PORT);
		cfcPath = engine.getCastUtil().toString(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.component", null), DEFAULT_COMPONENT);
		stateless = engine.getCastUtil().toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.stateless", null), false);
		if (Util.isEmpty(cfcPath, true)) cfcPath = DEFAULT_COMPONENT;

		log.info("lsp", "LSP server port: " + port);
		log.info("lsp", "LSP server component endpoint: " + cfcPath);
	}

	public static LSPEndpointFactory getInstance(Config config, boolean forceRestart) throws IOException {
		if (Caster.toBooleanValue(SystemUtil.getSystemPropOrEnvVar("lucee.lsp.enabled", null), false)) {
			synchronized (SystemUtil.createToken("LSPEndpointFactory", "init")) {
				if (forceRestart) {
					aprint.e("- LSPEndpointFactory Restarting");
					if (instance != null) {
						instance.stop();
					}
					instance = new LSPEndpointFactory(config).start();
				}
				else {
					if (instance == null) {
						aprint.e("- LSPEndpointFactory Starting");
						instance = new LSPEndpointFactory(config).start();
					}
				}
			}
			aprint.e("- LSPEndpointFactory Initialized");
		}
		return instance;
	}

	public static LSPEndpointFactory getExistingInstance() {
		return instance;
	}

	public Component getComponent() throws PageException, ServletException {
		// if component was not yet created, create it
		if (cfc == null && !stateless) {
			cfc = engine.getCreationUtil().createComponentFromName(LSPUtil.createPageContext(false), cfcPath);
		}
		return cfc;
	}

	private LSPEndpointFactory start() throws IOException {
		log.info("lsp", "starting LSP server");
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e) {
			error("lsp", e);
			throw e;
		}
		executor = Executors.newCachedThreadPool();

		// Start listening thread
		Thread listenerThread = new Thread(() -> {
			while (running) {
				try {
					Socket clientSocket = serverSocket.accept();
					executor.submit(() -> handleClient(clientSocket));
				}
				catch (IOException e) {
					if (running) {
						error("lsp", e);
					}
				}
			}
		}, "LSP-Listener");

		listenerThread.setDaemon(true);
		listenerThread.start();

		log.info("lsp", "LSP server started");
		return this;
	}

	private void stop() throws IOException {
		running = false;
		if (executor != null) {
			executor.shutdown();
		}
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		// Clean up client connections
		clientOutputStreams.clear();
	}

	private void handleClient(Socket clientSocket) {
		String clientId = "client-" + clientCounter.incrementAndGet();
		log.info("lsp", "LSP server handle client: " + clientId);

		try {
			// Store output stream for this client
			OutputStream out = clientSocket.getOutputStream();
			clientOutputStreams.put(clientId, out);

			BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));

			while (!clientSocket.isClosed()) {
				readStream(this, reader, out);
			}
		}
		catch (Exception e) {
			error("lsp", e);
		}
		finally {
			clientOutputStreams.remove(clientId);
			engine.getIOUtil().closeSilent(clientSocket);
		}
	}

	private static final String CONTENT_LENGTH = "Content-Length:";

	public static void readStream(MessageProcessor mp, BufferedReader reader, OutputStream out) throws PageException, IOException {
		StringBuilder buffer = new StringBuilder();
		// Read incoming data into buffer
		char[] cbuf = new char[1024];
		int len;
		while ((len = reader.read(cbuf)) != -1) {
			buffer.append(cbuf, 0, len);

			// Process complete messages in the buffer
			while (true) {
				// Look for Content-Length header
				final String content = buffer.toString();
				int headerIndex = content.indexOf(CONTENT_LENGTH);
				if (headerIndex == -1) break;

				// TODO allow json message as array [...]
				// we do not trust the whitespace, so we simply search for the start of the json message
				final int contentStart = content.indexOf("{", headerIndex + CONTENT_LENGTH.length());
				if (contentStart == -1) break;

				// number is between
				String strContentLength = content.substring(headerIndex + CONTENT_LENGTH.length(), contentStart).trim();
				int contentLength = Caster.toIntValue(strContentLength);

				// Check if we have the complete message
				if (buffer.length() < contentStart + contentLength) break;

				// Extract the JSON message
				int endIndex = contentStart + contentLength;
				String jsonMessage = content.substring(contentStart, endIndex);
				// in case content length is invalid

				int contentEnd = jsonMessage.lastIndexOf("}");
				int off = (jsonMessage.length() - 1) - contentEnd;
				String prev = null;
				if (off > 0) {
					prev = "[" + jsonMessage.substring(0, 10) + "..." + jsonMessage.substring(jsonMessage.length() - 11) + "]";
					endIndex -= off;
					jsonMessage = content.substring(contentStart, endIndex);
				}

				// simple validation of the message
				if (jsonMessage.startsWith("{") != jsonMessage.endsWith("}")) {
					throw new ApplicationException("json message with content length [" + strContentLength + "] and offset [" + off + "] is not valid: ["
							+ jsonMessage.substring(0, 10) + "..." + jsonMessage.substring(jsonMessage.length() - 11) + "], before correction:" + prev);
				}

				processMessage(mp, jsonMessage, out);

				buffer.delete(0, endIndex);
			}
		}

	}

	private static void processMessage(MessageProcessor mp, String jsonMessage, OutputStream out) throws IOException {
		String response = mp.processMessage(jsonMessage);

		if (response != null) {
			String formattedResponse = LSPUtil.formatLSPMessage(response);
			out.write(formattedResponse.getBytes());
			out.flush();
		}
	}

	@Override
	public String processMessage(String jsonMessage) {
		PageContext previousPC = null, pc = null;
		try {
			log.info("lsp", "Received message: " + jsonMessage);
			previousPC = ThreadLocalPageContext.get();
			pc = LSPUtil.createPageContext(true);
			if (cfc == null || stateless) {
				cfc = engine.getCreationUtil().createComponentFromName(pc, cfcPath);
			}

			String response;
			Object rsp = cfc.call(pc, "execute", new Object[] { DeserializeJSON.call(pc, jsonMessage), this });

			// serialize if needed
			if (rsp instanceof String) {
				response = (String) rsp;
			}
			else {
				response = SerializeJSON.call(pc, rsp);
			}

			log.info("lsp", "response from component [" + cfcPath + "]: " + response);

			return response;
		}
		catch (Exception e) {
			error("lsp", e);
			return null;
		}
		finally {
			LSPUtil.releasePageContext(pc, previousPC);
		}
	}

	@Deprecated
	public void sendMessageToClient(Struct message) throws IOException, PageException, ServletException {
		sendMessageToClient(message, true);
	}

	public void sendMessageToClient(Struct message, boolean addRandomIdIfMissing) throws IOException, PageException, ServletException {
		if (clientOutputStreams.isEmpty()) {
			log.error("lsp", "No connected clients to send message to");
			return;
		}

		Struct data = new StructImpl(StructImpl.TYPE_LINKED);

		// JSONRPC
		data.set(JSONRPC, Caster.toString(message.get(JSONRPC, null), "2.0"));
		// ID
		Integer id = Caster.toInteger(message.get(KeyConstants._id, null), null);
		if (id != null) data.set(KeyConstants._id, id);
		else if (addRandomIdIfMissing) data.set(KeyConstants._id, id = requestId.incrementAndGet());

		// all other entries
		Iterator<Entry<Key, Object>> it = message.entryIterator();
		Entry<Key, Object> entry;
		while (it.hasNext()) {
			entry = it.next();
			// we already added this key in proper case to the data struct, so no longer needed
			if (entry.getKey().equals(JSONRPC) || entry.getKey().equals(KeyConstants._id)) {
				continue;
			}
			data.set(entry.getKey(), entry.getValue());
		}

		boolean releasePC = false;
		PageContext pc = ThreadLocalPageContext.get();
		try {
			// if there is no pc for the thread (unlikely), create one
			if (pc == null) {
				pc = LSPUtil.createPageContext(true);
				releasePC = true;
			}

			String formattedMessage = LSPUtil.formatLSPMessage(SerializeJSON.call(pc, data));
			if (LogUtil.doesDebug(log)) {
				log.debug("lsp", formattedMessage);

			}

			byte[] formattedMessageBytes = formattedMessage.getBytes();
			for (OutputStream out: clientOutputStreams.values()) {
				try {
					out.write(formattedMessageBytes);
					out.flush();
					log.info("lsp", "Sent server-initiated message: " + message);
				}
				catch (IOException e) {
					log.error("lsp", "Failed to send message to client", e);
					throw e;
				}
			}
		}
		finally {
			if (releasePC) LSPUtil.releasePageContext(pc, null);
		}
	}

	private void error(String type, Exception e) {
		// System.err.println(type);
		// e.printStackTrace();
		log.error(type, e);
	}
}