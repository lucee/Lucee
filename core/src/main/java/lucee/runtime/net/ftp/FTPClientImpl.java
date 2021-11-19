package lucee.runtime.net.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;

public class FTPClientImpl extends AFTPClient {

	private FTPClient client;
	private InetAddress host;
	private int port;
	private String username;
	private String password;
	private boolean stopOnError;

	public FTPClientImpl(FTPClient client) {
		this.client = client;
	}

	FTPClientImpl() {
		this.client = new FTPClient();
	}

	@Override
	public void init(InetAddress host, int port, String username, String password, String fingerprint, boolean stopOnError) throws SocketException, IOException {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.stopOnError = stopOnError;
	}

	@Override
	public void connect() throws SocketException, IOException {
		client.connect(host, port);
		if (!StringUtil.isEmpty(username)) client.login(username, password);
	}

	@Override
	public boolean rename(String from, String to) throws IOException {
		return client.rename(from, to);
	}

	@Override
	public int getReplyCode() {
		return client.getReplyCode();
	}

	@Override
	public String getReplyString() {
		return client.getReplyString();
	}

	@Override
	public boolean changeWorkingDirectory(String pathname) throws IOException {
		return client.changeWorkingDirectory(pathname);
	}

	@Override
	public boolean makeDirectory(String pathname) throws IOException {
		return client.makeDirectory(pathname);
	}

	@Override
	public FTPFile[] listFiles(String pathname) throws IOException {
		return client.listFiles(pathname);
	}

	@Override
	public boolean removeDirectory(String pathname) throws IOException {
		return client.removeDirectory(pathname);
	}

	@Override
	public boolean setFileType(int fileType) throws IOException {
		return client.setFileType(toFTPClientFileType(fileType));
	}

	private int toFTPClientFileType(int fileType) {
		if (fileType == FILE_TYPE_BINARY) return FTP.BINARY_FILE_TYPE;
		return FTP.ASCII_FILE_TYPE;
	}

	@Override
	public boolean retrieveFile(String remote, OutputStream local) throws IOException {
		return client.retrieveFile(remote, local);
	}

	@Override
	public boolean storeFile(String remote, InputStream local) throws IOException {
		return client.storeFile(remote, local);
	}

	@Override
	public boolean deleteFile(String pathname) throws IOException {
		return client.deleteFile(pathname);
	}

	@Override
	public String printWorkingDirectory() throws IOException {
		return client.printWorkingDirectory();
	}

	@Override
	public String getPrefix() {
		return "ftp";
	}

	@Override
	public InetAddress getRemoteAddress() {
		return client.getRemoteAddress();
	}

	@Override
	public boolean isConnected() {
		return client.isConnected();
	}

	@Override
	public int quit() throws IOException {
		return client.quit();
	}

	@Override
	public void disconnect() throws IOException {
		client.disconnect();
	}

	@Override
	public int getDataConnectionMode() {
		return client.getDataConnectionMode();
	}

	@Override
	public void enterLocalPassiveMode() {
		client.enterLocalPassiveMode();
	}

	@Override
	public void enterLocalActiveMode() {
		client.enterLocalActiveMode();
	}

	@Override
	public boolean isPositiveCompletion() {
		return FTPReply.isPositiveCompletion(client.getReplyCode());
	}

	@Override
	public boolean directoryExists(String pathname) throws IOException {
		String pwd = null;
		try {
			pwd = client.printWorkingDirectory();
			return client.changeWorkingDirectory(pathname);
		}
		finally {
			if (pwd != null) client.changeWorkingDirectory(pwd);
		}
	}

	@Override
	public void setTimeout(int timeout) {
		client.setDataTimeout(timeout);
		try {
			client.setSoTimeout(timeout);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

}
