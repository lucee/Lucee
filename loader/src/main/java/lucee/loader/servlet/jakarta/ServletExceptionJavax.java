package lucee.loader.servlet.jakarta;

import javax.servlet.ServletException;

public class ServletExceptionJavax extends ServletException implements Jakarta {
	private static final long serialVersionUID = -109825224091435598L;
	private jakarta.servlet.ServletException exp;

	public ServletExceptionJavax(jakarta.servlet.ServletException exp) {
		super(exp);
		this.exp = exp;
	}

	@Override
	public Object getJakartaInstance() {
		return exp;
	}

}
