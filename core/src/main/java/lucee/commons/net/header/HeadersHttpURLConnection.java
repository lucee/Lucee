package lucee.commons.net.header;

import java.net.HttpURLConnection;

import lucee.commons.net.http.Header;
import lucee.commons.net.http.HeaderImpl;

import lucee.commons.lang.StringUtil;

public final class HeadersHttpURLConnection implements HeadersCollection {

	private HttpURLConnection conn;

	public HeadersHttpURLConnection(HttpURLConnection conn) {
		this.conn = conn;
	}

	@Override
	public Header[] getHeaders(String name) {
		String val = conn.getHeaderField(name);
		if (!StringUtil.isEmpty(val, true)) {
			return new Header[] { new HeaderImpl(name, val) };
		}
		return null;
	}
}