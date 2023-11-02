package lucee.runtime.net.rpc.ref;

import java.lang.reflect.Method;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.net.rpc.server.WSServer;
import lucee.runtime.op.Caster;

public class WSHandlerReflector implements WSHandler {

	public static final Class[] EMPTY_CLASS = new Class[0];
	public static final Object[] EMPTY_OBJECT = new Object[0];

	private Object obj;
	private Class<? extends Object> clazz;
	private Method isSOAPRequest;
	private Method addSOAPResponseHeader;
	private Method getSOAPRequestHeader;
	private Method getTypeAsString;
	private Method toWSTypeClass;
	private Method getWSClient;
	private Method getWSServer;

	public WSHandlerReflector(Object obj) {
		this.obj = obj;
		this.clazz = obj.getClass();
	}

	@Override
	public boolean isSOAPRequest() {
		try {
			if (isSOAPRequest == null) isSOAPRequest = clazz.getMethod("isSOAPRequest", EMPTY_CLASS);
			return Caster.toBooleanValue(isSOAPRequest.invoke(obj, EMPTY_OBJECT));
		}
		catch (Exception e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public void addSOAPResponseHeader(String namespace, String name, Object value, boolean mustUnderstand) throws PageException {
		try {
			if (addSOAPResponseHeader == null)
				addSOAPResponseHeader = clazz.getMethod("addSOAPResponseHeader", new Class[] { String.class, String.class, Object.class, boolean.class });
			addSOAPResponseHeader.invoke(obj, new Object[] { namespace, name, value, mustUnderstand });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object getSOAPRequestHeader(PageContext pc, String namespace, String name, boolean asXML) throws PageException {
		try {
			if (getSOAPRequestHeader == null)
				getSOAPRequestHeader = clazz.getMethod("getSOAPRequestHeader", new Class[] { PageContext.class, String.class, String.class, boolean.class });
			return getSOAPRequestHeader.invoke(obj, new Object[] { pc, namespace, name, asXML });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public String getTypeAsString() {
		try {
			if (getTypeAsString == null) getTypeAsString = clazz.getMethod("getTypeAsString", EMPTY_CLASS);
			return Caster.toString(getTypeAsString.invoke(obj, EMPTY_OBJECT));
		}
		catch (Exception e) {
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public Class<?> toWSTypeClass(Class<?> _clazz) {
		try {
			if (toWSTypeClass == null) toWSTypeClass = this.clazz.getMethod("toWSTypeClass", new Class[] { Class.class });
			return (Class<?>) toWSTypeClass.invoke(obj, new Object[] { _clazz });
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new PageRuntimeException(e);
		}
	}

	@Override
	public WSServer getWSServer(PageContext pc) throws PageException {
		try {
			if (getWSServer == null) getWSServer = clazz.getMethod("getWSServer", new Class[] { PageContext.class, });
			Object o = getWSServer.invoke(obj, new Object[] { pc });
			if (o instanceof WSServer) return (WSServer) o;
			return new WSServerReflector(o);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public WSClient getWSClient(String wsdlUrl, String username, String password, ProxyData proxyData) throws PageException {
		try {
			if (getWSClient == null) getWSClient = clazz.getMethod("getWSClient", new Class[] { String.class, String.class, String.class, ProxyData.class, });
			Object o = getWSClient.invoke(obj, new Object[] { wsdlUrl, username, password, proxyData });
			if (o instanceof WSClient) return (WSClient) o;
			return new WSClientReflector(o);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

}
