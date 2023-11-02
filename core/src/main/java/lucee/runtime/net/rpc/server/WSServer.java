package lucee.runtime.net.rpc.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.rpc.WSHandler;

public interface WSServer {
	public void doGet(PageContext pc, HttpServletRequest request, HttpServletResponse response, Component component) throws PageException;

	public void doPost(PageContext pc, HttpServletRequest req, HttpServletResponse res, Component component) throws PageException;

	public Object invoke(String name, Object[] args) throws PageException;

	public void registerTypeMapping(Class clazz);

	public WSHandler getWSHandler();
}
