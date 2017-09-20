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
package lucee.runtime.net.rpc.axis1;

import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;
import javax.xml.rpc.encoding.TypeMappingRegistry;

import lucee.runtime.net.rpc.RPCConstants;
import lucee.runtime.net.rpc.axis1.server.StringDeserializerFactory;
import lucee.runtime.net.rpc.axis1.server.StringSerializerFactory;

import org.apache.axis.encoding.ser.ArrayDeserializerFactory;
import org.apache.axis.encoding.ser.ArraySerializerFactory;
import org.apache.axis.encoding.ser.BeanDeserializerFactory;
import org.apache.axis.encoding.ser.BeanSerializerFactory;
import org.apache.axis.server.AxisServer;

import coldfusion.xml.rpc.QueryBean;

public class TypeMappingUtil {

	public static void registerDefaults(TypeMappingRegistry tmr) { 
		TypeMapping tm = tmr.getDefaultTypeMapping();
		if(!tm.isRegistered(QueryBean.class, RPCConstants.QUERY_QNAME))
			tm.register(QueryBean.class, 
                RPCConstants.QUERY_QNAME,
                new BeanSerializerFactory(QueryBean.class,RPCConstants.QUERY_QNAME),
                new BeanDeserializerFactory(QueryBean.class,RPCConstants.QUERY_QNAME));
		
		//Adding custom string serialization for non printable characters.
		tm.register(String.class,
				RPCConstants.STRING_QNAME,
				new StringSerializerFactory(String.class, RPCConstants.STRING_QNAME),
				new StringDeserializerFactory(String.class, RPCConstants.STRING_QNAME));
		
		
	}
	
	public static void registerBeanTypeMapping(javax.xml.rpc.encoding.TypeMapping tm, Class clazz, QName qName) {
		if(tm.isRegistered(clazz, qName)) return;
		
		if(clazz.isArray()) {
			QName ct=AxisCaster.toComponentType(qName,null);
			if(ct!=null) {
				tm.register(
	    			clazz, 
	        		qName, 
	    			new ArraySerializerFactory(clazz, ct), 
	    			new ArrayDeserializerFactory(ct));
				return;
			}
		}
		
			tm.register(
    			clazz, 
        		qName, 
    			new BeanSerializerFactory(clazz, qName), 
    			new BeanDeserializerFactory(clazz, qName));
		
		
	}

	public static org.apache.axis.encoding.TypeMapping getServerTypeMapping(AxisServer axisServer) {
		org.apache.axis.encoding.TypeMappingRegistry reg = axisServer.getTypeMappingRegistry();
		return reg.getOrMakeTypeMapping("http://schemas.xmlsoap.org/soap/encoding/");
		
	}
	public static org.apache.axis.encoding.TypeMapping getServerTypeMapping(TypeMappingRegistry reg) {
		//org.apache.axis.encoding.TypeMappingRegistry reg = axisServer.getTypeMappingRegistry();
		return ((org.apache.axis.encoding.TypeMappingRegistry)reg).getOrMakeTypeMapping("http://schemas.xmlsoap.org/soap/encoding/");
		
	}
	
}