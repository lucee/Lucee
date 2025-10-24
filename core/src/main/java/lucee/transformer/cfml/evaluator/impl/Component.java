/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.config.Constants;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.Page;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.tag.TagCIObject;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.statement.Statement;
import lucee.transformer.statement.tag.Attribute;
import lucee.transformer.statement.tag.Tag;
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
		boolean inline = tag instanceof TagComponent && ((TagComponent) tag).isInline();

		Statement pPage = tag.getParent();
		Page page;
		boolean detached = false;
		if (inline) {
			try {
				page = ASMUtil.getAncestorPage(null, tag);
				tc.initDetachedComponent(page);
				detached = true;
			}
			catch (TransformerException te) {
				EvaluatorException ee = new EvaluatorException(te.getMessage());
				ExceptionUtil.initCauseEL(ee, te);
				throw ee;
			}
		}
		else {
			// move components inside script to root
			if (pPage instanceof Page) {
				page = (Page) pPage;
			}
			else {
				// is in script
				Tag p = ASMUtil.getParentTag(tag);
				if (p != null && (pPage = p.getParent()) instanceof Page && p.getTagLibTag().getName().equalsIgnoreCase(Constants.CFML_SCRIPT_TAG_NAME)) { // chnaged

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
				else throw new EvaluatorException(
						"Wrong Context, tag [" + tlt.getFullName() + "] can't be inside other tags" + ((p != null) ? ", tag is inside tag [" + p.getFullname() + "]" : ""));
			}
		}

		boolean main = isMainComponent(page, tc);

		// is a full grown component or an inline component
		if (!inline && isInsideCITemplate(page) == Boolean.FALSE) {
			throw new EvaluatorException("Wrong Context, [" + tlt.getFullName() + "] tag must be inside a file with the extension [" + Constants.getCFMLComponentExtension()
					+ "], only inline components are allowed outside ");
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
					if (!detached) tc.initDetachedComponent(page);

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
		attr = tag.getAttribute("implements");
		if (attr != null) {
			Expression expr = tag.getFactory().toExprString(attr.getValue());
			if (!(expr instanceof LitString)) throw new EvaluatorException("Attribute [implements] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
			LitString ls = (LitString) expr;
			String raw = ls.getString();
			if (!StringUtil.isEmpty(raw, true)) {
				String[] arr = ListUtil.listToStringArray(raw.trim(), ',');
				Set<String> implementsJava = null;
				Set<String> implementsCFML = null;
				for (String str: arr) {
					if (StringUtil.isEmpty(str, true)) continue;
					str = str.trim();
					// java
					if (str.startsWith("java:") || str.startsWith("java :") || str.startsWith("java	:")) {
						int index = str.indexOf(':');
						str = str.substring(index + 1).trim();
						if (implementsJava == null) implementsJava = new HashSet<>();
						implementsJava.add(str);
						continue;
					}

					if (str.startsWith("cfml:") || str.startsWith("cfml :") || str.startsWith("cfml	:")) {
						int index = str.indexOf(':');
						str = str.substring(index + 1).trim();
						if (implementsCFML == null) implementsCFML = new HashSet<>();
						implementsCFML.add(str);
						continue;
					}

					// no explicit type we take as CFML
					if (implementsCFML == null) implementsCFML = new HashSet<>();
					implementsCFML.add(str);
				}
				if (implementsCFML != null || implementsJava != null) {
					// no cfml (anymore)
					if (implementsCFML == null || implementsCFML.size() == 0) {
						tag.removeAttribute("implements");
					}
					// update CFML
					else {
						tag.addAttribute(new Attribute(false, "implements", tag.getFactory().createLitString(toString(implementsCFML)), "string"));
					}

					// java
					if (implementsJava != null && implementsJava.size() > 0) {
						Attribute jattr = tag.getAttribute("implementsJava");
						if (jattr != null) {
							Expression jexpr = tag.getFactory().toExprString(jattr.getValue());
							if (!(jexpr instanceof LitString))
								throw new EvaluatorException("Attribute [implementsJava] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
							LitString jls = (LitString) jexpr;
							String tmp = jls.getString();
							if (!StringUtil.isEmpty(tmp, true)) {
								for (String s: ListUtil.listToStringArray(tmp.trim(), ',')) {
									if (!StringUtil.isEmpty(s, true)) {
										implementsJava.add(s.trim());
									}
								}
							}
						}
						tag.addAttribute(new Attribute(false, "implementsJava", tag.getFactory().createLitString(toString(implementsJava)), "string"));
					}
				}
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

		// initmethod
		if (isComponent) {
			attr = tag.getAttribute("initmethod");
			if (attr != null) {
				Expression expr = tag.getFactory().toExprString(attr.getValue());
				if (!(expr instanceof LitString))
					throw new EvaluatorException("Attribute [initmethod] of the tag [" + tlt.getFullName() + "], must contain a literal string value");
			}
		}
	}

	private String toString(Set<String> set) {
		StringBuilder sb = new StringBuilder();
		if (set != null) {
			for (String s: set) {
				if (sb.length() > 0) sb.append(',');
				sb.append(s.trim());
			}
		}
		return sb.toString();
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
