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
package lucee.transformer.bytecode.statement;

import java.util.ArrayList;
import java.util.Iterator;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.visitor.ConditionVisitor;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;

public final class Condition extends StatementBaseNoFinal implements HasBodies {

	private ArrayList<Pair> ifs = new ArrayList<Pair>();
	private Pair _else;

	/**
	 * Constructor of the class
	 * 
	 * @param condition
	 * @param body
	 * @param line
	 */
	public Condition(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param condition
	 * @param body
	 * @param line
	 */
	public Condition(Factory f, ExprBoolean condition, Statement body, Position start, Position end) {
		super(condition.getFactory(), start, end);
		addElseIf(condition, body, start, end);

		body.setParent(this);
	}

	public Condition(boolean b, Statement body, Position start, Position end) {
		this(body.getFactory(), body.getFactory().createLitBoolean(b), body, start, end);
	}

	/**
	 * adds an else statement
	 * 
	 * @param condition
	 * @param body
	 */
	public Pair addElseIf(ExprBoolean condition, Statement body, Position start, Position end) {
		Pair pair;
		ifs.add(pair = new Pair(condition, body, start, end));
		body.setParent(this);
		return pair;
	}

	/**
	 * sets the else Block of the condition
	 * 
	 * @param body
	 */
	public Pair setElse(Statement body, Position start, Position end) {
		_else = new Pair(null, body, start, end);
		body.setParent(this);
		return _else;
	}

	public final class Pair {
		private ExprBoolean condition;
		private Statement body;
		private Position start;
		public Position end;

		public Pair(ExprBoolean condition, Statement body, Position start, Position end) {
			this.condition = condition;
			this.body = body;
			this.start = start;
			this.end = end;
		}
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		Iterator<Pair> it = ifs.iterator();
		Pair pair;
		ConditionVisitor cv = new ConditionVisitor();
		cv.visitBefore();
		// ifs
		while (it.hasNext()) {
			pair = it.next();
			ExpressionUtil.visitLine(bc, pair.start);
			cv.visitWhenBeforeExpr();
			pair.condition.writeOut(bc, Expression.MODE_VALUE);
			cv.visitWhenAfterExprBeforeBody(bc);
			pair.body.writeOut(bc);
			cv.visitWhenAfterBody(bc);
			if (pair.end != null) ExpressionUtil.visitLine(bc, pair.end);
		}
		// else
		if (_else != null && _else.body != null) {
			cv.visitOtherviseBeforeBody();
			_else.body.writeOut(bc);
			cv.visitOtherviseAfterBody();
		}

		cv.visitAfter(bc);
	}

	/**
	 * @see lucee.transformer.bytecode.statement.HasBodies#getBodies()
	 */
	@Override
	public Body[] getBodies() {
		int len = ifs.size(), count = 0;
		if (_else != null) len++;
		Body[] bodies = new Body[len];
		Pair p;
		Iterator<Pair> it = ifs.iterator();
		while (it.hasNext()) {
			p = it.next();
			bodies[count++] = (Body) p.body;
		}
		if (_else != null) bodies[count++] = (Body) _else.body;

		return bodies;
	}
}