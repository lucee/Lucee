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
package lucee.transformer.cfml.expression;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.Component;
import lucee.runtime.exp.CasterException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.functions.other.CreateUniqueId;
import lucee.runtime.op.Caster;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeSupport;
import lucee.runtime.type.util.UDFUtil;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.bytecode.expression.ExpressionInvoker;
import lucee.transformer.bytecode.expression.FunctionAsExpression;
import lucee.transformer.bytecode.expression.var.Argument;
import lucee.transformer.bytecode.expression.var.Assign;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.expression.var.Call;
import lucee.transformer.bytecode.expression.var.DynAssign;
import lucee.transformer.bytecode.expression.var.Func;
import lucee.transformer.bytecode.expression.var.FunctionMember;
import lucee.transformer.bytecode.expression.var.NamedArgument;
import lucee.transformer.bytecode.expression.var.UDF;
import lucee.transformer.bytecode.literal.Identifier;
import lucee.transformer.bytecode.literal.Null;
import lucee.transformer.bytecode.literal.NullConstant;
import lucee.transformer.bytecode.op.OpVariable;
import lucee.transformer.bytecode.statement.udf.Function;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.script.DocCommentTransformer;
import lucee.transformer.cfml.tag.CFMLTransformer;
import lucee.transformer.expression.ExprBoolean;
import lucee.transformer.expression.ExprDouble;
import lucee.transformer.expression.ExprString;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.Invoker;
import lucee.transformer.expression.literal.LitDouble;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.expression.literal.Literal;
import lucee.transformer.expression.var.DataMember;
import lucee.transformer.expression.var.Member;
import lucee.transformer.expression.var.Variable;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.library.tag.TagLibTagScript;
import lucee.transformer.util.SourceCode;

/**
 * 
 * 
 * Der CFMLExprTransfomer implementiert das Interface ExprTransfomer, er bildet die Parser Grammatik
 * ab, die unten definiert ist. Er erhaelt als Eingabe CFML Code, als String oder CFMLString, der
 * einen CFML Expression erhaelt und liefert ein CFXD Element zurueck, das diesen Ausdruck abbildet.
 * Mithilfe der FunctionLibs, kann er Funktionsaufrufe, die Teil eines Ausdruck sein koennen,
 * erkennen und validieren. Dies geschieht innerhalb der Methode function. Falls ein
 * Funktionsaufruf, einer Funktion innerhalb einer FunctionLib entspricht, werden diese
 * gegeneinander verglichen und der Aufruf wird als Built-In-Funktion uebernommen, andernfalls wird
 * der Funktionsaufruf als User-Defined-Funktion interpretiert. Die Klasse Cast, Operator und
 * ElementFactory (siehe 3.2) helfen ihm beim erstellen des Ausgabedokument CFXD.
 * 
 * <pre>
 * Parser Grammatik EBNF (Extended Backus-Naur Form) 

	transform      = spaces impOp;
	impOp          = eqvOp {"imp" spaces eqvOp};
	eqvOp          = xorOp {"eqv" spaces xorOp};
	xorOp          = orOp {"xor" spaces  orOp};
	orOp           = andOp {("or" | "||") spaces andOp}; 
			(* "||" Existiert in CFMX nicht *)
	andOp          = notOp {("and" | "&&") spaces notOp}; 
			(* "&&" Existiert in CFMX nicht *) 
	notOp          = [("not"|"!") spaces] decsionOp; 
			(* "!" Existiert in CFMX nicht *)
	decsionOp      = concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
	                 "contains"|"nct"|"does not contain") spaces concatOp}; 
			(* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)
	concatOp       = plusMinusOp {"&" spaces plusMinusOp};
	plusMinusOp    = modOp {("-"|"+") spaces modOp};
	modOp          = divMultiOp {("mod" | "%") spaces divMultiOp}; 
	                (* modulus operator , "%" Existiert in CFMX nicht *)
	divMultiOp     = expoOp {("*"|"/") spaces expoOp};
	expoOp         = clip {("exp"|"^") spaces clip}; 
	                (*exponent operator, " exp " Existiert in CFMX nicht *)
	clip           = ("(" spaces impOp ")" spaces) | checker;
	checker        = string | number | dynamic | sharp;
	string         = ("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") | 
	                 (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);
	number         = ["+"|"-"] digit {digit} {"." digit {digit}};
	digit          = "0"|..|"9";
	dynamic        = "true" | "false" | "yes" | "no" | startElement  
	                 {("." identifier | "[" structElement "]")[function] };
	startElement   = identifier "(" functionArg ")" | scope | identifier;
	scope          = "variable" | "cgi" | "url" | "form" | "session" | "application" | 
	                 "arguments" | "cookie" | "client ";
	identifier     = (letter | "_") {letter | "_"|digit};
	structElement  = "[" impOp "]";
	functionArg    = [impOp{"," impOp}];
	sharp          = "#" checker "#";
	spaces         = {space};
	space          = "\s"|"\t"|"\f"|"\t"|"\n";
	letter         = "a"|..|"z"|"A"|..|"Z";

{"x"}= 0 bis n mal "x"
["x"]= 0 bis 1 mal "x"
("x" | "y")"z" = "xz" oder "yz"
 * 
 * </pre>
 *
 */
public abstract class AbstrCFMLExprTransformer {

	private static final short STATIC = 0;
	private static final short DYNAMIC = 1;
	private static FunctionLibFunction GET_STATIC_SCOPE = null;
	private static FunctionLibFunction GET_SUPER_STATIC_SCOPE = null;
	private static FunctionLibFunction JSON_ARRAY = null;
	protected static FunctionLibFunction JSON_STRUCT = null;

	public static final short CTX_OTHER = TagLibTagScript.CTX_OTHER;
	public static final short CTX_NONE = TagLibTagScript.CTX_NONE;
	public static final short CTX_IF = TagLibTagScript.CTX_IF;
	public static final short CTX_ELSE_IF = TagLibTagScript.CTX_ELSE_IF;
	public static final short CTX_ELSE = TagLibTagScript.CTX_ELSE;
	public static final short CTX_FOR = TagLibTagScript.CTX_FOR;
	public static final short CTX_WHILE = TagLibTagScript.CTX_WHILE;
	public static final short CTX_DO_WHILE = TagLibTagScript.CTX_DO_WHILE;
	public static final short CTX_CFC = TagLibTagScript.CTX_CFC;
	public static final short CTX_INTERFACE = TagLibTagScript.CTX_INTERFACE;
	public static final short CTX_FUNCTION = TagLibTagScript.CTX_FUNCTION;
	public static final short CTX_BLOCK = TagLibTagScript.CTX_BLOCK;
	public static final short CTX_FINALLY = TagLibTagScript.CTX_FINALLY;
	public static final short CTX_SWITCH = TagLibTagScript.CTX_SWITCH;
	public static final short CTX_TRY = TagLibTagScript.CTX_TRY;
	public static final short CTX_CATCH = TagLibTagScript.CTX_CATCH;
	public static final short CTX_TRANSACTION = TagLibTagScript.CTX_TRANSACTION;
	public static final short CTX_THREAD = TagLibTagScript.CTX_THREAD;
	public static final short CTX_SAVECONTENT = TagLibTagScript.CTX_SAVECONTENT;
	public static final short CTX_LOCK = TagLibTagScript.CTX_LOCK;
	public static final short CTX_LOOP = TagLibTagScript.CTX_LOOP;
	public static final short CTX_QUERY = TagLibTagScript.CTX_QUERY;
	public static final short CTX_ZIP = TagLibTagScript.CTX_ZIP;
	public static final short CTX_STATIC = TagLibTagScript.CTX_STATIC;

	private DocCommentTransformer docCommentTransformer = new DocCommentTransformer();

	protected short ATTR_TYPE_NONE = TagLibTagAttr.SCRIPT_SUPPORT_NONE;
	protected short ATTR_TYPE_OPTIONAL = TagLibTagAttr.SCRIPT_SUPPORT_OPTIONAL;
	protected short ATTR_TYPE_REQUIRED = TagLibTagAttr.SCRIPT_SUPPORT_REQUIRED;

