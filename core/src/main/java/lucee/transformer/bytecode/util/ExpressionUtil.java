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
package lucee.transformer.bytecode.util;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.visitor.OnFinally;
import lucee.transformer.bytecode.visitor.TryFinallyVisitor;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.statement.Statement;

public final class ExpressionUtil {

	public static final Method START = new Method("exeLogStart", Types.VOID, new Type[] { Types.INT_VALUE, Types.STRING });
	public static final Method END = new Method("exeLogEnd", Types.VOID, new Type[] { Types.INT_VALUE, Types.STRING });

	public static final Method CURRENT_LINE = new Method("currentLine", Types.VOID, new Type[] { Types.INT_VALUE });

	// Cache for line number strings to avoid repeated Integer.toString() allocations
	private static final String[] LINE_CACHE = new String[10000];
	static {
		for (int i = 0; i < LINE_CACHE.length; i++) {
			LINE_CACHE[i] = Integer.toString(i);
		}
	}

	private Map<String, String> last = new HashMap<String, String>();

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
	 * @param bc
	 * @param pos
	 */
	public void visitLine(BytecodeContext bc, Position pos) {
		if (pos != null) {
			visitLine(bc, pos.line);
		}
	}

	private static String lineToString(int line) {
		return (line >= 0 && line < LINE_CACHE.length) ? LINE_CACHE[line] : Integer.toString(line);
	}

	private void visitLine(BytecodeContext bc, int line) {
		if (line > 0) {
			String lineStr = lineToString(line);
			String key = bc.getClassName() + ":" + bc.getId();
			if (!lineStr.equals(last.get(key))) {
				synchronized (SystemUtil.createToken("ExpressionUtil", bc.getClassName())) {
					if (!lineStr.equals(last.get(key))) {
						bc.visitLineNumber(line);
						last.put(key, lineStr);
						last.put(bc.getClassName(), lineStr);
					}
				}
			}
		}
	}

	public void lastLine(BytecodeContext bc) {
		synchronized (SystemUtil.createToken("ExpressionUtil", bc.getClassName())) {
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
		if (!bc.writeLog() || pos == null || (StringUtil.indexOfIgnoreCaseNoCheck(bc.getMethod().getName(), "call") == -1)) return;
		try {
			GeneratorAdapter adapter = bc.getAdapter();
			adapter.loadArg(0);

			// Check if execution log is line-based (cached in BytecodeContext)
			if (bc.isLineBased()) {
				// Push line number for line-based execution logs (e.g. debugger)
				adapter.push(pos.line);
			}
			else {
				// Push character position for char-based execution logs (e.g. console)
				int off = bc.getSourceOffset();
				adapter.push(pos.pos - off);
			}

			adapter.push(id);
			adapter.invokeVirtual(Types.PAGE_CONTEXT, method);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}
	}

	public static boolean doLog(BytecodeContext bc) {
		return bc.writeLog() && StringUtil.indexOfIgnoreCaseNoCheck(bc.getMethod().getName(), "call") != -1;
	}
}