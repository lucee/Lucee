/**
 *
 * Copyright (c) 2026, Lucee Association Switzerland. All rights reserved.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lucee.runtime.type.scope.Scope;
import lucee.transformer.Body;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.EmptyArray;
import lucee.transformer.bytecode.expression.var.EmptyStruct;
import lucee.transformer.bytecode.expression.var.NamedArgumentImpl;
import lucee.transformer.bytecode.literal.Null;
import lucee.transformer.bytecode.literal.NullConstant;
import lucee.transformer.bytecode.op.AbsOpUnary;
import lucee.transformer.bytecode.op.OpBigDecimal;
import lucee.transformer.bytecode.op.OpBool;
import lucee.transformer.bytecode.op.OpContional;
import lucee.transformer.bytecode.op.OpDecision;
import lucee.transformer.bytecode.op.OpNegate;
import lucee.transformer.bytecode.op.OpNegateNumber;
import lucee.transformer.bytecode.op.OpNumber;
import lucee.transformer.bytecode.op.OpString;
import lucee.transformer.bytecode.op.OpUnaryNumber;
import lucee.transformer.bytecode.statement.tag.TagBreak;
import lucee.transformer.bytecode.statement.tag.TagContinue;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitInteger;
import lucee.transformer.expression.literal.LitLong;
import lucee.transformer.expression.literal.LitNumber;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.Argument;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;
import lucee.transformer.statement.Statement;

/**
 * Detects a for loop that is a plain counting loop, for example <code>for (i = 1; i &lt;= n; i++)</code>
 * or <code>for (var i = 10; i &gt; 0; i -= 2)</code>, for which {@link For} can keep the counter in a
 * Java double instead of looking up and converting the variable on every single check and update.
 * <p>
 * The loop only qualifies if
 * <ul>
 * <li>the condition compares the counter (unscoped, local or variables scoped) with &lt;, &lt;=, &gt; or &gt;= to
 * an arbitrary bound, that bound is still evaluated in every iteration,</li>
 * <li>the update is ++, --, += or -= with a number literal on the same variable,</li>
 * <li>nothing in the bound or the body can possibly change the counter or call code that could do
 * so.</li>
 * </ul>
 * The last point is decided with a whitelist: every statement and expression that is not known to be
 * harmless makes the loop use the generic code path, so when in doubt the loop behaves exactly as
 * before (LDEV-4473).
 */
final class ForCounter {

	private static final Set<String> SCOPE_NAMES = new HashSet<>();
	private static final Set<String> BIF_DENY = new HashSet<>();
	static {
		for (String name: new String[] { "variables", "local", "arguments", "this", "application", "session", "request", "server", "cgi", "form", "url", "cookie", "client", "cluster",
				"super" }) {
			SCOPE_NAMES.add(name);
		}
		// functions that can write a variable by name, run code or give access to the scopes
		for (String name: new String[] { "evaluate", "precisionevaluate", "setvariable", "iif", "invoke", "queryexecute", "getpagecontext", "createobject", "callstackget",
				"getfunctiondata", "getcomponentmetadata", "getmetadata" }) {
			BIF_DENY.add(name);
		}
	}

	final int scope;
	final String name;
	/** the variable the update works on, used to create the variables to read and write the counter */
	final Variable updateVar;
	/** < <= > >=, see Factory.OP_DEC_* */
	final int comparison;
	final Expression bound;
	/** a number literal if the bound is a number literal, otherwise null */
	final Double literalBound;
	/** the step as a signed double (-2 for i -= 2) */
	final double step;

	private ForCounter(int scope, String name, Variable updateVar, int comparison, Expression bound, Double literalBound, double step) {
		this.scope = scope;
		this.name = name;
		this.updateVar = updateVar;
		this.comparison = comparison;
		this.bound = bound;
		this.literalBound = literalBound;
		this.step = step;
	}

