package lucee.commons.net.header;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.net.http.Header;
import lucee.commons.net.http.HeaderImpl;

import lucee.commons.net.http.HTTPResponse;

public final class HeadersHTTPResponse implements HeadersCollection {

	private HTTPResponse response;

	public HeadersHTTPResponse(HTTPResponse response) {
		this.response = response;
	}

	@Override
	public Header[] getHeaders(String name) {
		List<Header> list = new ArrayList<>();
		for (Header header: response.getAllHeaders()) {
			if (name.equals(header.getName())) {
				list.add(new HeaderImpl(header.getName(), header.getValue()));
			}
		}
		return list.toArray(new Header[list.size()]);
	}
}