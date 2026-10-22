package lucee.commons.net.header;

import lucee.commons.net.http.Header;

public interface HeadersCollection {
	public Header[] getHeaders(String name);
}
