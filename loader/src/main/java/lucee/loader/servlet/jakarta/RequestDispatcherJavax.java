package lucee.loader.servlet.jakarta;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestDispatcherJavax implements RequestDispatcher {

	private jakarta.servlet.RequestDispatcher dispatcher;

	public RequestDispatcherJavax(jakarta.servlet.RequestDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		jakarta.servlet.ServletRequest req = (jakarta.servlet.ServletRequest) ((ServletRequestJavax) request).getJakartaInstance();
		jakarta.servlet.ServletResponse rsp = (jakarta.servlet.ServletResponse) ((ServletResponseJavax) response).getJakartaInstance();
		try {
			dispatcher.forward(req, rsp);
		}
		catch (jakarta.servlet.ServletException se) {
			new ServletExceptionJavax(se);
		}
	}

	@Override
	public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
		jakarta.servlet.ServletRequest req = (jakarta.servlet.ServletRequest) ((ServletRequestJavax) request).getJakartaInstance();
		jakarta.servlet.ServletResponse rsp = (jakarta.servlet.ServletResponse) ((ServletResponseJavax) response).getJakartaInstance();
		try {
			dispatcher.include(req, rsp);
		}
		catch (jakarta.servlet.ServletException se) {
			new ServletExceptionJavax(se);
		}
	}
}