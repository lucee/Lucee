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
package lucee.transformer.cfml.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.ClassException;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.compiler.JavaCCompiler;
import lucee.commons.lang.compiler.JavaCompilerException;
import lucee.commons.lang.compiler.JavaFunction;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.Component;
import lucee.runtime.PageSource;
import lucee.runtime.component.Member;
import lucee.runtime.config.Constants;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.functions.system.CFFunction;
import lucee.runtime.listener.AppListenerUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.UDF;
import lucee.runtime.type.scope.Undefined;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.ComponentUtil;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.FunctionBody;
import lucee.transformer.bytecode.ScriptBody;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.expression.FunctionAsExpression;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.bytecode.expression.var.VariableString;
import lucee.transformer.bytecode.statement.Argument;
import lucee.transformer.bytecode.statement.Condition;
import lucee.transformer.bytecode.statement.Condition.Pair;
import lucee.transformer.bytecode.statement.DoWhile;
import lucee.transformer.bytecode.statement.ExpressionAsStatement;
import lucee.transformer.bytecode.statement.For;
import lucee.transformer.bytecode.statement.ForEach;
import lucee.transformer.bytecode.statement.Return;
import lucee.transformer.bytecode.statement.Switch;
import lucee.transformer.bytecode.statement.TryCatchFinally;
import lucee.transformer.bytecode.statement.While;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.statement.tag.TagComponent;
import lucee.transformer.bytecode.statement.tag.TagOther;
import lucee.transformer.bytecode.statement.tag.TagParam;
import lucee.transformer.bytecode.statement.udf.Closure;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.statement.udf.FunctionImpl;
import lucee.transformer.bytecode.statement.udf.Lambda;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.impl.ProcessingDirectiveException;
import lucee.transformer.cfml.expression.AbstrCFMLExprTransformer;
import lucee.transformer.cfml.script.java.JavaSourceException;
import lucee.transformer.cfml.script.java.function.FunctionDef;
import lucee.transformer.cfml.script.java.function.FunctionDefFactory;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.library.tag.TagLibTagScript;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * Innerhalb des Tag script kann in CFML eine eigene Scriptsprache verwendet werden, welche sich an
 * Javascript orientiert. Da der data.srcCode Transformer keine Spezialfaelle zulaesst, also Tags
 * einfach anhand der eingegeben TLD einliest und transformiert, aus diesem Grund wird der Inhalt
 * des Tag script einfach als Zeichenkette eingelesen. Erst durch den Evaluator (siehe 3.3), der
 * fuer das Tag script definiert ist, wird der Inhalt des Tag script uebersetzt.
 * 
 */
public abstract class AbstrCFMLScriptTransformer extends AbstrCFMLExprTransformer {

	private static final String[] IGNORE_LIST_COMPONENT = new String[] { "output", "synchronized", "extends", "implements", "displayname", "style", "persistent", "accessors" };
	private static final String[] IGNORE_LIST_INTERFACE = new String[] { "output", "extends", "displayname", "style", "persistent", "accessors" };
	private static final String[] IGNORE_LIST_PROPERTY = new String[] { "default", "fieldtype", "name", "type", "persistent", "remotingFetch", "column", "generator", "length",
			"ormtype", "params", "unSavedValue", "dbdefault", "formula", "generated", "insert", "optimisticlock", "update", "notnull", "precision", "scale", "unique", "uniquekey",
			"source" };

