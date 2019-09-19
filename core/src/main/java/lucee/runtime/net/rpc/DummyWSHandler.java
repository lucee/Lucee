package lucee.runtime.net.rpc;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.net.rpc.client.WSClient;
import lucee.runtime.net.rpc.server.WSServer;

public class DummyWSHandler implements WSHandler {

	@Override
	public boolean isSOAPRequest() {
		throw notInstalledEL();
	}

	@Override
	public void addSOAPResponseHeader(String namespace, String name, Object value, boolean mustUnderstand) throws PageException {
		throw notInstalled();
	}

	@Override
	public Object getSOAPRequestHeader(PageContext pc, String namespace, String name, boolean asXML) throws PageException {
		throw notInstalled();
	}

	@Override
	public String getTypeAsString() {
		throw notInstalledEL();
	}

	@Override
	public Class<?> toWSTypeClass(Class<?> clazz) {
		throw notInstalledEL();
	}

	@Override
	public WSServer getWSServer(PageContext pc) throws PageException {
		throw notInstalled();
	}

	@Override
	public WSClient getWSClient(String wsdlUrl, String username, String password, ProxyData proxyData) throws PageException {
		throw notInstalled();
	}

	private RPCException notInstalled() {
		return new RPCException("No Webservice Engine is installed! Check out the Extension Store in the Lucee Administrator for \"Webservices\".");
	}

	private PageRuntimeException notInstalledEL() {
		return new PageRuntimeException(notInstalled());
	}

}
