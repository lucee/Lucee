package lucee.transformer.interpreter;

import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.FactoryBase;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprFloat;
import lucee.transformer.expression.ExprInt;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitDouble;
import lucee.transformer.expression.literal.LitFloat;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.interpreter.cast.CastBoolean;
import lucee.transformer.interpreter.cast.CastDouble;
import lucee.transformer.interpreter.cast.CastFloat;
import lucee.transformer.interpreter.cast.CastInt;
import lucee.transformer.interpreter.cast.CastOther;
import lucee.transformer.interpreter.cast.CastString;
import lucee.transformer.interpreter.expression.var.EmptyArray;
import lucee.transformer.interpreter.expression.var.EmptyStruct;
import lucee.transformer.interpreter.literal.Empty;
import lucee.transformer.interpreter.literal.LitBooleanImpl;
import lucee.transformer.interpreter.literal.LitDoubleImpl;
import lucee.transformer.interpreter.literal.LitFloatImpl;
import lucee.transformer.interpreter.literal.LitIntegerImpl;
import lucee.transformer.interpreter.literal.LitLongImpl;
import lucee.transformer.interpreter.literal.LitStringImpl;
import lucee.transformer.interpreter.literal.Null;
import lucee.transformer.interpreter.literal.NullConstant;
import lucee.transformer.interpreter.op.OpBool;
import lucee.transformer.interpreter.op.OpContional;
import lucee.transformer.interpreter.op.OpDecision;
import lucee.transformer.interpreter.op.OpDouble;
import lucee.transformer.interpreter.op.OpElvis;
import lucee.transformer.interpreter.op.OpNegate;
import lucee.transformer.interpreter.op.OpNegateNumber;
import lucee.transformer.interpreter.op.OpString;

public class InterpreterFactory extends FactoryBase {

	private final LitBoolean TRUE;
	private final LitBoolean FALSE;
	private final LitString EMPTY;
	private final LitString NULL;
	private final LitDouble DOUBLE_ZERO;
	private final LitDouble DOUBLE_ONE;
	private Config config;

	private static InterpreterFactory instance;

	public static Factory getInstance(Config config) {
		if (instance == null) instance = new InterpreterFactory(config == null ? ThreadLocalPageContext.getConfig() : config);
		return instance;
	}

	public InterpreterFactory(Config config) {
		TRUE = createLitBoolean(true);
		FALSE = createLitBoolean(false);
		EMPTY = createLitString("");
		NULL = createLitString("NULL");
		DOUBLE_ZERO = createLitDouble(0);
		DOUBLE_ONE = createLitDouble(1);
		this.config = config;
	}

	@Override
	public LitBoolean TRUE() {
		return TRUE;
	}

	@Override
	public LitBoolean FALSE() {
		return FALSE;
	}

	@Override
	public LitString EMPTY() {
		return EMPTY;
	}

	@Override
	public LitDouble DOUBLE_ZERO() {
		return DOUBLE_ZERO;
	}

	@Override
	public LitDouble DOUBLE_ONE() {
		return DOUBLE_ONE;
	}

	@Override
	public LitString NULL() {
		return NULL;
	}

	@Override
	public LitString createLitString(String str) {
		return new LitStringImpl(this, str, null, null);
	}

	@Override
	public LitString createLitString(String str, Position start, Position end) {
		return new LitStringImpl(this, str, start, end);
	}

	@Override
	public LitBoolean createLitBoolean(boolean b) {
		return new LitBooleanImpl(this, b, null, null);
	}

	@Override
	public LitBoolean createLitBoolean(boolean b, Position start, Position end) {
		return new LitBooleanImpl(this, b, start, end);
	}

	@Override
	public LitDouble createLitDouble(double d) {
		return new LitDoubleImpl(this, d, null, null);
	}

	@Override
	public LitDouble createLitDouble(double d, Position start, Position end) {
		return new LitDoubleImpl(this, d, start, end);
	}

