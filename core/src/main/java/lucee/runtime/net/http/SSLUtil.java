package lucee.runtime.net.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.log.LogUtil;

/**
 * SSL utility class for creating SSLContexts that combine multiple trust sources.
 * Combines JVM cacerts with user-installed certificates in custom-cacerts.
 */
public final class SSLUtil {

	private static final String CUSTOM_CACERTS_ENABLED_PROP = "lucee.ssl.customcacerts.enabled";
	private static final String CUSTOM_CACERTS_FILENAME = "custom-cacerts";
	private static final char[] DEFAULT_PASSWORD = "changeit".toCharArray();

	// Cache the enabled flag - read once at startup
	private static final boolean customCaCertsEnabled;
	static {
		customCaCertsEnabled = !"false".equalsIgnoreCase( SystemUtil.getSystemPropOrEnvVar( CUSTOM_CACERTS_ENABLED_PROP, "true" ) );
	}

	private static volatile Path customCaCertsPath = null;

	private SSLUtil() {
		// utility class
	}

	/**
	 * Returns a fresh copy of the default keystore password ("changeit").
	 */
	public static char[] getDefaultPassword() {
		return DEFAULT_PASSWORD.clone();
	}

	/**
	 * Returns true if custom-cacerts is enabled (default: true).
	 * Can be disabled by setting lucee.ssl.customcacerts.enabled=false
	 */
	public static boolean isCustomCaCertsEnabled() {
		return customCaCertsEnabled;
	}

	/**
	 * Called once at server startup to set the security directory path.
	 * After this, getCustomCaCertsPath() works from any thread.
	 */
	public static void init( Path securityDir ) {
		if ( !customCaCertsEnabled ) return;
		try {
			if ( !Files.exists( securityDir ) ) {
				Files.createDirectories( securityDir );
			}
			customCaCertsPath = securityDir.resolve( CUSTOM_CACERTS_FILENAME );
		}
		catch ( IOException e ) {
			LogUtil.log( "ssl", e );
		}
	}

	/**
	 * Gets the path to the custom-cacerts keystore file.
	 * Returns null if custom-cacerts is disabled or init() has not been called yet.
	 */
	public static Path getCustomCaCertsPath() {
		return customCaCertsPath;
	}

	/**
	 * Creates an empty custom-cacerts keystore if it doesn't exist.
	 */
	public static void initCustomCaCerts() throws GeneralSecurityException, IOException {
		Path path = getCustomCaCertsPath();
		if ( path != null && !Files.exists( path ) ) {
			KeyStore ks = KeyStore.getInstance( KeyStore.getDefaultType() );
			ks.load( null, DEFAULT_PASSWORD ); // Initialize empty keystore
			try ( OutputStream os = Files.newOutputStream( path ) ) {
				ks.store( os, DEFAULT_PASSWORD );
			}
		}
	}

	/**
	 * Creates an SSLContext using JVM cacerts + custom-cacerts (if enabled and exists).
	 */
	public static SSLContext createSSLContext() throws GeneralSecurityException, IOException {
		return createSSLContext( null, null, null );
	}

	/**
	 * Creates an SSLContext with optional client certificate (identity material).
	 * Automatically includes custom-cacerts if enabled.
	 *
	 * @param clientCertPath Path to client certificate keystore (PKCS12 or JKS)
	 * @param clientCertPassword Password for the client certificate keystore
	 */
	public static SSLContext createSSLContext( Path clientCertPath, char[] clientCertPassword ) throws GeneralSecurityException, IOException {
		return createSSLContext( clientCertPath, clientCertPassword, null );
	}

	/**
	 * Creates an SSLContext with optional client certificate and additional trust stores.
	 * Automatically includes JVM cacerts and custom-cacerts (if enabled).
	 *
	 * @param clientCertPath Path to client certificate keystore (PKCS12 or JKS), may be null
	 * @param clientCertPassword Password for the client certificate keystore
	 * @param additionalTrustStores Additional trust stores to combine with JVM default, may be null
	 */
	public static SSLContext createSSLContext( Path clientCertPath, char[] clientCertPassword, List<TrustStoreConfig> additionalTrustStores )
			throws GeneralSecurityException, IOException {

		// Build list of trust managers
		List<X509TrustManager> trustManagers = new ArrayList<>();

		// Always add JVM default trust material
		TrustManagerFactory defaultTmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
		defaultTmf.init( (KeyStore) null ); // null = use JVM default cacerts
		for ( TrustManager tm : defaultTmf.getTrustManagers() ) {
			if ( tm instanceof X509TrustManager ) {
				trustManagers.add( (X509TrustManager) tm );
			}
		}

		// Add custom-cacerts if enabled and exists
		Path customCaCertsPath = getCustomCaCertsPath();
		if ( customCaCertsPath != null && Files.exists( customCaCertsPath ) ) {
			try {
				KeyStore ks = loadKeyStore( customCaCertsPath, DEFAULT_PASSWORD );
				TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
				tmf.init( ks );
				for ( TrustManager tm : tmf.getTrustManagers() ) {
					if ( tm instanceof X509TrustManager ) {
						trustManagers.add( (X509TrustManager) tm );
					}
				}
			}
			catch ( Exception e ) {
				// Log but don't fail - custom-cacerts is optional
				LogUtil.log( "ssl", e );
			}
		}

		// Add any additional trust stores (e.g., per-request trustStore attribute)
		if ( additionalTrustStores != null ) {
			for ( TrustStoreConfig config : additionalTrustStores ) {
				if ( config.path == null ) continue;
				if ( !Files.exists( config.path ) ) {
					throw new GeneralSecurityException( "trustStore file not found: " + config.path );
				}
				KeyStore ks = loadKeyStore( config.path, config.password );
				TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
				tmf.init( ks );
				for ( TrustManager tm : tmf.getTrustManagers() ) {
					if ( tm instanceof X509TrustManager ) {
						trustManagers.add( (X509TrustManager) tm );
					}
				}
			}
		}

		// Create composite trust manager
		X509TrustManager compositeTm = new CompositeX509TrustManager( trustManagers );

		// Load client certificate (identity material) if provided
		KeyManager[] keyManagers = null;
		if ( clientCertPath != null && Files.exists( clientCertPath ) ) {
			KeyStore clientKs = loadKeyStore( clientCertPath, clientCertPassword );
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			kmf.init( clientKs, clientCertPassword != null ? clientCertPassword : new char[0] );
			keyManagers = kmf.getKeyManagers();
		}

		// Create and initialize SSL context
		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( keyManagers, new TrustManager[] { compositeTm }, null );
		return sslContext;
	}

