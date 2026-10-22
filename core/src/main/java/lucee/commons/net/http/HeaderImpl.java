package lucee.commons.net.http;

/**
 * a header of a request or response
 */
public final class HeaderImpl implements Header {

	private final String name;
	private final String value;

	public HeaderImpl(String name, String value) {
		this.name = name;
		this.value = value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name + ": " + value;
	}
}
