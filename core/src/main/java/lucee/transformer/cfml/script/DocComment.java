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
package lucee.transformer.cfml.script;

import java.util.HashMap;
import java.util.Map;

import lucee.commons.lang.StringUtil;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.expression.literal.Literal;

public class DocComment {

	private StringBuilder tmpHint = new StringBuilder();
	private String hint;
	// private List<DocCommentParam> params=new ArrayList<DocComment.DocCommentParam>();
	Map<String, Attribute> params = new HashMap<String, Attribute>();

	public void addHint(char c) {
		tmpHint.append(c);
	}

	public void addParam(Attribute attribute) {
		params.put(attribute.getName(), attribute);
	}

	/**
	 * @return the hint
	 */
	public String getHint() {
		if (hint == null) {
			Attribute attr = params.remove("hint");
			if (attr != null) {
				Literal lit = (Literal) attr.getValue();
				hint = lit.getString().trim();
			}
			else {
				hint = StringUtil.unwrap(tmpHint.toString());
			}
		}
		return hint;
	}

	public Attribute getHintAsAttribute(Factory factory) {
		return new Attribute(true, "hint", factory.createLitString(getHint()), "string");
	}

	/**
	 * @return the params
	 */
	public Map<String, Attribute> getParams() {
		return params;
	}
}