	/**
	 * Creates an SSLContext that trusts all certificates (UNSAFE - for development/testing only).
	 * Equivalent to curl -k or sslVerify="false".
	 */
	public static SSLContext createUnsafeSSLContext() throws GeneralSecurityException, IOException {
		return createUnsafeSSLContext( null, null );
	}

	/**
	 * Creates an SSLContext that trusts all certificates with optional client cert.
	 */
	public static SSLContext createUnsafeSSLContext( Path clientCertPath, char[] clientCertPassword ) throws GeneralSecurityException, IOException {
		X509TrustManager unsafeTm = new X509TrustManager() {
			@Override
			public void checkClientTrusted( X509Certificate[] chain, String authType ) {
				// Trust all
			}

			@Override
			public void checkServerTrusted( X509Certificate[] chain, String authType ) {
				// Trust all
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
		};

		KeyManager[] keyManagers = null;
		if ( clientCertPath != null && Files.exists( clientCertPath ) ) {
			KeyStore clientKs = loadKeyStore( clientCertPath, clientCertPassword );
			KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
			kmf.init( clientKs, clientCertPassword != null ? clientCertPassword : new char[0] );
			keyManagers = kmf.getKeyManagers();
		}

		SSLContext sslContext = SSLContext.getInstance( "TLS" );
		sslContext.init( keyManagers, new TrustManager[] { unsafeTm }, null );
		return sslContext;
	}

	/**
	 * Loads a KeyStore from a file, auto-detecting the type (JKS or PKCS12).
	 */
	public static KeyStore loadKeyStore( Path path, char[] password ) throws GeneralSecurityException {
		// Try PKCS12 first (more common for client certs), then JKS
		KeyStoreException lastException = null;
		for ( String type : new String[] { "PKCS12", "JKS" } ) {
			try ( InputStream is = Files.newInputStream( path ) ) {
				KeyStore ks = KeyStore.getInstance( type );
				ks.load( is, password );
				return ks;
			}
			catch ( KeyStoreException | IOException e ) {
				lastException = new KeyStoreException( "Failed to load keystore as " + type + ": " + e.getMessage(), e );
			}
		}
		throw lastException;
	}

	/**
	 * Configuration for an additional trust store.
	 */
	public static class TrustStoreConfig {
		public final Path path;
		public final char[] password;

		public TrustStoreConfig( Path path, char[] password ) {
			this.path = path;
			this.password = password;
		}

		public TrustStoreConfig( Path path ) {
			this( path, "changeit".toCharArray() );
		}
	}

	/**
	 * A TrustManager that delegates to multiple underlying X509TrustManagers.
	 * If any trust manager accepts the certificate chain, the chain is trusted.
	 */
	private static class CompositeX509TrustManager implements X509TrustManager {
		private final List<X509TrustManager> trustManagers;

		public CompositeX509TrustManager( List<X509TrustManager> trustManagers ) {
			this.trustManagers = new ArrayList<>( trustManagers );
		}

		@Override
		public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
			CertificateException lastException = null;
			for ( X509TrustManager tm : trustManagers ) {
				try {
					tm.checkClientTrusted( chain, authType );
					return; // If any trust manager accepts, we're done
				}
				catch ( CertificateException e ) {
					lastException = e;
				}
			}
			if ( lastException != null ) {
				throw lastException;
			}
			throw new CertificateException( "No trust managers available" );
		}

		@Override
		public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException {
			CertificateException lastException = null;
			for ( X509TrustManager tm : trustManagers ) {
				try {
					tm.checkServerTrusted( chain, authType );
					return; // If any trust manager accepts, we're done
				}
				catch ( CertificateException e ) {
					lastException = e;
				}
			}
			if ( lastException != null ) {
				throw lastException;
			}
			throw new CertificateException( "No trust managers available" );
		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {
			List<X509Certificate> issuers = new ArrayList<>();
			for ( X509TrustManager tm : trustManagers ) {
				X509Certificate[] accepted = tm.getAcceptedIssuers();
				if ( accepted != null ) {
					issuers.addAll( Arrays.asList( accepted ) );
				}
			}
			return issuers.toArray( new X509Certificate[0] );
		}
	}
}
