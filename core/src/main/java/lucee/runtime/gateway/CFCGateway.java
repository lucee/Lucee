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

import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class CFCGateway implements GatewaySupport {

	// private static final Object OBJ = new Object();
	// private Component _cfc;
	private String id;
	private int state = Gateway.STOPPED;
	private String cfcPath;
	// private Config config;
	// private String requestURI;
	// private Resource cfcDirectory;
	private GatewayEngineImpl engine;
	private Thread thread;

	public CFCGateway(String cfcPath) {
		this.cfcPath = cfcPath;
	}

	@Override
	public void init(GatewayEngine engine, String id, String cfcPath, Map config) throws GatewayException {
		this.engine = (GatewayEngineImpl) engine;
		this.id = id;

		// requestURI=engine.toRequestURI(cfcPath);
		Struct args = new StructImpl(Struct.TYPE_LINKED);
		args.setEL(KeyConstants._id, id);
		args.setEL(KeyConstants._config, Caster.toStruct(config, null, false));
		if (!StringUtil.isEmpty(cfcPath)) {
			try {
				args.setEL(KeyConstants._listener, this.engine.getComponent(cfcPath, id));
			}
			catch (PageException e) {
				engine.log(this, GatewayEngine.LOGLEVEL_ERROR, e.getMessage());
			}
		}

		try {
			callOneWay("init", args);
		}
		catch (PageException pe) {

			engine.log(this, GatewayEngine.LOGLEVEL_ERROR, pe.getMessage());
			// throw new PageGatewayException(pe);
		}

	}

	@Override
	public void doRestart() throws GatewayException {

		engine.log(this, GatewayEngine.LOGLEVEL_INFO, "restart");
		Struct args = new StructImpl();
		try {
			boolean has = callOneWay("restart", args);
			if (!has) {
				if (callOneWay("stop", args)) {
					// engine.clear(cfcPath,id);
					callOneWay("start", args);
				}
			}
		}
		catch (PageException pe) {
			throw new PageGatewayException(pe);
		}

	}

	@Override
	public void doStart() throws GatewayException {
		engine.log(this, GatewayEngine.LOGLEVEL_INFO, "start");
		Struct args = new StructImpl();
		state = STARTING;
		try {
			callOneWay("start", args);
			engine.log(this, GatewayEngine.LOGLEVEL_INFO, "running");
			state = RUNNING;
		}
		catch (PageException pe) {
			state = FAILED;
			throw new PageGatewayException(pe);
		}
	}

	@Override
	public void doStop() throws GatewayException {

		engine.log(this, GatewayEngine.LOGLEVEL_INFO, "stop");
		Struct args = new StructImpl();
		state = STOPPING;
		try {
			callOneWay("stop", args);
			// engine.clear(cfcPath,id);
			state = STOPPED;
		}
		catch (PageException pe) {
			state = FAILED;
			// engine.clear(cfcPath,id);
			throw new PageGatewayException(pe);
		}
	}

	@Override
	public Object getHelper() {
		Struct args = new StructImpl(Struct.TYPE_LINKED);
		return callEL("getHelper", args, null);
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public int getState() {
		Struct args = new StructImpl();
		Integer state = Integer.valueOf(this.state);
		try {
			return GatewayEngineImpl.toIntState(Caster.toString(call("getState", args, state)), this.state);
		}
		catch (PageException pe) {
			engine.log(this, GatewayEngine.LOGLEVEL_ERROR, pe.getMessage());
		}
		return this.state;
	}

	@Override
	public String sendMessage(Map data) throws GatewayException {
		Struct args = new StructImpl(Struct.TYPE_LINKED);
		args.setEL("data", Caster.toStruct(data, null, false));
		try {
			return Caster.toString(call("sendMessage", args, ""));
		}
		catch (PageException pe) {
			throw new PageGatewayException(pe);
		}
	}

	private Object callEL(String methodName, Struct arguments, Object defaultValue) {
		return engine.callEL(cfcPath, id, methodName, arguments, true, defaultValue);
	}

	private boolean callOneWay(String methodName, Struct arguments) throws PageException {
		return engine.callOneWay(cfcPath, id, methodName, arguments, true);
	}

	private Object call(String methodName, Struct arguments, Object defaultValue) throws PageException {
		return engine.call(cfcPath, id, methodName, arguments, true, defaultValue);
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