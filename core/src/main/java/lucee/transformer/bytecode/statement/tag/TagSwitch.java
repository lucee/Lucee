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
package lucee.transformer.bytecode.statement.tag;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.bytecode.visitor.ConditionVisitor;
import lucee.transformer.bytecode.visitor.DecisionIntVisitor;
import lucee.transformer.expression.Expression;

public final class TagSwitch extends TagBaseNoFinal {

	// int listFindNoCase(String list, String value, String delimiter)
	private static final Method LIST_FIND_NO_CASE = new Method("listFindForSwitch", Types.INT_VALUE, new Type[] { Types.STRING, Types.STRING, Types.STRING });

	/**
	 * Constructor of the class
	 * 
	 * @param sl
	 * @param el
	 */
	public TagSwitch(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.statement.tag.TagBase#_writeOut(org.objectweb.asm.commons.GeneratorAdapter)
	 */
	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		// expression
		int expression = adapter.newLocal(Types.STRING);
		getAttribute("expression").getValue().writeOut(bc, Expression.MODE_REF);
		adapter.storeLocal(expression);

		List statements = getBody().getStatements();
		Statement stat;
		Tag tag;

		ConditionVisitor cv = new ConditionVisitor();
		cv.visitBefore();

		// cases
		Iterator it = statements.iterator();
		Tag def = null;
		while (it.hasNext()) {
			stat = (Statement) it.next();
			if (stat instanceof Tag) {
				tag = (Tag) stat;
				if (tag.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Case")) {
					addCase(bc, cv, tag, expression);
					continue;
				}
				else if (tag.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Defaultcase")) {
					if (def != null) throw new TransformerException("multiple defaultcases are not allowed", getStart());
					def = tag;
					// setDefaultCase(bc,cv,tag);
					// break;
				}
			}
		}

		// default
		if (def != null) setDefaultCase(bc, cv, def);

		cv.visitAfter(bc);
	}

	private void setDefaultCase(BytecodeContext bc, ConditionVisitor cv, Tag tag) throws TransformerException {
		cv.visitOtherviseBeforeBody();
		BodyBase.writeOut(bc, tag.getBody());
		// tag.getBody().writeOut(bc);
		cv.visitOtherviseAfterBody();
	}

	private void addCase(BytecodeContext bc, ConditionVisitor cv, Tag tag, int expression) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		cv.visitWhenBeforeExpr();
		DecisionIntVisitor div = new DecisionIntVisitor();
		div.visitBegin();
		// List.listFindNoCase(case.value,expression,del);
		tag.getAttribute("value").getValue().writeOut(bc, Expression.MODE_REF);
		adapter.loadLocal(expression);
		Attribute attr = tag.getAttribute("delimiters");
		if (attr != null) attr.getValue().writeOut(bc, Expression.MODE_REF);
		else adapter.push(",");
		adapter.invokeStatic(Types.LIST_UTIL, LIST_FIND_NO_CASE);
		div.visitNEQ();
		adapter.push(-1);
		div.visitEnd(bc);
		cv.visitWhenAfterExprBeforeBody(bc);
		BodyBase.writeOut(bc, tag.getBody());
		// tag.getBody().writeOut(bc);
		cv.visitWhenAfterBody(bc);

		/*
		 * if(List.listFindNoCase(case.value,expression,delimiters)!=-1) { <xsl:apply-templates
		 * select="./body/*"/> }
		 */
	}

}