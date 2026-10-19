/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.transformer.statement.tag;

import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.util.SourceCode;

public final class Attribute {

	final String nameOC;
	final String nameLC;
	final Expression value;
	private final String type;
	private final boolean dynamicType;
	private boolean defaultAttribute;
	private String setterName;
	private final boolean isDefaultValue;
	private String rawValue;

	public Attribute(boolean dynamicType, String name, Expression value, String type) {
		this(dynamicType, name, value, type, false);
	}

	public Attribute(boolean dynamicType, String name, Expression value, String type, boolean isDefaultValue) {
		this.dynamicType = dynamicType;
		this.nameOC = name;
		this.nameLC = name.toLowerCase();
		this.value = value;
		this.type = type;
		this.isDefaultValue = isDefaultValue;
	}

	/**
	 * Raw source text of a non-literal attribute value, without surrounding quotes or hashes (e.g.
	 * <code>now()</code> for both <code>default="#now()#"</code> and <code>default=#now()#</code>).
	 * Null for literal values and for synthesised or default-injected attributes. Only used for
	 * cfproperty expression defaults (LDEV-6303).
	 */
	public String getRawValue() {
		return rawValue;
	}

	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}

	/**
	 * Returns the source text of a non-literal attribute value parsed between the positions start and
	 * end, without surrounding quotes and without surrounding hashes (when the value is a single
	 * <code>#expression#</code>). Returns null for literals or invalid positions.
	 */
	public static String sliceSource(SourceCode srcCode, int start, int end, Expression value) {
		if (srcCode == null || value == null || value instanceof Literal) return null;
		if (start < 0 || end > srcCode.length() || end <= start) return null;
		String str = srcCode.substring(start, end - start).trim();
		if (str.length() >= 2 && (str.charAt(0) == '"' || str.charAt(0) == '\'')) str = unwrap(str, str.charAt(0));
		if (str.length() >= 2 && str.charAt(0) == '#') str = unwrap(str, '#');
		str = str.trim();
		return str.isEmpty() ? null : str;
	}

	// removes the surrounding char c, when the content does not contain an unescaped (not doubled) c
	private static String unwrap(String str, char c) {
		int len = str.length();
		if (str.charAt(len - 1) != c) return str;
		String inner = str.substring(1, len - 1);
		String doubled = new String(new char[] { c, c });
		if (inner.replace(doubled, "").indexOf(c) != -1) return str;
		return inner;
	}

	public boolean isDefaultValue() {
		return isDefaultValue;
	}

	public boolean isDefaultAttribute() {
		return defaultAttribute;
	}

	public void setDefaultAttribute(boolean defaultAttribute) {
		this.defaultAttribute = defaultAttribute;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return nameLC;
	}

	// TODO make this method obsolete
	public String getNameOC() {
		return nameOC;
	}

	/**
	 * @return the value
	 */
	public Expression getValue() {
		return value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the dynamicType
	 */
	public boolean isDynamicType() {
		return dynamicType;
	}

	@Override
	public String toString() {
		return "name:" + this.nameLC + ";value:" + this.value + ";type:" + this.type + ";dynamicType:" + this.dynamicType + ";setterName:" + this.setterName;
	}
}