/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import lucee.commons.net.http.HTTPResponse;
import lucee.commons.net.http.Header;

public interface HTTPUtil {

	/**
	 * Field <code>ACTION_POST</code>
	 */
	public static final short ACTION_POST = 0;

	/**
	 * Field <code>ACTION_GET</code>
	 */
	public static final short ACTION_GET = 1;

	/**
	 * Field <code>STATUS_OK</code>
	 */
	public static final int STATUS_OK = 200;

	// private static final String NO_MIMETYPE="Unable to determine MIME type of file.";

	/**
	 * make a http request to given url
	 * 
	 * @param url url
	 * @param username username
	 * @param password password 
	 * @param timeout timeoute
	 * @param charset charset
	 * @param useragent user agent
	 * @param proxyserver proxy server
	 * @param proxyport proxy port
	 * @param proxyuser proxy user
	 * @param proxypassword proxy password
	 * @param headers headers
	 * @return resulting inputstream
	 * @throws IOException IO Exception
	 */
	public HTTPResponse get(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException;

	/**
	 * 
	 * @param url url
	 * @param username username
	 * @param password password
	 * @param timeout timeout
	 * @param charset charset
	 * @param useragent user agent
	 * @param proxyserver proxy server
	 * @param proxyport proxy port
	 * @param proxyuser proxy user
	 * @param proxypassword proxy password
	 * @param headers headers
	 * @param body body
	 * @return resulting inputstream
	 * @throws IOException IO Exception
	 * @deprecated use instead
	 * @see #put(URL, String, String, int, String, String, String, String, int, String, String,
	 *      Header[], Object)
	 */
	@Deprecated
	public HTTPResponse put(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers, Object body) throws IOException;

	public HTTPResponse put(URL url, String username, String password, int timeout, String mimetype, String charset, String useragent, String proxyserver, int proxyport,
			String proxyuser, String proxypassword, Header[] headers, Object body) throws IOException;

	public HTTPResponse delete(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException;

	public HTTPResponse head(URL url, String username, String password, int timeout, String charset, String useragent, String proxyserver, int proxyport, String proxyuser,
			String proxypassword, Header[] headers) throws IOException;

	/**
	 * cast a string to a url
	 * 
	 * @param strUrl url
	 * @param port port
	 * @return url from string
	 * @throws MalformedURLException Malformed URL Exception
	 * @deprecated use instead
	 * @see #toURL(String, int, boolean)
	 * 
	 */
	@Deprecated
	public URL toURL(String strUrl, int port) throws MalformedURLException;

	/**
	 * 
	 * @param strUrl url
	 * @param port port
	 * @param encodeIfNecessary encode I fNecessary
	 * @return URL generated
	 * @throws MalformedURLException Malformed URL Exception
	 */
	public URL toURL(String strUrl, int port, boolean encodeIfNecessary) throws MalformedURLException;

	/**
	 * cast a string to a url
	 * 
	 * @param strUrl string represent a url
	 * @return url from string
	 * @throws MalformedURLException Malformed URL Exception
	 */
	public URL toURL(String strUrl) throws MalformedURLException;

	public URI toURI(String strUrl) throws URISyntaxException;

	public URI toURI(String strUrl, int port) throws URISyntaxException;

	/**
	 * translate a string in the URLEncoded Format
	 * 
	 * @param str String to translate
	 * @param charset charset used for translation
	 * @return encoded String
	 * @throws UnsupportedEncodingException Unsupported Encoding Exception
	 */
	public String encode(String str, String charset) throws UnsupportedEncodingException;

	/**
	 * translate a url encoded string to a regular string
	 * 
	 * @param str encoded string
	 * @param charset charset used
	 * @return raw string
	 * @throws UnsupportedEncodingException Unsupported Encoding Exception
	 */
	public String decode(String str, String charset) throws UnsupportedEncodingException;

	/**
	 * remove port information if the port is the default port for this protocol (http=80,https=443)
	 * 
	 * @param url url
	 * @return Returns a Url.
	 */
	public URL removeUnecessaryPort(URL url);

	Header createHeader(String name, String value);
}