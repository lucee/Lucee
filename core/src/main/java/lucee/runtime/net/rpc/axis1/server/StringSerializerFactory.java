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
package lucee.runtime.net.rpc.axis1.server;

import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import javax.xml.rpc.encoding.Serializer;

import org.apache.axis.encoding.ser.SimpleSerializerFactory;

public class StringSerializerFactory extends SimpleSerializerFactory {
	public StringSerializerFactory(Class javaType, QName xmlType) {
		super(javaType, xmlType);
	}

	@Override
	public Serializer getSerializerAs(String mechanismType) throws JAXRPCException {
        if (javaType == String.class) {
            return new StringSerializer(javaType, xmlType);
        }

        return super.getSerializerAs(mechanismType);
    }
}