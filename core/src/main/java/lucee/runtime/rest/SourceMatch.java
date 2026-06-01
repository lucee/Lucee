package lucee.runtime.rest;

import lucee.runtime.type.Struct;

public final class SourceMatch {

	private final Source source;
	private final int matchedIndex;
	private final Struct variables;

	public SourceMatch(Source source, int matchedIndex, Struct variables) {
		this.source = source;
		this.matchedIndex = matchedIndex;
		this.variables = variables;
	}

	public Source getSource() {
		return source;
	}

	public int getMatchedIndex() {
		return matchedIndex;
	}

	public Struct getVariables() {
		return variables;
	}
}