	/**
	 * @return the counter or null if the loop is not a (provably safe) plain counting loop
	 */
	static ForCounter detect(Expression condition, Expression update, Body body) {
		if (condition == null || update == null || body == null) return null;
		try {
			// update
			if (!(update instanceof OpUnaryNumber)) return null;
			AbsOpUnary unary = (AbsOpUnary) update;
			int op = unary.getOperation();
			if (op != Factory.OP_UNARY_PLUS && op != Factory.OP_UNARY_MINUS) return null;
			Double step = toDouble(unary.getValue());
			if (step == null || step.isNaN() || step.isInfinite() || step.doubleValue() == 0) return null;

			Variable var = unary.getVariable();
			String name = counterName(var);
			if (name == null) return null;

			// condition
			Expression cond = unwrap(condition);
			if (!(cond instanceof OpDecision)) return null;
			OpDecision dec = (OpDecision) cond;
			int comparison = dec.getOperation();
			if (comparison != Factory.OP_DEC_LT && comparison != Factory.OP_DEC_LTE && comparison != Factory.OP_DEC_GT && comparison != Factory.OP_DEC_GTE) return null;
			Expression left = unwrap(dec.getLeft());
			if (!(left instanceof Variable)) return null;
			Variable leftVar = (Variable) left;
			if (leftVar.getScope() != var.getScope() || !name.equalsIgnoreCase(counterName(leftVar))) return null;

			// the bound can be anything that does not touch the counter
			Expression bound = dec.getRight();
			if (!safeExpression(bound, name, false)) return null;

			// body
			if (!safeStatement(body, name, true)) return null;

			double s = op == Factory.OP_UNARY_MINUS ? -step.doubleValue() : step.doubleValue();
			Double literalBound = null;
			if (isNumberLiteral(bound)) literalBound = ((Literal) bound).getNumber(null).doubleValue();
			return new ForCounter(var.getScope(), name, var, comparison, bound, literalBound, s);
		}
		catch (RuntimeException e) {
			// whatever we do not understand is not for us
			return null;
		}
	}

	/**
	 * @return a new variable pointing to the counter
	 */
	Variable createVariable(Factory f) {
		Variable v = f.createVariable(scope, updateVar.getStart(), updateVar.getEnd());
		v.addMember(f.createDataMember(f.createLitString(name)));
		return v;
	}

	private static String counterName(Variable var) {
		int scope = var.getScope();
		if (scope != Scope.SCOPE_UNDEFINED && scope != Scope.SCOPE_LOCAL && scope != Scope.SCOPE_VARIABLES) return null;
		List<Member> members = var.getMembers();
		if (members.size() != 1 || var.getDefaultValue() != null) return null;
		Member m = members.get(0);
		if (!(m instanceof DataMember) || m.getSafeNavigated() || m.isOptional()) return null;
		String name = literalName(((DataMember) m).getName());
		if (name == null || SCOPE_NAMES.contains(name.toLowerCase())) return null;
		return name;
	}

	private static String literalName(Expression expr) {
		if (expr instanceof LitString) return ((LitString) expr).getString();
		return null;
	}

	private static Expression unwrap(Expression expr) {
		while (expr instanceof Cast) {
			expr = ((Cast) expr).getExpr();
		}
		return expr;
	}

	private static boolean isNumberLiteral(Expression expr) {
		return expr instanceof LitNumber || expr instanceof LitInteger || expr instanceof LitLong;
	}

	private static Double toDouble(Expression expr) {
		if (!isNumberLiteral(expr)) return null;
		Number n = ((Literal) expr).getNumber(null);
		return n == null ? null : n.doubleValue();
	}

	/* ------------------------ analysis, true means "can not change the counter" ------------------------ */

	private static boolean safeStatement(Statement stat, String name, boolean w) {
		if (stat == null) return true;
		if (stat instanceof Body) {
			for (Statement s: ((Body) stat).getStatements()) {
				if (!safeStatement(s, name, w)) return false;
			}
			return true;
		}
		if (stat instanceof ExpressionAsStatement) return safeExpression(((ExpressionAsStatement) stat).getExpr(), name, w);
		if (stat instanceof Condition) {
			Condition c = (Condition) stat;
			for (Expression e: c.getConditions()) {
				if (!safeExpression(e, name, w)) return false;
			}
			for (Body b: c.getBodies()) {
				if (!safeStatement(b, name, w)) return false;
			}
			return true;
		}
		if (stat instanceof While) return safeExpression(((While) stat).getExpr(), name, w) && safeStatement(((While) stat).getBody(), name, w);
		if (stat instanceof DoWhile) return safeExpression(((DoWhile) stat).getExpr(), name, w) && safeStatement(((DoWhile) stat).getBody(), name, w);
		if (stat instanceof For) {
			For f = (For) stat;
			return safeExpression(f.getInit(), name, w) && safeExpression(f.getCondition(), name, w) && safeExpression(f.getUpdate(), name, w) && safeStatement(f.getBody(), name, w);
		}
		if (stat instanceof Return) return safeExpression(((Return) stat).getExpr(), name, w);
		if (stat instanceof TagBreak || stat instanceof TagContinue) return true;
		return false;
	}

