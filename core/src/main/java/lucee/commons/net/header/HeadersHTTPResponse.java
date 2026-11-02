package lucee.commons.net.header;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import lucee.commons.net.http.HTTPResponse;

public final class HeadersHTTPResponse implements HeadersCollection {

	private HTTPResponse response;

	public HeadersHTTPResponse(HTTPResponse response) {
		this.response = response;
	}

	@Override
	public Header[] getHeaders(String name) {
		List<Header> list = new ArrayList<>();
		for (lucee.commons.net.http.Header header: response.getAllHeaders()) {
			if (name.equalsIgnoreCase(header.getName())) {
				list.add(new BasicHeader(header.getName(), header.getValue()));
			}
		}
		return list.toArray(new Header[list.size()]);
	}
}