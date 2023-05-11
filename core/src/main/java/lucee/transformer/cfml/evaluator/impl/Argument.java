/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.transformer.cfml.evaluator.impl;

import lucee.runtime.op.Caster;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.tag.TagLibTag;

/**
 *
 * Prueft den Kontext des Tag argument. Das Tag <code>argument</code> darf nur direkt innerhalb des
 * Tag <code>function</code> liegen. Dem Tag <code>argument</code> muss als erstes im tag function
 * vorkommen
 */
public final class Argument extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag) throws EvaluatorException {
		String ns = libTag.getTagLib().getNameSpaceAndSeparator();
		String functionName = ns + "function";

		ASMUtil.isLiteralAttribute(tag, "type", ASMUtil.TYPE_STRING, false, true);
		ASMUtil.isLiteralAttribute(tag, "name", ASMUtil.TYPE_STRING, false, true);
		// ASMUtil.isLiteralAttribute(tag,"hint",ASMUtil.TYPE_STRING,false,true);
		// ASMUtil.isLiteralAttribute(tag,"displayname",ASMUtil.TYPE_STRING,false,true);

		// check if default can be converted to a literal value that match the type declration.
		checkDefaultValue(tag);

		// check attribute passby
		Attribute attrPassBy = tag.getAttribute("passby");
		if (attrPassBy != null) {
			ExprString expr = tag.getFactory().toExprString(attrPassBy.getValue());
			if (!(expr instanceof LitString)) throw new EvaluatorException("Attribute [passby] of the tag [Argument], must be a literal string");
			LitString lit = (LitString) expr;
			String passBy = lit.getString().toLowerCase().trim();
			if (!"value".equals(passBy) && !"ref".equals(passBy) && !"reference".equals(passBy))
				throw new EvaluatorException("Attribute [passby] of the tag [Argument] has an invalid value [" + passBy + "], valid values are [reference,value]");
		}

		// check if tag is direct inside function
		if (!ASMUtil.isParentTag(tag, functionName)) {
			Tag parent = ASMUtil.getParentTag(tag);

			String addText = (parent != null) ? "but tag [" + libTag.getFullName() + "] is inside tag [" + parent.getFullname() + "]"
					: "but tag [" + libTag.getFullName() + "] has no parent";

			throw new EvaluatorException("Wrong Context, tag [" + libTag.getFullName() + "] must be direct inside a [" + functionName + "] tag, " + addText);
		}
		// TODO check if there is a tag other than argument and text before

	}

	public static void checkDefaultValue(Tag tag) {
		Attribute _type = tag.getAttribute("type");
		if (_type != null) {
			ExprString typeValue = tag.getFactory().toExprString(_type.getValue());
			if (typeValue instanceof LitString) {
				String strType = ((LitString) typeValue).getString();
				Attribute _default = tag.getAttribute("default");
				if (_default != null) {
					Expression defaultValue = _default.getValue();
					if (defaultValue instanceof LitString) {
						String strDefault = ((LitString) defaultValue).getString();

						// check for boolean
						if ("boolean".equalsIgnoreCase(strType)) {
							if ("true".equalsIgnoreCase(strDefault) || "yes".equalsIgnoreCase(strDefault))
								tag.addAttribute(new Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().TRUE(), _default.getType()));
							if ("false".equalsIgnoreCase(strDefault) || "no".equalsIgnoreCase(strDefault))
								tag.addAttribute(new Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().FALSE(), _default.getType()));
						}

						// check for numbers
						if ("number".equalsIgnoreCase(strType) || "numeric".equalsIgnoreCase(strType)) {
							Double dbl = Caster.toDouble(strDefault, null);
							if (dbl != null) {
								tag.addAttribute(
										new Attribute(_default.isDynamicType(), _default.getName(), tag.getFactory().createLitNumber(dbl.doubleValue()), _default.getType()));
							}
						}
					}
				}
			}
		}
	}

}
