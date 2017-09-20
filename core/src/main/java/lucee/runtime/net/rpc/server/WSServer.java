package lucee.runtime.net.rpc.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lucee.runtime.Component;
import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;

public interface WSServer {
	public void doGet(HttpServletRequest request, HttpServletResponse response, Component component) throws PageException;
	public void doPost(PageContext pc,HttpServletRequest req, HttpServletResponse res, Component component) throws PageException;
	public Object invoke(String name, Object[] args) throws PageException;
	//public void doPost(HttpServletRequest req, HttpServletResponse res, Component component) throws ServletException, IOException;
	public void registerTypeMapping(Class clazz);
}
