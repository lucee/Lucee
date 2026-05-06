package lucee.runtime.component;

/** Immutable source-string wrapper for non-foldable cfproperty expression-form defaults. */
public final class ExpressionDefault {

	private final String source;

	public ExpressionDefault(String source) {
		this.source = source == null ? "" : source;
	}

	public String getSource() {
		return source;
	}

	@Override
	public String toString() {
		return source;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ExpressionDefault)) return false;
		return source.equals(((ExpressionDefault) obj).source);
	}

	@Override
	public int hashCode() {
		return source.hashCode();
	}
}
