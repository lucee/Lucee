package lucee.runtime.net.http.sni;

import java.io.IOException;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

public class SSLConnectionSocketFactoryImpl extends SSLConnectionSocketFactory {

	public static final String ENABLE_SNI = "*.disable.sni";

	/*
	 * Implement any constructor you need for your particular application - SSLConnectionSocketFactory
	 * has many variants
	 */
	public SSLConnectionSocketFactoryImpl(final SSLContext sslContext, final HostnameVerifier verifier) {
		super(sslContext, verifier);
	}

	public SSLConnectionSocketFactoryImpl(final SSLContext sslContext) {
		super(sslContext);
	}

	@Override
	public Socket createLayeredSocket(final Socket socket, final String target, final int port, final HttpContext context) throws IOException {
		Boolean enableSniValue = (Boolean) context.getAttribute(ENABLE_SNI);
		boolean enableSni = enableSniValue == null || enableSniValue;
		return super.createLayeredSocket(socket, enableSni ? target : ENABLE_SNI, port, context);
	}

	public static List<String> getSupportedSslProtocols() {
		try {
			return Arrays.asList(SSLContext.getDefault().getSupportedSSLParameters().getProtocols());
		}
		catch (NoSuchAlgorithmException ex) {
		}
		return Collections.emptyList();
	}

}