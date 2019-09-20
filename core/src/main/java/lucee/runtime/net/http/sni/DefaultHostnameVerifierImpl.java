package lucee.runtime.net.http.sni;

import java.security.cert.X509Certificate;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

public class DefaultHostnameVerifierImpl extends AbsDefaultHostnameVerifier {

	@Override
	public boolean verify(String host, SSLSession session) {
		if (SSLConnectionSocketFactoryImpl.ENABLE_SNI.equals(host)) return true;
		return super.verify(host, session);
	}

	@Override
	public void verify(String host, X509Certificate cert) throws SSLException {
		if (SSLConnectionSocketFactoryImpl.ENABLE_SNI.equals(host)) return;
		super.verify(host, cert);
	}
}