	private static boolean safeExpression(Expression expr, String name, boolean w) {
		if (expr == null) return true;
		if (expr instanceof Literal || expr instanceof Null || expr instanceof NullConstant || expr instanceof EmptyArray || expr instanceof EmptyStruct) return true;

		if (expr instanceof Variable) return safeVariable((Variable) expr, name, w);

		if (expr instanceof Assign) {
			if (!w) return false;
			Assign a = (Assign) expr;
			return safeTarget(a.getVariable(), name, w) && safeExpression(a.getValue(), name, w);
		}
		if (expr instanceof AbsOpUnary) {
			if (!w) return false;
			AbsOpUnary u = (AbsOpUnary) expr;
			return safeTarget(u.getVariable(), name, w) && safeExpression(u.getValue(), name, w);
		}

		if (expr instanceof Cast) return safeExpression(((Cast) expr).getExpr(), name, w);
		if (expr instanceof OpNumber) return safeExpression(((OpNumber) expr).getLeft(), name, w) && safeExpression(((OpNumber) expr).getRight(), name, w);
		if (expr instanceof OpBigDecimal) return safeExpression(((OpBigDecimal) expr).getLeft(), name, w) && safeExpression(((OpBigDecimal) expr).getRight(), name, w);
		if (expr instanceof OpDecision) return safeExpression(((OpDecision) expr).getLeft(), name, w) && safeExpression(((OpDecision) expr).getRight(), name, w);
		if (expr instanceof OpBool) return safeExpression(((OpBool) expr).getLeft(), name, w) && safeExpression(((OpBool) expr).getRight(), name, w);
		if (expr instanceof OpString) return safeExpression(((OpString) expr).getLeft(), name, w) && safeExpression(((OpString) expr).getRight(), name, w);
		if (expr instanceof OpNegate) return safeExpression(((OpNegate) expr).getExpr(), name, w);
		if (expr instanceof OpNegateNumber) return safeExpression(((OpNegateNumber) expr).getExpr(), name, w);
		if (expr instanceof OpContional) {
			OpContional c = (OpContional) expr;
			return safeExpression(c.getCondition(), name, w) && safeExpression(c.getLeft(), name, w) && safeExpression(c.getRight(), name, w);
		}

		// everything else (closures, UDF calls, new, dynamic assignments, ...) is not known to be harmless
		return false;
	}

	/**
	 * a variable that is only read, this includes the members, calls to a function are only fine for the
	 * built in functions
	 */
	private static boolean safeVariable(Variable var, String name, boolean w) {
		List<Member> members = var.getMembers();
		// a scope as a whole can be handed to a function that writes into it
		if (members.isEmpty()) return false;
		if (members.size() == 1 && members.get(0) instanceof DataMember) {
			String n = literalName(((DataMember) members.get(0)).getName());
			if (n != null && SCOPE_NAMES.contains(n.toLowerCase())) return false;
		}
		if (!safeExpression(var.getDefaultValue(), name, w)) return false;
		return safeMembers(members, name, w);
	}

	private static boolean safeMembers(List<Member> members, String name, boolean w) {
		for (Member m: members) {
			if (!safeExpression(m.getSafeNavigatedValue(), name, w)) return false;
			if (m instanceof DataMember) {
				if (!safeExpression(((DataMember) m).getName(), name, w)) return false;
			}
			else if (m instanceof BIF) {
				if (!safeBIF((BIF) m, name, w)) return false;
			}
			else return false; // UDF, member function, call, ...
		}
		return true;
	}

	private static boolean safeBIF(BIF bif, String name, boolean w) {
		FunctionLibFunction flf = bif.getFlf();
		if (flf == null || flf.getName() == null || BIF_DENY.contains(flf.getName().toLowerCase())) return false;
		// functions that take a callback (that can be any function)
		List<FunctionLibFunctionArg> args = flf.getArg();
		if (args != null) {
			for (FunctionLibFunctionArg arg: args) {
				if ("function".equalsIgnoreCase(arg.getTypeAsString())) return false;
			}
		}
		for (Argument arg: bif.getArguments()) {
			if (arg instanceof NamedArgumentImpl && !safeExpression(((NamedArgumentImpl) arg).getName(), name, w)) return false;
			if (!safeExpression(arg.getValue(), name, w)) return false;
		}
		return true;
	}

	/**
	 * the target of an assignment or a unary operation, it must not be (or start with) the counter, and
	 * we need to know the name
	 */
	private static boolean safeTarget(Variable var, String name, boolean w) {
		List<Member> members = var.getMembers();
		if (members.isEmpty()) return false;
		Member first = members.get(0);
		if (!(first instanceof DataMember)) return false;
		String n = literalName(((DataMember) first).getName());
		if (n == null || n.equalsIgnoreCase(name)) return false;
		return safeExpression(var.getDefaultValue(), name, w) && safeMembers(members, name, w);
	}
}
