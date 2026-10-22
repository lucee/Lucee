package lucee.commons.net.http.jdk;

import java.util.HashMap;
import java.util.Map;

/**
 * the reason phrases of the status codes, java.net.http does not give access to the reason phrase of
 * the response
 */
final class Reasons {

	private static final Map<Integer, String> MAP = new HashMap<>();

	static {
		MAP.put(100, "Continue");
		MAP.put(101, "Switching Protocols");
		MAP.put(102, "Processing");
		MAP.put(200, "OK");
		MAP.put(201, "Created");
		MAP.put(202, "Accepted");
		MAP.put(203, "Non-Authoritative Information");
		MAP.put(204, "No Content");
		MAP.put(205, "Reset Content");
		MAP.put(206, "Partial Content");
		MAP.put(207, "Multi-Status");
		MAP.put(300, "Multiple Choices");
		MAP.put(301, "Moved Permanently");
		MAP.put(302, "Found");
		MAP.put(303, "See Other");
		MAP.put(304, "Not Modified");
		MAP.put(305, "Use Proxy");
		MAP.put(307, "Temporary Redirect");
		MAP.put(308, "Permanent Redirect");
		MAP.put(400, "Bad Request");
		MAP.put(401, "Unauthorized");
		MAP.put(402, "Payment Required");
		MAP.put(403, "Forbidden");
		MAP.put(404, "Not Found");
		MAP.put(405, "Method Not Allowed");
		MAP.put(406, "Not Acceptable");
		MAP.put(407, "Proxy Authentication Required");
		MAP.put(408, "Request Timeout");
		MAP.put(409, "Conflict");
		MAP.put(410, "Gone");
		MAP.put(411, "Length Required");
		MAP.put(412, "Precondition Failed");
		MAP.put(413, "Payload Too Large");
		MAP.put(414, "URI Too Long");
		MAP.put(415, "Unsupported Media Type");
		MAP.put(416, "Range Not Satisfiable");
		MAP.put(417, "Expectation Failed");
		MAP.put(418, "I'm a teapot");
		MAP.put(421, "Misdirected Request");
		MAP.put(422, "Unprocessable Entity");
		MAP.put(423, "Locked");
		MAP.put(424, "Failed Dependency");
		MAP.put(425, "Too Early");
		MAP.put(426, "Upgrade Required");
		MAP.put(428, "Precondition Required");
		MAP.put(429, "Too Many Requests");
		MAP.put(431, "Request Header Fields Too Large");
		MAP.put(451, "Unavailable For Legal Reasons");
		MAP.put(500, "Internal Server Error");
		MAP.put(501, "Not Implemented");
		MAP.put(502, "Bad Gateway");
		MAP.put(503, "Service Unavailable");
		MAP.put(504, "Gateway Timeout");
		MAP.put(505, "HTTP Version Not Supported");
		MAP.put(507, "Insufficient Storage");
		MAP.put(511, "Network Authentication Required");
	}

	private Reasons() {
	}

	static String get(int status) {
		String reason = MAP.get(status);
		return reason != null ? reason : "";
	}
}