	protected static EndCondition SEMI_BLOCK = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent('{') || data.srcCode.isCurrent(';');
		}
	};
	protected static EndCondition SEMI = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent(';');
		}
	};
	protected static EndCondition COMMA_ENDBRACKED = new EndCondition() {
		@Override
		public boolean isEnd(Data data) {
			return data.srcCode.isCurrent(',') || data.srcCode.isCurrent(')');
		}
	};

	public static interface EndCondition {
		public boolean isEnd(Data data);
	}

	/*
	 * private short mode=0; protected CFMLString cfml; protected FunctionLib[] fld; private boolean
	 * ignoreScopes=false; private boolean allowLowerThan;
	 */

	/*
	 * public class Data extends Data {
	 * 
	 * public short mode=0; public boolean insideFunction; public String tagName; public boolean isCFC;
	 * public boolean isInterface; public short context=CTX_NONE; public DocComment docComment;
	 * 
	 * 
	 * public Data(Data data,boolean allowLowerThan) {
	 * super(data.factory,data.root,data.srcCode,data.ep,data.settings,data.tlibs,data.flibs,data.
	 * scriptTags,allowLowerThan); } }
	 */

	protected Expression transformAsString(Data data, String[] breakConditions) throws TemplateException {
		Expression el = null;

		// parse the houle Page String
		comments(data);

		// String
		if ((el = string(data)) != null) {
			data.mode = STATIC;
			return el;
		}
		// Sharp
		if ((el = sharp(data)) != null) {
			data.mode = DYNAMIC;
			return el;
		}
		// Simple
		return simple(data, breakConditions);
	}

	/**
	 * Initialmethode, wird aufgerufen um den internen Zustand des Objektes zu setzten.
	 * 
	 * @param fld Function Libraries zum validieren der Funktionen
	 * @param cfml CFML Code der transfomiert werden soll.
	 */

	protected Data init(Data data) {
		if (JSON_ARRAY == null) JSON_ARRAY = getFLF(data, "_literalArray");
		if (JSON_STRUCT == null) JSON_STRUCT = getFLF(data, "_literalStruct");
		if (GET_STATIC_SCOPE == null) GET_STATIC_SCOPE = getFLF(data, "_getStaticScope");
		if (GET_SUPER_STATIC_SCOPE == null) GET_SUPER_STATIC_SCOPE = getFLF(data, "_getSuperStaticScope");
		return data;
	}

	/*
	 * protected Data init(Factory factory,Root root,EvaluatorPool ep,TagLib[][] tld, FunctionLib[]
	 * fld,TagLibTag[] scriptTags, SourceCode cfml, TransfomerSettings settings, boolean allowLowerThan,
	 * int x) { Data data = new Data(factory,root,ep,cfml,tld,fld,settings,allowLowerThan,scriptTags);
	 * if(JSON_ARRAY==null)JSON_ARRAY=getFLF(data,"_literalArray");
	 * if(JSON_STRUCT==null)JSON_STRUCT=getFLF(data,"_literalStruct");
	 * if(GET_STATIC_SCOPE==null)GET_STATIC_SCOPE=getFLF(data,"_getStaticScope");
	 * if(GET_SUPER_STATIC_SCOPE==null)GET_SUPER_STATIC_SCOPE=getFLF(data,"_getSuperStaticScope");
	 * return data; }
	 */

	/**
	 * Startpunkt zum transfomieren einer Expression, ohne dass das Objekt neu initialisiert wird, dient
	 * vererbten Objekten als Einstiegspunkt.
	 * 
	 * @return Element
	 * @throws TemplateException
	 */
	protected Expression expression(Data data) throws TemplateException {
		return assignOp(data);
	}

	/**
	 * Liest einen gelableten Funktionsparamter ein <br />
	 * EBNF:<br />
	 * <code>assignOp [":" spaces assignOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Argument functionArgument(Data data, boolean varKeyUpperCase) throws TemplateException {
		return functionArgument(data, null, varKeyUpperCase);
	}

	private Argument functionArgument(Data data, String type, boolean varKeyUpperCase) throws TemplateException {
		Expression expr = assignOp(data);
		try {
			if (data.srcCode.forwardIfCurrent(":")) {
				comments(data);
				return new NamedArgument(expr, assignOp(data), type, varKeyUpperCase);
			}
			else if (expr instanceof DynAssign) {
				DynAssign da = (DynAssign) expr;
				return new NamedArgument(da.getName(), da.getValue(), type, varKeyUpperCase);
			}
			else if (expr instanceof Assign && !(expr instanceof OpVariable)) {
				Assign a = (Assign) expr;
				return new NamedArgument(a.getVariable(), a.getValue(), type, varKeyUpperCase);
			}
		}
		catch (TransformerException be) {
			throw new TemplateException(data.srcCode, be.getMessage());
		}
		return new Argument(expr, type);
	}

	/**
	 * Transfomiert Zuweisungs Operation. <br />
	 * EBNF:<br />
	 * <code>eqvOp ["=" spaces assignOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	protected Expression assignOp(Data data) throws TemplateException {

		Expression expr = conditionalOp(data);
		if (data.srcCode.forwardIfCurrent('=')) {

			comments(data);
			if (data.mode == STATIC) expr = new DynAssign(expr, assignOp(data));
			else {
				if (expr instanceof Variable) {
					Expression value = assignOp(data);
					expr = new Assign((Variable) expr, value, data.srcCode.getPosition());
				}
				else if (expr instanceof Null) {
					Variable var = ((Null) expr).toVariable();
					Expression value = assignOp(data);
					expr = new Assign(var, value, data.srcCode.getPosition());
				}
				else if (expr instanceof NullConstant) {
					Variable var = ((NullConstant) expr).toVariable();
					Expression value = assignOp(data);
					expr = new Assign(var, value, data.srcCode.getPosition());
				}
				else throw new TemplateException(data.srcCode, "invalid assignment left-hand side (" + expr.getClass().getName() + ")");
			}
		}

		// patch for test()(); only works at the end of an expression!
		comments(data);
		while (data.srcCode.isCurrent('(')) {
			comments(data);
			Call call = new Call(expr);
			getFunctionMemberAttrs(data, null, false, call, null);
			call.setEnd(data.srcCode.getPosition());
			comments(data);
			expr = call;
		}
		return expr;
	}

	private Expression conditionalOp(Data data) throws TemplateException {

		Expression expr = impOp(data);
		if (data.srcCode.forwardIfCurrent('?')) {
			comments(data);
			// Elvis
			if (data.srcCode.forwardIfCurrent(':')) {

				comments(data);
				Expression right = assignOp(data);

				if (expr instanceof ExprBoolean) return expr;

				if (!(expr instanceof Variable)) throw new TemplateException(data.srcCode, "left operand of the Elvis operator has to be a variable or a function call");

				Variable left = (Variable) expr;
				/// LDEV-1201
				/*
				 * List<Member> members = left.getMembers(); Member last=null; for(Member m:members) { last=m;
				 * m.setSafeNavigated(true); } if(last!=null) { last.setSafeNavigatedValue(right); } return left;
				 */
				return data.factory.opElvis(left, right);
			}

			Expression left = assignOp(data);
			comments(data);
			if (!data.srcCode.forwardIfCurrent(':')) throw new TemplateException(data.srcCode, "invalid conditional operator");
			comments(data);
			Expression right = assignOp(data);

			expr = data.factory.opContional(expr, left, right);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Implication (imp) Operation. <br />
	 * EBNF:<br />
	 * <code>eqvOp {"imp" spaces eqvOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression impOp(Data data) throws TemplateException {
		Expression expr = eqvOp(data);
		while (data.srcCode.forwardIfCurrentAndNoWordAfter("imp")) {
			comments(data);
			expr = data.factory.opBool(expr, eqvOp(data), Factory.OP_BOOL_IMP);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Equivalence (eqv) Operation. <br />
	 * EBNF:<br />
	 * <code>xorOp {"eqv" spaces xorOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression eqvOp(Data data) throws TemplateException {
		Expression expr = xorOp(data);
		while (data.srcCode.forwardIfCurrentAndNoWordAfter("eqv")) {
			comments(data);
			expr = data.factory.opBool(expr, xorOp(data), Factory.OP_BOOL_EQV);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Xor (xor) Operation. <br />
	 * EBNF:<br />
	 * <code>orOp {"xor" spaces  orOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression xorOp(Data data) throws TemplateException {
		Expression expr = orOp(data);
		while (data.srcCode.forwardIfCurrentAndNoWordAfter("xor")) {
			comments(data);
			expr = data.factory.opBool(expr, orOp(data), Factory.OP_BOOL_XOR);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Or (or) Operation. Im Gegensatz zu CFMX , werden "||" Zeichen auch als Or
	 * Operatoren anerkannt. <br />
	 * EBNF:<br />
	 * <code>andOp {("or" | "||") spaces andOp}; (* "||" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression orOp(Data data) throws TemplateException {
		Expression expr = andOp(data);

		while (data.srcCode.forwardIfCurrent("||") || data.srcCode.forwardIfCurrentAndNoWordAfter("or")) {
			comments(data);
			expr = data.factory.opBool(expr, andOp(data), Factory.OP_BOOL_OR);
		}
		return expr;
	}

	/**
	 * Transfomiert eine And (and) Operation. Im Gegensatz zu CFMX , werden "&&" Zeichen auch als And
	 * Operatoren anerkannt. <br />
	 * EBNF:<br />
	 * <code>notOp {("and" | "&&") spaces notOp}; (* "&&" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression andOp(Data data) throws TemplateException {
		Expression expr = notOp(data);

		while (data.srcCode.forwardIfCurrent("&&") || data.srcCode.forwardIfCurrentAndNoWordAfter("and")) {
			comments(data);
			expr = data.factory.opBool(expr, notOp(data), Factory.OP_BOOL_AND);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Not (not) Operation. Im Gegensatz zu CFMX , wird das "!" Zeichen auch als Not
	 * Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>[("not"|"!") spaces] decsionOp; (* "!" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression notOp(Data data) throws TemplateException {
		// And Operation
		Position line = data.srcCode.getPosition();
		if (data.srcCode.isCurrent('!') && !data.srcCode.isCurrent("!=")) {
			data.srcCode.next();
			comments(data);
			return data.factory.opNegate(notOp(data), line, data.srcCode.getPosition());
		}
		else if (data.srcCode.forwardIfCurrentAndNoWordAfter("not")) {
			comments(data);
			return data.factory.opNegate(notOp(data), line, data.srcCode.getPosition());
		}
		return decsionOp(data);
	}

	/**
	 * <font f>Transfomiert eine Vergleichs Operation. <br />
	 * EBNF:<br />
	 * <code>concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
	                 "contains"|"nct"|"does not contain") spaces concatOp}; 
			(* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression decsionOp(Data data) throws TemplateException {

		Expression expr = concatOp(data);
		boolean hasChanged = false;
		// ct, contains
		do {
			hasChanged = false;
			if (data.srcCode.isCurrent('c')) {
				if (data.srcCode.forwardIfCurrent("ct", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_CT, expr);
					hasChanged = true;
				}
				else if (data.srcCode.forwardIfCurrent("contains", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_CT, expr);
					hasChanged = true;
				}
			}
			// does not contain
			else if (data.srcCode.forwardIfCurrent("does", "not", "contain", false, true)) {
				expr = decisionOpCreate(data, Factory.OP_DEC_NCT, expr);
				hasChanged = true;
			}

			// equal, eq
			else if (data.srcCode.isCurrent("eq") && !data.srcCode.isCurrent("eqv")) {
				int plus = 2;
				data.srcCode.setPos(data.srcCode.getPos() + 2);
				if (data.srcCode.forwardIfCurrent("ual")) plus = 5;

				if (data.srcCode.isCurrentVariableCharacter()) {
					data.srcCode.setPos(data.srcCode.getPos() - plus);
				}
				else {
					expr = decisionOpCreate(data, Factory.OP_DEC_EQ, expr);
					hasChanged = true;
				}

			}
			// ==
			else if (data.srcCode.forwardIfCurrent("==")) {
				if (data.srcCode.forwardIfCurrent('=')) expr = decisionOpCreate(data, Factory.OP_DEC_EEQ, expr);
				else expr = decisionOpCreate(data, Factory.OP_DEC_EQ, expr);
				hasChanged = true;
			}
			// !=
			else if (data.srcCode.forwardIfCurrent("!=")) {
				if (data.srcCode.forwardIfCurrent('=')) expr = decisionOpCreate(data, Factory.OP_DEC_NEEQ, expr);
				else expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr);
				hasChanged = true;
			}
			// <=/</<>
			else if (data.srcCode.isCurrent('<')) {
				hasChanged = true;
				if (data.srcCode.isNext('=')) {
					data.srcCode.next();
					data.srcCode.next();
					expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr);
				}
				else if (data.srcCode.isNext('>')) {
					data.srcCode.next();
					data.srcCode.next();
					expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr);
				}
				else if (data.srcCode.isNext('/')) {
					hasChanged = false;
				}
				else {
					data.srcCode.next();
					expr = decisionOpCreate(data, Factory.OP_DEC_LT, expr);
				}
			}
			// >=/>
			else if (data.allowLowerThan && data.srcCode.forwardIfCurrent('>')) {
				if (data.srcCode.forwardIfCurrent('=')) expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr);
				else expr = decisionOpCreate(data, Factory.OP_DEC_GT, expr);
				hasChanged = true;
			}

			// gt, gte, greater than or equal to, greater than
			else if (data.srcCode.isCurrent('g')) {
				if (data.srcCode.forwardIfCurrent("gt")) {
					if (data.srcCode.forwardIfCurrentAndNoWordAfter("e")) {
						if (data.srcCode.isCurrentVariableCharacter()) {
							data.srcCode.setPos(data.srcCode.getPos() - 3);
						}
						else {
							expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr);
							hasChanged = true;
						}
					}
					else {
						if (data.srcCode.isCurrentVariableCharacter()) {
							data.srcCode.setPos(data.srcCode.getPos() - 2);
						}
						else {
							expr = decisionOpCreate(data, Factory.OP_DEC_GT, expr);
							hasChanged = true;
						}
					}
				}
				else if (data.srcCode.forwardIfCurrent("greater", "than", false, true)) {
					if (data.srcCode.forwardIfCurrent("or", "equal", "to", true, true)) expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr);
					else expr = decisionOpCreate(data, Factory.OP_DEC_GT, expr);
					hasChanged = true;
				}
				else if (data.srcCode.forwardIfCurrent("ge", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_GTE, expr);
					hasChanged = true;
				}
			}

			// is, is not
			else if (data.srcCode.forwardIfCurrent("is", false, true)) {
				if (data.srcCode.forwardIfCurrent("not", true, true)) expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr);
				else expr = decisionOpCreate(data, Factory.OP_DEC_EQ, expr);
				hasChanged = true;
			}

			// lt, lte, less than, less than or equal to
			else if (data.srcCode.isCurrent('l')) {
				if (data.srcCode.forwardIfCurrent("lt")) {
					if (data.srcCode.forwardIfCurrentAndNoWordAfter("e")) {
						if (data.srcCode.isCurrentVariableCharacter()) {
							data.srcCode.setPos(data.srcCode.getPos() - 3);
						}
						else {
							expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr);
							hasChanged = true;
						}
					}
					else {
						if (data.srcCode.isCurrentVariableCharacter()) {
							data.srcCode.setPos(data.srcCode.getPos() - 2);
						}
						else {
							expr = decisionOpCreate(data, Factory.OP_DEC_LT, expr);
							hasChanged = true;
						}
					}
				}
				else if (data.srcCode.forwardIfCurrent("less", "than", false, true)) {
					if (data.srcCode.forwardIfCurrent("or", "equal", "to", true, true)) expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr);
					else expr = decisionOpCreate(data, Factory.OP_DEC_LT, expr);
					hasChanged = true;
				}
				else if (data.srcCode.forwardIfCurrent("le", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_LTE, expr);
					hasChanged = true;
				}
			}

			// neq, not equal, nct
			else if (data.srcCode.isCurrent('n')) {
				// Not Equal
				if (data.srcCode.forwardIfCurrent("neq", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr);
					hasChanged = true;
				}
				// Not Equal (Alias)
				else if (data.srcCode.forwardIfCurrent("not", "equal", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_NEQ, expr);
					hasChanged = true;
				}
				// nct
				else if (data.srcCode.forwardIfCurrent("nct", false, true)) {
					expr = decisionOpCreate(data, Factory.OP_DEC_NCT, expr);
					hasChanged = true;
				}
			}

		}
		while (hasChanged);
		return expr;
	}

	private Expression decisionOpCreate(Data data, int operation, Expression left) throws TemplateException {
		comments(data);
		return data.factory.opDecision(left, concatOp(data), operation);
	}

	/**
	 * Transfomiert eine Konkatinations-Operator (&) Operation. Im Gegensatz zu CFMX , wird das "!"
	 * Zeichen auch als Not Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>plusMinusOp {"&" spaces concatOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression concatOp(Data data) throws TemplateException {
		Expression expr = plusMinusOp(data);

		while (data.srcCode.isCurrent('&') && !data.srcCode.isCurrent("&&")) {
			data.srcCode.next();

			// &=
			if (data.srcCode.isCurrent('=') && expr instanceof Variable) {
				data.srcCode.next();
				comments(data);
				Expression value = assignOp(data);

				expr = data.factory.opUnary((Variable) expr, value, Factory.OP_UNARY_PRE, Factory.OP_UNARY_CONCAT, expr.getStart(), data.srcCode.getPosition());

				// ExprString res = OpString.toExprString(expr, right);
				// expr=new OpVariable((Variable)expr,res,data.cfml.getPosition());
			}
			else {
				comments(data);
				expr = data.factory.opString(expr, plusMinusOp(data));
			}

		}
		return expr;
	}

	/**
	 * Transfomiert die mathematischen Operatoren Plus und Minus (1,-). <br />
	 * EBNF:<br />
	 * <code>modOp [("-"|"+") spaces plusMinusOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression plusMinusOp(Data data) throws TemplateException {
		Expression expr = modOp(data);

		while (!data.srcCode.isLast()) {

			// Plus Operation
			if (data.srcCode.forwardIfCurrent('+')) expr = _plusMinusOp(data, expr, Factory.OP_DBL_PLUS);
			// Minus Operation
			else if (data.srcCode.forwardIfCurrent('-')) expr = _plusMinusOp(data, expr, Factory.OP_DBL_MINUS);
			else break;
		}
		return expr;
	}

	private Expression _plusMinusOp(Data data, Expression expr, int opr) throws TemplateException {
		// +=
		// plus|Minus Assignment
		if (data.srcCode.isCurrent('=') && expr instanceof Variable) {
			data.srcCode.next();
			comments(data);
			Expression value = assignOp(data);
			// if(opr==OpDouble.MINUS) value=OpNegateNumber.toExprDouble(value, null, null);

			expr = data.factory.opUnary((Variable) expr, value, Factory.OP_UNARY_PRE, opr, expr.getStart(), data.srcCode.getPosition());

			// ExprDouble res = OpDouble.toExprDouble(expr, right,opr);
			// expr=new OpVariable((Variable)expr,res,data.cfml.getPosition());
		}

		else {
			comments(data);
			expr = data.factory.opDouble(expr, modOp(data), opr);
		}
		return expr;
	}

	/**
	 * Transfomiert eine Modulus Operation. Im Gegensatz zu CFMX , wird das "%" Zeichen auch als Modulus
	 * Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>divMultiOp {("mod" | "%") spaces divMultiOp}; (* modulus operator , "%" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression modOp(Data data) throws TemplateException {
		Expression expr = divMultiOp(data);

		// Modulus Operation
		while (data.srcCode.forwardIfCurrent('%') || data.srcCode.forwardIfCurrentAndNoWordAfter("mod")) {
			expr = _modOp(data, expr);
			// comments(data);
			// expr=OpDouble.toExprDouble(expr, divMultiOp(), OpDouble.MODULUS);
		}
		return expr;
	}

	private Expression _modOp(Data data, Expression expr) throws TemplateException {
		if (data.srcCode.isCurrent('=') && expr instanceof Variable) {
			data.srcCode.next();
			comments(data);
			Expression right = assignOp(data);
			ExprDouble res = data.factory.opDouble(expr, right, Factory.OP_DBL_MODULUS);
			return new OpVariable((Variable) expr, res, data.srcCode.getPosition());
		}
		comments(data);
		return data.factory.opDouble(expr, expoOp(data), Factory.OP_DBL_MODULUS);
	}

	/**
	 * Transfomiert die mathematischen Operatoren Mal und Durch (*,/). <br />
	 * EBNF:<br />
	 * <code>expoOp {("*"|"/") spaces expoOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression divMultiOp(Data data) throws TemplateException {
		Expression expr = expoOp(data);

		while (!data.srcCode.isLast()) {

			// Multiply Operation
			if (data.srcCode.forwardIfCurrent('*')) {
				expr = _divMultiOp(data, expr, Factory.OP_DBL_MULTIPLY);
				// comments(data);
				// expr=OpDouble.toExprDouble(expr, expoOp(), OpDouble.MULTIPLY);
			}
			// Divide Operation
			else if (data.srcCode.isCurrent('/') && (!data.srcCode.isCurrent('/', '>'))) {
				data.srcCode.next();
				expr = _divMultiOp(data, expr, Factory.OP_DBL_DIVIDE);
				// comments(data);
				// expr=OpDouble.toExprDouble(expr, expoOp(), OpDouble.DIVIDE);
			}
			// Divide Operation
			else if (data.srcCode.isCurrent('\\')) {
				data.srcCode.next();
				expr = _divMultiOp(data, expr, Factory.OP_DBL_INTDIV);
				// comments(data);
				// expr=OpDouble.toExprDouble(expr, expoOp(), OpDouble.INTDIV);
			}
			else {
				break;
			}

		}
		return expr;
	}

	private Expression _divMultiOp(Data data, Expression expr, int iOp) throws TemplateException {
		if (data.srcCode.isCurrent('=') && expr instanceof Variable) {
			data.srcCode.next();
			comments(data);
			Expression value = assignOp(data);

			return data.factory.opUnary((Variable) expr, value, Factory.OP_UNARY_PRE, iOp, expr.getStart(), data.srcCode.getPosition());

			// ExprDouble res = OpDouble.toExprDouble(expr, right,iOp);
			// return new OpVariable((Variable)expr,res,data.cfml.getPosition());
		}
		comments(data);
		return data.factory.opDouble(expr, expoOp(data), iOp);
	}

	/**
	 * Transfomiert den Exponent Operator (^,exp). Im Gegensatz zu CFMX , werden die Zeichen " exp "
	 * auch als Exponent anerkannt. <br />
	 * EBNF:<br />
	 * <code>clip {("exp"|"^") spaces clip};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression expoOp(Data data) throws TemplateException {
		Expression expr = unaryOp(data);

		// Modulus Operation
		while (data.srcCode.forwardIfCurrent('^') || data.srcCode.forwardIfCurrentAndNoWordAfter("exp")) {
			comments(data);
			expr = data.factory.opDouble(expr, unaryOp(data), Factory.OP_DBL_EXP);
		}
		return expr;
	}

	private Expression unaryOp(Data data) throws TemplateException {
		Expression expr = negatePlusMinusOp(data);

		// Plus Operation
		if (data.srcCode.forwardIfCurrent("++") && expr instanceof Variable) expr = _unaryOp(data, expr, Factory.OP_DBL_PLUS);
		// Minus Operation
		else if (data.srcCode.forwardIfCurrent("--") && expr instanceof Variable) expr = _unaryOp(data, expr, Factory.OP_DBL_MINUS);
		return expr;
	}

	private Expression _unaryOp(Data data, Expression expr, int op) throws TemplateException {
		Position leftEnd = expr.getEnd(), start = null, end = null;
		comments(data);
		if (leftEnd != null) {
			start = leftEnd;
			end = new Position(leftEnd.line, leftEnd.column + 2, leftEnd.pos + 2);
		}
		return data.factory.opUnary((Variable) expr, data.factory.DOUBLE_ONE(), Factory.OP_UNARY_POST, op, start, end);

		// ExprDouble res = OpDouble.toExprDouble(expr, LitDouble.toExprDouble(1D,start,end),opr);
		// expr=new OpVariable((Variable)expr,res,data.cfml.getPosition());
		// return OpDouble.toExprDouble(expr,LitDouble.toExprDouble(1D,start,end),opr==OpDouble.PLUS?
		// OpDouble.MINUS:OpDouble.PLUS);
	}

	/**
	 * Negate Numbers
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression negatePlusMinusOp(Data data) throws TemplateException {
		// And Operation
		Position line = data.srcCode.getPosition();
		if (data.srcCode.forwardIfCurrent('-')) {
			// pre increment
			if (data.srcCode.forwardIfCurrent('-')) {
				comments(data);
				Expression expr = clip(data);
				return data.factory.opUnary((Variable) expr, data.factory.DOUBLE_ONE(), Factory.OP_UNARY_PRE, Factory.OP_UNARY_MINUS, line, data.srcCode.getPosition());

				// ExprDouble res = OpDouble.toExprDouble(expr, LitDouble.toExprDouble(1D),OpDouble.MINUS);
				// return new OpVariable((Variable)expr,res,data.cfml.getPosition());

			}
			comments(data);
			return data.factory.opNegateNumber(clip(data), Factory.OP_NEG_NBR_MINUS, line, data.srcCode.getPosition());

		}
		else if (data.srcCode.forwardIfCurrent('+')) {
			if (data.srcCode.forwardIfCurrent('+')) {
				comments(data);
				Expression expr = clip(data);

				return data.factory.opUnary((Variable) expr, data.factory.DOUBLE_ONE(), Factory.OP_UNARY_PRE, Factory.OP_UNARY_PLUS, line, data.srcCode.getPosition());
			}
			comments(data);
			return data.factory.toExprDouble(clip(data));// OpNegateNumber.toExprDouble(clip(),OpNegateNumber.PLUS,line);
		}
		return clip(data);
	}

	/**
	 * Verarbeitet Ausdruecke die inerhalb einer Klammer stehen. <br />
	 * EBNF:<br />
	 * <code>("(" spaces impOp ")" spaces) | checker;</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression clip(Data data) throws TemplateException {
		return checker(data);
	}

	/**
	 * Hier werden die verschiedenen Moeglichen Werte erkannt und jenachdem wird mit der passenden
	 * Methode weitergefahren <br />
	 * EBNF:<br />
	 * <code>string | number | dynamic | sharp;</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression checker(Data data) throws TemplateException {
		Expression expr = null;
		// String
		if ((expr = string(data)) != null) {
			expr = subDynamic(data, expr, false, false);
			data.mode = STATIC;// (expr instanceof Literal)?STATIC:DYNAMIC;// STATIC
			return expr;
		}
		// Number
		if ((expr = number(data)) != null) {
			expr = subDynamic(data, expr, false, false);
			data.mode = STATIC;// (expr instanceof Literal)?STATIC:DYNAMIC;// STATIC
			return expr;
		}
		// closure
		if ((expr = closure(data)) != null) {
			data.mode = DYNAMIC;
			return expr;
		}
		// lambda
		if ((expr = lambda(data)) != null) {
			data.mode = DYNAMIC;
			return expr;
		}

		// Dynamic
		if ((expr = dynamic(data)) != null) {
			expr = newOp(data, expr);
			expr = subDynamic(data, expr, true, false);
			data.mode = DYNAMIC;
			return expr;
		}
		// Sharp
		if ((expr = sharp(data)) != null) {
			data.mode = DYNAMIC;
			return expr;
		}
		// JSON
		if ((expr = json(data, JSON_ARRAY, '[', ']')) != null) {
			expr = subDynamic(data, expr, false, false);
			data.mode = DYNAMIC;
			return expr;
		}
		if ((expr = json(data, JSON_STRUCT, '{', '}')) != null) {
			expr = subDynamic(data, expr, false, false);
			data.mode = DYNAMIC;
			return expr;
		}
		// else Error
		throw new TemplateException(data.srcCode, "Syntax Error, Invalid Construct");
	}

	/*
	 * private Expression variable(Data data) throws TemplateException { Expression expr=null;
	 * 
	 * // Dynamic if((expr=dynamic(data))!=null) { expr = subDynamic(data,expr); data.mode=DYNAMIC;
	 * return expr; } return null; }
	 */

	/**
	 * Transfomiert einen lierale Zeichenkette. <br />
	 * EBNF:<br />
	 * <code>("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") | 
	                 (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);</code>
	 * 
	 * @param data
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	protected Expression string(Data data) throws TemplateException {

		// check starting character for a string literal
		if (!data.srcCode.isCurrent('"') && !data.srcCode.isCurrent('\'')) return null;
		Position line = data.srcCode.getPosition();

		// Init Parameter
		char quoter = data.srcCode.getCurrentLower();
		StringBuilder str = new StringBuilder();
		Expression expr = null;

		while (data.srcCode.hasNext()) {
			data.srcCode.next();
			// check sharp
			if (data.srcCode.isCurrent('#')) {

				// Ecaped sharp
				if (data.srcCode.isNext('#')) {
					data.srcCode.next();
					str.append('#');
				}
				// get Content of sharp
				else {
					data.srcCode.next();
					comments(data);
					Expression inner = assignOp(data);
					comments(data);
					if (!data.srcCode.isCurrent('#')) throw new TemplateException(data.srcCode, "Invalid Syntax Closing [#] not found");

					ExprString exprStr = null;
					if (str.length() != 0) {
						exprStr = data.factory.createLitString(str.toString(), line, data.srcCode.getPosition());
						if (expr != null) {
							expr = data.factory.opString(expr, exprStr);
						}
						else expr = exprStr;
						str = new StringBuilder();
					}
					if (expr == null) {
						expr = inner;
					}
					else {
						expr = data.factory.opString(expr, inner);
					}
				}
			}
			// check quoter
			else if (data.srcCode.isCurrent(quoter)) {
				// Ecaped sharp
				if (data.srcCode.isNext(quoter)) {
					data.srcCode.next();
					str.append(quoter);
				}
				// finsish
				else {
					break;
				}
			}
			// all other character
			else {
				str.append(data.srcCode.getCurrent());
			}
		}
		if (!data.srcCode.forwardIfCurrent(quoter)) throw new TemplateException(data.srcCode, "Invalid Syntax Closing [" + quoter + "] not found");

		if (expr == null) expr = data.factory.createLitString(str.toString(), line, data.srcCode.getPosition());
		else if (str.length() != 0) {
			expr = data.factory.opString(expr, data.factory.createLitString(str.toString(), line, data.srcCode.getPosition()));
		}
		comments(data);

		if (expr instanceof Variable) {
			Variable var = (Variable) expr;
			var.fromHash(true);
		}

		return expr;

	}

	/**
	 * Transfomiert einen numerische Wert. Die Laenge des numerischen Wertes interessiert nicht zu
	 * uebersetzungszeit, ein "Overflow" fuehrt zu einem Laufzeitfehler. Da die zu erstellende CFXD,
	 * bzw. dieser Transfomer, keine Vorwegnahme des Laufzeitsystems vornimmt. <br />
	 * EBNF:<br />
	 * <code>["+"|"-"] digit {digit} {"." digit {digit}};</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private LitDouble number(Data data) throws TemplateException {
		// check first character is a number literal representation
		if (!(data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrent('.'))) return null;

		Position line = data.srcCode.getPosition();
		StringBuffer rtn = new StringBuffer();

		// get digit on the left site of the dot
		if (data.srcCode.isCurrent('.')) rtn.append('0');
		else rtn.append(digit(data));
		// read dot if exist
		if (data.srcCode.forwardIfCurrent('.')) {
			rtn.append('.');
			String rightSite = digit(data);
			if (rightSite.length() > 0 && data.srcCode.forwardIfCurrent('e')) {
				Boolean expOp = null;
				if (data.srcCode.forwardIfCurrent('+')) expOp = Boolean.TRUE;
				else if (data.srcCode.forwardIfCurrent('-')) expOp = Boolean.FALSE;

				if (data.srcCode.isCurrentBetween('0', '9')) {
					if (expOp == Boolean.FALSE) rightSite += "e-";
					else if (expOp == Boolean.TRUE) rightSite += "e+";
					else rightSite += "e";
					rightSite += digit(data);
				}
				else {
					if (expOp != null) data.srcCode.previous();
					data.srcCode.previous();
				}
			}
			// read right side of the dot
			if (rightSite.length() == 0) rightSite = "0";// throw new TemplateException(cfml, "Number can't end with [.]"); // DIFF 23
			rtn.append(rightSite);
		}
		// scientific notation
		else if (data.srcCode.forwardIfCurrent('e')) {
			Boolean expOp = null;
			if (data.srcCode.forwardIfCurrent('+')) expOp = Boolean.TRUE;
			else if (data.srcCode.forwardIfCurrent('-')) expOp = Boolean.FALSE;

			if (data.srcCode.isCurrentBetween('0', '9')) {
				String rightSite = "e";
				if (expOp == Boolean.FALSE) rightSite += "-";
				else if (expOp == Boolean.TRUE) rightSite += "+";
				rightSite += digit(data);
				rtn.append(rightSite);
			}
			else {
				if (expOp != null) data.srcCode.previous();
				data.srcCode.previous();
			}
		}
		comments(data);

		try {
			return data.factory.createLitDouble(Caster.toDoubleValue(rtn.toString()), line, data.srcCode.getPosition());
		}
		catch (CasterException e) {
			throw new TemplateException(data.srcCode, e.getMessage());
		}

	}

	/**
	 * Liest die reinen Zahlen innerhalb des CFMLString aus und gibt diese als Zeichenkette zurueck.
	 * <br />
	 * EBNF:<br />
	 * <code>"0"|..|"9";</code>
	 * 
	 * @return digit Ausgelesene Zahlen als Zeichenkette.
	 */
	private String digit(Data data) {
		String rtn = "";
		while (data.srcCode.isValidIndex()) {
			if (!data.srcCode.isCurrentBetween('0', '9')) break;
			rtn += data.srcCode.getCurrentLower();
			data.srcCode.next();
		}
		return rtn;
	}

	/**
	 * Liest den folgenden idetifier ein und prueft ob dieser ein boolscher Wert ist. Im Gegensatz zu
	 * CFMX wird auch "yes" und "no" als bolscher <wert akzeptiert, was bei CFMX nur beim Umwandeln
	 * einer Zeichenkette zu einem boolschen Wert der Fall ist.<br />
	 * Wenn es sich um keinen bolschen Wert handelt wird der folgende Wert eingelesen mit seiner ganzen
	 * Hirarchie. <br />
	 * EBNF:<br />
	 * <code>"true" | "false" | "yes" | "no" | startElement {("." identifier | "[" structElement "]" )[function] };</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression dynamic(Data data) throws TemplateException {
		// Die Implementation weicht ein wenig von der Grammatik ab,
		// aber nicht in der Logik sondern rein wie es umgesetzt wurde.

		// get First Element of the Variable
		Position line = data.srcCode.getPosition();
		Identifier id = identifier(data, false, true);
		if (id == null) {
			if (!data.srcCode.forwardIfCurrent('(')) return null;

			comments(data);
			Expression expr = assignOp(data);

			if (!data.srcCode.forwardIfCurrent(')')) throw new TemplateException(data.srcCode, "Invalid Syntax Closing [)] not found");
			comments(data);
			return expr;// subDynamic(expr);

		}

		Variable var;
		comments(data);

		// Boolean constant
		if (id.getString().equalsIgnoreCase("TRUE")) {// || name.equals("YES")) {
			comments(data);
			return id.getFactory().createLitBoolean(true, line, data.srcCode.getPosition());
		}
		else if (id.getString().equalsIgnoreCase("FALSE")) {// || name.equals("NO")) {
			comments(data);
			return id.getFactory().createLitBoolean(false, line, data.srcCode.getPosition());
		}
		else if (id.getString().equalsIgnoreCase("NULL") && !data.srcCode.isCurrent('.') && !data.srcCode.isCurrent('[')) {
			comments(data);
			return id.getFactory().createNullConstant(line, data.srcCode.getPosition());
		}

		// Extract Scope from the Variable
		var = startElement(data, id, line);
		var.setStart(line);
		var.setEnd(data.srcCode.getPosition());
		return var;
	}

	protected Expression json(Data data, FunctionLibFunction flf, char start, char end) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent(start)) return null;

		Position line = data.srcCode.getPosition();

		data.srcCode.removeSpace();
		// [:|=]
		if (data.srcCode.forwardIfCurrent(':', ']') || data.srcCode.forwardIfCurrent('=', ']')) {
			flf = flf.getFunctionLib().getFunction("_literalOrderedStruct");
			BIF bif = new BIF(data.factory, data.settings, flf);
			bif.setArgType(flf.getArgType());
			try {
				bif.setClassDefinition(flf.getFunctionClassDefinition());
			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw new PageRuntimeException(t);
			}
			bif.setReturnType(flf.getReturnTypeAsString());

			data.ep.add(flf, bif, data.srcCode);
			Variable var = data.factory.createVariable(line, data.srcCode.getPosition());
			var.addMember(bif);
			return var;
		}

		BIF bif = new BIF(data.factory, data.settings, flf);
		bif.setArgType(flf.getArgType());
		try {
			bif.setClassDefinition(flf.getFunctionClassDefinition());
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw new PageRuntimeException(t);
		}
		bif.setReturnType(flf.getReturnTypeAsString());

		do {
			comments(data);
			if (data.srcCode.isCurrent(end)) break;

			bif.addArgument(functionArgument(data, data.settings.dotNotationUpper));
			comments(data);
		}
		while (data.srcCode.forwardIfCurrent(','));
		comments(data);

		if (!data.srcCode.forwardIfCurrent(end)) throw new TemplateException(data.srcCode, "Invalid Syntax Closing [" + end + "] not found");
		comments(data);

		if (flf.hasTteClass()) {
			FunctionLibFunction tmp = flf.getEvaluator().pre(bif, flf);
			if (tmp != null && tmp != flf) {
				bif.setFlf(flf = tmp);
				bif.setArgType(flf.getArgType());
				try {
					bif.setClassDefinition(flf.getFunctionClassDefinition());
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					throw new PageRuntimeException(t);
				}
				bif.setReturnType(flf.getReturnTypeAsString());

			}
		}

		data.ep.add(flf, bif, data.srcCode);
		Variable var = data.factory.createVariable(line, data.srcCode.getPosition());
		var.addMember(bif);
		return var;
	}

	private Expression closure(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent("function", '(')) return null;
		data.srcCode.previous();
		Function func = closurePart(data, "closure_" + CreateUniqueId.invoke(), Component.ACCESS_PUBLIC, Component.MODIFIER_NONE, "any", data.srcCode.getPosition(), true);
		func.setParent(data.getParent());
		return new FunctionAsExpression(func);
	}

	protected abstract Function closurePart(Data data, String id, int access, int modifier, String rtnType, Position line, boolean closure) throws TemplateException;

	private Expression lambda(Data data) throws TemplateException {
		int pos = data.srcCode.getPos();
		if (!data.srcCode.forwardIfCurrent("(")) return null;
		ArrayList<lucee.transformer.bytecode.statement.Argument> args = null;
		// data.cfml.previous();
		try {
			args = getScriptFunctionArguments(data);
		}
		catch (TemplateException e) {
			// if there is a template exception, the argument syntax is not correct, and must not be a lambda
			// expression
			// TODO find a better way to test for lambda than to attempt processing the arguments and catch an
			// exception if it fails.
			data.srcCode.setPos(pos);
			return null;
		}

		if (!data.srcCode.forwardIfCurrent(")")) {
			data.srcCode.setPos(pos);
			return null;
		}

		data.srcCode.removeSpace();
		if (!data.srcCode.forwardIfCurrent("=>")) {
			data.srcCode.setPos(pos);
			return null;
		}

		return new FunctionAsExpression(
				lambdaPart(data, "lambda_" + CreateUniqueId.invoke(), Component.ACCESS_PUBLIC, Component.MODIFIER_NONE, "any", data.srcCode.getPosition(), args));
	}

	protected abstract Function lambdaPart(Data data, String id, int access, int modifier, String rtnType, Position line,
			ArrayList<lucee.transformer.bytecode.statement.Argument> args) throws TemplateException;

	protected abstract ArrayList<lucee.transformer.bytecode.statement.Argument> getScriptFunctionArguments(Data data) throws TemplateException;

	protected FunctionLibFunction getFLF(Data data, String name) {
		FunctionLibFunction flf = null;
		for (int i = 0; i < data.flibs.length; i++) {
			flf = data.flibs[i].getFunction(name);
			if (flf != null) break;
		}
		return flf;
	}

	private Expression subDynamic(Data data, Expression expr, boolean tryStatic, boolean isStaticChild) throws TemplateException {

		String name = null;
		Invoker invoker = null;
		// Loop over nested Variables
		boolean safeNavigation;
		while (data.srcCode.isValidIndex()) {
			safeNavigation = false;
			ExprString nameProp = null, namePropUC = null;
			// []
			if (data.srcCode.forwardIfCurrent('[')) {
				isStaticChild = false;
				// get Next Var
				nameProp = structElement(data);
				namePropUC = nameProp;
				// Valid Syntax ???
				if (!data.srcCode.forwardIfCurrent(']')) throw new TemplateException(data.srcCode, "Invalid Syntax Closing []] not found");
			}
			// .
			else if (isStaticChild || data.srcCode.forwardIfCurrent('.') || (safeNavigation = data.srcCode.forwardIfCurrent('?', '.'))) {
				isStaticChild = false;
				// Extract next Var String
				comments(data);
				Position line = data.srcCode.getPosition();
				name = identifier(data, true);
				if (name == null) throw new TemplateException(data.srcCode, "Invalid identifier");
				comments(data);
				nameProp = Identifier.toIdentifier(data.factory, name, line, data.srcCode.getPosition());
				namePropUC = Identifier.toIdentifier(data.factory, name, data.settings.dotNotationUpper ? Identifier.CASE_UPPER : Identifier.CASE_ORIGNAL, line,
						data.srcCode.getPosition());
			}

			// finish
			else {
				break;
			}

			comments(data);

			if (expr instanceof Invoker) {
				invoker = (Invoker) expr;
			}
			else {
				invoker = new ExpressionInvoker(expr);
				expr = invoker;
			}

			// safe navigation
			Member member;
			if (safeNavigation) {
				List<Member> members = invoker.getMembers();
				if (members.size() > 0) {
					member = members.get(members.size() - 1);
					member.setSafeNavigated(true);
				}
			}

			// Method
			if (data.srcCode.isCurrent('(')) {
				if (nameProp == null && name != null) nameProp = Identifier.toIdentifier(data.factory, name, Identifier.CASE_ORIGNAL, null, null);// properly this is never used
				invoker.addMember(member = getFunctionMember(data, nameProp, false));
			}

			// property
			else invoker.addMember(member = data.factory.createDataMember(namePropUC));

			if (safeNavigation) {
				member.setSafeNavigated(true);
			}
		}

		// static scipe call?

		// STATIC SCOPE CALL
		if (tryStatic) {
			comments(data);
			Expression staticCall = staticScope(data, expr);
			if (staticCall != null) return staticCall;
		}
		return expr;
	}

	private Expression staticScope(Data data, Expression expr) throws TemplateException {
		if (data.srcCode.forwardIfCurrent("::")) {
			if (!(expr instanceof Variable)) throw new TemplateException(data.srcCode, "Invalid syntax before [::]");

			Variable old = (Variable) expr;
			// set back to read again as a component path
			data.srcCode.setPos(old.getStart().pos);

			// now we read the component path
			ExprString componentPath = readComponentPath(data);
			if (!data.srcCode.forwardIfCurrent("::")) throw new TemplateException(data.srcCode, "Invalid syntax before [::]" + data.srcCode.getCurrent());

			comments(data);

			BIF bif = null;
			if (componentPath instanceof LitString) {
				LitString ls = (LitString) componentPath;
				if ("super".equalsIgnoreCase(ls.getString())) {
					bif = ASMUtil.createBif(data, GET_SUPER_STATIC_SCOPE);
				}
			}

			// now we generate a _getStaticScope function call with that path
			if (bif == null) {
				bif = ASMUtil.createBif(data, GET_STATIC_SCOPE);
				bif.addArgument(new Argument(componentPath, "string"));
			}

			Variable var = data.factory.createVariable(old.getStart(), data.srcCode.getPosition());
			var.addMember(bif);

			// now we are reading what is coming after ":::"
			Expression sd = subDynamic(data, var, false, true);
			return sd;
		}
		return null;
	}

	private Expression newOp(Data data, Expression expr) throws TemplateException {
		if (!(expr instanceof Variable)) return expr;
		Variable var = (Variable) expr;
		Member m = var.getFirstMember();
		if (!(m instanceof DataMember)) return expr;

		ExprString n = ((DataMember) m).getName();
		if (!(n instanceof LitString)) return expr;

		LitString ls = (LitString) n;

		if (!"new".equalsIgnoreCase(ls.getString())) return expr;

		int start = data.srcCode.getPos();

		ExprString exprName = readComponentPath(data);
		if (exprName == null) {
			data.srcCode.setPos(start);
			return expr;
		}

		comments(data);

		if (data.srcCode.isCurrent('(')) {
			FunctionMember func = getFunctionMember(data, Identifier.toIdentifier(data.factory, "_createComponent", Identifier.CASE_ORIGNAL, null, null), true);
			func.addArgument(new Argument(exprName, "string"));
			Variable v = expr.getFactory().createVariable(expr.getStart(), expr.getEnd());
			v.addMember(func);
			comments(data);
			return v;
		}
		data.srcCode.setPos(start);
		return expr;

	}

	private ExprString readComponentPath(Data data) throws TemplateException {
		// first identifier
		String name = identifier(data, true);

		if (name != null) {
			StringBuilder fullName = new StringBuilder();
			fullName.append(name);
			// Loop over additional identifier
			while (data.srcCode.isValidIndex()) {
				if (data.srcCode.forwardIfCurrent('.')) {
					comments(data);
					name = identifier(data, true);
					if (name == null) return null;

					fullName.append('.');
					fullName.append(name);
					comments(data);
				}
				else break;
			}

			// sub component
			/*
			 * if (data.srcCode.forwardIfCurrent(':')) { fullName.append(':'); name = identifier(data,true);
			 * if(name==null) return null; fullName.append(name); }
			 */

			return data.factory.createLitString(fullName.toString());
		}

		Expression str = string(data);
		if (str != null) {
			return data.factory.toExprString(str);
		}
		return null;

	}

	/**
	 * Extrahiert den Start Element einer Variale, dies ist entweder eine Funktion, eine Scope
	 * Definition oder eine undefinierte Variable. <br />
	 * EBNF:<br />
	 * <code>identifier "(" functionArg ")" | scope | identifier;</code>
	 * 
	 * @param name Einstiegsname
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Variable startElement(Data data, Identifier name, Position line) throws TemplateException {

		// check function
		if (data.srcCode.isCurrent('(')) {
			FunctionMember func = getFunctionMember(data, name, true);

			Variable var = name.getFactory().createVariable(line, data.srcCode.getPosition());
			var.addMember(func);
			comments(data);
			return var;
		}

		// check scope
		Variable var = scope(data, name, line);
		if (var != null) return var;

		// undefined variable
		var = name.getFactory().createVariable(line, data.srcCode.getPosition());
		var.addMember(data.factory.createDataMember(name));

		comments(data);
		return var;

	}

	/**
	 * Liest einen CFML Scope aus, falls der folgende identifier keinem Scope entspricht, gibt die
	 * Variable null zurueck. <br />
	 * EBNF:<br />
	 * <code>"variable" | "cgi" | "url" | "form" | "session" | "application" | "arguments" | "cookie" | " client";</code>
	 * 
	 * @param id String identifier, wird aus Optimierungszwechen nicht innerhalb dieser Funktion
	 *            ausgelsen.
	 * @return CFXD Variable Element oder null
	 * @throws TemplateException
	 */
	private Variable scope(Data data, Identifier id, Position line) throws TemplateException {
		String idStr = id.getUpper();

		if (idStr.equals("ARGUMENTS")) return data.factory.createVariable(Scope.SCOPE_ARGUMENTS, line, data.srcCode.getPosition());
		else if (idStr.equals("LOCAL")) return data.factory.createVariable(Scope.SCOPE_LOCAL, line, data.srcCode.getPosition());
		else if (idStr.equals("VAR")) {
			Identifier _id = identifier(data, false, true);
			if (_id != null) {
				comments(data);
				Variable local = data.factory.createVariable(ScopeSupport.SCOPE_VAR, line, data.srcCode.getPosition());
				if (!"LOCAL".equalsIgnoreCase(_id.getString())) local.addMember(data.factory.createDataMember(_id));
				else {
					local.ignoredFirstMember(true);
				}
				return local;
			}
		}
		else if (idStr.equals("VARIABLES")) return data.factory.createVariable(Scope.SCOPE_VARIABLES, line, data.srcCode.getPosition());
		else if (idStr.equals("REQUEST")) return data.factory.createVariable(Scope.SCOPE_REQUEST, line, data.srcCode.getPosition());
		else if (idStr.equals("SERVER")) return data.factory.createVariable(Scope.SCOPE_SERVER, line, data.srcCode.getPosition());

		if (data.settings.ignoreScopes) return null;

		if (idStr.equals("CGI")) return data.factory.createVariable(Scope.SCOPE_CGI, line, data.srcCode.getPosition());
		else if (idStr.equals("SESSION")) return data.factory.createVariable(Scope.SCOPE_SESSION, line, data.srcCode.getPosition());
		else if (idStr.equals("APPLICATION")) return data.factory.createVariable(Scope.SCOPE_APPLICATION, line, data.srcCode.getPosition());
		else if (idStr.equals("FORM")) return data.factory.createVariable(Scope.SCOPE_FORM, line, data.srcCode.getPosition());
		else if (idStr.equals("URL")) return data.factory.createVariable(Scope.SCOPE_URL, line, data.srcCode.getPosition());
		else if (idStr.equals("CLIENT")) return data.factory.createVariable(Scope.SCOPE_CLIENT, line, data.srcCode.getPosition());
		else if (idStr.equals("COOKIE")) return data.factory.createVariable(Scope.SCOPE_COOKIE, line, data.srcCode.getPosition());
		else if (idStr.equals("CLUSTER")) return data.factory.createVariable(Scope.SCOPE_CLUSTER, line, data.srcCode.getPosition());

		return null;
	}

	/**
	 * Liest einen Identifier aus und gibt diesen als String zurueck. <br />
	 * EBNF:<br />
	 * <code>(letter | "_") {letter | "_"|digit};</code>
	 * 
	 * @param firstCanBeNumber
	 * @param upper
	 * @return Identifier.
	 */
	protected Identifier identifier(Data data, boolean firstCanBeNumber, boolean upper) {
		Position start = data.srcCode.getPosition();
		if (!data.srcCode.isCurrentLetter() && !data.srcCode.isCurrentSpecial()) {
			if (!firstCanBeNumber) return null;
			else if (!data.srcCode.isCurrentBetween('0', '9')) return null;
		}
		do {
			data.srcCode.next();
			if (!(data.srcCode.isCurrentLetter() || data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrentSpecial())) {
				break;
			}
		}
		while (data.srcCode.isValidIndex());
		return Identifier.toIdentifier(data.factory, data.srcCode.substring(start.pos, data.srcCode.getPos() - start.pos),
				upper && data.settings.dotNotationUpper ? Identifier.CASE_UPPER : Identifier.CASE_ORIGNAL, start, data.srcCode.getPosition());
	}

	protected String identifier(Data data, boolean firstCanBeNumber) {
		int start = data.srcCode.getPos();
		if (!data.srcCode.isCurrentLetter() && !data.srcCode.isCurrentSpecial()) {
			if (!firstCanBeNumber) return null;
			else if (!data.srcCode.isCurrentBetween('0', '9')) return null;
		}
		do {
			data.srcCode.next();
			if (!(data.srcCode.isCurrentLetter() || data.srcCode.isCurrentBetween('0', '9') || data.srcCode.isCurrentSpecial())) {
				break;
			}
		}
		while (data.srcCode.isValidIndex());
		return data.srcCode.substring(start, data.srcCode.getPos() - start);
	}

	/**
	 * Transfomiert ein Collection Element das in eckigen Klammern aufgerufen wird. <br />
	 * EBNF:<br />
	 * <code>"[" impOp "]"</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private ExprString structElement(Data data) throws TemplateException {
		comments(data);
		ExprString name = data.factory.toExprString(assignOp(data));
		if (name instanceof LitString) ((LitString) name).fromBracket(true);
		comments(data);
		return name;
	}

	/**
	 * Liest die Argumente eines Funktonsaufruf ein und prueft ob die Funktion innerhalb der FLD
	 * (Function Library Descriptor) definiert ist. Falls sie existiert wird die Funktion gegen diese
	 * geprueft und ein built-in-function CFXD Element generiert, ansonsten ein normales funcion-call
	 * Element. <br />
	 * EBNF:<br />
	 * <code>[impOp{"," impOp}];</code>
	 * 
	 * @param name Identifier der Funktion als Zeichenkette
	 * @param checkLibrary Soll geprueft werden ob die Funktion innerhalb der Library existiert.
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private FunctionMember getFunctionMember(Data data, final ExprString name, boolean checkLibrary) throws TemplateException {

		// get Function Library
		checkLibrary = checkLibrary && data.flibs != null;
		FunctionLibFunction flf = null;
		if (checkLibrary) {
			if (!(name instanceof Literal)) throw new TemplateException(data.srcCode, "Syntax error"); // should never happen!

			for (int i = 0; i < data.flibs.length; i++) {
				flf = data.flibs[i].getFunction(((Literal) name).getString());
				if (flf != null) break;
			}
			if (flf == null) {
				checkLibrary = false;
			}
		}

		FunctionMember fm = null;
		while (true) {
			int pos = data.srcCode.getPos();
			// Element Function
			if (checkLibrary) {
				BIF bif = new BIF(data.factory, data.settings, flf);
				// TODO data.ep.add(flf, bif, data.srcCode);

				bif.setArgType(flf.getArgType());
				try {
					bif.setClassDefinition(flf.getFunctionClassDefinition());
				}
				catch (Throwable t) {
					ExceptionUtil.rethrowIfNecessary(t);
					throw new PageRuntimeException(t);
				}
				bif.setReturnType(flf.getReturnTypeAsString());
				fm = bif;

				if (flf.getArgType() == FunctionLibFunction.ARG_DYNAMIC && flf.hasDefaultValues()) {
					ArrayList<FunctionLibFunctionArg> args = flf.getArg();
					Iterator<FunctionLibFunctionArg> it = args.iterator();
					FunctionLibFunctionArg arg;
					while (it.hasNext()) {
						arg = it.next();
						if (arg.getDefaultValue() != null) bif.addArgument(
								new NamedArgument(data.factory.createLitString(arg.getName()), data.factory.createLitString(arg.getDefaultValue()), arg.getTypeAsString(), false));
					}
				}
			}
			else {
				fm = new UDF(name);
			}

			int count = getFunctionMemberAttrs(data, name, checkLibrary, fm, flf);

			if (checkLibrary) {
				// pre
				if (flf.hasTteClass()) {
					FunctionLibFunction tmp = flf.getEvaluator().pre((BIF) fm, flf);
					if (tmp != null && tmp != flf) {
						flf = tmp;
						data.srcCode.setPos(pos);
						continue;
					}
				}

				// check max attributes
				{
					boolean isDynamic = flf.getArgType() == FunctionLibFunction.ARG_DYNAMIC;
					int max = flf.getArgMax();
					// Dynamic
					if (isDynamic) {
						if (max != -1 && max < fm.getArguments().length) throw new TemplateException(data.srcCode,
								"Too many arguments (" + max + ":" + fm.getArguments().length + ") in function [ " + ASMUtil.display(name) + " ]");
					}
					// Fix
					else {
						if (flf.getArg().size() < fm.getArguments().length) {
							TemplateException te = new TemplateException(data.srcCode,
									"Too many arguments (" + flf.getArg().size() + ":" + fm.getArguments().length + ") in function call [" + ASMUtil.display(name) + "]");
							UDFUtil.addFunctionDoc(te, flf);
							throw te;
						}
					}

				}

				// check min attributes
				if (flf.getArgMin() > count) {
					TemplateException te = new TemplateException(data.srcCode, "Too few arguments in function [" + ASMUtil.display(name) + "]");
					if (flf.getArgType() == FunctionLibFunction.ARG_FIX) UDFUtil.addFunctionDoc(te, flf);
					throw te;
				}

				// evaluator
				if (flf.hasTteClass()) {
					flf.getEvaluator().execute((BIF) fm, flf);

				}
			}
			comments(data);

			if (checkLibrary) data.ep.add(flf, (BIF) fm, data.srcCode);

			break;
		}

		return fm;
	}

	private int getFunctionMemberAttrs(Data data, ExprString name, boolean checkLibrary, Func fm, FunctionLibFunction flf) throws TemplateException {
		// Function Attributes
		ArrayList<FunctionLibFunctionArg> arrFuncLibAtt = null;
		// int libLen = 0;
		if (checkLibrary) {
			arrFuncLibAtt = flf.getArg();
			// libLen = arrFuncLibAtt.size();
		}
		int count = 0;
		do {
			data.srcCode.next();
			comments(data);

			// finish
			if (count == 0 && data.srcCode.isCurrent(')')) break;

			// Argument arg;
			if (checkLibrary && flf.getArgType() != FunctionLibFunction.ARG_DYNAMIC) {
				// current attribues from library
				String _type;
				try {
					_type = arrFuncLibAtt.get(count).getTypeAsString();
				}
				catch (IndexOutOfBoundsException e) {
					_type = null;
				}
				fm.addArgument(functionArgument(data, _type, false));

			}
			else {
				fm.addArgument(functionArgument(data, false));
			}

			comments(data);
			count++;
			if (data.srcCode.isCurrent(')')) break;
		}
		while (data.srcCode.isCurrent(','));

		if (!data.srcCode.forwardIfCurrent(')')) {
			if (name != null) {
				throw new TemplateException(data.srcCode, "Invalid Syntax Closing [)] for function [" + (flf != null ? flf.getName() : ASMUtil.display(name)) + "] not found");
			}
			throw new TemplateException(data.srcCode, "Invalid Syntax Closing [)] not found");
		}
		return count;
	}

	/**
	 * Sharps (#) die innerhalb von Expressions auftauchen haben in CFML keine weitere Beteutung und
	 * werden durch diese Methode einfach entfernt. <br />
	 * Beispiel:<br />
	 * <code>arrayLen(#arr#)</code> und <code>arrayLen(arr)</code> sind identisch. EBNF:<br />
	 * <code>"#" checker "#";</code>
	 * 
	 * @return CFXD Element
	 * @throws TemplateException
	 */
	private Expression sharp(Data data) throws TemplateException {
		if (!data.srcCode.forwardIfCurrent('#')) return null;
		Expression expr;
		comments(data);
		boolean old = data.allowLowerThan;
		data.allowLowerThan = true;
		expr = assignOp(data);
		data.allowLowerThan = old;
		comments(data);
		if (!data.srcCode.forwardIfCurrent('#'))
			throw new TemplateException(data.srcCode, "Syntax Error, Invalid Construct " + (data.srcCode.length() < 30 ? data.srcCode.toString() : ""));
		comments(data);
		return expr;
	}

	/**
	 * @param data
	 * @return parsed Element
	 * @throws TemplateException
	 */
	private Expression simple(Data data, String[] breakConditions) throws TemplateException {
		StringBuffer sb = new StringBuffer();
		Position line = data.srcCode.getPosition();
		outer: while (data.srcCode.isValidIndex()) {
			for (int i = 0; i < breakConditions.length; i++) {
				if (data.srcCode.isCurrent(breakConditions[i])) break outer;
			}

			if (data.srcCode.isCurrent('"') || data.srcCode.isCurrent('#') || data.srcCode.isCurrent('\'')) {
				throw new TemplateException(data.srcCode, "Simple attribute value can't contain [" + data.srcCode.getCurrent() + "]");
			}
			sb.append(data.srcCode.getCurrent());
			data.srcCode.next();
		}
		comments(data);

		return data.factory.createLitString(sb.toString(), line, data.srcCode.getPosition());
	}

	/**
	 * Liest alle folgenden Komentare ein. <br />
	 * EBNF:<br />
	 * <code>{?-"\n"} "\n";</code>
	 * 
	 * @param data
	 * @throws TemplateException
	 */
	protected void comments(Data data) throws TemplateException {
		data.srcCode.removeSpace();
		while (comment(data)) {
			data.srcCode.removeSpace();
		}
	}

	/**
	 * Liest einen Einzeiligen Kommentar ein. <br />
	 * EBNF:<br />
	 * <code>{?-"\n"} "\n";</code>
	 * 
	 * @return bool Wurde ein Kommentar entfernt?
	 * @throws TemplateException
	 */
	private boolean comment(Data data) throws TemplateException {
		if (singleLineComment(data.srcCode) || multiLineComment(data) || CFMLTransformer.comment(data.srcCode)) return true;
		return false;
	}

	/**
	 * Liest einen Mehrzeiligen Kommentar ein. <br />
	 * EBNF:<br />
	 * <code>?-"*<!-- -->/";</code>
	 * 
	 * @return bool Wurde ein Kommentar entfernt?
	 * @throws TemplateException
	 */
	private boolean multiLineComment(Data data) throws TemplateException {
		SourceCode cfml = data.srcCode;
		if (!cfml.forwardIfCurrent("/*")) return false;
		int pos = cfml.getPos();
		boolean isDocComment = cfml.isCurrent('*');
		while (cfml.isValidIndex()) {
			if (cfml.isCurrent("*/")) break;
			cfml.next();
		}
		if (!cfml.forwardIfCurrent("*/")) {
			cfml.setPos(pos);
			throw new TemplateException(cfml, "comment is not closed");
		}
		if (isDocComment) {
			String comment = cfml.substring(pos - 2, cfml.getPos() - pos);
			data.docComment = docCommentTransformer.transform(data.factory, comment);
		}
		return true;
	}

	/**
	 * Liest einen Einzeiligen Kommentar ein. <br />
	 * EBNF:<br />
	 * <code>{?-"\n"} "\n";</code>
	 * 
	 * @return bool Wurde ein Kommentar entfernt?
	 */
	private boolean singleLineComment(SourceCode cfml) {
		if (!cfml.forwardIfCurrent("//")) return false;
		return cfml.nextLine();
	}

}