	@Override
	public LitFloat createLitFloat(float f) {
		return new LitFloatImpl(this, f, null, null);
	}

	@Override
	public LitFloat createLitFloat(float f, Position start, Position end) {
		return new LitFloatImpl(this, f, start, end);
	}

	@Override
	public LitLong createLitLong(long l) {
		return new LitLongImpl(this, l, null, null);
	}

	@Override
	public LitLong createLitLong(long l, Position start, Position end) {
		return new LitLongImpl(this, l, start, end);
	}

	@Override
	public LitInteger createLitInteger(int i) {
		return new LitIntegerImpl(this, i, null, null);
	}

	@Override
	public LitInteger createLitInteger(int i, Position start, Position end) {
		return new LitIntegerImpl(this, i, start, end);
	}

	@Override
	public boolean isNull(Expression e) {
		return e instanceof Null;
	}

	@Override
	public Expression createNull() {
		return new Null(this, null, null);
	}

	@Override
	public Expression createNull(Position start, Position end) {
		return new Null(this, start, end);
	}

	@Override
	public Expression createNullConstant(Position start, Position end) {
		return new NullConstant(this, null, null);
	}

	@Override
	public Expression createEmpty() {
		return new Empty(this, null, null);
	}

	@Override
	public DataMember createDataMember(ExprString name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable createVariable(Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Variable createVariable(int scope, Position start, Position end) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Expression createStruct() {
		return new EmptyStruct(this);
	}

	@Override
	public Expression createArray() {
		return new EmptyArray(this);
	}

	@Override
	public ExprDouble toExprDouble(Expression expr) {
		return CastDouble.toExprDouble(expr);
	}

	@Override
	public ExprString toExprString(Expression expr) {
		return CastString.toExprString(expr);
	}

	@Override
	public ExprBoolean toExprBoolean(Expression expr) {
		return CastBoolean.toExprBoolean(expr);
	}

	@Override
	public ExprInt toExprInt(Expression expr) {
		return CastInt.toExprInt(expr);
	}

	@Override
	public ExprFloat toExprFloat(Expression expr) {
		return CastFloat.toExprFloat(expr);
	}

	@Override
	public Expression toExpression(Expression expr, String type) {
		return CastOther.toExpression(expr, type);
	}

	@Override
	public ExprString opString(Expression left, Expression right) {
		return OpString.toExprString(left, right, true);
	}

	@Override
	public ExprString opString(Expression left, Expression right, boolean concatStatic) {
		return OpString.toExprString(left, right, concatStatic);
	}

	@Override
	public ExprBoolean opBool(Expression left, Expression right, int operation) {
		return OpBool.toExprBoolean(left, right, operation);
	}

	@Override
	public ExprDouble opDouble(Expression left, Expression right, int operation) {
		return OpDouble.toExprDouble(left, right, operation);
	}

	@Override
	public ExprDouble opUnary(Variable var, Expression value, short type, int operation, Position start, Position end) {
		return null;
	}

	@Override
	public Expression opNegate(Expression expr, Position start, Position end) {
		return OpNegate.toExprBoolean(expr, start, end);
	}

	@Override
	public ExprDouble opNegateNumber(Expression expr, int operation, Position start, Position end) {
		return OpNegateNumber.toExprDouble(expr, operation, start, end);
	}

	@Override
	public Expression opContional(Expression cont, Expression left, Expression right) {
		return OpContional.toExpr(cont, left, right);
	}

	@Override
	public ExprBoolean opDecision(Expression left, Expression right, int operation) {
		return OpDecision.toExprBoolean(left, right, operation);
	}

	@Override
	public Expression opElvis(Variable left, Expression right) {
		return OpElvis.toExpr(left, right);
	}

	@Override
	public Expression removeCastString(Expression expr) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void registerKey(Context bc, Expression name, boolean doUpperCase) throws TransformerException {
		// TODO Auto-generated method stub

	}

	@Override
	public Config getConfig() {
		// TODO Auto-generated method stub
		return null;
	}

}
