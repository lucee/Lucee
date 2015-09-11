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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.wsdl.Message;
import javax.wsdl.Port;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.xml.namespace.QName;

import lucee.runtime.net.rpc.RPCException;

public class WSUtil {
	public static Port getSoapPort(javax.wsdl.Service service) throws RPCException {
		String name = null;
		Port port = null;
		List list = null;
		Map ports = service.getPorts();
		Iterator it;
		Iterator<Port> itr = ports.values().iterator();
		Object v;
		while(itr.hasNext()) {
			port = itr.next();
			
			list=port.getExtensibilityElements();
			if(list != null) {
				it = list.iterator();
				while(it.hasNext()) {
					v=it.next();
					if(v instanceof SOAPAddress) {
						return port;
					}
				}

			}
		}
		throw new RPCException("Can't locate port entry for service " + service.getQName().toString() + " WSDL");
	}

	public static Message getMessageByLocalName(Map<QName, Message> messages, String name) {
		Iterator<Entry<QName,Message>> it = messages.entrySet().iterator();
		Entry<QName,Message> e;
		while(it.hasNext()){
        	e = it.next();
        	//print.e(e.getKey().getLocalPart()+":"+name);
        	if(e.getKey().getLocalPart().equals(name)) return e.getValue();
        }
        return null;
	}
}