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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;

public final class TagIf extends TagBaseNoFinal {

	public TagIf(Factory f, Position start, Position end) {
		super(f, start, end);
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		Label end = new Label();
		List<Statement> tmp = new ArrayList<Statement>();
		Iterator<Statement> it = getBody().getStatements().iterator();
		Tag t;
		Label endIf = writeOutElseIfStart(bc, this);
		boolean hasElse = false;
		while (it.hasNext()) {
			Statement stat = it.next();
			if (!hasElse && stat instanceof Tag) {
				t = (Tag) stat;

				if (t.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.ElseIf")) {
					__writeOut(bc, tmp);
					writeOutElseIfEnd(adapter, endIf, end);
					endIf = writeOutElseIfStart(bc, t);
					continue;
				}
				else if (t.getTagLibTag().getTagClassDefinition().isClassNameEqualTo("lucee.runtime.tag.Else")) {
					__writeOut(bc, tmp);
					ExpressionUtil.visitLine(bc, t.getStart());
					hasElse = true;
					writeOutElseIfEnd(adapter, endIf, end);
					continue;
				}
			}
			tmp.add(stat);
			// ExpressionUtil.writeOut(stat, bc);
		}
		__writeOut(bc, tmp);

		if (!hasElse) writeOutElseIfEnd(adapter, endIf, end);

		adapter.visitLabel(end);
	}

	private void __writeOut(BytecodeContext bc, List<Statement> statements) throws TransformerException {
		if (statements.size() > 0) {
			BodyBase.writeOut(bc, statements);
			statements.clear();
		}
	}

	private static Label writeOutElseIfStart(BytecodeContext bc, Tag tag) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();

		ExprBoolean cont = bc.getFactory().toExprBoolean(tag.getAttribute("condition").getValue());

		Label endIf = new Label();

		ExpressionUtil.visitLine(bc, tag.getStart());
		cont.writeOut(bc, Expression.MODE_VALUE);
		adapter.ifZCmp(Opcodes.IFEQ, endIf);
		return endIf;
	}

	private static void writeOutElseIfEnd(GeneratorAdapter adapter, Label endIf, Label end) {
		adapter.visitJumpInsn(Opcodes.GOTO, end);
		adapter.visitLabel(endIf);
	}
}