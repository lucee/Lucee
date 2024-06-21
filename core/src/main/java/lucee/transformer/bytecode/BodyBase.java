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
package lucee.transformer.bytecode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.statement.PrintOut;
import lucee.transformer.bytecode.statement.StatementBaseNoFinal;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.bytecode.util.ExpressionUtil;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;

/**
 * Base Body implementation
 */
public class BodyBase extends StatementBaseNoFinal implements Body {

	private static long counter = 0;
	private LinkedList<Statement> statements = new LinkedList<Statement>();
	private Statement last = null;
	// private int count=-1;
	private final static int MAX_STATEMENTS = 206;

	/**
	 * Constructor of the class
	 */
	public BodyBase(Factory f) {
		super(f, null, null);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.Body#addStatement(lucee.transformer.bytecode.Statement)
	 */
	@Override
	public void addStatement(Statement statement) {

		if (statement instanceof PrintOut) {
			Expression expr = ((PrintOut) statement).getExpr();
			if (expr instanceof LitString && concatPrintouts(((LitString) expr).getString())) return;
		}
		statement.setParent(this);
		this.statements.add(statement);
		last = statement;
	}

	@Override
	public void addFirst(Statement statement) {
		statement.setParent(this);
		this.statements.add(0, statement);
	}

	@Override
	public void remove(Statement statement) {
		statement.setParent(null);
		this.statements.remove(statement);
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.Body#getStatements()
	 */
	@Override
	public List<Statement> getStatements() {
		return statements;
	}

	@Override
	public boolean hasStatements() {
		return !statements.isEmpty();
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.Body#moveStatmentsTo(lucee.transformer.bytecode.Body)
	 */
	@Override
	public void moveStatmentsTo(Body trg) {
		Iterator<Statement> it = statements.iterator();
		while (it.hasNext()) {
			Statement stat = it.next();
			stat.setParent(trg);
			trg.getStatements().add(stat);
		}
		statements.clear();
	}

	@Override
	public void addPrintOut(Factory f, String str, Position start, Position end) {
		if (concatPrintouts(str)) return;

		last = new PrintOut(f.createLitString(str, start, end), start, end);
		last.setParent(this);
		this.statements.add(last);
	}

	private boolean concatPrintouts(String str) {
		if (last instanceof PrintOut) {
			PrintOut po = (PrintOut) last;
			Expression expr = po.getExpr();
			if (expr instanceof LitString) {
				LitString lit = (LitString) expr;
				if (lit.getString().length() < 1024) {
					po.setExpr(lit.getFactory().createLitString(lit.getString().concat(str), lit.getStart(), lit.getEnd()));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void _writeOut(BytecodeContext bc) throws TransformerException {
		writeOut(bc, this);
	}

	public static void writeOut(final BytecodeContext bc, Body body) throws TransformerException {
		writeOut(bc, body.getStatements());
	}

	// FUTURE not used anymore, we keep it around for the moment in case we need a fallback that works
	public static void writeOutOld(final BytecodeContext bc, List<Statement> statements) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		boolean isOutsideMethod;
		GeneratorAdapter a = null;
		Method m;
		BytecodeContext _bc = bc;
		Iterator<Statement> it = statements.iterator();
		boolean split = bc.getPage().getSplitIfNecessary();

		// int lastLine=-1;
		while (it.hasNext()) {
			isOutsideMethod = bc.getMethod().getReturnType().equals(Types.VOID);
			Statement s = it.next();
			if (split && _bc.incCount() > MAX_STATEMENTS && bc.doSubFunctions() && (isOutsideMethod || !s.hasFlowController()) && s.getStart() != null) {
				if (a != null) {
					a.returnValue();
					a.endMethod();
				}
				// ExpressionUtil.visitLine(bc, s.getLine());
				String method = ASMUtil.createOverfowMethod(bc.getMethod().getName(), bc.getPage().getMethodCount());
				bc.visitLine(s.getStart());
				// ExpressionUtil.lastLine(bc);
				m = new Method(method, Types.VOID, new Type[] { Types.PAGE_CONTEXT });
				a = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, m, null, new Type[] { Types.THROWABLE }, bc.getClassWriter());

				_bc = new BytecodeContext(bc.getConstructor(), bc.getKeys(), bc, a, m);
				if (bc.getRoot() != null) _bc.setRoot(bc.getRoot());
				else _bc.setRoot(bc);

				adapter.visitVarInsn(Opcodes.ALOAD, 0);
				adapter.visitVarInsn(Opcodes.ALOAD, 1);
				adapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc.getClassName(), method, "(Llucee/runtime/PageContext;)V");
			}
			if (_bc != bc && s.hasFlowController()) {
				if (a != null) {
					a.returnValue();
					a.endMethod();
				}
				_bc = bc;
				a = null;
			}
			ExpressionUtil.writeOut(s, _bc);
		}
		if (a != null) {
			a.returnValue();
			a.endMethod();
		}
	}

	public static void writeOut(final BytecodeContext bc, List<Statement> statements) throws TransformerException {

		if (statements == null || statements.size() == 0) return;

		Statement s;

		// boolean split = bc.getPage().getSplitIfNecessary();
		// split

		// print.e("descendants:" + (p == null ? 0 : p.getDescendants()));
		// print.e("statments:" + statements.size());

		if (statements.size() > MAX_STATEMENTS && bc.doSubFunctions()) {
			int ratio = Math.round(statements.size() / ((float) MAX_STATEMENTS));
			int collectionSize = statements.size() / ratio;
			if (collectionSize < 1) collectionSize = 1;
			Iterator<Statement> it = statements.iterator();

			final List<Statement> subStatements = new ArrayList<Statement>();
			while (it.hasNext()) {
				s = it.next();
				// reached the max size
				if (subStatements.size() >= MAX_STATEMENTS) {
					addToSubMethod(bc, subStatements.toArray(new Statement[subStatements.size()]));
					subStatements.clear();
				}
				// statement i cannot move to a sub method
				if (s.hasFlowController()) {
					// add existing statements to sub method
					if (subStatements.size() > 0) {
						addToSubMethod(bc, subStatements.toArray(new Statement[subStatements.size()]));
						subStatements.clear();
					}
					ExpressionUtil.writeOut(s, bc);
				}
				else {
					subStatements.add(s);
				}
			}

			if (subStatements.size() > 0) addToSubMethod(bc, subStatements.toArray(new Statement[subStatements.size()]));
		}
		// no split
		else {
			Iterator<Statement> it = statements.iterator();
			while (it.hasNext()) {
				ExpressionUtil.writeOut(it.next(), bc);
			}
		}
	}

	private static void addToSubMethod(BytecodeContext callerBC, Statement... statements) throws TransformerException {
		if (statements == null || statements.length == 0) return;

		GeneratorAdapter callerAdapter = callerBC.getAdapter();
		String method = ASMUtil.createOverfowMethod(callerBC.getMethod().getName(), callerBC.getPage().getMethodCount());

		/*
		 * for (int i = 0; i < statements.length; i++) { if (statements[i].getStart() != null) {
		 * bc.visitLine(statements[i].getStart()); break; } }
		 */

		// ExpressionUtil.lastLine(bc);
		Method m = new Method(method, Types.VOID, new Type[] { Types.PAGE_CONTEXT });
		GeneratorAdapter a = new GeneratorAdapter(Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, m, null, new Type[] { Types.THROWABLE }, callerBC.getClassWriter());

		BytecodeContext bc = new BytecodeContext(callerBC.getConstructor(), callerBC.getKeys(), callerBC, a, m);
		if (callerBC.getRoot() != null) bc.setRoot(callerBC.getRoot());
		else bc.setRoot(callerBC);

		callerAdapter.visitVarInsn(Opcodes.ALOAD, 0);
		callerAdapter.visitVarInsn(Opcodes.ALOAD, 1);
		callerAdapter.visitMethodInsn(Opcodes.INVOKEVIRTUAL, callerBC.getClassName(), method, "(Llucee/runtime/PageContext;)V");

		for (int i = 0; i < statements.length; i++) {
			ExpressionUtil.writeOut(statements[i], bc);
		}

		a.returnValue();
		a.endMethod();
	}

	/**
	 *
	 * @see lucee.transformer.bytecode.Body#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return statements.isEmpty();
	}
}