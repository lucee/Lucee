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

	/** Raw source text of the attribute value, post-unwrap (i.e. <code>now()</code> for both
	 *  <code>default="#now()#"</code> and <code>default=#now()#</code>). Null for synthesised
	 *  or default-injected attributes. Public so PageImpl bytecode emission can read it
	 *  cross-package — only loaded for cfproperty expression-form defaults today. */
	public String getRawValue() {
		return rawValue;
	}

	public void setRawValue(String rawValue) {
		this.rawValue = rawValue;
	}

	/** Capture the post-unwrap source for an Expression by slicing from the parse-time SourceCode.
	 *  Uses Expression.getStart()/getEnd() — these point at the inner expression already, so quotes
	 *  and hash markers fall outside the slice. The peek-back handles json()'s post-delimiter
	 *  position-recording for { and [ literals. Returns null when positions are unavailable. */
	public static String sliceSource(SourceCode srcCode, Expression value) {
		if (value == null || value.getStart() == null || value.getEnd() == null) return null;
		int start = value.getStart().pos;
		int end = value.getEnd().pos;
		if (start > 0) {
			String peek = srcCode.subCFMLString(start - 1, 1).toString();
			if (peek.length() == 1 && (peek.charAt(0) == '{' || peek.charAt(0) == '[')) start--;
		}
		if (end <= start) return null;
		return srcCode.subCFMLString(start, end - start).toString();
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