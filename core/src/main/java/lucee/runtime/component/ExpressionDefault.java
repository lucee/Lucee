package lucee.runtime.component;

import java.util.Set;

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.converter.ScriptConvertable;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.dump.Dumpable;
import lucee.runtime.dump.SimpleDumpData;

/** Immutable source-string wrapper for non-foldable cfproperty expression-form defaults.
 *  Serialises to JSON as { "expression": "<source>" } so consumers can programmatically
 *  distinguish expression-form metadata.default from literal-form (which serialises as a bare string). */
public final class ExpressionDefault implements Dumpable, ScriptConvertable {

	private static final long serialVersionUID = 1L;

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
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		DumpTable table = new DumpTable("Expression", "#ff6600", "#ffcc99", "#000000");
		table.appendRow(1, new SimpleDumpData("Expression"), new SimpleDumpData(source));
		return table;
	}

	@Override
	public String serialize() {
		return serialize(null);
	}

	@Override
	public String serialize(Set<Object> done) {
		return "{\"expression\":" + StringUtil.escapeJS(source, '"') + "}";
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
