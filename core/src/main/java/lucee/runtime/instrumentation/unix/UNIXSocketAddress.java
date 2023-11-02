package lucee.runtime.instrumentation.unix;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Describes an {@link InetSocketAddress} that actually uses AF_UNIX sockets instead of AF_INET.
 * 
 * The ability to specify a port number is not specified by AF_UNIX sockets, but we need it
 * sometimes, for example for RMI-over-AF_UNIX.
 */
public class UNIXSocketAddress extends InetSocketAddress {

	private static final long serialVersionUID = 1L;
	private final String socketFile;

	/**
	 * Creates a new {@link UNIXSocketAddress} that points to the AF_UNIX socket specified by the given
	 * file.
	 * 
	 * @param socketFile The socket to connect to.
	 */
	public UNIXSocketAddress(final File socketFile) throws IOException {
		this(socketFile, 0);
	}

	/**
	 * Creates a new {@link UNIXSocketAddress} that points to the AF_UNIX socket specified by the given
	 * file, assigning the given port to it.
	 * 
	 * @param socketFile The socket to connect to.
	 * @param port The port associated with this socket, or {@code 0} when no port should be assigned.
	 */
	public UNIXSocketAddress(final File socketFile, int port) throws IOException {
		super(0);
		if (port != 0) {
			NativeUnixSocket.setPort1(this, port);
		}
		this.socketFile = socketFile.getCanonicalPath();
	}

	/**
	 * Returns the (canonical) file path for this {@link UNIXSocketAddress}.
	 * 
	 * @return The file path.
	 */
	public String getSocketFile() {
		return socketFile;
	}

	@Override
	public String toString() {
		return getClass().getName() + "[host=" + getHostName() + ";port=" + getPort() + ";file=" + socketFile + "]";
	}
}
