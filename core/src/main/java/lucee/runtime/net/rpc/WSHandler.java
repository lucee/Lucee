package lucee.runtime.net.rpc;

import java.util.Set;

import javax.servlet.ServletContext;

import org.w3c.dom.Node;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.net.rpc.server.WSServer;
import lucee.runtime.type.Struct;

public abstract class WSHandler {
	
	private static WSHandler instance=new lucee.runtime.net.rpc.axis1.Axis1Handler();
	
	public static WSHandler getInstance() {
		return instance;
	}
	
	public abstract boolean isSOAPRequest();
	public abstract void addSOAPRequestHeader(WSClient client, String nameSpace,String name, Object value, boolean mustUnderstand) throws PageException;
	public abstract void addSOAPResponseHeader(String namespace, String name, Object value, boolean mustUnderstand) throws PageException;
	public abstract Node getSOAPRequest(WSClient client) throws PageException;
	public abstract Node getSOAPResponse(WSClient client) throws PageException;
	public abstract Object getSOAPRequestHeader(PageContext pc, String namespace, String name, boolean asXML) throws PageException;
	public abstract Object getSOAPResponseHeader(PageContext pc, WSClient client, String namespace, String name, boolean asXML) throws PageException;
	public abstract String getTypeAsString();
	
	public abstract Component toComponent(PageContext pc, Pojo pojo, String compPath , Component defaultValue);
	public abstract Class<?> toWSTypeClass(Class<?> clazz);
	public abstract Pojo toPojo(Pojo pojo, Component comp, Set<Object> done) throws PageException;
	public abstract Pojo toPojo(Pojo pojo, Struct sct, Set<Object> done) throws PageException;
	
	public abstract WSServer getWSServer(PageContext pc) throws PageException;
	public abstract WSClient getWSClient(String wsdlUrl, String username, String password, ProxyData proxyData) throws PageException;
		
}
