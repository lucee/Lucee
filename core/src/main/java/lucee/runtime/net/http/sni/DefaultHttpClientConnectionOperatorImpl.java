package lucee.runtime.net.http.sni;

import java.io.IOException;
import java.net.InetSocketAddress;

import javax.net.ssl.SSLProtocolException;

import org.apache.http.HttpHost;
import org.apache.http.config.Lookup;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ManagedHttpClientConnection;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.impl.conn.DefaultHttpClientConnectionOperator;
import org.apache.http.protocol.HttpContext;

public class DefaultHttpClientConnectionOperatorImpl extends DefaultHttpClientConnectionOperator {

	public DefaultHttpClientConnectionOperatorImpl(Lookup<ConnectionSocketFactory> socketFactoryRegistry) {
		super(socketFactoryRegistry, null, null);
	}

	@Override
	public void connect(final ManagedHttpClientConnection conn, final HttpHost host, final InetSocketAddress localAddress, final int connectTimeout,
			final SocketConfig socketConfig, final HttpContext context) throws IOException {
		try {
			super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
		}
		catch (SSLProtocolException e) {
			Boolean enableSniValue = (Boolean) context.getAttribute(SSLConnectionSocketFactoryImpl.ENABLE_SNI);
			boolean enableSni = enableSniValue == null || enableSniValue;
			if (enableSni && e.getMessage() != null && e.getMessage().equals("handshake alert:  unrecognized_name")) {
				// print.e("Server received saw wrong SNI host, retrying without SNI");
				context.setAttribute(SSLConnectionSocketFactoryImpl.ENABLE_SNI, false);
				super.connect(conn, host, localAddress, connectTimeout, socketConfig, context);
			}
			else throw e;
		}
	}
}