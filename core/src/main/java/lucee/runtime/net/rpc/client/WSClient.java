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

import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;

public interface WSClient extends Objects, Iteratorable {

	public void addHeader(Object header) throws PageException; // Object instead of header because Java 11 no longer support javax.xml.soap.SOAPHeaderElement

	public Object callWithNamedValues(Config config, Collection.Key methodName, Struct arguments) throws PageException;

	public void addSOAPRequestHeader(String namespace, String name, Object value, boolean mustUnderstand) throws PageException;

	public Node getSOAPRequest() throws PageException;

	public Node getSOAPResponse() throws PageException;

	public Object getSOAPResponseHeader(PageContext pc, String namespace, String name, boolean asXML) throws PageException;

	public WSHandler getWSHandler();

}