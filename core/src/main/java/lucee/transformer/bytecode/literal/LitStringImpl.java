/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.bytecode.literal;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import lucee.commons.io.CharsetUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.op.Caster;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.BytecodeContext;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Range;
import lucee.transformer.bytecode.expression.ExpressionBase;
import lucee.transformer.bytecode.util.Types;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.literal.LitString;

/**
 * A Literal String
 */
public class LitStringImpl extends ExpressionBase implements LitString, ExprString {

	public static final int MAX_SIZE = 65535;
	public static final int TYPE_ORIGINAL = 0;
	public static final int TYPE_UPPER = 1;
	public static final int TYPE_LOWER = 2;

	private String str;
	private boolean fromBracket;

	/*
	 * public static ExprString toExprString(String str, Position start,Position end) { return new
	 * LitStringImpl(str,start,end); }
	 * 
	 * public static ExprString toExprString(String str) { return new LitStringImpl(str,null,null); }
	 * 
	 * public static LitString toLitString(String str) { return new LitStringImpl(str,null,null); }
	 */

	/**
	 * constructor of the class
	 * 
	 * @param str
	 * @param line
	 */
	public LitStringImpl(Factory f, String str, Position start, Position end) {
		super(f, start, end);
		this.str = str;
	}

	@Override
	public String getString() {
		return str;
	}

	/**
	 * @see lucee.transformer.expression.Expression#_writeOut(org.objectweb.asm.commons.GeneratorAdapter,
	 *      int)
	 */
	private static Type _writeOut(BytecodeContext bc, int mode, String str) throws TransformerException {
		// write to a file instead to the bytecode
		// str(0,10);
		// print.ds(str);
		int externalizeStringGTE = ((ConfigPro) bc.getConfig()).getExternalizeStringGTE();

		if (externalizeStringGTE > -1 && str.length() > externalizeStringGTE && StringUtil.indexOfIgnoreCase(bc.getMethod().getName(), "call") != -1) {
			try {
				GeneratorAdapter ga = bc.getAdapter();
				Page page = bc.getPage();
				Range range = page.registerString(bc, str);
				if (range != null) {
					ga.visitVarInsn(Opcodes.ALOAD, 0);
					ga.visitVarInsn(Opcodes.ALOAD, 1);
					ga.push(range.from);
					ga.push(range.to);
					ga.visitMethodInsn(Opcodes.INVOKEVIRTUAL, bc.getClassName(), "str", "(Llucee/runtime/PageContext;II)Ljava/lang/String;");
					return Types.STRING;
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
			}
		}

		if (toBig(str)) {
			_toExpr(bc.getFactory(), str).writeOut(bc, mode);
		}
		else {
			bc.getAdapter().push(str);
		}
		return Types.STRING;
	}

	@Override
	public Type _writeOut(BytecodeContext bc, int mode) throws TransformerException {
		return _writeOut(bc, mode, str);
	}

	public Type writeOut(BytecodeContext bc, int mode, int caseType) throws TransformerException {
		if (TYPE_UPPER == caseType) return _writeOut(bc, mode, str.toUpperCase());
		if (TYPE_LOWER == caseType) return _writeOut(bc, mode, str.toLowerCase());
		return _writeOut(bc, mode, str);
	}

	private static boolean toBig(String str) {
		if (str.length() < (MAX_SIZE / 2)) return false; // a char is max 2 bytes
		return str.getBytes(CharsetUtil.UTF8).length > MAX_SIZE;
	}

	private static ExprString _toExpr(Factory factory, String str) {
		int size = str.length() / 2;
		String l = str.substring(0, size);
		String r = str.substring(size);
		ExprString left = toBig(l) ? _toExpr(factory, l) : factory.createLitString(l);
		ExprString right = toBig(r) ? _toExpr(factory, r) : factory.createLitString(r);
		return factory.opString(left, right, false);
	}

	@Override
	public Double getDouble(Double defaultValue) {
		return Caster.toDouble(getString(), defaultValue);
	}

	@Override
	public Boolean getBoolean(Boolean defaultValue) {
		return Caster.toBoolean(getString(), defaultValue);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof LitString)) return false;

		return str.equals(((LitStringImpl) obj).getString());
	}

	@Override
	public String toString() {
		return str;
	}

	@Override
	public void upperCase() {
		str = str.toUpperCase();
	}

	public void lowerCase() {
		str = str.toLowerCase();
	}

	@Override
	public LitString duplicate() {
		return new LitStringImpl(getFactory(), str, getStart(), getEnd());
	}

	@Override
	public void fromBracket(boolean fromBracket) {
		this.fromBracket = fromBracket;
	}

	@Override
	public boolean fromBracket() {
		return fromBracket;
	}
}