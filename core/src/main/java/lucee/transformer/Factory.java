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
package lucee.transformer;

import java.math.BigDecimal;

import lucee.runtime.config.Config;
import lucee.runtime.exp.CasterException;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprFloat;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprNumber;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitDouble;
import lucee.transformer.expression.literal.LitFloat;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.expression.literal.LitNumber;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Variable;

public abstract class Factory {

	public static final int OP_BOOL_AND = 0;
	public static final int OP_BOOL_OR = 1;
	public static final int OP_BOOL_XOR = 2;
	public static final int OP_BOOL_EQV = 3;
	public static final int OP_BOOL_IMP = 4;

	public static final int OP_DBL_PLUS = 0;
	public static final int OP_DBL_MINUS = 1;
	public static final int OP_DBL_MODULUS = 2;
	public static final int OP_DBL_DIVIDE = 3;
	public static final int OP_DBL_MULTIPLY = 4;
	public static final int OP_DBL_EXP = 5;
	public static final int OP_DBL_INTDIV = 6;

	public static final short OP_UNARY_POST = 1;
	public static final short OP_UNARY_PRE = 2;

	// must always be alias to OP only
	public static final int OP_UNARY_PLUS = OP_DBL_PLUS;
	public static final int OP_UNARY_MINUS = OP_DBL_MINUS;
	public static final int OP_UNARY_DIVIDE = OP_DBL_DIVIDE;
	public static final int OP_UNARY_MULTIPLY = OP_DBL_MULTIPLY;
	public static final int OP_UNARY_CONCAT = 1001314342;

	public static final int OP_DEC_LT = 1;
	public static final int OP_DEC_LTE = 2;
	public static final int OP_DEC_GTE = 3;
	public static final int OP_DEC_GT = 4;
	public static final int OP_DEC_EQ = 5;
	public static final int OP_DEC_NEQ = 6;
	public static final int OP_DEC_CT = 1000;
	public static final int OP_DEC_NCT = 1001;
	public static final int OP_DEC_EEQ = 1002;
	public static final int OP_DEC_NEEQ = 1003;

	public static final int OP_NEG_NBR_PLUS = 0;
	public static final int OP_NEG_NBR_MINUS = 1;
	public static final boolean PERCISE_NUMBERS = true;

	public abstract LitBoolean TRUE();

	public abstract LitBoolean FALSE();

	public abstract LitString EMPTY();

	public abstract LitDouble DOUBLE_ZERO();

	public abstract LitDouble DOUBLE_ONE();

	public abstract LitString NULL();

	// CREATION
	public abstract LitString createLitString(String str);

	public abstract LitString createLitString(String str, Position start, Position end);

	public abstract LitBoolean createLitBoolean(boolean b);

	public abstract LitBoolean createLitBoolean(boolean b, Position start, Position end);

	public abstract LitDouble createLitDouble(double d);

	public abstract LitDouble createLitDouble(double d, Position start, Position end);

	public abstract LitNumber createLitNumber(String number) throws CasterException;

	public abstract LitNumber createLitNumber(String number, Position start, Position end) throws CasterException;

	public abstract LitNumber createLitNumber(BigDecimal bd);

	public abstract LitNumber createLitNumber(BigDecimal bd, Position start, Position end);

	public abstract LitFloat createLitFloat(float f);

	public abstract LitFloat createLitFloat(float f, Position start, Position end);

	public abstract LitLong createLitLong(long l);

	public abstract LitLong createLitLong(long l, Position start, Position end);

	public abstract LitInteger createLitInteger(int i);

	public abstract LitInteger createLitInteger(int i, Position start, Position end);

	public abstract Expression createNull();

	public abstract Expression createNull(Position start, Position end);

	public abstract Expression createNullConstant(Position start, Position end);

	public abstract boolean isNull(Expression expr);

	/**
	 * return null if full null support is enabled, otherwise an empty string
	 * 
	 * @return
	 */
	public abstract Expression createEmpty();

	public abstract Literal createLiteral(Object obj, Literal defaultValue);

	public abstract DataMember createDataMember(ExprString name);

	public abstract Variable createVariable(Position start, Position end);

	public abstract Variable createVariable(int scope, Position start, Position end);

	public abstract Expression createStruct();

	public abstract Expression createArray();

	// CASTING
	public abstract ExprDouble toExprDouble(Expression expr);

	public abstract ExprString toExprString(Expression expr);

	public abstract ExprBoolean toExprBoolean(Expression expr);

	public abstract ExprInt toExprInt(Expression expr);

	public abstract ExprFloat toExprFloat(Expression expr);

	public abstract Expression toExpression(Expression expr, String type);

	// OPERATIONS
	public abstract ExprString opString(Expression left, Expression right);

	public abstract ExprString opString(Expression left, Expression right, boolean concatStatic);

	public abstract ExprBoolean opBool(Expression left, Expression right, int operation);

	public abstract ExprNumber opNumber(Expression left, Expression right, int operation);

	public abstract ExprDouble opUnary(Variable var, Expression value, short type, int operation, Position start, Position end);

	public abstract Expression opNegate(Expression expr, Position start, Position end);

	public abstract ExprNumber opNegateNumber(Expression expr, int operation, Position start, Position end);

	public abstract Expression opContional(Expression cont, Expression left, Expression right);

	public abstract ExprBoolean opDecision(Expression left, Expression concatOp, int operation);

	public abstract Expression opElvis(Variable left, Expression right);

	public abstract Expression removeCastString(Expression expr);
	// TODO more removes?

	public abstract void registerKey(Context bc, Expression name, boolean doUpperCase) throws TransformerException;

	public abstract Config getConfig();

	public static boolean canRegisterKey(Expression name) {
		return name instanceof LitString;
	}
}