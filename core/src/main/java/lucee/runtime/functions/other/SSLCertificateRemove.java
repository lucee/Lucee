package lucee.runtime.functions.other;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import lucee.commons.net.http.httpclient.HTTPEngine4Impl;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.net.http.SSLUtil;
import lucee.runtime.op.Caster;

public final class SSLCertificateRemove implements Function {

	private static final long serialVersionUID = 1L;

	public static boolean call( PageContext pc, String alias ) throws PageException {
		if ( !SSLUtil.isCustomCaCertsEnabled() ) {
			throw new ApplicationException( "custom-cacerts is disabled. Set lucee.ssl.customcacerts.enabled=true to enable." );
		}

		Path customCaCertsPath = SSLUtil.getCustomCaCertsPath();
		if ( customCaCertsPath == null || !Files.exists( customCaCertsPath ) ) {
			throw new ApplicationException( "custom-cacerts keystore does not exist." );
		}

		try {
			KeyStore ks = SSLUtil.loadKeyStore( customCaCertsPath, SSLUtil.getDefaultPassword() );

			if ( !ks.containsAlias( alias ) ) {
				throw new ApplicationException( "Certificate with alias [" + alias + "] not found in custom-cacerts." );
			}

			ks.deleteEntry( alias );

			try ( OutputStream os = Files.newOutputStream( customCaCertsPath ) ) {
				ks.store( os, SSLUtil.getDefaultPassword() );
			}

			// Invalidate connection managers so new connections use updated trust material
			HTTPEngine4Impl.releaseConnectionManager();

			return true;
		}
		catch ( ApplicationException ae ) {
			throw ae;
		}
		catch ( Exception e ) {
			throw Caster.toPageException( e );
		}
	}

}