	private static EndCondition SEMI_BLOCK = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent('{') || data.srcCode.isCurrent(';');
		}
	};
	private static EndCondition SEMI = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent(';');
		}
	};
	private static EndCondition COMMA_ENDBRACKED = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent(',') || data.srcCode.isCurrent(')');
		}
	};

	private static EndCondition BRACKED = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent(')');
		}
	};

	private short ATTR_TYPE_NONE = TagLibTagAttr.SCRIPT_SUPPORT_NONE;
	private short ATTR_TYPE_OPTIONAL = TagLibTagAttr.SCRIPT_SUPPORT_OPTIONAL;
	private short ATTR_TYPE_REQUIRED = TagLibTagAttr.SCRIPT_SUPPORT_REQUIRED;

	public static class ComponentTemplateException extends TemplateException {
		private static final long serialVersionUID = -8103635220891288231L;

		private TemplateException te;

		public ComponentTemplateException(TemplateException te) {
			super(te.getPageSource(), te.getLine(), 0, te.getMessage());
			this.te = te;

		}

		/**
		 * @return the te
		 */
		public TemplateException getTemplateException() {
			return te;
		}
	}

	// private static final Expression NULL = data.factory.createLitString("NULL");
	// private static final Attribute ANY = new
	// Attribute(false,"type",data.factory.createLitString("any"),"string");
	private static final char NO_ATTR_SEP = 0;
	public static final String TAG_ISLAND_INDICATOR = "```";

	/**
	 * Liest saemtliche Statements des CFScriptString ein. <br />
	 * EBNF:<br />
	 * <code>{statement spaces};</code>
	 * 
	 * @return a statement
	 * @throws TemplateException
	 */
	protected final Body statements(Data data) throws TemplateException {
		ScriptBody body = new ScriptBody(data.factory);

		statements(data, body, true);
		return body;
	}

	/**
	 * Liest saemtliche Statements des CFScriptString ein. <br />
	 * EBNF:<br />
	 * <code>{statement spaces};</code>
	 * 
	 * @param parent uebergeornetes Element dem das Statement zugewiesen wird.
	 * @param isRoot befindet sich der Parser im root des data.srcCode Docs
	 * @throws TemplateException
	 */
	private final void statements(Data data, Body body, boolean isRoot) throws TemplateException {
		do {
			if (isRoot && isFinish(data)) return;
			statement(data, body);
			comments(data);
		}
		while (data.srcCode.isValidIndex() && !data.srcCode.isCurrent('}'));
	}

	/**
	 * Liest ein einzelnes Statement ein (if,for,while usw.). <br />
	 * EBNF:<br />
	 * <code>";" | "if" spaces "(" ifStatement | "function " funcStatement |  "while" spaces "(" whileStatement  |  
			  "do" spaces "{" doStatement  | "for" spaces "(" forStatement | "return" returnStatement | 
			  "break" breakStatement | "continue" continueStatement | "/*" comment | expressionStatement;</code>
	 * 
	 * @param parent uebergeornetes Element dem das Statement zugewiesen wird.
	 * @throws TemplateException
	 */
	private final void statement(Data data, Body parent) throws TemplateException {
		statement(data, parent, data.context);
	}

	private boolean statement(Data data, Body parent, short context) throws TemplateException {
		short prior = data.context;
		data.context = context;
		comments(data);
		Statement child = null;
		if (data.srcCode.forwardIfCurrent(';')) {
			return true;
		}
		else if ((child = ifStatement(data)) != null) parent.addStatement(child);
		else if ((child = propertyStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = paramStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = funcStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = whileStatement(data)) != null) parent.addStatement(child);
		else if ((child = doStatement(data)) != null) parent.addStatement(child);
		else if ((child = forStatement(data)) != null) parent.addStatement(child);
		else if ((child = returnStatement(data)) != null) parent.addStatement(child);
		else if ((child = switchStatement(data)) != null) parent.addStatement(child);
		else if ((child = tryStatement(data)) != null) parent.addStatement(child);
		else if (islandStatement(data, parent)) {
		}
		// else if(staticStatement(data,parent)) ; // do nothing, happen already inside the method
		else if ((child = staticStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = componentStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = tagStatement(data, parent)) != null) parent.addStatement(child);
		else if ((child = cftagStatement(data, parent)) != null) parent.addStatement(child);
		else if (block(data, parent)) {
		}

		else parent.addStatement(expressionStatement(data, parent));
		data.docComment = null;
		data.context = prior;

		return false;
	}

	/**
	 * Liest ein if Statement ein. <br />
	 * EBNF:<br />
	 * <code>spaces condition spaces ")" spaces block {"else if" spaces "(" elseifStatement spaces }
			 [("else"  spaces "(" | "else ") elseStatement spaces];</code>
	 * 
	 * @return if Statement
	 * @throws TemplateException
	 */
	private final Statement ifStatement(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("if", '(')) return null;

		Position line = data.srcCode.getPosition();

		Body body = new BodyBase(data.factory);
		Condition cont = new Condition(data.factory, condition(data), body, line, null);

		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "if statement must end with a [)]");
		// ex block
		Body prior = data.setParent(body);
		statement(data, body, CTX_IF);
		data.setParent(prior);
		// else if
		comments(data);
		while (elseifStatement(data, cont)) {
			comments(data);
		}
		// else
		if (elseStatement(data, cont)) {
			comments(data);
		}

		cont.setEnd(data.srcCode.getPosition());
		return cont;
	}

	/**
	 * Liest ein else if Statement ein. <br />
	 * EBNF:<br />
	 * <code>spaces condition spaces ")" spaces block;</code>
	 * 
	 * @return else if Statement
	 * @throws TemplateException
	 */
	private final boolean elseifStatement(Data data, Condition cont) throws TemplateException {
		int pos = data.srcCode.getPos();
		if (!data.srcCode.forwardIfCurrent("else")) return false;

		comments(data);
		if (!data.srcCode.forwardIfCurrent("if", '(')) {
			data.srcCode.setPos(pos);
			return false;
		}

		Position line = data.srcCode.getPosition();
		Body body = new BodyBase(data.factory);
		Pair pair = cont.addElseIf(condition(data), body, line, null);

		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "else if statement must end with a [)]");
		// ex block
		Body prior = data.setParent(body);
		statement(data, body, CTX_ELSE_IF);
		data.setParent(prior);

		pair.end = data.srcCode.getPosition();
		return true;
	}

	/**
	 * Liest ein else Statement ein. <br />
	 * EBNF:<br />
	 * <code>block;</code>
	 * 
	 * @return else Statement
	 * @throws TemplateException
	 * 
	 */
	private final boolean elseStatement(Data data, Condition cont) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("else", '{') && !data.srcCode.forwardIfCurrent("else ") && !data.srcCode.forwardIfCurrent("else", '/')) return false;

		// start (
		data.srcCode.previous();
		// ex block
		Body body = new BodyBase(data.factory);
		Pair p = cont.setElse(body, data.srcCode.getPosition(), null);

		Body prior = data.setParent(body);
		statement(data, body, CTX_ELSE);
		data.setParent(prior);

		p.end = data.srcCode.getPosition();
		return true;
	}

	private final boolean finallyStatement(Data data, TryCatchFinally tcf) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("finally", '{') && !data.srcCode.forwardIfCurrent("finally ") && !data.srcCode.forwardIfCurrent("finally", '/')) return false;

		// start (
		data.srcCode.previous();
		// ex block
		Body body = new BodyBase(data.factory);
		tcf.setFinally(body, data.srcCode.getPosition());

		Body prior = data.setParent(body);
		statement(data, body, CTX_FINALLY);
		data.setParent(prior);

		return true;
	}

	/**
	 * Liest ein while Statement ein. <br />
	 * EBNF:<br />
	 * <code>spaces condition spaces ")" spaces block;</code>
	 * 
	 * @return while Statement
	 * @throws TemplateException
	 */
	private final While whileStatement(Data data) throws TemplateException {
		int pos = data.srcCode.getPos();

		// id
		String id = variableDec(data, false);
		if (id == null) {
			data.srcCode.setPos(pos);
			return null;
		}
		if (id.equalsIgnoreCase("while")) {
			id = null;
			data.srcCode.removeSpace();
			if (!data.srcCode.forwardIfCurrent('(')) {
				data.srcCode.setPos(pos);
				return null;
			}
		}
		else {
			data.srcCode.removeSpace();
			if (!data.srcCode.forwardIfCurrent(':')) {
				data.srcCode.setPos(pos);
				return null;
			}
			data.srcCode.removeSpace();

			if (!data.srcCode.forwardIfCurrent("while", '(')) {
				data.srcCode.setPos(pos);
				return null;
			}
		}

		Position line = data.srcCode.getPosition();
		Body body = new BodyBase(data.factory);
		While whil = new While(condition(data), body, line, null, id);

		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "while statement must end with a [)]");

		Body prior = data.setParent(body);
		statement(data, body, CTX_WHILE);
		data.setParent(prior);

		whil.setEnd(data.srcCode.getPosition());
		return whil;
	}

	/**
	 * Liest ein switch Statment ein
	 * 
	 * @return switch Statement
	 * @throws TemplateException
	 */
	private final Switch switchStatement(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("switch", '(')) return null;

		Position line = data.srcCode.getPosition();

		comments(data);
		Expression expr = super.expression(data);
		comments(data);
		// end )
		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "switch statement must end with a [)]");
		comments(data);

		if (!data.srcCode.forwardIfCurrent('{')) throw new TemplateException(data.srcCode, "switch statement must have a starting  [{]");

		Switch swit = new Switch(expr, line, null);

		// cases
		// Node child=null;
		comments(data);
		while (caseStatement(data, swit)) {
			comments(data);
		}
		// default
		if (defaultStatement(data, swit)) {
			comments(data);
		}

		while (caseStatement(data, swit)) {
			comments(data);
		}

		// }
		if (!data.srcCode.forwardIfCurrent('}')) throw new TemplateException(data.srcCode, "invalid construct in switch statement");
		swit.setEnd(data.srcCode.getPosition());
		return swit;
	}

	private final TagComponent componentStatement(Data data, Body parent) throws TemplateException {

		int pos = data.srcCode.getPos();

		// get the idendifier in front of
		String id = identifier(data, false);
		if (id == null) {
			data.srcCode.setPos(pos);
			return null;
		}

		int mod = ComponentUtil.toModifier(id, Component.MODIFIER_NONE, Component.MODIFIER_NONE);
		if (mod == Component.MODIFIER_NONE) {
			data.srcCode.setPos(pos);
		}

		comments(data);

		// do we have a starting component?
		if (!data.srcCode.isCurrent(getComponentName(data.srcCode.getDialect()))
				&& (data.srcCode.getDialect() == CFMLEngine.DIALECT_CFML || !data.srcCode.isCurrent(Constants.CFML_COMPONENT_TAG_NAME))) {
			data.srcCode.setPos(pos);
			return null;
		}

		// parse the component
		TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode, getComponentName(data.srcCode.getDialect()), data.config.getIdentification());
		TagComponent comp = (TagComponent) _multiAttrStatement(parent, data, tlt);
		if (mod != Component.MODIFIER_NONE) comp.addAttribute(new Attribute(false, "modifier", data.factory.createLitString(id), "string"));
		return comp;
	}

	private String getComponentName(int dialect) {
		return dialect == CFMLEngine.DIALECT_LUCEE ? Constants.LUCEE_COMPONENT_TAG_NAME : Constants.CFML_COMPONENT_TAG_NAME;
	}

	/**
	 * Liest ein Case Statement ein
	 * 
	 * @return case Statement
	 * @throws TemplateException
	 */
	private final boolean caseStatement(Data data, Switch swit) throws TemplateException {
		if (!data.srcCode.forwardIfCurrentAndNoWordAfter("case")) return false;

		// int line=data.srcCode.getLine();
		comments(data);
		Expression expr = super.expression(data);
		comments(data);

		if (!data.srcCode.forwardIfCurrent(':')) throw new TemplateException(data.srcCode, "case body must start with [:]");

		Body body = new BodyBase(data.factory);
		switchBlock(data, body);
		swit.addCase(expr, body);
		return true;
	}

	/**
	 * Liest ein default Statement ein
	 * 
	 * @return default Statement
	 * @throws TemplateException
	 */
	private final boolean defaultStatement(Data data, Switch swit) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("default", ':')) return false;

		// int line=data.srcCode.getLine();

		Body body = new BodyBase(data.factory);
		swit.setDefaultCase(body);
		switchBlock(data, body);
		return true;
	}

	/**
	 * Liest ein Switch Block ein
	 * 
	 * @param block
	 * @throws TemplateException
	 */
	private final void switchBlock(Data data, Body body) throws TemplateException {
		while (data.srcCode.isValidIndex()) {
			comments(data);
			if (data.srcCode.isCurrent("case ") || data.srcCode.isCurrent("default", ':') || data.srcCode.isCurrent('}')) return;

			Body prior = data.setParent(body);
			statement(data, body, CTX_SWITCH);
			data.setParent(prior);
		}
	}

	/**
	 * Liest ein do Statement ein. <br />
	 * EBNF:<br />
	 * <code>block spaces "while" spaces "(" spaces condition spaces ")";</code>
	 * 
	 * @return do Statement
	 * @throws TemplateException
	 */
	private final DoWhile doStatement(Data data) throws TemplateException {
		int pos = data.srcCode.getPos();

		// id
		String id = variableDec(data, false);
		if (id == null) {
			data.srcCode.setPos(pos);
			return null;
		}
		if (id.equalsIgnoreCase("do")) {
			id = null;
			if (!data.srcCode.isCurrent('{') && !data.srcCode.isCurrent(' ') && !data.srcCode.isCurrent('/')) {
				data.srcCode.setPos(pos);
				return null;
			}
		}
		else {
			data.srcCode.removeSpace();
			if (!data.srcCode.forwardIfCurrent(':')) {
				data.srcCode.setPos(pos);
				return null;
			}
			data.srcCode.removeSpace();

			if (!data.srcCode.forwardIfCurrent("do", '{') && !data.srcCode.forwardIfCurrent("do ") && !data.srcCode.forwardIfCurrent("do", '/')) {
				data.srcCode.setPos(pos);
				return null;
			}
			data.srcCode.previous();
		}

		// if(!data.srcCode.forwardIfCurrent("do",'{') && !data.srcCode.forwardIfCurrent("do ") &&
		// !data.srcCode.forwardIfCurrent("do",'/'))
		// return null;

		Position line = data.srcCode.getPosition();
		Body body = new BodyBase(data.factory);

		// data.srcCode.previous();

		Body prior = data.setParent(body);
		statement(data, body, CTX_DO_WHILE);
		data.setParent(prior);

		comments(data);
		if (!data.srcCode.forwardIfCurrent("while", '(')) throw new TemplateException(data.srcCode, "do statement must have a while at the end");

		DoWhile doWhile = new DoWhile(condition(data), body, line, data.srcCode.getPosition(), id);

		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "do statement must end with a [)]");

		return doWhile;
	}

	/*
	 * private CFMLTransformer tag; private final Statement cfmlTagStatement(Data data,Body parent)
	 * throws TemplateException {
	 * 
	 * if(tag==null)tag=new CFMLTransformer(); tag.body(data, parent, parseExpression, transformer);
	 * 
	 * return null; }
	 */

	/**
	 * Liest ein for Statement ein. <br />
	 * EBNF:<br />
	 * <code>expression spaces ";" spaces condition spaces ";" spaces expression spaces ")" spaces block;</code>
	 * 
	 * @return for Statement
	 * @throws TemplateException
	 */
	private final Statement forStatement(Data data) throws TemplateException {

		int pos = data.srcCode.getPos();

		// id
		String id = variableDec(data, false);
		if (id == null) {
			data.srcCode.setPos(pos);
			return null;
		}
		if (id.equalsIgnoreCase("for")) {
			id = null;
			data.srcCode.removeSpace();
			if (!data.srcCode.forwardIfCurrent('(')) {
				data.srcCode.setPos(pos);
				return null;
			}
		}
		else {
			data.srcCode.removeSpace();
			if (!data.srcCode.forwardIfCurrent(':')) {
				data.srcCode.setPos(pos);
				return null;
			}
			data.srcCode.removeSpace();

			if (!data.srcCode.forwardIfCurrent("for", '(')) {
				data.srcCode.setPos(pos);
				return null;
			}
		}

		Expression left = null;
		Body body = new BodyBase(data.factory);
		Position line = data.srcCode.getPosition();
		comments(data);
		if (!data.srcCode.isCurrent(';')) {
			// left
			left = expression(data);
			comments(data);
		}
		// middle for
		if (data.srcCode.forwardIfCurrent(';')) {

			Expression cont = null;
			Expression update = null;
			// condition
			comments(data);
			if (!data.srcCode.isCurrent(';')) {
				cont = condition(data);
				comments(data);
			}
			// middle
			if (!data.srcCode.forwardIfCurrent(';')) throw new TemplateException(data.srcCode, "invalid syntax in for statement");
			// update
			comments(data);
			if (!data.srcCode.isCurrent(')')) {
				update = expression(data);
				comments(data);
			}
			// start )
			if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "invalid syntax in for statement, for statement must end with a [)]");
			// ex block
			Body prior = data.setParent(body);
			statement(data, body, CTX_FOR);

			// performance improvement in special combination
			// TagLoop loop = asLoop(data.factory,left,cont,update,body,line,data.srcCode.getPosition(),id);
			// if(loop!=null) return loop;

			data.setParent(prior);

			return new For(data.factory, left, cont, update, body, line, data.srcCode.getPosition(), id);
		}
		// middle foreach
		else if (data.srcCode.forwardIfCurrent("in")) {
			// condition
			comments(data);
			Expression value = expression(data);
			comments(data);
			if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "invalid syntax in for statement, for statement must end with a [)]");

			// ex block
			Body prior = data.setParent(body);
			statement(data, body, CTX_FOR);
			data.setParent(prior);

			if (!(left instanceof Variable)) throw new TemplateException(data.srcCode, "invalid syntax in for statement, left value is invalid");

			// if(!(value instanceof Variable))
			// throw new TemplateException(data.srcCode,"invalid syntax in for statement, right value is
			// invalid");
			return new ForEach((Variable) left, value, body, line, data.srcCode.getPosition(), id);
		}
		else throw new TemplateException(data.srcCode, "invalid syntax in for statement");
	}

	/*
	 * private TagLoop asLoop(Factory factory, Expression expLeft, Expression expMiddle, Expression
	 * expRight, Body body, Position start, Position end, String label) {
	 * 
	 * // LEFT // left must be an assignment if(!(expLeft instanceof Assign)) return null; Assign
	 * left=(Assign) expLeft; String leftVarName=toVariableName(left.getVariable());
	 * if(leftVarName==null) return null; if(!"susi".equalsIgnoreCase(leftVarName)) return null;
	 * 
	 * // MIDDLE // midfdle must be an operation if(!(expMiddle instanceof OPDecision)) return null;
	 * OPDecision middle=(OPDecision) expMiddle;
	 * 
	 * // middle must be an operation LT or LTE boolean isLT=middle.getOperation()==OPDecision.LT;
	 * if(!isLT && middle.getOperation()!=OPDecision.LTE) return null;
	 * 
	 * // middle variable need to be the same as the left variable
	 * if(!leftVarName.equals(toVariableName(middle.getLeft()))) return null;
	 * 
	 * // RIGHT // right need to be an assignment (i=i+1 what is the same as i++) if(!(expRight
	 * instanceof Assign)) return null; Assign right=(Assign) expRight;
	 * 
	 * // increment need to be a literal number if(!(right.getValue() instanceof OpDouble)) return null;
	 * OpDouble opRight=(OpDouble) right.getValue();
	 * 
	 * // must be an increment of the same variable (i on both sides)
	 * if(!leftVarName.equals(toVariableName(right.getVariable()))) return null;
	 * if(!leftVarName.equals(toVariableName(opRight.getLeft()))) return null;
	 * 
	 * // must be a literal number if(!(opRight.getRight() instanceof LitDouble)) return null; LitDouble
	 * rightIncValue=(LitDouble) opRight.getRight(); if(opRight.getOperation()!=Factory.OP_DBL_PLUS)
	 * return null;
	 * 
	 * // create loop tag TagLoop tl=new TagLoop(factory, start, end); tl.setBody(body);
	 * tl.setType(TagLoop.TYPE_FROM_TO);
	 * 
	 * // id tl.addAttribute( new Attribute( false, "index",
	 * factory.createLitString(leftVarName,right.getVariable().getStart(),right.getVariable().getEnd()),
	 * "string" ) ); // from tl.addAttribute( new Attribute( false, "from",
	 * factory.toExprDouble(left.getValue()), "number" ) ); // to ExprDouble val = isLT?
	 * factory.opDouble(middle.getLeft(), factory.createLitDouble(1),
	 * Factory.OP_DBL_MINUS):factory.toExprDouble(middle.getLeft()); tl.addAttribute( new Attribute(
	 * false, "to", val, "number" ) ); // step tl.addAttribute( new Attribute( false, "step",
	 * factory.toExprDouble(rightIncValue), "number" ) );
	 * 
	 * return tl; }
	 */

	private String toVariableName(Expression variable) {
		if (!(variable instanceof Variable)) return null;
		try {
			return VariableString.variableToString((Variable) variable, false);
		}
		catch (TransformerException e) {
			return null;
		}
	}

	/**
	 * Liest ein function Statement ein. <br />
	 * EBNF:<br />
	 * <code>identifier spaces "(" spaces identifier spaces {"," spaces identifier spaces} ")" spaces block;</code>
	 * 
	 * @return function Statement
	 * @throws TemplateException
	 */
	private final Statement funcStatement(Data data, Body parent) throws TemplateException {
		int pos = data.srcCode.getPos();

		// read 5 tokens (returntype,access modifier,"abstract|final|static","function", function name)
		String str = variableDec(data, false);
		// if there is no token at all we have no function
		if (str == null) {
			data.srcCode.setPos(pos);
			return null;
		}
		comments(data);
		String[] tokens = new String[] { str, null, null, null, null };

		tokens[1] = variableDec(data, false);
		comments(data);
		if (tokens[1] != null) {
			tokens[2] = variableDec(data, false);
			comments(data);
			if (tokens[2] != null) {
				tokens[3] = variableDec(data, false);
				comments(data);
				if (tokens[3] != null) {
					tokens[4] = identifier(data, false);
					comments(data);
				}
			}
		}

		// function name
		String functionName = null;
		for (int i = tokens.length - 1; i >= 0; i--) {
			// first from right is the function name
			if (tokens[i] != null) {
				functionName = tokens[i];
				tokens[i] = null;
				break;
			}
		}
		if (functionName == null || functionName.indexOf(',') != -1 || functionName.indexOf('[') != -1) {
			data.srcCode.setPos(pos);
			return null;
		}
		// throw new TemplateException(data.srcCode, "invalid syntax");

		String returnType = null;

		// search for "function"
		boolean hasOthers = false, first = true;
		for (int i = tokens.length - 1; i >= 0; i--) {
			if ("function".equalsIgnoreCase(tokens[i])) {
				// if it is the first "function" (from right) and we had already something else, the syntax is
				// broken!
				if (hasOthers && first) throw new TemplateException(data.srcCode, "invalid syntax");
				// we already have a return type,so this is the 3th "function"!
				else if (returnType != null) throw new TemplateException(data.srcCode, "invalid syntax");
				else if (!first) returnType = tokens[i];
				first = false;
				tokens[i] = null;
			}
			else if (tokens[i] != null) {
				hasOthers = true;
			}
		}
		// no "function" found
		if (first) {
			data.srcCode.setPos(pos);
			return null;
		}

		// access modifier
		int _access, access = -1;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null && (_access = ComponentUtil.toIntAccess(tokens[i], -1)) != -1) {
				// we already have an access modifier
				if (access != -1) {
					// we already have a return type
					if (returnType != null) throw new TemplateException(data.srcCode, "invalid syntax");
					returnType = tokens[i];
				}
				else access = _access;
				tokens[i] = null;

			}
		}
		// no access defined
		if (access == -1) access = Component.ACCESS_PUBLIC;

		// Non access modifier
		int _modifier, modifier = Component.MODIFIER_NONE;
		boolean isStatic = false;
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null) {
				_modifier = ComponentUtil.toModifier(tokens[i], Component.MODIFIER_NONE, Component.MODIFIER_NONE);

				// abstract|final
				if (_modifier != Component.MODIFIER_NONE) {
					// we already have an Non access modifier
					if (modifier != Component.MODIFIER_NONE || isStatic) {
						// we already have a return type
						if (returnType != null) throw new TemplateException(data.srcCode, "invalid syntax");
						returnType = tokens[i];
					}
					else modifier = _modifier;
					tokens[i] = null;
				}
				// static
				else if (tokens[i].equalsIgnoreCase("static")) {
					// we already have an Non access modifier
					if (modifier != Component.MODIFIER_NONE || isStatic) {
						// we already have a return type
						if (returnType != null) throw new TemplateException(data.srcCode, "invalid syntax");
						returnType = tokens[i];
					}
					else isStatic = true;
					tokens[i] = null;
				}
			}
		}

		// return type
		for (int i = 0; i < tokens.length; i++) {
			if (tokens[i] != null) {
				if (returnType != null) throw new TemplateException(data.srcCode, "invalid syntax");
				returnType = tokens[i];
			}
		}

		Position line = data.srcCode.getPosition();

		// comments(data);

		// Name
		if (!data.isCFC && !data.isInterface) {
			FunctionLibFunction flf = getFLF(data, functionName);
			try {
				if (flf != null && flf.getFunctionClassDefinition().getClazz() != CFFunction.class) {
					PageSource ps = null;
					if (data.srcCode instanceof PageSourceCode) {
						ps = ((PageSourceCode) data.srcCode).getPageSource();
					}

					String path = null;
					if (ps != null) {
						path = ps.getDisplayPath();
						path = path.replace('\\', '/');
					}
					if (path == null || path.indexOf("/library/function/") == -1)// TODO make better
						throw new TemplateException(data.srcCode, "The name [" + functionName + "] is already used by a built in Function");
				}
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw new PageRuntimeException(Caster.toPageException(t));
			}
		}
		Function res = closurePart(data, functionName, access, modifier, returnType, line, false);
		if (isStatic) {

			if (data.context == CTX_INTERFACE) throw new TemplateException(data.srcCode, "static functions are not allowed within the interface body");

			TagOther tag = createStaticTag(data, res.getStart());

			tag.getBody().addStatement(res);
			return tag;
		}
		return res;
	}

	@Override
	public ArrayList<Argument> getScriptFunctionArguments(Data data) throws TemplateException {
		// arguments
		LitBoolean passByRef;
		Expression displayName;
		Expression hint;
		Map<String, Attribute> meta;
		String _name;
		ArrayList<Argument> result = new ArrayList<Argument>();
		do {
			comments(data);
			// finish
			if (data.srcCode.isCurrent(')')) break;

			// attribute

			// name
			// String idName=identifier(data,false,true);
			boolean required = false;

			String idName = variableDec(data, false);
			// required
			if ("required".equalsIgnoreCase(idName)) {
				comments(data);
				String idName2 = variableDec(data, false);
				if (idName2 != null) {
					idName = idName2;
					required = true;
				}
				if (idName == null) throw new TemplateException(data.srcCode, "invalid argument definition");
			}

			String typeName = "any";
			if (idName == null) throw new TemplateException(data.srcCode, "invalid argument definition");
			comments(data);
			if (!data.srcCode.isCurrent(')') && !data.srcCode.isCurrent('=') && !data.srcCode.isCurrent(':') && !data.srcCode.isCurrent(',')) {
				typeName = idName;
				idName = identifier(data, false); // MUST was upper case before, is this a problem?
			}
			else if (idName.indexOf('.') != -1 || idName.indexOf('[') != -1) {
				throw new TemplateException(data.srcCode, "invalid argument name [" + idName + "] definition");
			}
			if (idName == null) throw new TemplateException(data.srcCode, "invalid argument definition");

			comments(data);
			Expression defaultValue;

			if (data.srcCode.isCurrent('=') || data.srcCode.isCurrent(':')) {
				data.srcCode.next();
				comments(data);
				defaultValue = expression(data);
			}
			else defaultValue = null;

			// assign meta data defined in doc comment
			passByRef = data.factory.TRUE();
			displayName = data.factory.EMPTY();
			hint = data.factory.EMPTY();
			meta = null;
			if (data.docComment != null) {
				Map<String, Attribute> params = data.docComment.getParams();
				Attribute[] attrs = params.values().toArray(new Attribute[params.size()]);
				Attribute attr;
				String name;

				for (int i = 0; i < attrs.length; i++) {
					attr = attrs[i];
					name = attr.getName();
					// hint
					if (idName.equalsIgnoreCase(name) || name.equalsIgnoreCase(idName + ".hint")) {
						hint = data.factory.toExprString(attr.getValue());
						params.remove(name);
					}
					// meta
					if (StringUtil.startsWithIgnoreCase(name, idName + ".")) {
						if (name.length() > idName.length() + 1) {
							if (meta == null) meta = new HashMap<String, Attribute>();
							_name = name.substring(idName.length() + 1);
							meta.put(_name, new Attribute(attr.isDynamicType(), _name, attr.getValue(), attr.getType()));
						}
						params.remove(name);
					}
				}
			}

			// argument attributes
			Attribute[] _attrs = attributes(null, null, data, COMMA_ENDBRACKED, data.factory.EMPTY(), Boolean.TRUE, null, false, NO_ATTR_SEP, true);

			Attribute _attr;
			if (!ArrayUtil.isEmpty(_attrs)) {
				if (meta == null) meta = new HashMap<String, Attribute>();
				for (int i = 0; i < _attrs.length; i++) {
					_attr = _attrs[i];
					meta.put(_attr.getName(), _attr);
				}
			}

			result.add(new Argument(data.factory.createLitString(idName), data.factory.createLitString(typeName), data.factory.createLitBoolean(required), defaultValue, passByRef,
					displayName, hint, meta));

			comments(data);
		}
		while (data.srcCode.forwardIfCurrent(','));
		return result;
	}

	@Override
	protected final Function closurePart(Data data, String id, int access, int modifier, String rtnType, Position line, boolean closure) throws TemplateException {
		Body body = new FunctionBody(data.factory);
		Function func = closure ? new Closure(id, access, modifier, rtnType, body, line, null) : new FunctionImpl(id, access, modifier, rtnType, body, line, null);

		comments(data);
		if (!data.srcCode.forwardIfCurrent('(')) throw new TemplateException(data.srcCode, "invalid syntax in function head, missing begin [(]");

		// arguments
		ArrayList<Argument> args = getScriptFunctionArguments(data);

		for (Argument arg: args) {
			func.addArgument(arg.getName(), arg.getType(), arg.getRequired(), arg.getDefaultValue(), arg.isPassByReference(), arg.getDisplayName(), arg.getHint(),
					arg.getMetaData());
		}
		// end )
		comments(data);
		if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "invalid syntax in function head, missing ending [)]");

		// TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode,"function");

		// doc comment
		String hint = null;
		if (data.docComment != null) {
			func.setHint(data.factory, hint = data.docComment.getHint());
			func.setMetaData(data.docComment.getParams());
			data.docComment = null;
		}

		comments(data);

		// attributes
		Attribute[] attrs = attributes(null, null, data, SEMI_BLOCK, data.factory.EMPTY(), Boolean.TRUE, null, false, NO_ATTR_SEP, true);
		boolean isJava = false;
		for (Attribute attr: attrs) {
			// check type
			if ("type".equalsIgnoreCase(attr.getName())) {
				if (attr.getValue() instanceof LitString) {
					if (((LitString) attr.getValue()).getString().equalsIgnoreCase("java")) isJava = true;
				}
				else throw new TemplateException(data.srcCode, "attribute type must be a literal string, ");
			}

			func.addAttribute(attr);
		}

		// body
		boolean oldInsideFunction = data.insideFunction;
		data.insideFunction = true;
		try {
			// ex block
			Body prior = data.setParent(body);
			if (isJava) {

				// return type
				Literal lit = extract(attrs, "returntype");
				if (lit != null) {
					rtnType = lit.getString();
					attrs = remove(attrs, "returntype");
				}

				// output
				Boolean output = null;
				lit = extract(attrs, "output");
				if (lit != null) {
					output = lit.getBoolean(null);
					attrs = remove(attrs, "output");
				}

				// bufferoutput
				Boolean bufferOutput = null;
				lit = extract(attrs, "bufferoutput");
				if (lit != null) {
					bufferOutput = lit.getBoolean(null);
					attrs = remove(attrs, "bufferoutput");
				}

				// modifier
				lit = extract(attrs, "modifier");
				if (lit != null) {
					modifier = ComponentUtil.toModifier(lit.getString(), Component.MODIFIER_NONE, modifier);
					attrs = remove(attrs, "modifier");
				}

				// access
				lit = extract(attrs, "access");
				if (lit != null) {
					access = ComponentUtil.toIntAccess(lit.getString(), access);
					attrs = remove(attrs, "access");
				}

				// displayname
				String displayName = null;
				lit = extract(attrs, "displayname");
				if (lit != null) {
					displayName = lit.getString();
					attrs = remove(attrs, "displayname");
				}

				// hint
				lit = extract(attrs, "hint");
				if (lit != null) {
					hint = lit.getString();
					attrs = remove(attrs, "hint");
				}

				// description
				String description = null;
				lit = extract(attrs, "description");
				if (lit != null) {
					description = lit.getString();
					attrs = remove(attrs, "description");
				}

				// returnFormat
				int returnFormat = UDF.RETURN_FORMAT_WDDX;
				lit = extract(attrs, "returnformat");
				if (lit != null) {
					returnFormat = UDFUtil.toReturnFormat(lit.getString(), returnFormat);
					attrs = remove(attrs, "returnformat");
				}

				// secureJson
				Boolean secureJson = null;
				lit = extract(attrs, "securejson");
				if (lit != null) {
					secureJson = lit.getBoolean(null);
					attrs = remove(attrs, "securejson");
				}

				// verifyClient
				Boolean verifyClient = null;
				lit = extract(attrs, "verifyclient");
				if (lit != null) {
					verifyClient = lit.getBoolean(null);
					attrs = remove(attrs, "verifyclient");
				}

				// localMode
				int localMode = Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS;
				lit = extract(attrs, "localmode");
				if (lit != null) {
					localMode = AppListenerUtil.toLocalMode(lit.getString(), Undefined.MODE_LOCAL_OR_ARGUMENTS_ONLY_WHEN_EXISTS);
					attrs = remove(attrs, "localmode");
				}

				// TODO cachedwithin

				func.setJavaFunction(java(data, body, id, access, modifier, hint, args, attrs, rtnType, output, bufferOutput, displayName, description, returnFormat, secureJson,
						verifyClient, localMode));
			}
			else {
				func.register(data.page);
				statement(data, body, CTX_FUNCTION);
			}
			data.setParent(prior);
		}
		finally {
			data.insideFunction = oldInsideFunction;
		}
		func.setEnd(data.srcCode.getPosition());

		if (closure) comments(data);

		return func;
	}

	private Literal extract(Attribute[] attrs, String name) {
		if (attrs == null) return null;

		for (Attribute attr: attrs) {
			if (name.equalsIgnoreCase(attr.getName())) {
				Expression val = attr.getValue();
				if (val instanceof Literal) return (Literal) val;
				break;
			}
		}
		return null;
	}

	private Attribute[] remove(Attribute[] attrs, String name) {
		if (attrs == null) return null;

		List<Attribute> list = new ArrayList<Attribute>();
		for (Attribute attr: attrs) {
			if (!name.equalsIgnoreCase(attr.getName())) {
				list.add(attr);
			}
		}
		return list.toArray(new Attribute[list.size()]);
	}

	private JavaFunction java(Data data, Body body, String functionName, int access, int modifier, String hint, ArrayList<Argument> args, Attribute[] attrs, String rtnType,
			Boolean output, Boolean bufferOutput, String displayName, String description, int returnFormat, Boolean secureJson, Boolean verifyClient, int localMode)
			throws TemplateException {

		FunctionDef fd = null;
		try {
			fd = FunctionDefFactory.getFunctionDef(args, rtnType);
		}
		catch (JavaSourceException e) {
			throw new TemplateException(data.srcCode, e.getMessage());
		}
		catch (ClassException e) {
			throw new TemplateException(data.srcCode, e.getMessage());
		}

		PageSourceCode psc = (PageSourceCode) data.srcCode;// TODO get PS in an other way
		PageSource ps = psc.getPageSource();

		SourceCode sc = data.srcCode;
		Position start = sc.getPosition();
		findTheEnd(data, start.line);
		Position end = sc.getPosition();
		String javaCode = sc.substring(start.pos, end.pos - start.pos);
		try {
			String id = data.page.registerJavaFunctionName(functionName);

			JavaFunction jf = JavaCCompiler.compile(ps, fd.createSourceCode(ps, javaCode, id, functionName, access, modifier, hint, args, output, bufferOutput, displayName,
					description, returnFormat, secureJson, verifyClient, localMode));
			// print.e("-->" + (jf.byteCode == null ? -1 : jf.byteCode.length));
			// jf.setTemplateName(ps.getRealpathWithVirtual());
			// jf.setFunctionName(fn);
			return jf;
		}
		catch (JavaCompilerException e) {
			TemplateException te = new TemplateException(data.srcCode, (int) (start.line + e.getLineNumber()), (int) e.getColumnNumber(), e.getMessage());
			te.setStackTrace(e.getStackTrace());
			throw te;
		}
		catch (Exception e) {
			TemplateException te = new TemplateException(data.srcCode, start.line, 0, e.getMessage());
			te.setStackTrace(e.getStackTrace());
			throw te;
		}

	}

	private void findTheEnd(Data data, int lineOffset) throws TemplateException {
		comments(data);
		SourceCode sc = data.srcCode;

		if (!sc.forwardIfCurrent('{')) throw new TemplateException(sc, "missing starting {");

		boolean insideD = false;
		boolean insideS = false;
		int depth = 0;
		char c;
		do {
			c = sc.getCurrent();
			if (insideD) {
				if (c == '"') {
					if (!sc.isPrevious('\\')) insideD = false;
				}
			}
			// only can have a single character escaped or not
			else if (insideS) {
				if (c == '\'') {
					if (!sc.isPrevious('\\')) insideS = false;
				}
			}
			else {
				if (c == '{') {
					depth++;
				}
				else if (c == '}') {
					if (depth == 0) {
						sc.next();
						return;
					}
					else depth--;
				}
				else if (c == '\'') {
					insideS = true;
				}
				else if (c == '"') {
					insideD = true;
				}
			}
			if (sc.hasNext()) sc.next();
			else {
				Position pos = sc.getPosition();
				throw new TemplateException(sc, pos.line + lineOffset, pos.column, "reached end without finding the ending }");
			}
		}
		while (true);
	}

	@Override
	protected final Function lambdaPart(Data data, String id, int access, int modifier, String rtnType, Position line, ArrayList<Argument> args) throws TemplateException {
		Body body = new FunctionBody(data.factory);
		Function func = new Lambda(data.page, id, access, modifier, rtnType, body, line, null);
		func.register(data.page);
		comments(data);

		// add arguments
		for (Argument arg: args) {
			func.addArgument(arg.getName(), arg.getType(), arg.getRequired(), arg.getDefaultValue(), arg.isPassByReference(), arg.getDisplayName(), arg.getHint(),
					arg.getMetaData());
		}

		comments(data);

		// body
		boolean oldInsideFunction = data.insideFunction;
		data.insideFunction = true;

		try {
			if (data.srcCode.isCurrent('{')) {
				Body prior = data.setParent(body);
				statement(data, body, CTX_FUNCTION);

				data.setParent(prior);
			}
			else {
				if (data.srcCode.forwardIfCurrent("return ")) {
					comments(data);
				}

				// ex block
				short prior = data.context;
				data.context = CTX_FUNCTION;
				comments(data);
				Expression expr = expression(data);
				Return rtn = new Return(expr, line, data.srcCode.getPosition());
				body.addStatement(rtn);
				data.docComment = null;
				data.context = prior;

			}
		}
		finally {
			data.insideFunction = oldInsideFunction;
		}

		func.setEnd(data.srcCode.getPosition());

		comments(data);

		return func;

	}

	private Statement tagStatement(Data data, Body parent) throws TemplateException {
		Statement child;
		for (int i = 0; i < data.scriptTags.length; i++) {
			// single
			if (data.scriptTags[i].getScript().getType() == TagLibTagScript.TYPE_SINGLE) {
				if ((child = _singleAttrStatement(parent, data, data.scriptTags[i])) != null) return child;
			}
			// multiple
			else {// if(tags[i].getScript().getType()==TagLibTagScript.TYPE_MULTIPLE) {
				if ((child = _multiAttrStatement(parent, data, data.scriptTags[i])) != null) return child;
			}
		}
		return null;
	}

	private final Statement _multiAttrStatement(Body parent, Data data, TagLibTag tlt) throws TemplateException {
		int pos = data.srcCode.getPos();
		try {
			return __multiAttrStatement(parent, data, tlt);
		}
		catch (ProcessingDirectiveException e) {
			throw e;
		}
		catch (TemplateException e) {
			try {
				data.srcCode.setPos(pos);
				return expressionStatement(data, parent);
			}
			catch (TemplateException e1) {
				if (tlt.getScript().getContext() == CTX_CFC) throw new ComponentTemplateException(e);
				throw e;
			}
		}
	}

	private final Tag __multiAttrStatement(Body parent, Data data, TagLibTag tlt) throws TemplateException {
		if (data.ep == null) return null;
		int pos = data.srcCode.getPos();
		String type = tlt.getName();
		String appendix = null;
		if (data.srcCode.forwardIfCurrent(type) ||

		// lucee dialect support component as alias for class
				(data.srcCode.getDialect() == CFMLEngine.DIALECT_LUCEE && type.equalsIgnoreCase(Constants.LUCEE_COMPONENT_TAG_NAME)
						&& data.srcCode.forwardIfCurrent(Constants.CFML_COMPONENT_TAG_NAME))) {

			if (tlt.hasAppendix()) {
				appendix = CFMLTransformer.identifier(data.srcCode, false, true);
				if (StringUtil.isEmpty(appendix)) {
					data.srcCode.setPos(pos);
					return null;
				}

			}

			boolean isValid = (data.srcCode.isCurrent(' ') || (tlt.getHasBody() && data.srcCode.isCurrent('{')));
			if (isValid && (data.srcCode.isCurrent(" ", "=") || data.srcCode.isCurrent(" ", "("))) { // simply avoid a later exception
				isValid = false;
			}
			if (!isValid) {
				data.srcCode.setPos(pos);
				// data.srcCode.setPos(data.srcCode.getPos() - type.length());
				return null;
			}
		}
		else return null;

		Position line = data.srcCode.getPosition(pos);

		TagLibTagScript script = tlt.getScript();
		// TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode,type);
		if (script.getContext() == CTX_CFC) data.isCFC = true;
		else if (script.getContext() == CTX_INTERFACE) data.isInterface = true;
		// Tag tag=new TagComponent(line);
		Tag tag = getTag(data, parent, tlt, line, null);
		tag.setTagLibTag(tlt);
		tag.setScriptBase(true);
		if (!StringUtil.isEmpty(appendix)) tag.setAppendix(appendix);

		// add component meta data
		if (data.isCFC) {
			addMetaData(data, tag, IGNORE_LIST_COMPONENT);
		}
		if (data.isInterface) {
			addMetaData(data, tag, IGNORE_LIST_INTERFACE);
		}
		// EvaluatorPool.getPool();
		comments(data);

		// attributes
		// attributes(func,data);
		Attribute[] attrs = attributes(tag, tlt, data, SEMI_BLOCK, data.factory.EMPTY(), script.getRtexpr() ? Boolean.TRUE : Boolean.FALSE, null, false, ',', false);

		for (int i = 0; i < attrs.length; i++) {
			tag.addAttribute(attrs[i]);
		}

		comments(data);

		// body
		if (tlt.getHasBody()) {
			Body body = new BodyBase(data.factory);
			Body prior = data.setParent(body);
			boolean wasSemiColon = statement(data, body, script.getContext());
			if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body);
			data.setParent(prior);

		}
		else checkSemiColonLineFeed(data, true, true, true);

		tag.setEnd(data.srcCode.getPosition());
		eval(tlt, data, tag);
		return tag;
	}

	private Statement cftagStatement(Data data, Body parent) throws TemplateException {
		if (data.ep == null) return null; // that is because cfloop-contition evaluator does not pass this

		final int start = data.srcCode.getPos();

		// namespace and separator
		final TagLib tagLib = CFMLTransformer.nameSpace(data);
		if (tagLib == null || !tagLib.isCore()) return null;

		// print.e("namespace:"+tagLib.getNameSpaceAndSeparator());

		// get the name of the tag
		String id = CFMLTransformer.identifier(data.srcCode, false, true);

		if (id == null) {
			data.srcCode.setPos(start);
			return null;
		}

		id = id.toLowerCase();
		String appendix = null;
		TagLibTag tlt = tagLib.getTag(id);

		/*
		 * Iterator<TagLibTag> it = tagLib.getTags().values().iterator(); while(it.hasNext()) { TagLibTag
		 * tmp = it.next(); if(tmp.getScript()==null) print.e(tmp.getFullName()); }
		 */

		// get taglib
		if (tlt == null) {
			tlt = tagLib.getAppendixTag(id);
			// print.e("appendix:"+tlt);

			if (tlt == null) {
				// if(tagLib.getIgnoreUnknowTags()){ if we do this an expression like the following no longer work
				// cfwhatever=1;
				data.srcCode.setPos(start);
				return null;
				// }
				// throw new TemplateException(data.srcCode,"undefined tag
				// ["+tagLib.getNameSpaceAndSeparator()+id+"]");
			}
			appendix = StringUtil.removeStartingIgnoreCase(id, tlt.getName());
		}
		if (tlt.getScript() == null) {
			data.srcCode.setPos(start);
			return null;
		}

		// check for opening bracked or closing semicolon
		comments(data);
		boolean noAttrs = false;
		if (!data.srcCode.forwardIfCurrent('(')) {
			if (checkSemiColonLineFeed(data, false, false, false)) {
				noAttrs = true;
			}
			else {
				data.srcCode.setPos(start);
				return null;
			}
		}

		Position line = data.srcCode.getPosition();

		// script specific behavior
		short context = CTX_OTHER;
		Boolean allowExpression = Boolean.TRUE;
		{

			TagLibTagScript script = tlt.getScript();
			if (script != null) {
				context = script.getContext();
				// always true for this tags allowExpression=script.getRtexpr()?Boolean.TRUE:Boolean.FALSE;
				if (context == CTX_CFC) data.isCFC = true;
				else if (context == CTX_INTERFACE) data.isInterface = true;
			}
		}

		Tag tag = getTag(data, parent, tlt, line, null);
		if (appendix != null) {
			tag.setAppendix(appendix);
			tag.setFullname(tlt.getFullName().concat(appendix));
		}
		else {
			tag.setFullname(tlt.getFullName());
		}

		tag.setTagLibTag(tlt);
		tag.setScriptBase(true);

		// add component meta data
		if (data.isCFC) {
			addMetaData(data, tag, IGNORE_LIST_COMPONENT);
		}
		if (data.isInterface) {
			addMetaData(data, tag, IGNORE_LIST_INTERFACE);
		}
		comments(data);

		// attributes
		Attribute[] attrs = noAttrs ? new Attribute[0] : attributes(tag, tlt, data, BRACKED, data.factory.EMPTY(), allowExpression, null, false, ',', true);
		data.srcCode.forwardIfCurrent(')');

		for (int i = 0; i < attrs.length; i++) {
			tag.addAttribute(attrs[i]);
		}

		comments(data);

		// body
		if (tlt.getHasBody()) {
			Body body = new BodyBase(data.factory);
			Body prior = data.setParent(body);
			boolean wasSemiColon = statement(data, body, context);
			if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body);
			data.setParent(prior);
		}
		else checkSemiColonLineFeed(data, true, true, true);

		tag.setEnd(data.srcCode.getPosition());
		eval(tlt, data, tag);
		return tag;
	}

	private final void addMetaData(Data data, Tag tag, String[] ignoreList) {
		if (data.docComment == null) return;

		tag.addMetaData(data.docComment.getHintAsAttribute(data.factory));

		Map<String, Attribute> params = data.docComment.getParams();
		Iterator<Attribute> it = params.values().iterator();
		Attribute attr;
		outer: while (it.hasNext()) {
			attr = it.next();
			// ignore list
			if (!ArrayUtil.isEmpty(ignoreList)) {
				for (int i = 0; i < ignoreList.length; i++) {
					if (ignoreList[i].equalsIgnoreCase(attr.getName())) continue outer;
				}
			}
			tag.addMetaData(attr);
		}
		data.docComment = null;
	}

	private final Statement propertyStatement(Data data, Body parent) throws TemplateException {
		int pos = data.srcCode.getPos();
		try {
			return _propertyStatement(data, parent);
		}
		catch (TemplateException e) {
			try {
				data.srcCode.setPos(pos);
				return expressionStatement(data, parent);
			}
			catch (TemplateException e1) {
				throw e;
			}
		}
	}

	private final Tag _propertyStatement(Data data, Body parent) throws TemplateException {
		if (data.context != CTX_CFC || !data.srcCode.forwardIfCurrent("property ")) return null;
		Position line = data.srcCode.getPosition();

		TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode, "property", data.config.getIdentification());
		Tag property = new TagOther(data.factory, line, null);
		addMetaData(data, property, IGNORE_LIST_PROPERTY);

		boolean hasName = false, hasType = false;

		int pos = data.srcCode.getPos();
		String tmp = variableDec(data, true);
		if (!StringUtil.isEmpty(tmp)) {
			if (tmp.indexOf('.') != -1) {
				property.addAttribute(new Attribute(false, "type", data.factory.createLitString(tmp), "string"));
				hasType = true;
			}
			else {
				data.srcCode.setPos(pos);
			}
		}
		else data.srcCode.setPos(pos);

		// folgend wird tlt extra nicht uebergeben, sonst findet pruefung statt
		Attribute[] attrs = attributes(property, tlt, data, SEMI, data.factory.NULL(), Boolean.FALSE, "name", true, NO_ATTR_SEP, false);

		checkSemiColonLineFeed(data, true, true, false);

		property.setTagLibTag(tlt);
		property.setScriptBase(true);

		Attribute attr;

		// first fill all regular attribute -> name="value"
		for (int i = attrs.length - 1; i >= 0; i--) {
			attr = attrs[i];
			if (!attr.getValue().equals(data.factory.NULL())) {
				if (attr.getName().equalsIgnoreCase("name")) {
					hasName = true;
				}
				else if (attr.getName().equalsIgnoreCase("type")) {
					hasType = true;
				}
				property.addAttribute(attr);
			}
		}

		// now fill name named attributes -> attr1 attr2
		String first = null, second = null;
		for (int i = 0; i < attrs.length; i++) {
			attr = attrs[i];
			if (attr.getValue().equals(data.factory.NULL())) {
				// type
				if (first == null && ((!hasName && !hasType) || !hasName)) {
					first = attr.getNameOC();
				}
				// name
				else if (second == null && !hasName && !hasType) {
					second = attr.getNameOC();
				}
				// attr with no value
				else {
					attr = new Attribute(true, attr.getName(), data.factory.EMPTY(), "string");
					property.addAttribute(attr);
				}
			}
		}

		if (first != null) {
			hasName = true;
			if (second != null) {
				hasType = true;
				property.addAttribute(new Attribute(false, "name", data.factory.createLitString(second), "string"));
				property.addAttribute(new Attribute(false, "type", data.factory.createLitString(first), "string"));
			}
			else {
				property.addAttribute(new Attribute(false, "name", data.factory.createLitString(first), "string"));
			}
		}

		if (!hasType) {
			property.addAttribute(new Attribute(false, "type", data.factory.createLitString("any"), "string"));
		}
		if (!hasName) throw new TemplateException(data.srcCode, "missing name declaration for property");

		/*
		 * Tag property=new TagBase(line); property.setTagLibTag(tlt); property.addAttribute(new
		 * Attribute(false,"name",data.factory.createLitString(name),"string")); property.addAttribute(new
		 * Attribute(false,"type",data.factory.createLitString(type),"string"));
		 */
		property.setEnd(data.srcCode.getPosition());

		return property;
	}

	private final Tag staticStatement(Data data, Body parent) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("static", '{')) return null;
		// get one back to have again { so the parser works
		data.srcCode.previous();
		Position start = data.srcCode.getPosition();

		TagOther tag = createStaticTag(data, start);
		Body body = tag.getBody();
		Body prior = data.setParent(body);
		statement(data, body, CTX_STATIC);
		data.setParent(prior);
		return tag;
	}

	public static TagOther createStaticTag(Data data, Position start) throws TemplateException {
		TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode, "static", data.config.getIdentification());
		BodyBase body = new BodyBase(data.factory);
		TagOther tag = new TagOther(data.factory, start, data.srcCode.getPosition());
		tag.setTagLibTag(tlt);
		tag.setBody(body);
		data.ep.add(tlt, tag, data.flibs, data.srcCode);
		return tag;
	}

	public Statement paramStatement(Data data, Body parent) throws TemplateException {
		int pos = data.srcCode.getPos();
		try {
			return _paramStatement(data, parent);
		}
		catch (TemplateException e) {
			try {
				data.srcCode.setPos(pos);
				return expressionStatement(data, parent);
			}
			catch (TemplateException e1) {
				throw e;
			}
		}
	}

	private Tag _paramStatement(Data data, Body parent) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("param ")) return null;
		Position line = data.srcCode.getPosition();

		TagLibTag tlt = CFMLTransformer.getTLT(data.srcCode, "param", data.config.getIdentification());
		TagParam param = new TagParam(data.factory, line, null);

		// type
		boolean hasType = false;
		boolean hasName = false;
		int pos = data.srcCode.getPos();

		// first 2 arguments can be type/name directly
		String tmp = variableDec(data, true);
		do {
			if (!StringUtil.isEmpty(tmp)) {
				TagLibTagAttr attr = tlt.getAttribute(tmp.toLowerCase(), true);
				// name is not a defined attribute
				if (attr == null) {
					comments(data);

					// it could be a name followed by default value
					if (data.srcCode.forwardIfCurrent('=')) {
						comments(data);
						Expression v = attributeValue(data, true);
						param.addAttribute(new Attribute(false, "name", data.factory.createLitString(tmp), "string"));
						param.addAttribute(new Attribute(false, "default", v, "string"));
						hasName = true;
						break; // if we had a value this was already name
					}
					// can be type or name
					int pos2 = data.srcCode.getPos();

					// first could be type, followed by name
					comments(data);
					String tmp2 = variableDec(data, true);
					if (!StringUtil.isEmpty(tmp2)) {
						attr = tlt.getAttribute(tmp2.toLowerCase(), true);
						if (attr == null) {
							param.addAttribute(new Attribute(false, "name", data.factory.createLitString(tmp2), "string"));
							param.addAttribute(new Attribute(false, "type", data.factory.createLitString(tmp), "string"));
							if (data.srcCode.forwardIfCurrent('=')) {
								Expression v = attributeValue(data, true);
								param.addAttribute(new Attribute(false, "default", v, "string"));
							}

							hasName = true;
							hasType = true;
							break;
						}
					}
					param.addAttribute(new Attribute(false, "name", data.factory.createLitString(tmp), "string"));
					data.srcCode.setPos(pos2);
					hasName = true;
				}
				else data.srcCode.setPos(pos);
			}
			else data.srcCode.setPos(pos);
		}
		while (false);

		// folgend wird tlt extra nicht uebergeben, sonst findet pruefung statt
		Attribute[] attrs = attributes(param, tlt, data, SEMI, data.factory.NULL(), Boolean.TRUE, "name", true, ',', false);
		checkSemiColonLineFeed(data, true, true, true);

		param.setTagLibTag(tlt);
		param.setScriptBase(true);

		Attribute attr;

		// first fill all regular attribute -> name="value"
		boolean hasDynamic = false;
		for (int i = attrs.length - 1; i >= 0; i--) {
			attr = attrs[i];
			if (!attr.getValue().equals(data.factory.NULL())) {
				if (attr.getName().equalsIgnoreCase("name")) {
					hasName = true;
					param.addAttribute(attr);
				}
				else if (attr.getName().equalsIgnoreCase("type")) {
					hasType = true;
					param.addAttribute(attr);
				}
				else if (attr.isDynamicType()) {
					hasName = true;
					if (hasDynamic) throw attrNotSupported(data.srcCode, tlt, attr.getName());
					hasDynamic = true;
					param.addAttribute(new Attribute(false, "name", data.factory.createLitString(attr.getName()), "string"));
					param.addAttribute(new Attribute(false, "default", attr.getValue(), "any"));
				}
				else param.addAttribute(attr);
			}
		}

		// now fill name named attributes -> attr1 attr2
		String first = null, second = null;
		for (int i = 0; i < attrs.length; i++) {
			attr = attrs[i];

			if (attr.getValue().equals(data.factory.NULL())) {
				// type
				if (first == null && (!hasName || !hasType)) {
					first = attr.getName();
				}
				// name
				else if (second == null && !hasName && !hasType) {
					second = attr.getName();
				}
				// attr with no value
				else {
					attr = new Attribute(true, attr.getName(), data.factory.EMPTY(), "string");
					param.addAttribute(attr);
				}
			}
		}

		if (first != null) {
			if (second != null) {
				hasName = true;
				hasType = true;
				if (hasDynamic) throw attrNotSupported(data.srcCode, tlt, first);
				hasDynamic = true;
				param.addAttribute(new Attribute(false, "name", data.factory.createLitString(second), "string"));
				param.addAttribute(new Attribute(false, "type", data.factory.createLitString(first), "string"));
			}
			else {
				param.addAttribute(new Attribute(false, hasName ? "type" : "name", data.factory.createLitString(first), "string"));
				hasName = true;
			}
		}

		// if(!hasType)
		// param.addAttribute(ANY);

		if (!hasName) throw new TemplateException(data.srcCode, "missing name declaration for param");

		param.setEnd(data.srcCode.getPosition());
		return param;
	}

	private TemplateException attrNotSupported(SourceCode cfml, TagLibTag tag, String id) {
		String names = tag.getAttributeNames();
		if (StringUtil.isEmpty(names)) return new TemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]");

		return new TemplateException(cfml, "Attribute [" + id + "] is not allowed for statement [" + tag.getName() + "]", "valid attribute names are [" + names + "]");
	}

	private final String variableDec(Data data, boolean firstCanBeNumber) {

		String id = identifier(data, firstCanBeNumber);
		if (id == null) return null;

		StringBuffer rtn = new StringBuffer(id);
		data.srcCode.removeSpace();

		while (data.srcCode.forwardIfCurrent('.')) {
			data.srcCode.removeSpace();
			rtn.append('.');
			id = identifier(data, firstCanBeNumber);
			if (id == null) return null;
			rtn.append(id);
			data.srcCode.removeSpace();
		}

		while (data.srcCode.forwardIfCurrent("[]")) {
			data.srcCode.removeSpace();
			rtn.append("[]");
		}

		data.srcCode.revertRemoveSpace();

		return rtn.toString();
	}

	/**
	 * Liest ein return Statement ein. <br />
	 * EBNF:<br />
	 * <code>spaces expressionStatement spaces;</code>
	 * 
	 * @return return Statement
	 * @throws TemplateException
	 */
	private final Return returnStatement(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrentAndNoVarExt("return")) return null;

		Position line = data.srcCode.getPosition();
		Return rtn;

		comments(data);
		if (checkSemiColonLineFeed(data, false, false, false)) rtn = new Return(data.factory, line, data.srcCode.getPosition());
		else {
			Expression expr = expression(data);
			checkSemiColonLineFeed(data, true, true, false);
			rtn = new Return(expr, line, data.srcCode.getPosition());
		}
		comments(data);

		return rtn;
	}

	private final boolean islandStatement(Data data, Body parent) throws TemplateException {

		if (!data.srcCode.forwardIfCurrent(TAG_ISLAND_INDICATOR)) return false;
		// now we have to jump into the tag parser
		CFMLTransformer tag = new CFMLTransformer(true);
		tag.transform(data, parent);

		if (!data.srcCode.forwardIfCurrent(TAG_ISLAND_INDICATOR)) throw new TemplateException(data.srcCode, "missing closing tag indicator [" + TAG_ISLAND_INDICATOR + "]");
		comments(data);

		return true;
	}

	private final Statement _singleAttrStatement(Body parent, Data data, TagLibTag tlt) throws TemplateException {
		int pos = data.srcCode.getPos();
		try {
			return __singleAttrStatement(parent, data, tlt, false);
		}
		catch (ProcessingDirectiveException e) {
			throw e;
		}
		catch (TemplateException e) {
			data.srcCode.setPos(pos);
			try {
				return expressionStatement(data, parent);
			}
			catch (TemplateException e1) {
				throw e;
			}
		}
	}

	private final Statement __singleAttrStatement(Body parent, Data data, TagLibTag tlt, boolean allowTwiceAttr) throws TemplateException {
		String tagName = tlt.getName();
		if (data.srcCode.forwardIfCurrent(tagName)) {
			if (!data.srcCode.isCurrent(' ') && !data.srcCode.isCurrent(';')) {
				data.srcCode.setPos(data.srcCode.getPos() - tagName.length());
				return null;
			}
		}
		else return null;

		int pos = data.srcCode.getPos() - tagName.length();
		Position line = data.srcCode.getPosition();
		// TagLibTag tlt =
		// CFMLTransformer.getTLT(data.srcCode,tagName.equals("pageencoding")?"processingdirective":tagName);

		Tag tag = getTag(data, parent, tlt, line, null);
		tag.setScriptBase(true);
		tag.setTagLibTag(tlt);

		comments(data);

		// attribute
		TagLibTagAttr attr = tlt.getScript().getSingleAttr();
		String attrName = null;
		Expression attrValue = null;
		short attrType = ATTR_TYPE_NONE;
		if (attr != null) {
			attrType = attr.getScriptSupport();
			char c = data.srcCode.getCurrent();
			if (ATTR_TYPE_REQUIRED == attrType || (!data.srcCode.isCurrent(';') && ATTR_TYPE_OPTIONAL == attrType)) {
				if (data.srcCode.isCurrent('{')) {// this can be only a json string
					int p = data.srcCode.getPos();
					try {
						attrValue = isSimpleValue(attr.getType()) ? null : json(data, JSON_STRUCT, '{', '}');
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
						data.srcCode.setPos(p);
					}
				}
				else attrValue = attributeValue(data, tlt.getScript().getRtexpr());

				if (attrValue != null && isOperator(c)) {
					data.srcCode.setPos(pos);
					return null;
				}
			}
		}

		if (attrValue != null) {
			attrName = attr.getName();
			TagLibTagAttr tlta = tlt.getAttribute(attr.getName(), true);
			tag.addAttribute(new Attribute(false, attrName, data.factory.toExpression(attrValue, tlta.getType()), tlta.getType()));
		}
		else if (ATTR_TYPE_REQUIRED == attrType) {
			data.srcCode.setPos(pos);
			return null;
		}
		// body
		if (tlt.getHasBody()) {
			Body body = new BodyBase(data.factory);
			Body prior = data.setParent(body);
			boolean wasSemiColon = statement(data, body, tlt.getScript().getContext());
			if (!wasSemiColon || !tlt.isBodyFree() || body.hasStatements()) tag.setBody(body);
			data.setParent(prior);
		}
		else checkSemiColonLineFeed(data, true, true, true);

		if (tlt.hasTTE()) data.ep.add(tlt, tag, data.flibs, data.srcCode);

		if (!StringUtil.isEmpty(attrName))
			validateAttributeName(attrName, data.srcCode, new ArrayList<String>(), tlt, new RefBooleanImpl(false), new StringBuffer(), allowTwiceAttr);
		tag.setEnd(data.srcCode.getPosition());
		eval(tlt, data, tag);
		return tag;
	}

	private boolean isSimpleValue(String type) {
		return type.equalsIgnoreCase("string") || type.equalsIgnoreCase("boolean") || type.equalsIgnoreCase("number") || type.equalsIgnoreCase("numeric");
	}

	private boolean isOperator(char c) {
		return c == '=' || c == '+' || c == '-';
	}

	/*
	 * protected Statement __singleAttrStatement(Body parent, Data data, String tagName,String
	 * attrName,int attrType, boolean allowExpression, boolean allowTwiceAttr) throws TemplateException
	 * {
	 * 
	 * if(data.srcCode.forwardIfCurrent(tagName)){ if(!data.srcCode.isCurrent(' ') &&
	 * !data.srcCode.isCurrent(';')){ data.srcCode.setPos(data.srcCode.getPos()-tagName.length());
	 * return null; } } else return null;
	 * 
	 * 
	 * int pos=data.srcCode.getPos()-tagName.length(); int line=data.srcCode.getLine(); TagLibTag tlt =
	 * CFMLTransformer.getTLT(data.srcCode,tagName.equals("pageencoding")?"processingdirective":tagName)
	 * ;
	 * 
	 * Tag tag=getTag(parent,tlt,line); tag.setScriptBase(true); tag.setTagLibTag(tlt);
	 * 
	 * comments(data);
	 * 
	 * // attribute Expression attrValue=null; if(ATTR_TYPE_REQUIRED==attrType ||
	 * (!data.srcCode.isCurrent(';') && ATTR_TYPE_OPTIONAL==attrType)) attrValue =attributeValue(data,
	 * allowExpression); //allowExpression?super.expression(data):string(data);
	 * 
	 * if(attrValue!=null){ TagLibTagAttr tlta = tlt.getAttribute(attrName); tag.addAttribute(new
	 * Attribute(false,attrName,Cast.toExpression(attrValue,tlta.getType()),tlta.getType())); } else
	 * if(ATTR_TYPE_REQUIRED==attrType){ data.srcCode.setPos(pos); return null; }
	 * 
	 * checkSemiColonLineFeed(data,true); if(!StringUtil.isEmpty(tlt.getTteClassName()))data.ep.add(tlt,
	 * tag, data.fld, data.srcCode);
	 * 
	 * if(!StringUtil.isEmpty(attrName))validateAttributeName(attrName, data.srcCode, new
	 * ArrayList<String>(), tlt, new RefBooleanImpl(false), new StringBuffer(), allowTwiceAttr);
	 * 
	 * eval(tlt,data,tag); return tag; }
	 */

	private final void eval(TagLibTag tlt, Data data, Tag tag) throws TemplateException {
		if (tlt.hasTTE()) {
			try {
				tlt.getEvaluator().execute(data.config, tag, tlt, data.flibs, data);
			}
			catch (EvaluatorException e) {
				throw new TemplateException(data.srcCode, e.getMessage());
			}
			data.ep.add(tlt, tag, data.flibs, data.srcCode);
		}
	}

	private final Tag getTag(Data data, Body parent, TagLibTag tlt, Position start, Position end) throws TemplateException {
		try {
			Tag tag = tlt.getTag(data.factory, start, end);
			tag.setParent(parent);
			return tag;
		}
		catch (TagLibException e) {
			throw new TemplateException(data.srcCode, e);
		}
	}

	/**
	 * List mithilfe des data.CFMLExprTransformer einen Ausruck ein. <br />
	 * EBNF:<br />
	 * <code>expression ";";</code>
	 * 
	 * @param parent
	 * @return Ausdruck
	 * @throws TemplateException
	 */
	private Statement expressionStatement(Data data, Body parent) throws TemplateException {

		// first we check if we have an access modifier
		int pos = data.srcCode.getPos();
		int access = -1;
		boolean _final = false;
		if (data.context == CTX_CFC || data.context == CTX_STATIC) {
			if (data.srcCode.forwardIfCurrent("final ")) {
				_final = true;
				comments(data);
			}
			if (data.srcCode.forwardIfCurrent("private ")) {
				access = Component.ACCESS_PRIVATE;
				comments(data);
			}
			else if (data.srcCode.forwardIfCurrent("package ")) {
				access = Component.ACCESS_PACKAGE;
				comments(data);
			}
			else if (data.srcCode.forwardIfCurrent("public ")) {
				access = Component.ACCESS_PUBLIC;
				comments(data);
			}
			else if (data.srcCode.forwardIfCurrent("remote ")) {
				access = Component.ACCESS_REMOTE;
				// comments(data);
				throw new TemplateException(data.srcCode, "access modifier [remote] not supported in this context");

			}
			if (!_final && data.srcCode.forwardIfCurrent("final ")) {
				_final = true;
				comments(data);
			}
		}
		Body prior = data.setParent(parent);
		Expression expr = expression(data);
		data.setParent(prior);
		checkSemiColonLineFeed(data, true, true, false);

		// variable declaration (variable in body)
		if (expr instanceof Variable) {
			Variable v = (Variable) expr;
			if (ASMUtil.isOnlyDataMember(v)) {

				expr = new Assign(v, data.factory.createEmpty(), data.srcCode.getPosition());
			}
		}

		// if a specific access was defined
		if (access > -1 || _final) {
			if (!(expr instanceof Assign)) {
				data.srcCode.setPos(pos);
				throw new TemplateException(data.srcCode, "invalid syntax, access modifier cannot be used in this context");
			}
			if (access > -1) {
				// this is only supported with the Lucee dialect
				// if(data.srcCode.getDialect()==CFMLEngine.DIALECT_CFML)
				// throw new TemplateException(data.srcCode,
				// "invalid syntax, access modifier cannot be used in this context");
				((Assign) expr).setAccess(access);
			}
			if (_final) ((Assign) expr).setModifier(Member.MODIFIER_FINAL);
		}

		if (expr instanceof FunctionAsExpression) return ((FunctionAsExpression) expr).getFunction();

		return new ExpressionAsStatement(expr);
	}

	private final boolean checkSemiColonLineFeed(Data data, boolean throwError, boolean checkNLBefore, boolean allowEmptyCurlyBracked) throws TemplateException {
		comments(data);
		if (!data.srcCode.forwardIfCurrent(';')) {

			// curly brackets?
			if (allowEmptyCurlyBracked) {
				int pos = data.srcCode.getPos();
				if (data.srcCode.forwardIfCurrent('{')) {
					comments(data);
					if (data.srcCode.forwardIfCurrent('}')) return true;
					data.srcCode.setPos(pos);
				}
			}

			if ((!checkNLBefore || !data.srcCode.hasNLBefore()) && !isFinish(data) && !data.srcCode.isCurrent('}')) {
				if (!throwError) return false;
				throw new TemplateException(data.srcCode, "Missing [;] or [line feed] after expression");
			}
		}
		return true;
	}

	/**
	 * Ruft die Methode expression der zu vererbenten Klasse auf und prueft ob der Rueckgabewert einen
	 * boolschen Wert repraesentiert und castet den Wert allenfalls. <br />
	 * EBNF:<br />
	 * <code>TemplateException::expression;</code>
	 * 
	 * @return condition
	 * @throws TemplateException
	 */
	private final ExprBoolean condition(Data data) throws TemplateException {
		ExprBoolean condition = null;
		comments(data);
		condition = data.factory.toExprBoolean(super.expression(data));
		comments(data);
		return condition;
	}

	/**
	 * Liest eine try Block ein <br />
	 * EBNF:<br />
	 * <code>;</code>
	 * 
	 * @return Try Block
	 * @throws TemplateException
	 */
	private final TryCatchFinally tryStatement(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("try", '{') && !data.srcCode.forwardIfCurrent("try ") && !data.srcCode.forwardIfCurrent("try", '/')) return null;
		data.srcCode.previous();

		Body body = new BodyBase(data.factory);
		TryCatchFinally tryCatchFinally = new TryCatchFinally(data.factory, body, data.srcCode.getPosition(), null);

		Body prior = data.setParent(body);
		statement(data, body, CTX_TRY);
		data.setParent(prior);
		comments(data);

		// catches
		short catchCount = 0;
		while (data.srcCode.forwardIfCurrent("catch", '(')) {
			catchCount++;
			comments(data);

			// type
			int pos = data.srcCode.getPos();
			Position line = data.srcCode.getPosition();
			Expression name = null, type = null;

			StringBuffer sbType = new StringBuffer();
			String id;
			while (true) {
				id = identifier(data, false);
				if (id == null) break;
				sbType.append(id);
				data.srcCode.removeSpace();
				if (!data.srcCode.forwardIfCurrent('.')) break;
				sbType.append('.');
				data.srcCode.removeSpace();
			}

			if (sbType.length() == 0) {
				type = string(data);
				if (type == null) throw new TemplateException(data.srcCode, "a catch statement must begin with the throwing type (query, application ...).");
			}
			else {
				type = data.factory.createLitString(sbType.toString());
			}

			// name = expression();
			comments(data);

			// name
			if (!data.srcCode.isCurrent(')')) {
				name = expression(data);
			}
			else {
				data.srcCode.setPos(pos);
				name = expression(data);
				type = data.factory.createLitString("any");
			}
			comments(data);

			Body b = new BodyBase(data.factory);
			try {
				tryCatchFinally.addCatch(type, name, b, line);
			}
			catch (TransformerException e) {
				throw new TemplateException(data.srcCode, e.getMessage());
			}
			comments(data);

			if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "invalid catch statement, missing closing )");

			Body _prior = data.setParent(b);
			statement(data, b, CTX_CATCH);
			data.setParent(_prior);
			comments(data);
		}

		// finally
		if (finallyStatement(data, tryCatchFinally)) {
			comments(data);
		}
		else if (catchCount == 0) throw new TemplateException(data.srcCode, "a try statement must have at least one catch statement");

		// if(body.isEmpty()) return null;
		tryCatchFinally.setEnd(data.srcCode.getPosition());
		return tryCatchFinally;
	}

	/**
	 * Prueft ob sich der Zeiger am Ende eines Script Blockes befindet
	 * 
	 * @return Ende ScriptBlock?
	 * @throws TemplateException
	 */
	private final boolean isFinish(Data data) throws TemplateException {
		comments(data);
		if (data.tagName == null) return false;
		return data.srcCode.isCurrent("</", data.tagName);
	}

	/**
	 * Liest den Block mit Statements ein. <br />
	 * EBNF:<br />
	 * <code>"{" spaces {statements} "}" | statement;</code>
	 * 
	 * @param block
	 * @return was a block
	 * @throws TemplateException
	 */
	private final boolean block(Data data, Body body) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent('{')) return false;
		comments(data);
		if (data.srcCode.forwardIfCurrent('}')) {

			return true;
		}
		statements(data, body, false);

		if (!data.srcCode.forwardIfCurrent('}')) throw new TemplateException(data.srcCode, "Missing ending [}]");
		return true;
	}

	private final Attribute[] attributes(Tag tag, TagLibTag tlt, Data data, EndCondition endCond, Expression defaultValue, Object oAllowExpression, String ignoreAttrReqFor,
			boolean allowTwiceAttr, char attributeSeparator, boolean allowColonAsNameValueSeparator) throws TemplateException {
		Map<String, Attribute> attrs = new LinkedHashMap<String, Attribute>(); // need to be linked hashmap to keep the right order
		ArrayList<String> ids = new ArrayList<String>();
		while (data.srcCode.isValidIndex()) {
			data.srcCode.removeSpace();
			// if no more attributes break
			if (endCond.isEnd(data)) break;
			Attribute attr = attribute(tlt, data, ids, defaultValue, oAllowExpression, allowTwiceAttr, allowColonAsNameValueSeparator);
			attrs.put(attr.getName().toLowerCase(), attr);

			// seperator
			if (attributeSeparator > 0) {
				data.srcCode.removeSpace();
				data.srcCode.forwardIfCurrent(attributeSeparator);
			}

		}

		// not defined attributes
		if (tlt != null) {
			boolean hasAttributeCollection = false;
			Iterator<Attribute> iii = attrs.values().iterator();
			while (iii.hasNext()) {
				if ("attributecollection".equalsIgnoreCase(iii.next().getName())) {
					hasAttributeCollection = true;
					break;
				}
			}

			int type = tlt.getAttributeType();
			if (type == TagLibTag.ATTRIBUTE_TYPE_FIXED || type == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
				Map<String, TagLibTagAttr> hash = tlt.getAttributes();
				Iterator<Entry<String, TagLibTagAttr>> it = hash.entrySet().iterator();
				Entry<String, TagLibTagAttr> e;
				while (it.hasNext()) {
					e = it.next();
					TagLibTagAttr att = e.getValue();
					if (att.isRequired() && !contains(attrs.values(), att) && att.getDefaultValue() == null && !att.getName().equals(ignoreAttrReqFor)) {
						if (!hasAttributeCollection)
							throw new TemplateException(data.srcCode, "attribute [" + att.getName() + "] is required for statement [" + tlt.getName() + "]");
						if (tag != null) tag.addMissingAttribute(att);
					}
				}
			}
		}

		// set default values
		if (tlt != null && tlt.hasDefaultValue()) {
			Map<String, TagLibTagAttr> hash = tlt.getAttributes();
			Iterator<TagLibTagAttr> it = hash.values().iterator();
			TagLibTagAttr att;
			while (it.hasNext()) {
				att = it.next();
				if (!attrs.containsKey(att.getName().toLowerCase()) && att.hasDefaultValue()) {

					Attribute attr = new Attribute(tlt.getAttributeType() == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC, att.getName(),
							data.factory.toExpression(data.factory.createLitString(Caster.toString(att.getDefaultValue(), null)), att.getType()), att.getType());
					attr.setDefaultAttribute(true);
					attrs.put(att.getName().toLowerCase(), attr);
				}
			}
		}

		return attrs.values().toArray(new Attribute[attrs.size()]);
	}

	private final boolean contains(Collection<Attribute> attrs, TagLibTagAttr attr) {

		Iterator<Attribute> it = attrs.iterator();
		String name;
		String[] alias;
		while (it.hasNext()) {
			name = it.next().getName();

			// check name
			if (name.equals(attr.getName())) return true;

			// and aliases
			alias = attr.getAlias();
			if (!ArrayUtil.isEmpty(alias)) for (int i = 0; i < alias.length; i++) {
				if (alias[i].equals(attr.getName())) return true;
			}
		}

		return false;
	}

	private final Attribute attribute(TagLibTag tlt, Data data, ArrayList<String> args, Expression defaultValue, Object oAllowExpression, boolean allowTwiceAttr,
			boolean allowColonSeparator) throws TemplateException {
		StringBuffer sbType = new StringBuffer();
		RefBoolean dynamic = new RefBooleanImpl(false);

		// Name
		String name = attributeName(data.srcCode, args, tlt, dynamic, sbType, allowTwiceAttr, !allowColonSeparator);
		String nameLC = name == null ? null : name.toLowerCase();
		boolean allowExpression = false;
		if (oAllowExpression instanceof Boolean) allowExpression = ((Boolean) oAllowExpression).booleanValue();
		else if (oAllowExpression instanceof String) allowExpression = ((String) oAllowExpression).equalsIgnoreCase(nameLC);

		Expression value = null;

		comments(data);

		// value
		boolean hasValue = data.srcCode.forwardIfCurrent('=') || (allowColonSeparator && data.srcCode.forwardIfCurrent(':'));
		if (hasValue) {
			comments(data);
			value = attributeValue(data, allowExpression);

		}
		else {
			value = defaultValue;
		}
		comments(data);

		// Type
		TagLibTagAttr tlta = null;
		if (tlt != null) {
			tlta = tlt.getAttribute(nameLC, true);
			if (tlta != null && tlta.getName() != null) nameLC = tlta.getName();
		}
		return new Attribute(dynamic.toBooleanValue(), name, tlta != null ? data.factory.toExpression(value, tlta.getType()) : value, sbType.toString(), !hasValue);
	}

	private final String attributeName(SourceCode cfml, ArrayList<String> args, TagLibTag tag, RefBoolean dynamic, StringBuffer sbType, boolean allowTwiceAttr, boolean allowColon)
			throws TemplateException {
		String id = CFMLTransformer.identifier(cfml, true, allowColon);
		return validateAttributeName(id, cfml, args, tag, dynamic, sbType, allowTwiceAttr);
	}

	private final String validateAttributeName(String idOC, SourceCode cfml, ArrayList<String> args, TagLibTag tag, RefBoolean dynamic, StringBuffer sbType, boolean allowTwiceAttr)
			throws TemplateException {
		String idLC = idOC.toLowerCase();

		if (args.contains(idLC) && !allowTwiceAttr) throw new TemplateException(cfml, "you can't use the same attribute [" + idOC + "] twice");
		args.add(idLC);

		if (tag == null) return idOC;
		int typeDef = tag.getAttributeType();
		if ("attributecollection".equals(idLC)) {
			dynamic.setValue(tag.getAttribute(idLC, true) == null);
			sbType.append("struct");
		}
		else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED || typeDef == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
			TagLibTagAttr attr = tag.getAttribute(idLC, true);
			if (attr == null) {
				if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED) {
					String names = tag.getAttributeNames();
					if (StringUtil.isEmpty(names)) throw new TemplateException(cfml, "Attribute [" + idOC + "] is not allowed for tag [" + tag.getFullName() + "]");

					throw new TemplateException(cfml, "Attribute [" + idOC + "] is not allowed for statement [" + tag.getName() + "]", "Valid Attribute names are [" + names + "]");
				}
				dynamic.setValue(true);

			}
			else {
				idOC = attr.getName();
				idLC = idOC.toLowerCase();
				sbType.append(attr.getType());
				// parseExpression[0]=attr.getRtexpr();
			}
		}
		else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) {
			dynamic.setValue(true);
		}
		return idOC;
	}

	private final Expression attributeValue(Data data, boolean allowExpression) throws TemplateException {
		return allowExpression ? super.expression(data) : transformAsString(data, new String[] { " ", ";", "{" });
	}

	public static interface EndCondition {
		public boolean isEnd(Data data);
	}
}