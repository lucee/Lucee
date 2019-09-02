/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package lucee.runtime.gateway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.util.Cast;
import lucee.runtime.util.Creation;

public class SocketGateway implements GatewaySupport {

	private GatewayEngine engine;
	private int port;
	private String welcomeMessage = "Welcome to the Lucee Socket Gateway";

	private String id;
	private CFMLEngine cfmlEngine;
	private Cast caster;
	private Creation creator;
	private List<SocketServerThread> sockets = new ArrayList<SocketServerThread>();
	private ServerSocket serverSocket;
	protected int state = STOPPED;
	private String cfcPath;
	private Thread thread;

	@Override
	public void init(GatewayEngine engine, String id, String cfcPath, Map config) throws GatewayException {
		this.engine = engine;
		cfmlEngine = CFMLEngineFactory.getInstance();
		caster = cfmlEngine.getCastUtil();
		creator = cfmlEngine.getCreationUtil();
		this.cfcPath = cfcPath;
		this.id = id;

		// config
		Object oPort = config.get("port");
		port = caster.toIntValue(oPort, 1225);

		Object oWM = config.get("welcomeMessage");
		String strWM = caster.toString(oWM, "").trim();
		if (strWM.length() > 0) welcomeMessage = strWM;
	}

	@Override
	public void doStart() {
		state = STARTING;
		try {
			createServerSocket();
			state = RUNNING;
			do {
				try {
					SocketServerThread sst = new SocketServerThread(serverSocket.accept());
					sst.start();
					sockets.add(sst);
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					error("Failed to listen on Socket [" + id + "] on port [" + port + "]: " + t.getMessage());
				}
			}
			while (getState() == RUNNING || getState() == STARTING);

			close(serverSocket);
			serverSocket = null;
		}
		catch (Exception e) {
			state = FAILED;
			error("Error in Socet Gateway [" + id + "]: " + e.getMessage());
			LogUtil.log(ThreadLocalPageContext.getConfig(), SocketGateway.class.getName(), e);
		}
	}

	@Override
	public void doStop() {
		state = STOPPING;
		try {

			// close all open connections
			Iterator<SocketServerThread> it = sockets.iterator();
			while (it.hasNext()) {
				close(it.next().socket);
			}

			// close server socket
			close(serverSocket);
			serverSocket = null;
			state = STOPPED;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			state = FAILED;
			error("Error in Socket Gateway [" + id + "]: " + t.getMessage());
			// throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(e);
		}
	}

	private void createServerSocket() throws PageException, RuntimeException {
		try {
			serverSocket = new ServerSocket(port);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			error("Failed to start Socket Gateway [" + id + "] on port [" + port + "] " + t.getMessage());
			throw CFMLEngineFactory.getInstance().getCastUtil().toPageException(t);
		}
	}

	private void invokeListener(String line, String originatorID) {

		Struct data = creator.createStruct();
		data.setEL(creator.createKey("message"), line);
		Struct event = creator.createStruct();
		event.setEL(creator.createKey("data"), data);
		event.setEL(creator.createKey("originatorID"), originatorID);

		event.setEL(creator.createKey("cfcMethod"), "onIncomingMessage");
		event.setEL(creator.createKey("cfcTimeout"), new Double(10));
		event.setEL(creator.createKey("cfcPath"), cfcPath);

		event.setEL(creator.createKey("gatewayType"), "Socket");
		event.setEL(creator.createKey("gatewayId"), id);

		if (engine.invokeListener(this, "onIncomingMessage", event)) info("Socket Gateway Listener [" + id + "] invoked.");
		else error("Failed to call Socket Gateway Listener [" + id + "]");
	}

	private class SocketServerThread extends Thread {
		private Socket socket;
		private PrintWriter out;
		private String _id;

		public SocketServerThread(Socket socket) throws IOException {
			this.socket = socket;
			out = new PrintWriter(socket.getOutputStream(), true);
			this._id = String.valueOf(hashCode());
		}

		@Override
		public void run() {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				out.println(welcomeMessage);
				out.print("> ");
				String line;
				while ((line = in.readLine()) != null) {
					if (line.trim().equals("exit")) break;
					invokeListener(line, _id);
				}
				// socketRegistry.remove(this.getName());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				error("Failed to read from Socket Gateway [" + id + "]: " + t.getMessage());
			}
			finally {
				close(out);
				out = null;
				close(in);
				close(socket);
				sockets.remove(this);
			}
		}

		public void writeOutput(String str) {
			out.println(str);
			out.print("> ");
		}
	}

	@Override
	public String sendMessage(Map _data) {
		Struct data = caster.toStruct(_data, null, false);
		String msg = (String) data.get("message", null);
		String originatorID = (String) data.get("originatorID", null);

		String status = "OK";
		if (msg != null) {

			Iterator<SocketServerThread> it = sockets.iterator();
			SocketServerThread sst;
			try {
				boolean hasSend = false;
				while (it.hasNext()) {
					sst = it.next();
					if (originatorID != null && !sst._id.equalsIgnoreCase(originatorID)) continue;
					sst.writeOutput(msg);
					hasSend = true;
				}

				if (!hasSend) {
					if (sockets.size() == 0) {
						error("There is no connection");
						status = "EXCEPTION";
					}
					else {
						it = sockets.iterator();
						StringBuilder sb = new StringBuilder();
						while (it.hasNext()) {
							if (sb.length() > 0) sb.append(", ");
							sb.append(it.next()._id);
						}
						error("There is no connection with originatorID [" + originatorID + "], available originatorIDs are [" + sb + "]");
						status = "EXCEPTION";
					}
				}
			}
			catch (Exception e) {
				LogUtil.log(ThreadLocalPageContext.getConfig(), SocketGateway.class.getName(), e);
				error("Failed to send message with exception: " + e.toString());
				status = "EXCEPTION";
			}
		}
		return status;
	}

	@Override
	public void doRestart() {
		doStop();
		doStart();
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getState() {
		return state;
	}

	@Override
	public Object getHelper() {
		return null;
	}

	public void info(String msg) {
		engine.log(this, GatewayEngine.LOGLEVEL_INFO, msg);
	}

	public void error(String msg) {
		engine.log(this, GatewayEngine.LOGLEVEL_ERROR, msg);
	}

	private void close(Writer writer) {
		if (writer == null) return;
		try {
			writer.close();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void close(Reader reader) {
		if (reader == null) return;
		try {
			reader.close();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void close(Socket socket) {
		if (socket == null) return;
		try {
			socket.close();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	private void close(ServerSocket socket) {
		if (socket == null) return;
		try {
			socket.close();
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	@Override
	public void setThread(Thread thread) {
		this.thread = thread;
	}

	@Override
	public Thread getThread() {
		return thread;
	}
}