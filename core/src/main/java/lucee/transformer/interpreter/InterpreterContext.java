package lucee.transformer.interpreter;

import java.util.Stack;

import lucee.runtime.PageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.expression.Expression;

public class InterpreterContext implements Context {

	private PageContext pc;

	private Stack<Object> stack = new Stack<Object>();

	public InterpreterContext(PageContext pc) {
		this.pc = pc;
	}

	@Override
	public Factory getFactory() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * public void stack(boolean b) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Double d) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(double d) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Float f) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(float f) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Long l) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(long l) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(Integer i) { // TODO Auto-generated method stub
	 * 
	 * } public void stack(int i) { // TODO Auto-generated method stub
	 * 
	 * }
	 * 
	 * public void stack(String str) { // TODO Auto-generated method stub
	 * 
	 * }
	 */

	public void stack(Object obj) {
		stack.add(obj);
	}

	public PageContext getPageContext() {
		return pc;
	}

	/**
	 * removes the element from top of the stack
	 * 
	 * @return
	 * 
	 *         public String stackPopTopAsString() { // TODO Auto-generated method stub return null; }
	 *         public boolean stackPopTopAsBoolean() { // TODO Auto-generated method stub return false;
	 *         }
	 */

	/**
	 * removes the element from top of the stack
	 * 
	 * @return
	 * 
	 *         public String stackPopBottomAsString() { // TODO Auto-generated method stub return null;
	 *         } public boolean stackPopBottomAsBoolean() { // TODO Auto-generated method stub return
	 *         false; }
	 */

	public String getValueAsString(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toString(stack.pop());
	}

	public Boolean getValueAsBoolean(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toBoolean(stack.pop());
	}

	public boolean getValueAsBooleanValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toBooleanValue(stack.pop());
	}

	public Byte getValueAsByte(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toByte(stack.pop());
	}

	public byte getValueAsByteValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toByteValue(stack.pop());
	}

	public Integer getValueAsInteger(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toInteger(stack.pop());
	}

	public int getValueAsIntValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toIntValue(stack.pop());
	}

	public Float getValueAsFloat(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toFloat(stack.pop());
	}

	public float getValueAsFloatValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toFloatValue(stack.pop());
	}

	public Double getValueAsDouble(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toDouble(stack.pop());
	}

	public Number getValueAsNumber(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toNumber(stack.pop());
	}

	public double getValueAsDoubleValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toDoubleValue(stack.pop());
	}

	public Character getValueAsCharacter(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toCharacter(stack.pop());
	}

	public char getValueAsCharValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toCharValue(stack.pop());
	}

	public Short getValueAsShort(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toShort(stack.pop());
	}

	public short getValueAsShortValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toShortValue(stack.pop());
	}

	public Long getValueAsLong(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return Caster.toLong(stack.pop());
	}

	public long getValueAsLongValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_VALUE);
		return Caster.toLongValue(stack.pop());
	}

	public Object getValue(Expression expr) throws PageException {
		expr.writeOut(this, Expression.MODE_REF);
		return stack.pop();
	}

}
