package lucee.runtime.net.rpc.ref;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map.Entry;

import org.w3c.dom.Node;

import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.rpc.WSHandler;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Iteratorable;
import lucee.runtime.type.Objects;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;

public class WSClientReflector implements WSClient {

	private final Object obj;
	private final Objects objects;
	private final Iteratorable it;
	private final Class<? extends Object> clazz;
	private Method addSOAPRequestHeader;
	private Method getSOAPRequest;
	private Method getSOAPResponse;
	private Method getSOAPResponseHeader;
	// private Method getLastCall;
	private Method callWithNamedValues;
	private Method addHeader;
	private Method getWSHandler;

	public WSClientReflector(Object obj) {
		this.obj = obj;
		this.objects = (Objects) obj;
		this.it = (Iteratorable) obj;
		this.clazz = obj.getClass();
	}

	@Override
	public Object call(PageContext arg0, Key arg1, Object[] arg2) throws PageException {
		return objects.call(arg0, arg1, arg2);
	}

	@Override
	public Object callWithNamedValues(PageContext arg0, Key arg1, Struct arg2) throws PageException {
		return objects.callWithNamedValues(arg0, arg1, arg2);
	}

	@Override
	public Object get(PageContext arg0, Key arg1) throws PageException {
		return objects.get(arg0, arg1);
	}

	@Override
	public Object get(PageContext arg0, Key arg1, Object arg2) {
		return objects.get(arg0, arg1, arg2);
	}

	@Override
	public Object set(PageContext arg0, Key arg1, Object arg2) throws PageException {
		return objects.set(arg0, arg1, arg2);
	}

	@Override
	public Object setEL(PageContext arg0, Key arg1, Object arg2) {
		return objects.setEL(arg0, arg1, arg2);
	}

	@Override
	public DumpData toDumpData(PageContext arg0, int arg1, DumpProperties arg2) {
		return objects.toDumpData(arg0, arg1, arg2);
	}

	@Override
	public Boolean castToBoolean(Boolean arg0) {
		return objects.castToBoolean(arg0);
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		return objects.castToBooleanValue();
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		return objects.castToDateTime();
	}

	@Override
	public DateTime castToDateTime(DateTime arg0) {
		return objects.castToDateTime(arg0);
	}

	@Override
	public double castToDoubleValue() throws PageException {
		return objects.castToDoubleValue();
	}

	@Override
	public double castToDoubleValue(double arg0) {
		return objects.castToDoubleValue(arg0);
	}

	@Override
	public String castToString() throws PageException {
		return objects.castToString();
	}

	@Override
	public String castToString(String arg0) {
		return objects.castToString(arg0);
	}

	@Override
	public int compareTo(String arg0) throws PageException {
		return objects.compareTo(arg0);
	}

	@Override
	public int compareTo(boolean arg0) throws PageException {
		return objects.compareTo(arg0);
	}

	@Override
	public int compareTo(double arg0) throws PageException {
		return objects.compareTo(arg0);
	}

	@Override
	public int compareTo(DateTime arg0) throws PageException {
		return objects.compareTo(arg0);
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return it.entryIterator();
	}

	@Override
	public Iterator<Key> keyIterator() {
		return it.keyIterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return it.keysAsStringIterator();
	}

	@Override
	public Iterator<Object> valueIterator() {
		return it.valueIterator();
	}

	@Override
	public void addHeader(Object header) throws PageException { // Object instead of header because Java 11 no longer support javax.xml.soap.SOAPHeaderElement
		try {
			if (addHeader == null) addHeader = clazz.getMethod("addHeader", new Class[] { Class.forName("javax.xml.soap.SOAPHeaderElement") });
			addHeader.invoke(obj, new Object[] { header });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	/*
	 * @Override public Call getLastCall() throws PageException { try { if(getLastCall==null)
	 * getLastCall=clazz.getMethod("getLastCall", WSHandlerReflector.EMPTY_CLASS); return (Call)
	 * getLastCall.invoke(obj,WSHandlerReflector.EMPTY_OBJECT); } catch(Exception e) { throw
	 * Caster.toPageException(e); } }
	 */

	@Override
	public Object callWithNamedValues(Config config, Key methodName, Struct arguments) throws PageException {
		try {
			if (callWithNamedValues == null) callWithNamedValues = clazz.getMethod("callWithNamedValues", new Class[] { Config.class, Key.class, Struct.class });
			return callWithNamedValues.invoke(obj, new Object[] { config, methodName, arguments });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public void addSOAPRequestHeader(String nameSpace, String name, Object value, boolean mustUnderstand) throws PageException {
		try {
			if (addSOAPRequestHeader == null)
				addSOAPRequestHeader = clazz.getMethod("addSOAPRequestHeader", new Class[] { String.class, String.class, Object.class, boolean.class });
			addSOAPRequestHeader.invoke(obj, new Object[] { nameSpace, name, value, mustUnderstand });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Node getSOAPRequest() throws PageException {
		try {
			if (getSOAPRequest == null) getSOAPRequest = clazz.getMethod("getSOAPRequest", WSHandlerReflector.EMPTY_CLASS);
			return (Node) getSOAPRequest.invoke(obj, WSHandlerReflector.EMPTY_OBJECT);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Node getSOAPResponse() throws PageException {
		try {
			if (getSOAPResponse == null) getSOAPResponse = clazz.getMethod("getSOAPResponse", WSHandlerReflector.EMPTY_CLASS);
			return (Node) getSOAPResponse.invoke(obj, WSHandlerReflector.EMPTY_OBJECT);
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public Object getSOAPResponseHeader(PageContext pc, String namespace, String name, boolean asXML) throws PageException {
		try {
			if (getSOAPResponseHeader == null)
				getSOAPResponseHeader = clazz.getMethod("getSOAPResponseHeader", new Class[] { PageContext.class, String.class, String.class, boolean.class });
			return getSOAPResponseHeader.invoke(obj, new Object[] { pc, namespace, name, asXML });
		}
		catch (Exception e) {
			throw Caster.toPageException(e);
		}
	}

	@Override
	public WSHandler getWSHandler() {
		try {
			if (getWSHandler == null) getWSHandler = clazz.getMethod("getWSHandler", WSHandlerReflector.EMPTY_CLASS);
			return (WSHandler) getWSHandler.invoke(obj, WSHandlerReflector.EMPTY_OBJECT);
		}
		catch (Exception e) {
			throw new PageRuntimeException(e);
		}
	}

}
