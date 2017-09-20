package lucee.runtime.net.rpc.axis1;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.rpc.encoding.TypeMapping;

import lucee.commons.io.log.Log;
import lucee.commons.lang.Pair;
import lucee.runtime.Component;
import lucee.runtime.ComponentScope;
import lucee.runtime.PageContext;
import lucee.runtime.component.Property;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.listener.ApplicationContext;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.Pojo;
import lucee.runtime.net.rpc.PojoIterator;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.net.rpc.axis1.client.Axis1Client;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.net.rpc.cxf.client.CXFClient;
import lucee.runtime.net.rpc.jaxws.client.JaxWSClient;
import lucee.runtime.net.rpc.server.WSServer;
import lucee.runtime.op.Caster;
import lucee.runtime.text.xml.XMLCaster;
import lucee.runtime.text.xml.XMLUtil;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.net.rpc.axis1.server.Axis1Server;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.wsdl.symbolTable.ElementDecl;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

public class Axis1Handler extends WSHandler {

	@Override
	public boolean isSOAPRequest() {
		MessageContext context = MessageContext.getCurrentContext();
		return context != null && !context.isClient();
	}

	@Override
	public void addSOAPRequestHeader(WSClient client, String namespace,
			String name, Object value, boolean mustUnderstand)
			throws PageException {
		SOAPHeaderElement header = toSOAPHeaderElement(namespace, name, value);
		header.setMustUnderstand(mustUnderstand);
		client.addHeader(header);
	}

	public void addSOAPResponseHeader(String namespace, String name,
			Object value, boolean mustUnderstand) throws PageException {
		try {
			MessageContext context = MessageContext.getCurrentContext();
			if (context == null || context.isClient())
				throw new AxisFault("not inside a Soap Request");

			SOAPEnvelope env = context.getResponseMessage().getSOAPEnvelope();
			SOAPHeaderElement header = toSOAPHeaderElement(namespace, name,
					value);
			header.setMustUnderstand(mustUnderstand);
			env.addHeader(header);
		} catch (AxisFault af) {
			throw Caster.toPageException(af);
		}
	}

	public Node getSOAPRequest(WSClient client) throws PageException {
		try {
			MessageContext context = getMessageContext(client);
			SOAPEnvelope env = context.getRequestMessage().getSOAPEnvelope();
			return XMLCaster.toXMLStruct(env.getAsDocument(), true);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public Node getSOAPResponse(WSClient client) throws PageException {
		try {
			Call call = (Call) client.getLastCall();
			if (call == null)
				throw new AxisFault("web service was not invoked yet");
			SOAPEnvelope env = call.getResponseMessage().getSOAPEnvelope();
			return XMLCaster.toXMLStruct(env.getAsDocument(), true);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public Object getSOAPRequestHeader(PageContext pc, String namespace,
			String name, boolean asXML) throws PageException {
		try {
			MessageContext context = MessageContext.getCurrentContext();
			if (context == null || context.isClient())
				throw new AxisFault("not inside a Soap Request");

			SOAPEnvelope env = context.getRequestMessage().getSOAPEnvelope();
			SOAPHeaderElement header = env.getHeaderByName(namespace, name);
			return toValue(header, asXML);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public Object getSOAPResponseHeader(PageContext pc, WSClient client,
			String namespace, String name, boolean asXML) throws PageException {
		try {
			MessageContext context = getMessageContext(client);
			SOAPEnvelope env = context.getResponseMessage().getSOAPEnvelope();
			SOAPHeaderElement header = env.getHeaderByName(namespace, name);
			return toValue(header, asXML);
		} catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	public TypeEntry getContainedElement(TypeEntry type, String name,
			TypeEntry defaultValue) {
		if (type == null)
			return defaultValue;
		Vector v = type.getContainedElements();
		if (v != null) {
			Iterator it = v.iterator();
			ElementDecl ed;
			String tmp;
			while (it.hasNext()) {
				ed = (ElementDecl) it.next();
				if (ed.getQName() == null)
					continue;
				tmp = lucee.runtime.type.util.ListUtil.last(ed.getQName()
						.getLocalPart(), '>');

				if (tmp.equalsIgnoreCase(name))
					return ed.getType();
			}
		}
		return defaultValue;
	}

	private static Object toValue(SOAPHeaderElement header, boolean asXML)
			throws Exception {
		if (header == null)
			return "";
		if (asXML) {
			String strXML = header.toString();
			InputSource is = new InputSource(new StringReader(strXML.trim()));
			return XMLCaster.toXMLStruct(XMLUtil.parse(is, null, false), true);
		}

		Object value = header.getObjectValue();
		if (value == null) {
			value = header.getObjectValue(String.class);
		}
		return value;
	}

	private static MessageContext getMessageContext(WSClient client)
			throws AxisFault, PageException {
		if (client != null) {
			Call call = (Call) client.getLastCall();
			if (call == null)
				throw new AxisFault("web service was not invoked yet");
			return call.getMessageContext();
		}
		MessageContext context = MessageContext.getCurrentContext();
		if (context == null)
			throw new AxisFault("not inside a Soap Request");
		return context;
	}

	private static SOAPHeaderElement toSOAPHeaderElement(String namespace,
			String name, Object value) {
		Element el = XMLCaster.toRawElement(value, null);
		if (el != null)
			return new SOAPHeaderElement(el);
		return new SOAPHeaderElement(namespace, name, value);
	}

	@Override
	public String getTypeAsString() {
		return "Axis1";
	}

	@Override
	public Component toComponent(PageContext pc, Pojo pojo, String compPath,
			Component defaultValue) {
		return AxisCaster.toComponent(pc, pojo, compPath, defaultValue);
	}

	@Override
	public Class<?> toWSTypeClass(Class<?> clazz) {
		return AxisCaster.toAxisTypeClass(clazz);
	}

	@Override
	public Pojo toPojo(Pojo pojo, Component comp, Set<Object> done)
			throws PageException {
		return AxisCaster.toPojo(pojo, null, null, null, comp, done);
	}

	@Override
	public Pojo toPojo(Pojo pojo, Struct sct, Set<Object> done)
			throws PageException {
		return AxisCaster.toPojo(pojo, null, null, null, sct, done);
	}

	@Override
	public WSServer getWSServer(PageContext pc) throws PageException {
		try {
			return Axis1Server.getInstance(pc);
		}
		catch (AxisFault af) {
			throw Caster.toPageException(af);
		}
	}

	@Override
	public WSClient getWSClient(String wsdlUrl, String username, String password, ProxyData proxyData) throws PageException {
		/*pc=ThreadLocalPageContext.get(pc);
		if(pc!=null) {
			Log l = pc.getConfig().getLog("application");
			ApplicationContext ac = pc.getApplicationContext();
			if(ac!=null) {
				if(ApplicationContext.WS_TYPE_JAX_WS==ac.getWSType()) {
					l.info("RPC","using JAX WS Client");
					return new JaxWSClient(wsdlUrl, username, password, proxyData);
				}
				if(ApplicationContext.WS_TYPE_CXF==ac.getWSType()) {
					l.info("RPC","using CXF Client");
					return new CXFClient(wsdlUrl, username, password, proxyData);
				}
			}
			l.info("RPC","using Axis 1 RPC Client");
		}*/
		return new lucee.runtime.net.rpc.axis1.client.Axis1Client(wsdlUrl,username,password,proxyData);
	}
}
