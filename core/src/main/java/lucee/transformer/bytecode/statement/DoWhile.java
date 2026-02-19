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
package lucee.transformer.bytecode.statement;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;
import lucee.transformer.Body;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.statement.HasBody;

public final class DoWhile extends StatementBaseNoFinal implements FlowControlBreak, FlowControlContinue, HasBody {

	private ExprBoolean expr;
	private Body body;

	private Label begin = new Label();
	private Label beforeEnd = new Label();
	private Label end = new Label();
	private String label;

	/**
	 * Constructor of the class
	 * 
	 * @param expr
	 * @param body
	 * @param start
	 * @param end
	 * @param label
	 */
	public DoWhile(Expression expr, Body body, Position start, Position end, String label) {
		super(expr.getFactory(), start, end);
		this.expr = expr.getFactory().toExprBoolean(expr);
		this.body = body;
		body.setParent(this);
		this.label = label;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.visitLabel(begin);
		body.writeOut(bc);

		adapter.visitLabel(beforeEnd);

		expr.writeOut(bc, Expression.MODE_VALUE);
		adapter.ifZCmp(Opcodes.IFNE, begin);

		adapter.visitLabel(end);

	}

	@Override
	public Label getBreakLabel() {
		return end;
	}

	@Override
	public Label getContinueLabel() {
		return beforeEnd;
	}

	@Override
	public Body getBody() {
		return body;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void dump(Struct sct) {
		super.dump(sct);
		sct.setEL(KeyConstants._type, "DoWhileStatement");

		// label
		if ( label != null ) {
			sct.setEL(KeyConstants._label, label);
		}

		// body
		{
			Struct body = new StructImpl(Struct.TYPE_LINKED);
			this.body.dump(body);
			sct.setEL(KeyConstants._body, body);
		}
		// test
		{
			Struct test = new StructImpl(Struct.TYPE_LINKED);
			expr.dump(test);
			sct.setEL(KeyConstants._test, test);
		}
	}

}