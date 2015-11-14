package lucee.commons.lang.types;

public class RefStringImpl implements RefString {

	private String value;

	public RefStringImpl(String value) {
		this.value=value;
	}

	@Override
	public void setValue(String value) {
		this.value=value;
	}

	@Override
	public String getValue() {
		return value;
	}

}
