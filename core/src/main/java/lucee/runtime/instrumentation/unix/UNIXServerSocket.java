package lucee.runtime.instrumentation.unix;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * The server part of an AF_UNIX domain socket.
 */
public class UNIXServerSocket extends ServerSocket {
	private final UNIXSocketImpl implementation;
	private UNIXSocketAddress boundEndpoint = null;

	private final Thread shutdownThread = new Thread() {
		@Override
		public void run() {
			try {
				if (boundEndpoint != null) {
					NativeUnixSocket.unlink(boundEndpoint.getSocketFile());
				}
			}
			catch (IOException e) {
				// ignore
			}
		}
	};

	protected UNIXServerSocket() throws IOException {
		super();
		this.implementation = new UNIXSocketImpl();
		NativeUnixSocket.initServerImpl(this, implementation);

		Runtime.getRuntime().addShutdownHook(shutdownThread);
		NativeUnixSocket.setCreatedServer(this);
	}

	/**
	 * Returns a new, unbound AF_UNIX {@link ServerSocket}.
	 * 
	 * @return The new, unbound {@link UNIXServerSocket}.
	 */
	public static UNIXServerSocket newInstance() throws IOException {
		UNIXServerSocket instance = new UNIXServerSocket();
		return instance;
	}

	/**
	 * Returns a new AF_UNIX {@link ServerSocket} that is bound to the given {@link UNIXSocketAddress}.
	 * 
	 * @return The new, unbound {@link UNIXServerSocket}.
	 */
	public static UNIXServerSocket bindOn(final UNIXSocketAddress addr) throws IOException {
		UNIXServerSocket socket = newInstance();
		socket.bind(addr);
		return socket;
	}

	@Override
	public void bind(SocketAddress endpoint, int backlog) throws IOException {
		if (isClosed()) {
			throw new SocketException("Socket is closed");
		}
		if (isBound()) {
			throw new SocketException("Already bound");
		}
		if (!(endpoint instanceof UNIXSocketAddress)) {
			throw new IOException("Can only bind to endpoints of type " + UNIXSocketAddress.class.getName());
		}
		implementation.bind(backlog, endpoint);
		boundEndpoint = (UNIXSocketAddress) endpoint;
	}

	@Override
	public boolean isBound() {
		return boundEndpoint != null;
	}

	@Override
	public Socket accept() throws IOException {
		if (isClosed()) {
			throw new SocketException("Socket is closed");
		}
		UNIXSocket as = UNIXSocket.newInstance();
		implementation.accept(as.impl);
		as.addr = boundEndpoint;
		NativeUnixSocket.setConnected(as);
		return as;
	}

	@Override
	public String toString() {
		if (!isBound()) {
			return "AFUNIXServerSocket[unbound]";
		}
		return "AFUNIXServerSocket[" + boundEndpoint.getSocketFile() + "]";
	}

	@Override
	public void close() throws IOException {
		if (isClosed()) {
			return;
		}

		super.close();
		implementation.close();
		if (boundEndpoint != null) {
			NativeUnixSocket.unlink(boundEndpoint.getSocketFile());
		}
		try {
			Runtime.getRuntime().removeShutdownHook(shutdownThread);
		}
		catch (IllegalStateException e) {
			// ignore
		}
	}

	public static boolean isSupported() {
		return NativeUnixSocket.isLoaded();
	}
}
