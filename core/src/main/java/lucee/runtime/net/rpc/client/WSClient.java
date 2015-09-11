/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.runtime.net.rpc.client;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;

import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.log4j.Logger;

public abstract class WSClient implements Objects, Iteratorable {
	
	public static WSClient getInstance(PageContext pc,String wsdlUrl, String username, String password, ProxyData proxyData) throws PageException {
		pc=ThreadLocalPageContext.get(pc);
		if(pc!=null) {
			Logger l = ((ConfigImpl)pc.getConfig()).getLogger("application", true);
			ApplicationContext ac = pc.getApplicationContext();
			if(ac!=null) {
				if(ApplicationContext.WS_TYPE_JAX_WS==ac.getWSType()) {
					l.info("using JAX WS Client");
					return new JaxWSClient(wsdlUrl, username, password, proxyData);
				}
				if(ApplicationContext.WS_TYPE_CXF==ac.getWSType()) {
					l.info("using CXF Client");
					return new CXFClient(wsdlUrl, username, password, proxyData);
				}
			}
			l.info("using Axis 1 RPC Client");
		}
		return new Axis1Client(wsdlUrl,username,password,proxyData);
	}
	
	
	
	public abstract void addHeader(SOAPHeaderElement header) throws PageException;
	public abstract Call getLastCall()throws PageException;
	public abstract Object callWithNamedValues(Config config, Collection.Key methodName, Struct arguments) throws PageException;
	@Override
	public abstract Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct arguments) throws PageException;
	    
}