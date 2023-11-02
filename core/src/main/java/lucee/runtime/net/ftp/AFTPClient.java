package lucee.runtime.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

public abstract class AFTPClient {

	public final static int FILE_TYPE_BINARY = 1;
	public final static int FILE_TYPE_TEXT = 2;

	public static AFTPClient getInstance(boolean secure, InetAddress host, int port, String username, String password, String fingerprint, boolean stopOnError) throws IOException {

		AFTPClient client = secure ? new SFTPClientImpl() : new FTPClientImpl();
		client.init(host, port, username, password, fingerprint, stopOnError);
		return client;
	}

	/**
	 * Renames a remote file.
	 * <p>
	 * 
	 * @param from The name of the remote file to rename.
	 * @param to The new name of the remote file.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 * @throws FTPException
	 */
	public abstract boolean rename(String from, String to) throws IOException;

	/***
	 * Returns the integer value of the reply code of the last FTP reply. You will usually only use this
	 * method after you connect to the FTP server to check that the connection was successful since
	 * <code> connect </code> is of type void.
	 * <p>
	 * 
	 * @return The integer value of the reply code of the last FTP reply.
	 ***/
	public abstract int getReplyCode();

	/***
	 * Returns the entire text of the last FTP server response exactly as it was received, including all
	 * end of line markers in NETASCII format.
	 * <p>
	 * 
	 * @return The entire text from the last FTP response as a String.
	 ***/
	public abstract String getReplyString();

	/**
	 * Change the current working directory of the FTP session.
	 * <p>
	 * 
	 * @param pathname The new current working directory.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean changeWorkingDirectory(String pathname) throws IOException;

	/**
	 * Creates a new subdirectory on the FTP server in the current directory (if a relative pathname is
	 * given) or where specified (if an absolute pathname is given).
	 * <p>
	 * 
	 * @param pathname The pathname of the directory to create.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean makeDirectory(String pathname) throws IOException;

	/**
	 * Using the default system autodetect mechanism, obtain a list of file information for the current
	 * working directory or for just a single file.
	 * <p>
	 * This information is obtained through the LIST command. The contents of the returned array is
	 * determined by the<code> FTPFileEntryParser </code> used.
	 * <p>
	 * 
	 * @param pathname The file or directory to list. Since the server may or may not expand glob
	 *            expressions, using them here is not recommended and may well cause this method to
	 *            fail. Also, some servers treat a leading '-' as being an option. To avoid this
	 *            interpretation, use an absolute pathname or prefix the pathname with ./ (unix style
	 *            servers). Some servers may support "--" as meaning end of options, in which case "--
	 *            -xyz" should work.
	 *
	 * @return The list of file information contained in the given path in the format determined by the
	 *         autodetection mechanism
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 * @exception org.apache.commons.net.ftp.parser.ParserInitializationException Thrown if the
	 *                parserKey parameter cannot be resolved by the selected parser factory. In the
	 *                DefaultFTPEntryParserFactory, this will happen when parserKey is neither the fully
	 *                qualified class name of a class implementing the interface
	 *                org.apache.commons.net.ftp.FTPFileEntryParser nor a string containing one of the
	 *                recognized keys mapping to such a parser or if class loader security issues
	 *                prevent its being loaded.
	 */
	public abstract FTPFile[] listFiles(String pathname) throws IOException;

	/**
	 * Removes a directory on the FTP server (if empty).
	 * <p>
	 * 
	 * @param pathname The pathname of the directory to remove.
	 * @param recursive if true it also can delete
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean removeDirectory(String pathname) throws IOException;

	/**
	 * Sets the file type to be transferred. This should be one of <code> FTP.ASCII_FILE_TYPE </code>,
	 * <code> FTP.BINARY_FILE_TYPE</code>, etc. The file type only needs to be set when you want to
	 * change the type. After changing it, the new type stays in effect until you change it again. The
	 * default file type is <code> FTP.ASCII_FILE_TYPE </code> if this method is never called. <br>
	 * The server default is supposed to be ASCII (see RFC 959), however many ftp servers default to
	 * BINARY. <b>To ensure correct operation with all servers, always specify the appropriate file type
	 * after connecting to the server.</b> <br>
	 * <p>
	 * <b>N.B.</b> currently calling any connect method will reset the type to FTP.ASCII_FILE_TYPE.
	 * 
	 * @param fileType The <code> _FILE_TYPE </code> constant indcating the type of file.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean setFileType(int fileType) throws IOException;

	/**
	 * Retrieves a named file from the server and writes it to the given OutputStream. This method does
	 * NOT close the given OutputStream. If the current file type is ASCII, line separators in the file
	 * are converted to the local representation.
	 * <p>
	 * Note: if you have used {@link #setRestartOffset(long)}, the file data will start from the
	 * selected offset.
	 * 
	 * @param remote The name of the remote file.
	 * @param local The local OutputStream to which to write the file.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually
	 *                transferring the file. The CopyStreamException allows you to determine the number
	 *                of bytes transferred and the IOException causing the error. This exception may be
	 *                caught either as an IOException or independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean retrieveFile(String remote, OutputStream local) throws IOException;

	/**
	 * Stores a file on the server using the given name and taking input from the given InputStream.
	 * This method does NOT close the given InputStream. If the current file type is ASCII, line
	 * separators in the file are transparently converted to the NETASCII format (i.e., you should not
	 * attempt to create a special InputStream to do this).
	 * <p>
	 * 
	 * @param remote The name to give the remote file.
	 * @param local The local InputStream from which to read the file.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception org.apache.commons.net.io.CopyStreamException If an I/O error occurs while actually
	 *                transferring the file. The CopyStreamException allows you to determine the number
	 *                of bytes transferred and the IOException causing the error. This exception may be
	 *                caught either as an IOException or independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean storeFile(String remote, InputStream local) throws IOException;

	/**
	 * Deletes a file on the FTP server.
	 * <p>
	 * 
	 * @param pathname The pathname of the file to be deleted.
	 * @return True if successfully completed, false if not.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract boolean deleteFile(String pathname) throws IOException;

	/**
	 * Returns the pathname of the current working directory.
	 * <p>
	 * 
	 * @return The pathname of the current working directory. If it cannot be obtained, returns null.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending a command to the server or
	 *                receiving a reply from the server.
	 */
	public abstract String printWorkingDirectory() throws IOException;

