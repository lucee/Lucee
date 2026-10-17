package lucee.runtime.net.ldap;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.SSLSocketFactory;

/**
 * SSLSocketFactory that delegates to a ThreadLocal-configured factory, allowing per-connection
 * client certificate configuration for JNDI LDAP connections. JNDI requires a class name for
 * java.naming.ldap.factory.socket, so we use ThreadLocal to pass the configured factory.
 */
public class LDAPSSLSocketFactory extends SSLSocketFactory {

	private static final ThreadLocal<SSLSocketFactory> delegate = new ThreadLocal<>();

	public static void set(SSLSocketFactory factory) {
		delegate.set(factory);
	}

	public static void clear() {
		delegate.remove();
	}

	private SSLSocketFactory get() {
		SSLSocketFactory f = delegate.get();
		return f != null ? f : (SSLSocketFactory) SSLSocketFactory.getDefault();
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return get().getDefaultCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return get().getSupportedCipherSuites();
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
		return get().createSocket(s, host, port, autoClose);
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException {
		return get().createSocket(host, port);
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
		return get().createSocket(host, port, localHost, localPort);
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException {
		return get().createSocket(host, port);
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return get().createSocket(address, port, localAddress, localPort);
	}
}
