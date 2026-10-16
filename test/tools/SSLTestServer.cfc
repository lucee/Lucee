/**
 * Self-contained HTTPS test server for SSL integration tests.
 *
 * Generates a self-signed cert via BouncyCastle, starts an SSLServerSocket
 * on a random port, and exports a JKS truststore for tests to use.
 *
 * Usage:
 *   ssl = new test.tools.SSLTestServer();
 *   ssl.start();
 *   // ... cfhttp url="https://localhost:#ssl.getPort()#/" trustStore=ssl.getTruststorePath() ...
 *   ssl.stop();
 */
component javaSettings='{ "maven": ["org.bouncycastle:bcpkix-jdk18on:1.78.1"] }' {

	import java.io.FileOutputStream;
	import java.math.BigInteger;
	import java.security.KeyPairGenerator;
	import java.security.KeyStore;
	import java.security.Security;
	import java.time.Instant;
	import java.util.Date;
	import javax.net.ssl.KeyManagerFactory;
	import javax.net.ssl.SSLContext;
	import org.bouncycastle.asn1.x500.X500Name;
	import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
	import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
	import org.bouncycastle.jce.provider.BouncyCastleProvider;
	import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

	variables.password       = "changeit";
	variables.serverSocket   = nullValue();
	variables.port           = 0;
	variables.truststorePath = "";
	variables.threadName     = "";

	function start() {
		// Register BC provider if not already present
		if ( isNull( Security::getProvider( "BC" ) ) ) {
			Security::addProvider( new java:org.bouncycastle.jce.provider.BouncyCastleProvider() );
		}

		// 1. Generate RSA keypair
		var keyGen = KeyPairGenerator::getInstance( "RSA" );
		keyGen.initialize( 2048 );
		var kp = keyGen.generateKeyPair();

		// 2. Build self-signed cert
		var issuer      = new java:org.bouncycastle.asn1.x500.X500Name( "CN=localhost" );
		var notBefore   = new java:java.util.Date();
		var notAfter    = Date::from( Instant::now().plusSeconds( javaCast( "long", 365 * 86400 ) ) );
		var certBuilder = new java:org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder(
			issuer,
			BigInteger::valueOf( javaCast( "long", getTickCount() ) ),
			notBefore,
			notAfter,
			issuer,
			kp.getPublic()
		);
		var signer = new java:org.bouncycastle.operator.jcajce.JcaContentSignerBuilder( "SHA256WithRSA" ).build( kp.getPrivate() );
		var cert   = new java:org.bouncycastle.cert.jcajce.JcaX509CertificateConverter().getCertificate( certBuilder.build( signer ) );

		// 3. Server keystore — holds private key + cert chain
		var serverKs = KeyStore::getInstance( "JKS" );
		serverKs.load( nullValue(), variables.password.toCharArray() );
		serverKs.setKeyEntry( "test", kp.getPrivate(), variables.password.toCharArray(), [ cert ] );

		// 4. Trust store — cert only, exported for tests
		var trustKs = KeyStore::getInstance( "JKS" );
		trustKs.load( nullValue(), variables.password.toCharArray() );
		trustKs.setCertificateEntry( "test", cert );
		variables.truststorePath = getTempDirectory() & "ssl-test-#createUUID()#.jks";
		var fos = new java:java.io.FileOutputStream( variables.truststorePath );
		try {
			trustKs.store( fos, variables.password.toCharArray() );
		} finally {
			fos.close();
		}

		// 5. SSLContext from server keystore
		var kmf = KeyManagerFactory::getInstance( KeyManagerFactory::getDefaultAlgorithm() );
		kmf.init( serverKs, variables.password.toCharArray() );
		var sslCtx = SSLContext::getInstance( "TLS" );
		sslCtx.init( kmf.getKeyManagers(), nullValue(), nullValue() );

		// 6. SSLServerSocket on a random port
		variables.serverSocket = sslCtx.getServerSocketFactory().createServerSocket( 0 );
		variables.port         = variables.serverSocket.getLocalPort();

		// 7. Accept loop in background thread
		variables.threadName = "ssl-test-server-#createUUID()#";
		thread action="run" name=variables.threadName serverSocket=variables.serverSocket {
			try {
				while ( true ) {
					var conn = attributes.serverSocket.accept();
					try {
						var os       = conn.getOutputStream();
						var response = "HTTP/1.1 200 OK#chr(13)##chr(10)#Content-Length: 2#chr(13)##chr(10)#Connection: close#chr(13)##chr(10)##chr(13)##chr(10)#OK";
						os.write( response.getBytes( "UTF-8" ) );
						os.flush();
					} catch ( any e ) {
						// SSL handshake failures are expected when testing without a trusted cert
						var isExpectedError = findNoCase( "SSLHandshakeException", e.type )
							|| findNoCase( "SSLException", e.type )
							|| findNoCase( "SocketException", e.type );
						if ( !isExpectedError ) systemOutput( "SSLTestServer connection error: #e.stacktrace#", true );
					} finally {
						conn.close();
					}
				}
			} catch ( any e ) {
				// Socket closed is expected when stop() is called
				if ( !findNoCase( "SocketException", e.type ) ) systemOutput( "SSLTestServer accept loop exit: #e.stacktrace#", true );
			}
		}

		return this;
	}

	function getPort() {
		return variables.port;
	}

	function getTruststorePath() {
		return variables.truststorePath;
	}

	function getTruststorePassword() {
		return variables.password;
	}

	function isRunning() {
		return !isNull( variables.serverSocket )
			&& variables.serverSocket.isBound()
			&& !variables.serverSocket.isClosed();
	}

	function stop() {
		if ( !isNull( variables.serverSocket ) ) {
			variables.serverSocket.close();
		}
		if ( len( variables.truststorePath ) && fileExists( variables.truststorePath ) ) {
			fileDelete( variables.truststorePath );
		}
	}

}