	public abstract String getPrefix();

	/**
	 * @return The remote address to which the client is connected. Delegates to
	 *         {@link Socket#getInetAddress()}
	 * @throws NullPointerException if the socket is not currently open
	 */
	public abstract InetAddress getRemoteAddress();

	/**
	 * Returns true if the client is currently connected to a server.
	 * <p>
	 * Delegates to {@link Socket#isConnected()}
	 * 
	 * @return True if the client is currently connected to a server, false otherwise.
	 */
	public abstract boolean isConnected();

	/***
	 * A convenience method to send the FTP QUIT command to the server, receive the reply, and return
	 * the reply code.
	 * <p>
	 * 
	 * @return The reply code received from the server.
	 * @exception FTPConnectionClosedException If the FTP server prematurely closes the connection as a
	 *                result of the client being idle or some other reason causing the server to send
	 *                FTP reply code 421. This exception may be caught either as an IOException or
	 *                independently as itself.
	 * @exception IOException If an I/O error occurs while either sending the command or receiving the
	 *                server reply.
	 ***/
	public abstract int quit() throws IOException;

	/**
	 * Closes the connection to the FTP server and restores connection parameters to the default values.
	 * <p>
	 * 
	 * @exception IOException If an error occurs while disconnecting.
	 */
	public abstract void disconnect() throws IOException;

	/**
	 * timeout in milli seconds
	 * 
	 * @param timeout
	 */
	public abstract void setTimeout(int timeout);

	/**
	 * Returns the current data connection mode (one of the <code> _DATA_CONNECTION_MODE </code>
	 * constants.
	 * <p>
	 * 
	 * @return The current data connection mode (one of the <code> _DATA_CONNECTION_MODE </code>
	 *         constants.
	 */
	public abstract int getDataConnectionMode();

	/**
	 * Set the current data connection mode to <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code>. Use
	 * this method only for data transfers between the client and server. This method causes a PASV (or
	 * EPSV) command to be issued to the server before the opening of every data connection, telling the
	 * server to open a data port to which the client will connect to conduct data transfers. The
	 * FTPClient will stay in <code> PASSIVE_LOCAL_DATA_CONNECTION_MODE </code> until the mode is
	 * changed by calling some other method such as {@link #enterLocalActiveMode enterLocalActiveMode()
	 * }
	 * <p>
	 * <b>N.B.</b> currently calling any connect method will reset the mode to
	 * ACTIVE_LOCAL_DATA_CONNECTION_MODE.
	 */
	public abstract void enterLocalPassiveMode();

	/**
	 * Set the current data connection mode to <code>ACTIVE_LOCAL_DATA_CONNECTION_MODE</code>. No
	 * communication with the FTP server is conducted, but this causes all future data transfers to
	 * require the FTP server to connect to the client's data port. Additionally, to accommodate
	 * differences between socket implementations on different platforms, this method causes the client
	 * to issue a PORT command before every data transfer.
	 */
	public abstract void enterLocalActiveMode();

	public abstract void init(InetAddress host, int port, String username, String password, String fingerprint, boolean stopOnError) throws SocketException, IOException;

	/**
	 * Opens a Socket connected to a remote host at the specified port and originating from the current
	 * host at a system assigned port. Before returning, {@link #_connectAction_ _connectAction_() } is
	 * called to perform connection initialization actions.
	 * <p>
	 * 
	 * @param host The remote host.
	 * @param port The port to connect to on the remote host.
	 * @exception SocketException If the socket timeout could not be set.
	 * @exception IOException If the socket could not be opened. In most cases you will only want to
	 *                catch IOException since SocketException is derived from it.
	 * @throws FTPException
	 */
	public abstract void connect() throws SocketException, IOException;

	/***
	 * Determine if a reply code is a positive completion response. All codes beginning with a 2 are
	 * positive completion responses. The FTP server will send a positive completion response on the
	 * final successful completion of a command.
	 * <p>
	 * 
	 * @param reply The reply code to test.
	 * @return True if a reply code is a postive completion response, false if not.
	 ***/
	public abstract boolean isPositiveCompletion();

	public abstract boolean directoryExists(String pathname) throws IOException;
}
