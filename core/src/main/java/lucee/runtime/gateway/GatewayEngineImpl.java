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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.osgi.framework.BundleException;

import lucee.commons.io.DevNullOutputStream;
import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.Md5;
import lucee.commons.lang.Pair;
import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.CFMLFactory;
import lucee.runtime.Component;
import lucee.runtime.ComponentPageImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.Constants;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class GatewayEngineImpl implements GatewayEngine {

	private static final Object OBJ = new Object();

	private static final Collection.Key AMF_FORWARD = KeyImpl.getInstance("AMF-Forward");

	private Map<String, GatewayEntry> entries = new HashMap<String, GatewayEntry>();
	private ConfigWeb config;
	private Log log;

	public GatewayEngineImpl(ConfigWeb config) {
		this.config = config;
		this.log = config.getLog("gateway");

	}

	public void addEntries(Config config, Map<String, GatewayEntry> entries) throws ClassException, PageException, IOException, BundleException {
		Iterator<Entry<String, GatewayEntry>> it = entries.entrySet().iterator();
		while (it.hasNext()) {
			addEntry(config, it.next().getValue());
		}
	}

	public void addEntry(Config config, GatewayEntry ge) throws ClassException, PageException, IOException, BundleException {
		String id = ge.getId().toLowerCase().trim();
		GatewayEntry existing = entries.get(id);
		Gateway g = null;

		// does not exist
		if (existing == null) {
			entries.put(id, load(config, ge));
		}
		// exist but changed
		else if (!existing.equals(ge)) {
			g = existing.getGateway();
			if (g.getState() == Gateway.RUNNING) g.doStop();
			entries.put(id, load(config, ge));
		}
		// not changed
		// else print.out("untouched:"+id);
	}

	private GatewayEntry load(Config config, GatewayEntry ge) throws ClassException, PageException, BundleException {
		ge.createGateway(config);
		return ge;
	}

	/**
	 * @return the entries
	 */
	public Map<String, GatewayEntry> getEntries() {
		return entries;
	}

	public void remove(GatewayEntry ge) {
		String id = ge.getId().toLowerCase().trim();
		GatewayEntry existing = entries.remove(id);
		Gateway g = null;

		// does not exist
		if (existing != null) {
			g = existing.getGateway();
			try {
				if (g.getState() == Gateway.RUNNING) g.doStop();
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}
	}

	/**
	 * get the state of gateway
	 * 
	 * @param gatewayId
	 * @return
	 * @throws PageException
	 */
	public int getState(String gatewayId) throws PageException {
		return getGateway(gatewayId).getState();
	}

	/**
	 * get helper object
	 * 
	 * @param gatewayId
	 * @return
	 * @throws PageException
	 */
	public Object getHelper(String gatewayId) throws PageException {
		return getGateway(gatewayId).getHelper();
	}

	/**
	 * send the message to the gateway
	 * 
	 * @param gatewayId
	 * @param data
	 * @return
	 * @throws PageException
	 */
	public String sendMessage(String gatewayId, Struct data) throws PageException, IOException {
		Gateway g = getGateway(gatewayId);
		if (g.getState() != Gateway.RUNNING) throw new GatewayException("Gateway [" + gatewayId + "] is not running");
		return g.sendMessage(data);
	}

	/**
	 * start the gateway
	 * 
	 * @param gatewayId
	 * @throws PageException
	 */
	public void start(String gatewayId) throws PageException {
		executeThread(gatewayId, GatewayThread.START);
	}

	private void start(Gateway gateway) {
		executeThread(gateway, GatewayThread.START);
	}

	public void autoStart() {
		Gateway g;
		for (GatewayEntry ge: entries.values()) {
			if (ge.getStartupMode() != GatewayEntry.STARTUP_MODE_AUTOMATIC) continue;
			g = ge.getGateway();
			if (g.getState() != Gateway.RUNNING && g.getState() != Gateway.STARTING) {
				start(g);
			}
		}
	}

	/**
	 * stop the gateway
	 * 
	 * @param gatewayId
	 * @throws PageException
	 */
	public void stop(String gatewayId) throws PageException {
		executeThread(gatewayId, GatewayThread.STOP);
	}

	private void stop(Gateway gateway) {
		executeThread(gateway, GatewayThread.STOP);
	}

	/**
	 * stop all entries
	 */
	public void stopAll() {
		Iterator<GatewayEntry> it = getEntries().values().iterator();
		Gateway g;
		while (it.hasNext()) {
			g = it.next().getGateway();
			if (g != null) stop(g);
		}
	}

	public void reset() {
		Iterator<Entry<String, GatewayEntry>> it = entries.entrySet().iterator();
		Entry<String, GatewayEntry> entry;
		GatewayEntry ge;
		Gateway g;
		while (it.hasNext()) {
			entry = it.next();
			ge = entry.getValue();
			g = ge.getGateway();
			if (g.getState() == Gateway.RUNNING) {
				try {
					g.doStop();
					if (g instanceof GatewaySupport) {
						Thread t = ((GatewaySupport) g).getThread();
						t.interrupt();
						SystemUtil.stop(t);
					}
				}
				catch (IOException e) {
					log(g.getId(), LOGLEVEL_ERROR, e.getMessage(), e);
				}
			}
			// if (ge.getStartupMode() == GatewayEntry.STARTUP_MODE_AUTOMATIC) start(g);

		}
	}

	public void clear() {
		synchronized (entries) {
			Iterator<Entry<String, GatewayEntry>> it = entries.entrySet().iterator();
			Entry<String, GatewayEntry> entry;
			while (it.hasNext()) {
				entry = it.next();
				if (entry.getValue().getGateway().getState() == Gateway.RUNNING) stop(entry.getValue().getGateway());
			}
			entries.clear();
		}
	}

	/**
	 * restart the gateway
	 * 
	 * @param gatewayId
	 * @throws PageException
	 */
	public void restart(String gatewayId) throws PageException {
		executeThread(gatewayId, GatewayThread.RESTART);
	}

	private Gateway getGateway(String gatewayId) throws PageException {
		return getGatewayEntry(gatewayId).getGateway();
	}

	private GatewayEntry getGatewayEntry(String gatewayId) throws PageException {
		String id = gatewayId.toLowerCase().trim();
		GatewayEntry ge = entries.get(id);
		if (ge != null) return ge;

		// create list
		Iterator<String> it = entries.keySet().iterator();
		StringBuilder sb = new StringBuilder();
		while (it.hasNext()) {
			if (sb.length() > 0) sb.append(", ");
			sb.append(it.next());
		}

		throw new ExpressionException("there is no gateway instance with id [" + gatewayId + "], available gateway instances are [" + sb + "]");
	}

	private GatewayEntry getGatewayEntry(Gateway gateway) {
		String gatewayId = gateway.getId();
		// it must exist, because it only can come from here
		return entries.get(gatewayId);
	}

	private void executeThread(String gatewayId, int action) throws PageException {
		new GatewayThread(this, getGateway(gatewayId), action).start();
	}

	private void executeThread(Gateway g, int action) {
		new GatewayThread(this, g, action).start();
	}

	public static int toIntState(String state, int defaultValue) {
		state = state.trim().toLowerCase();
		if ("running".equals(state)) return Gateway.RUNNING;
		if ("started".equals(state)) return Gateway.RUNNING;
		if ("run".equals(state)) return Gateway.RUNNING;

		if ("failed".equals(state)) return Gateway.FAILED;
		if ("starting".equals(state)) return Gateway.STARTING;
		if ("stopped".equals(state)) return Gateway.STOPPED;
		if ("stopping".equals(state)) return Gateway.STOPPING;

		return defaultValue;
	}

	public static String toStringState(int state, String defaultValue) {
		if (Gateway.RUNNING == state) return "running";
		if (Gateway.FAILED == state) return "failed";
		if (Gateway.STOPPED == state) return "stopped";
		if (Gateway.STOPPING == state) return "stopping";
		if (Gateway.STARTING == state) return "starting";

		return defaultValue;
	}

	@Override
	public boolean invokeListener(Gateway gateway, String method, Map data) {// FUTUTE add generic type to interface
		return invokeListener(gateway.getId(), method, data);
	}

	public boolean invokeListener(String gatewayId, String method, Map data) {// do not add this method to loade, it can be removed with Lucee 5
		data = GatewayUtil.toCFML(data);

		GatewayEntry entry;
		try {
			entry = getGatewayEntry(gatewayId);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
		String cfcPath = entry.getListenerCfcPath();
		if (!StringUtil.isEmpty(cfcPath, true)) {
			try {
				if (!callOneWay(cfcPath, gatewayId, method, Caster.toStruct(data, null, false), false))
					log(gatewayId, LOGLEVEL_ERROR, "function [" + method + "] does not exist in cfc [" + cfcPath + "]");
				else return true;
			}
			catch (PageException e) {
				log(gatewayId, LOGLEVEL_ERROR, e.getMessage(), e);
			}
		}
		else log(gatewayId, LOGLEVEL_ERROR, "there is no listener cfc defined");
		return false;
	}

	public Object callEL(String cfcPath, String id, String functionName, Struct arguments, boolean cfcPeristent, Object defaultValue) {
		try {
			return call(cfcPath, id, functionName, arguments, cfcPeristent, defaultValue);
		}
		catch (PageException e) {
			return defaultValue;
		}
	}

	public boolean callOneWay(String cfcPath, String id, String functionName, Struct arguments, boolean cfcPeristent) throws PageException {
		return call(cfcPath, id, functionName, arguments, cfcPeristent, OBJ) != OBJ;
	}

	public Object getComponent(String cfcPath, String id) throws PageException {
		String requestURI = toRequestURI(cfcPath);

		PageContext oldPC = ThreadLocalPageContext.get();
		PageContextImpl pc = null;
		try {
			pc = createPageContext(requestURI, id, "init", null, false, true);
			// ThreadLocalPageContext.register(pc);
			return getCFC(pc, requestURI);
		}
		finally {
			CFMLFactory f = config.getFactory();
			f.releaseLuceePageContext(pc, true);
			ThreadLocalPageContext.register(oldPC);
		}
	}

	public Object call(String cfcPath, String id, String functionName, Struct arguments, boolean cfcPeristent, Object defaultValue) throws PageException {
		String requestURI = toRequestURI(cfcPath);

		PageContext oldPC = ThreadLocalPageContext.get();

		PageContextImpl pc = null;

		try {
			pc = createPageContext(requestURI, id, functionName, arguments, cfcPeristent, true);
			String ext = ResourceUtil.getExtension(cfcPath, null);
			ConfigWeb config = (ConfigWeb) ThreadLocalPageContext.getConfig();
			int dialect = ext == null ? CFMLEngine.DIALECT_CFML : config.getFactory().toDialect(ext);
			// ThreadLocalPageContext.register(pc);
			Component cfc = getCFC(pc, requestURI);
			if (cfc.containsKey(functionName)) {
				if (dialect == CFMLEngine.DIALECT_LUCEE) pc.execute(requestURI, true, false);
				else pc.executeCFML(requestURI, true, false);

				// Result
				return pc.variablesScope().get(AMF_FORWARD, null);
			}
		}
		finally {
			CFMLFactory f = config.getFactory();
			f.releaseLuceePageContext(pc, true);
			ThreadLocalPageContext.register(oldPC);
		}
		return defaultValue;
	}

	private Component getCFC(PageContextImpl pc, String requestURI) throws PageException {
		HttpServletRequest req = pc.getHttpServletRequest();
		try {
			String ext = ResourceUtil.getExtension(requestURI, "");
			ConfigWeb config = (ConfigWeb) ThreadLocalPageContext.getConfig(pc);
			int dialect = config.getFactory().toDialect(ext);

			req.setAttribute("client", "lucee-gateway-1-0");
			req.setAttribute("call-type", "store-only");
			if (dialect == CFMLEngine.DIALECT_LUCEE) pc.execute(requestURI, true, false);
			else pc.executeCFML(requestURI, true, false);
			return (Component) req.getAttribute("component");
		}
		finally {
			req.removeAttribute("call-type");
			req.removeAttribute("component");
		}
	}

	private PageContextImpl createPageContext(String requestURI, String id, String functionName, Struct arguments, boolean cfcPeristent, boolean register) throws PageException {
		Struct attrs = new StructImpl();
		String remotePersisId;
		try {
			remotePersisId = Md5.getDigestAsString(requestURI + id);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
		PageContextImpl pc = ThreadUtil.createPageContext(config, DevNullOutputStream.DEV_NULL_OUTPUT_STREAM, "localhost", requestURI,
				"method=" + functionName + (cfcPeristent ? "&" + ComponentPageImpl.REMOTE_PERSISTENT_ID + "=" + remotePersisId : ""), null,
				new Pair[] { new Pair<String, Object>("AMF-Forward", "true") }, null, null, attrs, register, Long.MAX_VALUE);

		pc.setGatewayContext(true);
		if (arguments != null) attrs.setEL(KeyConstants._argumentCollection, arguments);
		attrs.setEL("client", "lucee-gateway-1-0");
		return pc;
	}

	private String toRequestURI(String cfcPath) {
		// MUSTMUST support also Lucee extension!
		String requestURI = cfcPath.replace('.', '/');
		if (!requestURI.startsWith("/")) requestURI = "/" + requestURI + "." + Constants.GATEWAY_COMPONENT_EXTENSION;
		return requestURI;
	}

	@Override
	public void log(Gateway gateway, int level, String message) {
		log(gateway.getId(), level, message);
	}

	public void log(String gatewayId, int level, String message) {
		log(gatewayId, level, message, null);
	}

	public void log(String gatewayId, int level, String message, Exception e) {
		int l = level;
		switch (level) {
		case LOGLEVEL_INFO:
			l = Log.LEVEL_INFO;
			break;
		case LOGLEVEL_DEBUG:
			l = Log.LEVEL_DEBUG;
			break;
		case LOGLEVEL_ERROR:
			l = Log.LEVEL_ERROR;
			break;
		case LOGLEVEL_FATAL:
			l = Log.LEVEL_FATAL;
			break;
		case LOGLEVEL_WARN:
			l = Log.LEVEL_WARN;
			break;
		case LOGLEVEL_TRACE:
			l = Log.LEVEL_TRACE;
			break;
		}
		if (e == null) log.log(l, "Gateway:" + gatewayId, message);
		else log.log(l, "Gateway:" + gatewayId, message, e);
	}

	private Map<String, Component> persistentRemoteCFC;

	public Component getPersistentRemoteCFC(String id) {
		if (persistentRemoteCFC == null) persistentRemoteCFC = new HashMap<String, Component>();
		return persistentRemoteCFC.get(id);
	}

	public Component setPersistentRemoteCFC(String id, Component cfc) {
		if (persistentRemoteCFC == null) persistentRemoteCFC = new HashMap<String, Component>();
		return persistentRemoteCFC.put(id, cfc);
	}
}