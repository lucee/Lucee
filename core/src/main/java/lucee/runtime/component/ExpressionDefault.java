/**
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 **/
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

/**
 * Immutable holder for the source of a cfproperty expression default (e.g. <code>now()</code> for
 * <code>default="#now()#"</code>). The expression itself is evaluated per instance in the
 * pseudo-constructor; this object only surfaces it in the metadata (LDEV-6303).
 */
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
