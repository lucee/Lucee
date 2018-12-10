package lucee.runtime.vault;

import lucee.print;

public class Test {

    public static void main(String[] args) throws Exception {
	Credential c = CredentialFactory.getCredential("s", "susi", "sorglos");
	print.e(CredentialFactory.getUsername(c));
	print.e(CredentialFactory.getPassword(c));
    }
}
