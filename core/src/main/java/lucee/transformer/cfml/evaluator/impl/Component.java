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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageSource;
import lucee.runtime.config.Constants;
import lucee.runtime.type.util.ComponentUtil;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagCIObject;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * Prueft den Kontext des Tag break. Das Tag <code>break</code> darf nur innerhalb des Tag
 * <code>loop, while, foreach</code> liegen.
 */
public class Component extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag tlt) throws EvaluatorException {

		TagCIObject tc = (TagCIObject) tag;

		Statement pPage = tag.getParent();
		// String className=tag.getTagLibTag().getTagClassName();
		Page page;

		// move components inside script to root
		if (pPage instanceof Page) {
			page = (Page) pPage;
		}
		else {
			// is in script
			Tag p = ASMUtil.getParentTag(tag);
			if ((pPage = p.getParent()) instanceof Page && p.getTagLibTag().getName()
					.equalsIgnoreCase(((Page) pPage).getSourceCode().getDialect() == CFMLEngine.DIALECT_CFML ? Constants.CFML_SCRIPT_TAG_NAME : Constants.LUCEE_SCRIPT_TAG_NAME)) { // chnaged
				// order
				// of
				// the
				// condition,
				// not
				// sure
				// if
				// this
				// is
				// ok
				page = (Page) pPage;
				// move imports from script to component body
				List<Statement> children = p.getBody().getStatements();
				Iterator<Statement> it = children.iterator();
				Statement stat;
				Tag t;
				while (it.hasNext()) {
					stat = it.next();
					if (!(stat instanceof Tag)) continue;
					t = (Tag) stat;
					if (t.getTagLibTag().getName().equals("import")) {
						tag.getBody().addStatement(t);
					}
				}

				// move to page
				ASMUtil.move(tag, page);

				// if(!inline)ASMUtil.replace(p, tag, false);
			}
			else throw new EvaluatorException("Wrong Context, tag [" + tlt.getFullName() + "] can't be inside other tags, tag is inside tag [" + p.getFullname() + "]");
		}

		// Page page=(Page) pPage;
		Boolean insideCITemplate = isInsideCITemplate(page);
		boolean main = isMainComponent(page, tc);

		// is a full grown component or an inline component
		if (insideCITemplate == Boolean.FALSE) {
			throw new EvaluatorException("Wrong Context, [" + tlt.getFullName() + "] tag must be inside a file with the extension [" + Constants.getCFMLComponentExtension()
					+ "] or [" + Constants.getLuceeComponentExtension() + "]");
		}

		boolean isComponent = tlt.getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Component");
		/*
		 * boolean isInterface="lucee.runtime.tag.Interface".equals(tlt.getTagClassName()); if(main) {
		 * if(isComponent) page.setIsComponent(true); else if(isInterface) page.setIsInterface(true); }
		 */
		tc.setMain(main);

		// Attributes

		// Name
		String name = null;
		if (!main) {
			Map<String, Attribute> attrs = tag.getAttributes();
			if (attrs.size() > 0) {
				Attribute first = attrs.values().iterator().next();
				if (first.isDefaultValue()) {
					name = first.getName();
				}
			}

			if (name == null) {
				Attribute attr = tag.getAttribute("name");
				if (attr != null) {
					Expression expr = tag.getFactory().toExprString(attr.getValue());
					if (!(expr instanceof LitString)) throw new EvaluatorException("Name of the component [" + tlt.getFullName() + "], must be a literal string value");
					name = ((LitString) expr).getString();
				}
				else throw new EvaluatorException("Missing name of the component [" + tlt.getFullName() + "]");
			}
			tc.setName(name);
		}

		// output
		// "output=true" is handled in "lucee.transformer.cfml.attributes.impl.Function"
		Attribute attr = tag.getAttribute("output");
		if (attr != null) {
			Expression expr = tag.getFactory().toExprBoolean(attr.getValue());
			if (!(expr instanceof LitBoolean))
				throw new EvaluatorException("Attribute [output] of the tag [" + tlt.getFullName() + "], must contain a static boolean value (true or false, yes or no)");
			// boolean output = ((LitBoolean)expr).getBooleanValue();
			// if(!output) ASMUtil.removeLiterlChildren(tag, true);
		}

		// extends
		attr = tag.getAttribute("extends");
		if (attr != null) {
			Expression expr = tag.getFactory().toExprString(attr.getValue());
			if (!(expr instanceof LitString)) throw new EvaluatorException("Attribute [extends] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
		}

		// implements
		if (isComponent) {
			attr = tag.getAttribute("implements");
			if (attr != null) {
				Expression expr = tag.getFactory().toExprString(attr.getValue());
				if (!(expr instanceof LitString)) throw new EvaluatorException("Attribute [implements] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
			}
		}
		// modifier
		if (isComponent) {
			attr = tag.getAttribute("modifier");
			if (attr != null) {
				Expression expr = tag.getFactory().toExprString(attr.getValue());
				if (!(expr instanceof LitString)) throw new EvaluatorException("Attribute [modifier] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
				LitString ls = (LitString) expr;
				int mod = ComponentUtil.toModifier(ls.getString(), lucee.runtime.Component.MODIFIER_NONE, -1);

				if (mod == -1) throw new EvaluatorException(
						"Value [" + ls.getString() + "] from attribute [modifier] of the tag [" + tlt.getFullName() + "] is invalid, valid values are [none, abstract, final]");
			}
		}
	}

	private boolean isMainComponent(Page page, TagCIObject comp) {
		// first is main
		Iterator<Statement> it = page.getStatements().iterator();
		while (it.hasNext()) {
			Statement s = it.next();
			if (s instanceof TagCIObject) return s == comp;
		}
		return false;
	}

	/**
	 * is the template ending with a component extension?
	 * 
	 * @param page
	 * @return return true if so false otherwse and null if the code is not depending on a template
	 */
	private Boolean isInsideCITemplate(Page page) {
		SourceCode sc = page.getSourceCode();
		if (!(sc instanceof PageSourceCode)) return null;
		PageSource psc = ((PageSourceCode) sc).getPageSource();
		String src = psc.getDisplayPath();
		return Constants.isComponentExtension(ResourceUtil.getExtension(src, ""));
		// int pos=src.lastIndexOf(".");
		// return pos!=-1 && pos<src.length() && src.substring(pos+1).equals(Constants.COMPONENT_EXTENSION);
	}
}
