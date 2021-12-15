package lucee.runtime.instrumentation.unix;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Implementation of an AF_UNIX domain socket.
 */
public class UNIXSocket extends Socket {
	protected UNIXSocketImpl impl;
	UNIXSocketAddress addr;

	private UNIXSocket(final UNIXSocketImpl impl) throws IOException {
		super(impl);
		try {
			NativeUnixSocket.setCreated(this);
		}
		catch (UnsatisfiedLinkError e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a new, unbound {@link UNIXSocket}.
	 * 
	 * This "default" implementation is a bit "lenient" with respect to the specification.
	 * 
	 * In particular, we ignore calls to {@link Socket#getTcpNoDelay()} and
	 * {@link Socket#setTcpNoDelay(boolean)}.
	 * 
	 * @return A new, unbound socket.
	 */
	public static UNIXSocket newInstance() throws IOException {
		final UNIXSocketImpl impl = new UNIXSocketImpl.Lenient();
		UNIXSocket instance = new UNIXSocket(impl);
		instance.impl = impl;
		return instance;
	}

	/**
	 * Creates a new, unbound, "strict" {@link UNIXSocket}.
	 * 
	 * This call uses an implementation that tries to be closer to the specification than
	 * {@link #newInstance()}, at least for some cases.
	 * 
	 * @return A new, unbound socket.
	 */
	public static UNIXSocket newStrictInstance() throws IOException {
		final UNIXSocketImpl impl = new UNIXSocketImpl();
		UNIXSocket instance = new UNIXSocket(impl);
		instance.impl = impl;
		return instance;
	}

	/**
	 * Creates a new {@link UNIXSocket} and connects it to the given {@link UNIXSocketAddress}.
	 * 
	 * @param addr The address to connect to.
	 * @return A new, connected socket.
	 */
	public static UNIXSocket connectTo(UNIXSocketAddress addr) throws IOException {
		UNIXSocket socket = newInstance();
		socket.connect(addr);
		return socket;
	}

	/**
	 * Binds this {@link UNIXSocket} to the given bindpoint. Only bindpoints of the type
	 * {@link UNIXSocketAddress} are supported.
	 */
	@Override
	public void bind(SocketAddress bindpoint) throws IOException {
		super.bind(bindpoint);
		this.addr = (UNIXSocketAddress) bindpoint;
	}

	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		connect(endpoint, 0);
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		if (!(endpoint instanceof UNIXSocketAddress)) {
			throw new IOException("Can only connect to endpoints of type " + UNIXSocketAddress.class.getName());
		}
		impl.connect(endpoint, timeout);
		this.addr = (UNIXSocketAddress) endpoint;
		NativeUnixSocket.setConnected(this);
	}

	@Override
	public String toString() {
		if (isConnected()) {
			return "AFUNIXSocket[fd=" + impl.getFD() + ";path=" + addr.getSocketFile() + "]";
		}
		return "AFUNIXSocket[unconnected]";
	}

	/**
	 * Returns <code>true</code> iff {@link UNIXSocket}s are supported by the current Java VM.
	 * 
	 * To support {@link UNIXSocket}s, a custom JNI library must be loaded that is supplied with
	 * <em>junixsocket</em>.
	 * 
	 * @return {@code true} iff supported.
	 */
	public static boolean isSupported() {
		return NativeUnixSocket.isLoaded();
	}
}
