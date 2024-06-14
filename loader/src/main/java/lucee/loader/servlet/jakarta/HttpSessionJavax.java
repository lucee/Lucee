package lucee.loader.servlet.jakarta;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

public class HttpSessionJavax implements HttpSession {

	private jakarta.servlet.http.HttpSession session;

	public HttpSessionJavax(jakarta.servlet.http.HttpSession session) {
		this.session = session;
	}

	@Override
	public long getCreationTime() {
		return session.getCreationTime();
	}

	@Override
	public String getId() {
		return session.getId();
	}

	@Override
	public long getLastAccessedTime() {
		return session.getLastAccessedTime();
	}

	@Override
	public ServletContext getServletContext() {
		return new ServletContextJavax(session.getServletContext());
	}

	@Override
	public void setMaxInactiveInterval(int interval) {
		session.setMaxInactiveInterval(interval);
	}

	@Override
	public int getMaxInactiveInterval() {
		return session.getMaxInactiveInterval();
	}

	@Override
	public HttpSessionContext getSessionContext() {
		throw new RuntimeException("method [getSessionContext] is not supported");
	}

	@Override
	public Object getAttribute(String name) {
		return session.getAttribute(name);
	}

	@Override
	public Object getValue(String name) {
		return session.getAttribute(name);
	}

	@Override
	public Enumeration<String> getAttributeNames() {
		return session.getAttributeNames();
	}

	@Override
	public String[] getValueNames() {
		Enumeration<String> en = session.getAttributeNames();
		List<String> namesList = new ArrayList<>();

		while (en.hasMoreElements()) {
			namesList.add(en.nextElement());
		}
		return namesList.toArray(new String[namesList.size()]);
	}

	@Override
	public void setAttribute(String name, Object value) {
		session.setAttribute(name, value);
	}

	@Override
	public void putValue(String name, Object value) {
		session.setAttribute(name, value);
	}

	@Override
	public void removeAttribute(String name) {
		session.removeAttribute(name);
	}

	@Override
	public void removeValue(String name) {
		session.removeAttribute(name);
	}

	@Override
	public void invalidate() {
		session.invalidate();
	}

	@Override
	public boolean isNew() {
		return session.isNew();
	}
}