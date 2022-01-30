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

import lucee.commons.lang.StringUtil;
import lucee.runtime.PageSource;
import lucee.runtime.functions.system.CFFunction;
import lucee.runtime.listener.AppListenerUtil;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.StaticBody;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorSupport;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * Prueft den Kontext des Tag function. Das Attribute <code>argument</code> darf nur direkt
 * innerhalb des Tag <code>function</code> liegen. Dem Tag <code>argument</code> muss als erstes im
 * tag function vorkommen
 */
public final class Function extends EvaluatorSupport {

	@Override
	public void evaluate(Tag tag, TagLibTag libTag, FunctionLib[] flibs) throws EvaluatorException {
		// Body p=(Body) tag.getParent();
		// Statement pp = p.getParent();

		boolean isCI = true;
		try {
			isCI = ASMUtil.getAncestorPage(tag).isComponent() || ASMUtil.getAncestorPage(tag).isInterface();
		}
		catch (TransformerException e) {
		}

		Attribute attrName = tag.getAttribute("name");
		if (attrName != null) {
			Expression expr = attrName.getValue();
			PageSource ps = null;
			if (expr instanceof LitString && !isCI) {
				Page p = ASMUtil.getAncestorPage(tag, null);
				if (p != null) {
					SourceCode sc = p.getSourceCode();
					if (sc instanceof PageSourceCode) {
						PageSourceCode psc = (PageSourceCode) sc;
						ps = psc.getPageSource();
					}
				}
				checkFunctionName(((LitString) expr).getString(), flibs, ps);
			}

		}
		// attribute modifier
		boolean isStatic = false;
		{
			Attribute attrModifier = tag.getAttribute("modifier");
			if (attrModifier != null) {
				ExprString expr = tag.getFactory().toExprString(attrModifier.getValue());
				if (!(expr instanceof Literal))
					throw new EvaluatorException("Attribute modifier of the Tag Function, must be one of the following literal string values: [abstract,final,static]");
				String modifier = StringUtil.emptyIfNull(((Literal) expr).getString()).trim();
				if (!StringUtil.isEmpty(modifier) && !"abstract".equalsIgnoreCase(modifier) && !"final".equalsIgnoreCase(modifier) && !"static".equalsIgnoreCase(modifier))
					throw new EvaluatorException("Attribute modifier of the Tag Function, must be one of the following literal string values: [abstract,final,static]");

				isStatic = "static".equalsIgnoreCase(modifier);
				boolean abstr = "abstract".equalsIgnoreCase(modifier);
				if (abstr) throwIfNotEmpty(tag);
			}
		}

		// cachedWithin
		{
			Attribute attrCachedWithin = tag.getAttribute("cachedwithin");
			if (attrCachedWithin != null) {
				Expression val = attrCachedWithin.getValue();
				tag.addAttribute(new Attribute(attrCachedWithin.isDynamicType(), attrCachedWithin.getName(), ASMUtil.cachedWithinValue(val), attrCachedWithin.getType()));
			}
		}

		// Attribute localMode
		{
			Attribute attrLocalMode = tag.getAttribute("localmode");
			if (attrLocalMode != null) {
				Expression expr = attrLocalMode.getValue();
				String str = ASMUtil.toString(expr, null);
				if (!StringUtil.isEmpty(str) && AppListenerUtil.toLocalMode(str, -1) == -1)
					throw new EvaluatorException("Attribute localMode of the Tag Function, must be a literal value (modern, classic, true or false)");
				// boolean output = ((LitBoolean)expr).getBooleanValue();
				// if(!output) ASMUtil.removeLiterlChildren(tag, true);
			}
		}

		// Attribute Output
		{
			Attribute attrOutput = tag.getAttribute("output");
			if (attrOutput != null) {
				Expression expr = tag.getFactory().toExprBoolean(attrOutput.getValue());
				if (!(expr instanceof LitBoolean)) throw new EvaluatorException("Attribute output of the Tag Function, must be a literal boolean value (true or false, yes or no)");
			}
		}

		// Buffer output
		{
			Attribute attrBufferOutput = tag.getAttribute("bufferoutput");
			if (attrBufferOutput != null) {
				Expression expr = tag.getFactory().toExprBoolean(attrBufferOutput.getValue());
				if (!(expr instanceof LitBoolean))
					throw new EvaluatorException("Attribute bufferOutput of the Tag Function, must be a literal boolean value (true or false, yes or no)");
			}
		}

		// check attribute values
		Map<String, Attribute> attrs = tag.getAttributes();
		Iterator<Attribute> it = attrs.values().iterator();
		while (it.hasNext()) {
			checkAttributeValue(tag, it.next());
		}

		// add to static scope
		if (isStatic) {
			// remove that tag from parent
			ASMUtil.remove(tag);

			Body body = (Body) tag.getParent();
			StaticBody sb = Static.getStaticBody(body);
			sb.addStatement(tag);
		}
	}

	public static void checkFunctionName(String name, FunctionLib[] flibs, PageSource ps) throws EvaluatorException {
		FunctionLibFunction flf;
		for (int i = 0; i < flibs.length; i++) {
			flf = flibs[i].getFunction(name);
			if (flf != null && flf.getFunctionClassDefinition().getClazz(null) != CFFunction.class) {
				String path = null;
				if (ps != null) {
					path = ps.getDisplayPath();
					path = path.replace('\\', '/');
				}
				if (path == null || path.indexOf("/library/function/") == -1)// TODO make better
					throw new EvaluatorException("The name [" + name + "] is already used by a built in Function");
			}
		}
	}

	public static void throwIfNotEmpty(Tag tag) throws EvaluatorException {
		Body body = tag.getBody();
		List<Statement> statments = body.getStatements();
		Statement stat;
		Iterator<Statement> it = statments.iterator();
		TagLibTag tlt;

		while (it.hasNext()) {
			stat = it.next();
			if (stat instanceof Tag) {
				tlt = ((Tag) stat).getTagLibTag();
				if (!tlt.getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Argument"))
					throw new EvaluatorException("tag [" + tlt.getFullName() + "] is not allowed inside a function declaration");
			}
			/*
			 * else if(stat instanceof PrintOut) { //body.remove(stat); }
			 */
		}
	}

	private void checkAttributeValue(Tag tag, Attribute attr) throws EvaluatorException {
		if (!(attr.getValue() instanceof Literal))
			throw new EvaluatorException("Attribute [" + attr.getName() + "] of the Tag [" + tag.getFullname() + "] must be a literal/constant value");

	}
}