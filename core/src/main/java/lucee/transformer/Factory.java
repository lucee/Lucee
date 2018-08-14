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

import lucee.runtime.config.Config;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitDouble;
import lucee.transformer.expression.literal.LitFloat;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Variable;

public abstract class Factory {
	

    public static final int OP_BOOL_AND=0;
    public static final int OP_BOOL_OR=1;
    public static final int OP_BOOL_XOR=2;
	public static final int OP_BOOL_EQV = 3;
	public static final int OP_BOOL_IMP = 4;
	

	public abstract  LitBoolean TRUE();
	public abstract  LitBoolean FALSE();
	public abstract  LitString EMPTY();
	public abstract LitDouble DOUBLE_ZERO();
	public abstract LitDouble DOUBLE_ONE();
	public abstract LitString NULL();

	// CREATION
	public abstract LitString createLitString(String str);
	public abstract LitString createLitString(String str, Position start, Position end);

	public abstract LitBoolean createLitBoolean(boolean b);
	public abstract LitBoolean createLitBoolean(boolean b, Position start,Position end);

	public abstract LitDouble createLitDouble(double d);
	public abstract LitDouble createLitDouble(double d, Position start,Position end);
	

	public abstract LitFloat createLitFloat(float f);
	public abstract LitFloat createLitFloat(float f, Position start,Position end);
	
	public abstract LitLong createLitLong(long l);
	public abstract LitLong createLitLong(long l, Position start,Position end);

	public abstract LitInteger createLitInteger(int i);
	public abstract LitInteger createLitInteger(int i, Position start,Position end);

	public abstract Expression createNull();
	public abstract Expression createNull(Position start,Position end);
	public abstract Expression createNullConstant(Position start,Position end);
	public abstract boolean isNull(Expression expr);
	
	/**
	 * return null if full null support is enabled, otherwise an empty string
	 * @return
	 */
	public abstract Expression createEmpty();
	
	public abstract Literal createLiteral(Object obj,Literal defaultValue);
	public abstract DataMember createDataMember(ExprString name);
	

	public abstract Variable createVariable(Position start, Position end);
	public abstract Variable createVariable(int scope,Position start, Position end);


	public abstract Expression createStruct();
	public abstract Expression createArray();
	
	
	
	// CASTING
	public abstract ExprDouble toExprDouble(Expression expr);
	public abstract ExprString toExprString(Expression expr);
	public abstract ExprBoolean toExprBoolean(Expression expr);
	public abstract ExprInt toExprInt(Expression expr);
	

	// OPERATIONS
	public abstract ExprString opString(Expression left,Expression right);
	public abstract ExprString opString(Expression left, Expression right, boolean concatStatic);
	
	public abstract ExprBoolean opBool(Expression left,Expression right,int operation);
	
	
	public abstract void registerKey(Context bc,Expression name,boolean doUpperCase) throws TransformerException;
	
	
	public abstract Config getConfig();
	
	public static boolean canRegisterKey(Expression name) {
		return name instanceof LitString;
	}
}