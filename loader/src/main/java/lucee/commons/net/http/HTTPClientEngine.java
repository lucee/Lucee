package lucee.commons.net.http;

import java.io.IOException;

import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;

/**
 * Sends HTTP requests for Lucee (the tag cfhttp, downloads of extensions, AI engines and so on). Lucee
 * ships with a simple built-in implementation, an extension can replace it with a full featured one
 * by declaring the implementation class in its manifest with the attribute "http-engine". An
 * implementation needs a public constructor without arguments, {@link #init(Config)} is called once
 * before any other method.
 */
public interface HTTPClientEngine {

	/**
	 * feature: authentication with NTLM
	 */
	public static final String FEATURE_NTLM = "ntlm";

	/**
	 * feature: preemptive basic authentication
	 */
	public static final String FEATURE_PREEMPTIVE_AUTH = "preemptive-auth";

	/**
	 * feature: setting the headers the JDK does not allow to set (Host, Connection, Content-Length and so
	 * on)
	 */
	public static final String FEATURE_RESTRICTED_HEADERS = "restricted-headers";

	/**
	 * feature: a trust store, a client certificate or turning off the certificate verification for a
	 * single request
	 */
	public static final String FEATURE_TLS_OPTIONS = "tls-options";

	/**
	 * feature: connection pool settings and statistics
	 */
	public static final String FEATURE_POOL = "pool";

	public void init(Config config) throws PageException;

	/**
	 * @return name of the engine, for example "jdk" or "httpclient5"
	 */
	public String getName();

	/**
	 * @param feature one of the FEATURE_ constants
	 * @return true if the engine supports the feature
	 */
	public boolean supports(String feature);

	/**
	 * sends the request, the caller has to close the response
	 * 
	 * @throws IOException if the request fails
	 */
	public HTTPResponse execute(HTTPRequest request) throws IOException;

	/**
	 * closes the connections of the pool, for example after the certificates changed
	 */
	public void releaseConnections();

	/**
	 * closes connections that were idle for too long, called regularly
	 */
	public void closeIdleConnections();

	/**
	 * closes everything, called when Lucee shuts down
	 */
	public void close();

	/**
	 * @return information about the engine and its connection pool
	 */
	public Struct getMetaData() throws IOException;

	/**
	 * @return statistics of the connection pool with the keys leased, available, pending and max (all
	 *         numbers) and routes (a struct with an entry for every target host with leased, available,
	 *         pending and max). Empty values when the engine has no pool.
	 */
	public Struct getPoolStats();
}
