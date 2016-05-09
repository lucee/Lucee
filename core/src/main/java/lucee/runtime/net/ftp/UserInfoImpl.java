package lucee.runtime.net.ftp;

import com.jcraft.jsch.UserInfo;

public class UserInfoImpl implements UserInfo {
	
	private String password;
	private String passphrase;

	public UserInfoImpl(String password, String passphrase) {
		this.password=password;
		this.passphrase=passphrase;
	}

	@Override
	public String getPassphrase() {
		return passphrase;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public boolean promptPassphrase(String msg) {
		return passphrase!=null && passphrase.trim().length()>0;
	}

	@Override
	public boolean promptPassword(String msg) {
		return password!=null && password.trim().length()>0;
	}

	@Override
	public boolean promptYesNo(String msg) {
		return true;
	}

	@Override
	public void showMessage(String msg) {}
}
