package lucee.runtime.net.ftp;

import org.apache.commons.net.ftp.FTPSClient;

public class FTPSClientImpl extends FTPClientImpl {

	private FTPSClient client;

	public FTPSClientImpl(FTPSClient client) {
		this.client = client;
	}

	FTPSClientImpl() {
		this.client = new FTPSClient();
	}
}
