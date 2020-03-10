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
package lucee.transformer.bytecode.util;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;

public final class ExpressionUtil {

	public static final Method START = new Method("exeLogStart", Types.VOID, new Type[] { Types.INT_VALUE, Types.STRING });
	public static final Method END = new Method("exeLogEnd", Types.VOID, new Type[] { Types.INT_VALUE, Types.STRING });

	public static final Method CURRENT_LINE = new Method("currentLine", Types.VOID, new Type[] { Types.INT_VALUE });

	private static Map<String, String> last = new HashMap<String, String>();

	public static void writeOutExpressionArray(BytecodeContext bc, Type arrayType, Expression[] array) throws TransformerException {
		GeneratorAdapter adapter = bc.getAdapter();
		adapter.push(array.length);
		adapter.newArray(arrayType);
		for (int i = 0; i < array.length; i++) {
			adapter.dup();
			adapter.push(i);
			array[i].writeOut(bc, Expression.MODE_REF);
			adapter.visitInsn(Opcodes.AASTORE);
		}
	}

	/**
	 * visit line number
	 * 
	 * @param adapter
	 * @param line
	 * @param silent id silent this is ignored for log
	 */
	public static void visitLine(BytecodeContext bc, Position pos) {
		if (pos != null) {
			visitLine(bc, pos.line);
		}
	}

	private static void visitLine(BytecodeContext bc, int line) {
		if (line > 0) {
			synchronized (last) {
				if (!("" + line).equals(last.get(bc.getClassName() + ":" + bc.getId()))) {
					bc.visitLineNumber(line);
					last.put(bc.getClassName() + ":" + bc.getId(), "" + line);
					last.put(bc.getClassName(), "" + line);
				}
			}
		}
	}

	public static void lastLine(BytecodeContext bc) {
		synchronized (last) {
			int line = Caster.toIntValue(last.get(bc.getClassName()), -1);
			visitLine(bc, line);
		}
	}

	/**
	 * write out expression without LNT
	 * 
	 * @param value
	 * @param bc
	 * @param mode
	 * @throws TransformerException
	 */
	public static void writeOutSilent(Expression value, BytecodeContext bc, int mode) throws TransformerException {
		Position start = value.getStart();
		Position end = value.getEnd();
		value.setStart(null);
		value.setEnd(null);
		value.writeOut(bc, mode);
		value.setStart(start);
		value.setEnd(end);
	}

	public static void writeOut(Expression value, BytecodeContext bc, int mode) throws TransformerException {
		value.writeOut(bc, mode);
	}

	public static void writeOut(final Statement s, BytecodeContext bc) throws TransformerException {
		if (ExpressionUtil.doLog(bc)) {
			final String id = CreateUniqueId.invoke();
			TryFinallyVisitor tfv = new TryFinallyVisitor(new OnFinally() {
				@Override
				public void _writeOut(BytecodeContext bc) {
					ExpressionUtil.callEndLog(bc, s, id);
				}
			}, null);

			tfv.visitTryBegin(bc);
			ExpressionUtil.callStartLog(bc, s, id);
			s.writeOut(bc);
			tfv.visitTryEnd(bc);
		}
		else s.writeOut(bc);
	}

	public static short toShortType(ExprString expr, boolean alsoAlias, short defaultValue) {
		if (expr instanceof LitString) {
			return CFTypes.toShort(((LitString) expr).getString(), alsoAlias, defaultValue);
		}
		return defaultValue;
	}

	public static void callStartLog(BytecodeContext bc, Statement s, String id) {
		call_Log(bc, START, s.getStart(), id);
	}

	public static void callEndLog(BytecodeContext bc, Statement s, String id) {
		call_Log(bc, END, s.getEnd(), id);
	}

	private static void call_Log(BytecodeContext bc, Method method, Position pos, String id) {
		if (!bc.writeLog() || pos == null || (StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") == -1)) return;
		try {
			GeneratorAdapter adapter = bc.getAdapter();
			adapter.loadArg(0);
			// adapter.checkCast(Types.PAGE_CONTEXT_IMPL);
			adapter.push(pos.pos);
			adapter.push(id);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, method);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static boolean doLog(BytecodeContext bc) {
		return bc.writeLog() && StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") != -1;
	}
}