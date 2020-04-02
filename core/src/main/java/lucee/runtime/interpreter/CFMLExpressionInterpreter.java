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
package lucee.runtime.interpreter;

import java.util.ArrayList;
import java.util.List;

import lucee.commons.lang.CFTypes;
import lucee.commons.lang.ParserString;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageContext;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.interpreter.ref.Ref;
import lucee.runtime.interpreter.ref.Set;
import lucee.runtime.interpreter.ref.cast.Casting;
import lucee.runtime.interpreter.ref.func.BIFCall;
import lucee.runtime.interpreter.ref.func.UDFCall;
import lucee.runtime.interpreter.ref.literal.LBoolean;
import lucee.runtime.interpreter.ref.literal.LFunctionValue;
import lucee.runtime.interpreter.ref.literal.LNumber;
import lucee.runtime.interpreter.ref.literal.LString;
import lucee.runtime.interpreter.ref.literal.LStringBuffer;
import lucee.runtime.interpreter.ref.literal.Literal;
import lucee.runtime.interpreter.ref.op.And;
import lucee.runtime.interpreter.ref.op.BigDiv;
import lucee.runtime.interpreter.ref.op.BigIntDiv;
import lucee.runtime.interpreter.ref.op.BigMinus;
import lucee.runtime.interpreter.ref.op.BigMod;
import lucee.runtime.interpreter.ref.op.BigMulti;
import lucee.runtime.interpreter.ref.op.BigPlus;
import lucee.runtime.interpreter.ref.op.CT;
import lucee.runtime.interpreter.ref.op.Concat;
import lucee.runtime.interpreter.ref.op.Cont;
import lucee.runtime.interpreter.ref.op.Div;
import lucee.runtime.interpreter.ref.op.EEQ;
import lucee.runtime.interpreter.ref.op.EQ;
import lucee.runtime.interpreter.ref.op.EQV;
import lucee.runtime.interpreter.ref.op.Elvis;
import lucee.runtime.interpreter.ref.op.Exp;
import lucee.runtime.interpreter.ref.op.GT;
import lucee.runtime.interpreter.ref.op.GTE;
import lucee.runtime.interpreter.ref.op.Imp;
import lucee.runtime.interpreter.ref.op.IntDiv;
import lucee.runtime.interpreter.ref.op.LT;
import lucee.runtime.interpreter.ref.op.LTE;
import lucee.runtime.interpreter.ref.op.Minus;
import lucee.runtime.interpreter.ref.op.Mod;
import lucee.runtime.interpreter.ref.op.Multi;
import lucee.runtime.interpreter.ref.op.NCT;
import lucee.runtime.interpreter.ref.op.NEEQ;
import lucee.runtime.interpreter.ref.op.NEQ;
import lucee.runtime.interpreter.ref.op.Negate;
import lucee.runtime.interpreter.ref.op.Not;
import lucee.runtime.interpreter.ref.op.Or;
import lucee.runtime.interpreter.ref.op.Plus;
import lucee.runtime.interpreter.ref.op.Xor;
import lucee.runtime.interpreter.ref.var.Assign;
import lucee.runtime.interpreter.ref.var.Bind;
import lucee.runtime.interpreter.ref.var.DynAssign;
import lucee.runtime.interpreter.ref.var.Variable;
import lucee.runtime.type.scope.Scope;
import lucee.runtime.type.scope.ScopeSupport;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.function.FunctionLibFunctionArg;

/**
 * 
 * 
 * Der CFMLExprTransfomer implementiert das Interface ExprTransfomer, er bildet die Parser Grammatik
 * ab, die unten definiert ist. Er erhaelt als Eingabe CFML Code, als String oder CFMLString, der
 * einen CFML Expression erhaelt und liefert ein CFXD Element zurueck, das diesen Ausdruck abbildet.
 * Mithilfe der FunctionLib's, kann er Funktionsaufrufe, die Teil eines Ausdruck sein koennen,
 * erkennen und validieren. Dies geschieht innerhalb der Methode function. Falls ein
 * Funktionsaufruf, einer Funktion innerhalb einer FunctionLib entspricht, werden diese
 * gegeneinander verglichen und der Aufruf wird als Build-In-Funktion uebernommen, andernfalls wird
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
                     "arguments" | "cookie" | " client";
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
public class CFMLExpressionInterpreter {

	private static final LNumber PLUS_ONE = new LNumber(new Double(1));
	private static final LNumber MINUS_ONE = new LNumber(new Double(-1));

	protected static final short STATIC = 0;
	private static final short DYNAMIC = 1;
	private static FunctionLibFunction LITERAL_ARRAY = null;
	private static FunctionLibFunction LITERAL_STRUCT = null;
	private static FunctionLibFunction JSON_ARRAY = null;
	private static FunctionLibFunction JSON_STRUCT = null;
	private static FunctionLibFunction LITERAL_ORDERED_STRUCT = null;

	// private static final int CASE_TYPE_UPPER = 0;
	// private static final int CASE_TYPE_LOWER = 1;
	// private static final int CASE_TYPE_ORIGINAL = 2;

	protected short mode = 0;

	protected ParserString cfml;
	// protected Document doc;
	// protected FunctionLib[] fld;
	protected PageContext pc;
	private FunctionLib fld;
	protected boolean allowNullConstant = false;
	private boolean preciseMath;
	private final boolean isJson;
	private final boolean limited;
	private ConfigImpl config;

	public CFMLExpressionInterpreter() {
		this(true);
	}

	public CFMLExpressionInterpreter(boolean limited) {
		this.isJson = this instanceof JSONExpressionInterpreter;
		this.limited = limited || isJson; // json is always limited
	}

	public Object interpret(PageContext pc, String str) throws PageException {
		return interpret(pc, str, false);
	}

	public Object interpret(PageContext pc, String str, boolean preciseMath) throws PageException {
		this.cfml = new ParserString(str);
		this.preciseMath = preciseMath;
		init(pc);

		if (LITERAL_ARRAY == null) LITERAL_ARRAY = fld.getFunction("_literalArray");
		if (LITERAL_STRUCT == null) LITERAL_STRUCT = fld.getFunction("_literalStruct");
		if (JSON_ARRAY == null) JSON_ARRAY = fld.getFunction("_jsonArray");
		if (JSON_STRUCT == null) JSON_STRUCT = fld.getFunction("_jsonStruct");
		if (LITERAL_ORDERED_STRUCT == null) LITERAL_ORDERED_STRUCT = fld.getFunction("_literalOrderedStruct");

		cfml.removeSpace();
		Ref ref = assignOp();
		cfml.removeSpace();

		if (cfml.isAfterLast()) {
			// data.put(str+":"+preciseMath,ref);
			return ref.getValue(pc);
		}
		if (cfml.toString().length() > 1024) throw new InterpreterException("Syntax Error, invalid Expression [" + cfml.toString().substring(0,1024) + "]");
		
		throw new InterpreterException("Syntax Error, invalid Expression [" + cfml.toString() + "]");
	}

	private void init(PageContext pc) {
		this.pc = pc = ThreadLocalPageContext.get(pc);

		int dialect = CFMLEngine.DIALECT_CFML;
		if (this.pc != null) {
			this.config = (ConfigImpl) this.pc.getConfig();
			dialect = this.pc.getCurrentTemplateDialect();
		}
		else {
			this.config = (ConfigImpl) ThreadLocalPageContext.getConfig();
			if (config == null) {
				try {
					config = (ConfigImpl) CFMLEngineFactory.getInstance().createConfig(null, "localhost", "/index.cfm");// TODO set a context root
				}
				catch (Exception e) {}
			}
		}
		fld = config.getCombinedFLDs(dialect);
	}

	/*
	 * private FunctionLibFunction getFLF(String name) { FunctionLibFunction flf=null; for (int i = 0; i
	 * < flds.length; i++) { flf = flds[i].getFunction(name); if (flf != null) break; } return flf; }
	 */

	protected Object interpretPart(PageContext pc, ParserString cfml) throws PageException {
		this.cfml = cfml;
		init(pc);

		cfml.removeSpace();
		return assignOp().getValue(pc);
	}

	/**
	 * Liest einen gelableten Funktionsparamter ein <br />
	 * EBNF:<br />
	 * <code>assignOp [":" spaces assignOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref functionArgDeclarationVarString() throws PageException {

		cfml.removeSpace();
		StringBuilder str = new StringBuilder();
		String id = null;
		while ((id = identifier(false)) != null) {
			if (str.length() > 0) str.append('.');
			str.append(id);
			cfml.removeSpace();
			if (!cfml.forwardIfCurrent('.')) break;
			cfml.removeSpace();
		}
		cfml.removeSpace();
		if (str.length() > 0 && cfml.charAt(cfml.getPos() - 1) != '.') return new LString(str.toString());

		throw new InterpreterException("invalid variable name definition");
	}

	/**
	 * Liest einen gelableten Funktionsparamter ein <br />
	 * EBNF:<br />
	 * <code>assignOp [":" spaces assignOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref functionArgDeclaration() throws PageException {
		Ref ref = impOp();
		if (cfml.forwardIfCurrent(':') || cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			ref = new LFunctionValue(ref, assignOp());
		}
		return ref;
	}

	/**
	 * Transfomiert Zuweisungs Operation. <br />
	 * EBNF:<br />
	 * <code>eqvOp ["=" spaces assignOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	protected Ref assignOp() throws PageException {
		Ref ref = contOp();

		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			if (mode == STATIC || ref instanceof Literal) {
				ref = new DynAssign(ref, assignOp(), limited);
			}
			else {
				ref = new Assign(ref, assignOp(), limited);
			}
		}
		return ref;
	}

	private Ref contOp() throws PageException {
		Ref ref = impOp();
		while (cfml.forwardIfCurrent('?')) {
			cfml.removeSpace();
			if (cfml.forwardIfCurrent(':')) {
				cfml.removeSpace();
				Ref right = assignOp();
				ref = new Elvis(ref, right, limited);

			}
			else {
				Ref left = assignOp();
				if (!cfml.forwardIfCurrent(':')) throw new InterpreterException("Syntax Error, invalid conditional operator [" + cfml.toString() + "]");
				cfml.removeSpace();
				Ref right = assignOp();
				ref = new Cont(ref, left, right, limited);
			}
		}
		return ref;
	}

	/**
	 * Transfomiert eine Implication (imp) Operation. <br />
	 * EBNF:<br />
	 * <code>eqvOp {"imp" spaces eqvOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref impOp() throws PageException {
		Ref ref = eqvOp();
		while (cfml.forwardIfCurrentAndNoWordAfter("imp")) {
			cfml.removeSpace();
			ref = new Imp(ref, eqvOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Equivalence (eqv) Operation. <br />
	 * EBNF:<br />
	 * <code>xorOp {"eqv" spaces xorOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref eqvOp() throws PageException {
		Ref ref = xorOp();
		while (cfml.forwardIfCurrent("eqv")) {
			cfml.removeSpace();
			ref = new EQV(ref, xorOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Xor (xor) Operation. <br />
	 * EBNF:<br />
	 * <code>orOp {"xor" spaces  orOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref xorOp() throws PageException {
		Ref ref = orOp();
		while (cfml.forwardIfCurrent("xor")) {
			cfml.removeSpace();
			ref = new Xor(ref, orOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Or (or) Operation. Im Gegensatz zu CFMX , werden "||" Zeichen auch als Or
	 * Operatoren anerkannt. <br />
	 * EBNF:<br />
	 * <code>andOp {("or" | "||") spaces andOp}; (* "||" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref orOp() throws PageException {
		Ref ref = andOp();
		while (cfml.isValidIndex() && (cfml.forwardIfCurrent("||") || cfml.forwardIfCurrent("or"))) {
			cfml.removeSpace();
			ref = new Or(ref, andOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine And (and) Operation. Im Gegensatz zu CFMX , werden "&&" Zeichen auch als And
	 * Operatoren anerkannt. <br />
	 * EBNF:<br />
	 * <code>notOp {("and" | "&&") spaces notOp}; (* "&&" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref andOp() throws PageException {
		Ref ref = notOp();
		while (cfml.isValidIndex() && (cfml.forwardIfCurrent("&&") || cfml.forwardIfCurrent("and"))) {
			cfml.removeSpace();
			ref = new And(ref, notOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Not (not) Operation. Im Gegensatz zu CFMX , wird das "!" Zeichen auch als Not
	 * Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>[("not"|"!") spaces] decsionOp; (* "!" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref notOp() throws PageException {
		if (cfml.isValidIndex()) {
			if (cfml.isCurrent('!') && !cfml.isCurrent("!=")) {
				cfml.next();
				cfml.removeSpace();
				return new Not(decsionOp(), limited);
			}
			else if (cfml.forwardIfCurrentAndNoWordAfter("not")) {
				cfml.removeSpace();
				return new Not(decsionOp(), limited);
			}
		}
		return decsionOp();
	}

	/**
	 * <font f>Transfomiert eine Vergleichs Operation. <br />
	 * EBNF:<br />
	 * <code>concatOp {("neq"|"eq"|"gte"|"gt"|"lte"|"lt"|"ct"|
	                  "contains"|"nct"|"does not contain") spaces concatOp}; 
	         (* "ct"=conatains und "nct"=does not contain; Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref decsionOp() throws PageException {

		Ref ref = concatOp();
		boolean hasChanged = false;
		// ct, contains
		if (cfml.isValidIndex()) {
			do {
				hasChanged = false;
				if (cfml.isCurrent('c')) {
					if (cfml.forwardIfCurrent("ct")) {
						cfml.removeSpace();
						ref = new CT(ref, concatOp(), limited);
						hasChanged = true;
					}
					else if (cfml.forwardIfCurrent("contains")) {
						cfml.removeSpace();
						ref = new CT(ref, concatOp(), limited);
						hasChanged = true;
					}
				}
				// does not contain
				else if (cfml.forwardIfCurrent("does", "not", "contain")) {
					cfml.removeSpace();
					ref = new NCT(ref, concatOp(), limited);
					hasChanged = true;
				}

				// equal, eq
				else if (cfml.isCurrent("eq") && !cfml.isCurrent("eqv")) {
					cfml.setPos(cfml.getPos() + 2);
					cfml.forwardIfCurrent("ual");
					cfml.removeSpace();
					ref = new EQ(ref, concatOp(), limited);
					hasChanged = true;
				}
				// ==
				else if (cfml.forwardIfCurrent("==")) {
					if (cfml.forwardIfCurrent('=')) {
						cfml.removeSpace();
						ref = new EEQ(ref, concatOp(), limited);
					}
					else {
						cfml.removeSpace();
						ref = new EQ(ref, concatOp(), limited);
					}
					hasChanged = true;
				}

				// !=
				else if (cfml.forwardIfCurrent("!=")) {
					if (cfml.forwardIfCurrent('=')) {
						cfml.removeSpace();
						ref = new NEEQ(ref, concatOp(), limited);
					}
					else {
						cfml.removeSpace();
						ref = new NEQ(ref, concatOp(), limited);
					}
					hasChanged = true;
				}

				// <=/</<>
				else if (cfml.forwardIfCurrent('<')) {
					if (cfml.forwardIfCurrent('=')) {
						cfml.removeSpace();
						ref = new LTE(ref, concatOp(), limited);
					}
					else if (cfml.forwardIfCurrent('>')) {
						cfml.removeSpace();
						ref = new NEQ(ref, concatOp(), limited);
					}
					else {
						cfml.removeSpace();
						ref = new LT(ref, concatOp(), limited);
					}
					hasChanged = true;
				}
				// >/>=
				else if (cfml.forwardIfCurrent('>')) {
					if (cfml.forwardIfCurrent('=')) {
						cfml.removeSpace();
						ref = new GTE(ref, concatOp(), limited);
					}
					else {
						cfml.removeSpace();
						ref = new GT(ref, concatOp(), limited);
					}
					hasChanged = true;
				}

				// gt, gte, greater than or equal to, greater than
				else if (cfml.isCurrent('g')) {
					if (cfml.forwardIfCurrent("gt")) {
						if (cfml.forwardIfCurrent('e')) {
							cfml.removeSpace();
							ref = new GTE(ref, concatOp(), limited);
						}
						else {
							cfml.removeSpace();
							ref = new GT(ref, concatOp(), limited);
						}
						hasChanged = true;
					}
					else if (cfml.forwardIfCurrent("greater", "than")) {
						if (cfml.forwardIfCurrent("or", "equal", "to", true)) {
							cfml.removeSpace();
							ref = new GTE(ref, concatOp(), limited);
						}
						else {
							cfml.removeSpace();
							ref = new GT(ref, concatOp(), limited);
						}
						hasChanged = true;
					}
					else if (cfml.forwardIfCurrent("ge")) {
						cfml.removeSpace();
						ref = new GTE(ref, concatOp(), limited);
						hasChanged = true;
					}
				}

				// is, is not
				else if (cfml.forwardIfCurrent("is")) {
					if (cfml.forwardIfCurrent("not", true)) {
						cfml.removeSpace();
						ref = new NEQ(ref, concatOp(), limited);
					}
					else {
						cfml.removeSpace();
						ref = new EQ(ref, concatOp(), limited);
					}
					hasChanged = true;
				}

				// lt, lte, less than, less than or equal to
				else if (cfml.isCurrent('l')) {
					if (cfml.forwardIfCurrent("lt")) {
						if (cfml.forwardIfCurrent('e')) {
							cfml.removeSpace();
							ref = new LTE(ref, concatOp(), limited);
						}
						else {
							cfml.removeSpace();
							ref = new LT(ref, concatOp(), limited);
						}
						hasChanged = true;
					}
					else if (cfml.forwardIfCurrent("less", "than")) {
						if (cfml.forwardIfCurrent("or", "equal", "to", true)) {
							cfml.removeSpace();
							ref = new LTE(ref, concatOp(), limited);
						}
						else {
							cfml.removeSpace();
							ref = new LT(ref, concatOp(), limited);
						}
						hasChanged = true;
					}
					else if (cfml.forwardIfCurrent("le")) {
						cfml.removeSpace();
						ref = new LTE(ref, concatOp(), limited);
						hasChanged = true;
					}
				}

				// neq, not equal, nct
				else if (cfml.isCurrent('n')) {
					// Not Equal
					if (cfml.forwardIfCurrent("neq")) {
						cfml.removeSpace();
						ref = new NEQ(ref, concatOp(), limited);
						hasChanged = true;
					}
					// Not Equal (Alias)
					else if (cfml.forwardIfCurrent("not", "equal")) {
						cfml.removeSpace();
						ref = new NEQ(ref, concatOp(), limited);
						hasChanged = true;
					}
					// nct
					else if (cfml.forwardIfCurrent("nct")) {
						cfml.removeSpace();
						ref = new NCT(ref, concatOp(), limited);
						hasChanged = true;
					}
				}
			}
			while (hasChanged);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Konkatinations-Operator (&) Operation. Im Gegensatz zu CFMX , wird das "!"
	 * Zeichen auch als Not Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>plusMinusOp {"&" spaces concatOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref concatOp() throws PageException {
		Ref ref = plusMinusOp();

		while (cfml.isCurrent('&') && !cfml.isNext('&')) {
			cfml.next();
			ref = _concat(ref);
		}
		return ref;
	}

	/**
	 * Transfomiert die mathematischen Operatoren Plus und Minus (1,-). <br />
	 * EBNF:<br />
	 * <code>modOp [("-"|"+") spaces plusMinusOp];</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref plusMinusOp() throws PageException {
		Ref ref = modOp();

		while (!cfml.isLast()) {
			// Plus Operation
			if (cfml.forwardIfCurrent('+')) {
				ref = _plus(ref);
			}
			// Minus Operation
			else if (cfml.forwardIfCurrent('-')) {
				ref = _minus(ref);
			}
			else break;
		}
		return ref;
	}

	private Ref _plus(Ref ref) throws PageException {
		// +=
		if (cfml.isCurrent('=')) {
			cfml.next();
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigPlus(ref, right, limited) : new Plus(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigPlus(ref, modOp(), limited) : new Plus(ref, modOp(), limited);
		}
		return ref;
	}

	private Ref _minus(Ref ref) throws PageException {
		// -=
		if (cfml.isCurrent('=')) {
			cfml.next();
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigMinus(ref, right, limited) : new Minus(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigMinus(ref, modOp(), limited) : new Minus(ref, modOp(), limited);
		}
		return ref;
	}

	private Ref _div(Ref ref) throws PageException {
		// /=
		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigDiv(ref, right, limited) : new Div(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigDiv(ref, expoOp(), limited) : new Div(ref, expoOp(), limited);
		}
		return ref;
	}

	private Ref _intdiv(Ref ref) throws PageException {
		// \=
		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigIntDiv(ref, right, limited) : new IntDiv(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigIntDiv(ref, expoOp(), limited) : new IntDiv(ref, expoOp(), limited);
		}
		return ref;
	}

	private Ref _mod(Ref ref) throws PageException {
		// %=
		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigMod(ref, right, limited) : new Mod(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigMod(ref, divMultiOp(), limited) : new Mod(ref, divMultiOp(), limited);
		}
		return ref;
	}

	private Ref _concat(Ref ref) throws PageException {
		// &=
		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = new Concat(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = new Concat(ref, plusMinusOp(), limited);
		}
		return ref;
	}

	private Ref _multi(Ref ref) throws PageException {
		// \=
		if (cfml.forwardIfCurrent('=')) {
			cfml.removeSpace();
			Ref right = assignOp();
			Ref res = preciseMath ? new BigMulti(ref, right, limited) : new Multi(ref, right, limited);
			ref = new Assign(ref, res, limited);
		}
		else {
			cfml.removeSpace();
			ref = preciseMath ? new BigMulti(ref, expoOp(), limited) : new Multi(ref, expoOp(), limited);
		}
		return ref;
	}

	/**
	 * Transfomiert eine Modulus Operation. Im Gegensatz zu CFMX , wird das "%" Zeichen auch als Modulus
	 * Operator anerkannt. <br />
	 * EBNF:<br />
	 * <code>divMultiOp {("mod" | "%") spaces divMultiOp}; (* modulus operator , "%" Existiert in CFMX nicht *)</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref modOp() throws PageException {
		Ref ref = divMultiOp();

		while (cfml.isValidIndex() && (cfml.forwardIfCurrent('%') || cfml.forwardIfCurrent("mod"))) {
			ref = _mod(ref);
		}
		return ref;
	}

	/**
	 * Transfomiert die mathematischen Operatoren Mal und Durch (*,/). <br />
	 * EBNF:<br />
	 * <code>expoOp {("*"|"/") spaces expoOp};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref divMultiOp() throws PageException {
		Ref ref = expoOp();

		while (!cfml.isLast()) {
			// Multiply Operation
			if (cfml.forwardIfCurrent('*')) {
				ref = _multi(ref);
			}
			// Divide Operation
			else if (cfml.isCurrent('/') && (!cfml.isCurrent("/>"))) {
				cfml.next();
				ref = _div(ref);
			}
			// Divide Operation
			else if (cfml.isCurrent('\\')) {
				cfml.next();
				ref = _intdiv(ref);
			}
			else {
				break;
			}
		}
		return ref;
	}

	/**
	 * Transfomiert den Exponent Operator (^,exp). Im Gegensatz zu CFMX , werden die Zeichen " exp "
	 * auch als Exponent anerkannt. <br />
	 * EBNF:<br />
	 * <code>clip {("exp"|"^") spaces clip};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref expoOp() throws PageException {
		Ref ref = unaryOp();

		while (cfml.isValidIndex() && (cfml.forwardIfCurrent('^') || cfml.forwardIfCurrent("exp"))) {
			cfml.removeSpace();
			ref = new Exp(ref, unaryOp(), limited);
		}
		return ref;
	}

	private Ref unaryOp() throws PageException {
		Ref ref = negateMinusOp();

		if (cfml.forwardIfCurrent("--")) ref = _unaryOp(ref, false);

		else if (cfml.forwardIfCurrent("++")) ref = _unaryOp(ref, true);
		return ref;
	}

	private Ref _unaryOp(Ref ref, boolean isPlus) throws PageException {
		cfml.removeSpace();
		Ref res = preciseMath ? new BigPlus(ref, isPlus ? PLUS_ONE : MINUS_ONE, limited) : new Plus(ref, isPlus ? PLUS_ONE : MINUS_ONE, limited);
		ref = new Assign(ref, res, limited);
		return preciseMath ? new BigPlus(ref, isPlus ? MINUS_ONE : PLUS_ONE, limited) : new Plus(ref, isPlus ? MINUS_ONE : PLUS_ONE, limited);
	}

	/**
	 * Liest die Vordlobe einer Zahl ein
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref negateMinusOp() throws PageException {
		// And Operation
		if (cfml.forwardIfCurrent('-')) {
			if (cfml.forwardIfCurrent('-')) {
				cfml.removeSpace();
				Ref expr = clip();
				Ref res = preciseMath ? new BigMinus(expr, new LNumber(new Double(1)), limited) : new Minus(expr, new LNumber(new Double(1)), limited);
				return new Assign(expr, res, limited);
			}
			cfml.removeSpace();
			return new Negate(clip(), limited);

		}
		if (cfml.forwardIfCurrent('+')) {
			if (cfml.forwardIfCurrent('+')) {
				cfml.removeSpace();
				Ref expr = clip();
				Ref res = preciseMath ? new BigPlus(expr, new LNumber(new Double(1)), limited) : new Plus(expr, new LNumber(new Double(1)), limited);
				return new Assign(expr, res, limited);
			}
			cfml.removeSpace();
			return new Casting("numeric", CFTypes.TYPE_NUMERIC, clip());

		}
		return clip();
	}

	/**
	 * Verarbeitet Ausdruecke die inerhalb einer Klammer stehen. <br />
	 * EBNF:<br />
	 * <code>("(" spaces impOp ")" spaces) | checker;</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref clip() throws PageException {
		return checker();
	}

	/**
	 * Hier werden die verschiedenen Moeglichen Werte erkannt und jenachdem wird mit der passenden
	 * Methode weitergefahren <br />
	 * EBNF:<br />
	 * <code>string | number | dynamic | sharp;</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref checker() throws PageException {

		Ref ref = null;
		// String
		if (cfml.isCurrentQuoter()) {
			// mode=STATIC; is at the end of the string function because must set after execution
			return string();
		}
		// Number
		if (cfml.isCurrentDigit() || cfml.isCurrent('.')) {
			// mode=STATIC; is at the end of the string function because must set after execution
			return number();
		}
		// Dynamic
		if ((ref = dynamic()) != null) {
			mode = DYNAMIC;
			return ref;
		}
		// Sharp
		if (!limited && (ref = sharp()) != null) {
			mode = DYNAMIC;
			return ref;
		}
		// JSON
		if ((ref = json(isJson ? JSON_ARRAY : LITERAL_ARRAY, '[', ']')) != null) {
			mode = DYNAMIC;
			return ref;
		}
		if ((ref = json(isJson ? JSON_STRUCT : LITERAL_STRUCT, '{', '}')) != null) {
			mode = DYNAMIC;
			return ref;
		}

		if (cfml.isAfterLast() && cfml.toString().trim().length() == 0) return new LString("");

		// else Error
		String str = cfml.toString();
		int pos = cfml.getPos();
		if (str.length() > 100) {
			// Failure is in the beginning
			if (pos <= 10) {
				str = str.substring(0, 20) + " ...";
			}
			// Failure is in the end
			else if ((str.length() - pos) <= 10) {
				str = "... " + str.substring(str.length() - 20, str.length());
			}
			else {
				str = "... " + str.substring(pos - 10, pos + 10) + " ...";
			}
		}
		throw new InterpreterException("Syntax Error, Invalid Construct", "at position " + (pos + 1) + " in [" + str + "]");
	}

	protected Ref json(FunctionLibFunction flf, char start, char end) throws PageException {
		if (!cfml.isCurrent(start)) return null;

		if (cfml.forwardIfCurrent('[', ':', ']') || cfml.forwardIfCurrent('[', '=', ']')) {
			return new BIFCall(LITERAL_ORDERED_STRUCT, new Ref[0]);
		}

		Ref[] args = functionArg(flf.getName(), false, flf, end);
		if (args != null && args.length > 0 && flf == LITERAL_ARRAY) {
			if (args[0] instanceof LFunctionValue) {
				for (int i = 1; i < args.length; i++) {
					if (!(args[i] instanceof LFunctionValue))
						throw new TemplateException("invalid argument for literal ordered struct, only named arguments are allowed like {name:\"value\",name2:\"value2\"}");
				}
				flf = LITERAL_ORDERED_STRUCT;
			}
			else {
				for (int i = 1; i < args.length; i++) {
					if (args[i] instanceof LFunctionValue) throw new TemplateException("invalid argument for literal array, no named arguments are allowed");
				}

			}
		}

		return new BIFCall(flf, args);
	}

	/**
	 * Transfomiert einen lierale Zeichenkette. <br />
	 * EBNF:<br />
	 * <code>("'" {"##"|"''"|"#" impOp "#"| ?-"#"-"'" } "'") | 
	                  (""" {"##"|""""|"#" impOp "#"| ?-"#"-""" } """);</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	protected Ref string() throws PageException {

		// Init Parameter
		char quoter = cfml.getCurrentLower();
		LStringBuffer str = new LStringBuffer();
		Ref value = null;

		while (cfml.hasNext()) {
			cfml.next();
			// check sharp
			if (!limited && cfml.isCurrent('#')) {
				if (cfml.isNext('#')) {
					cfml.next();
					str.append('#');
				}
				else {
					cfml.next();
					cfml.removeSpace();
					if (!str.isEmpty() || value != null) str.append(assignOp());
					else value = assignOp();
					cfml.removeSpace();
					if (!cfml.isCurrent('#')) throw new InterpreterException("Invalid Syntax Closing [#] not found");
				}
			}
			else if (cfml.isCurrent(quoter)) {
				if (cfml.isNext(quoter)) {
					cfml.next();
					str.append(quoter);
				}
				else {
					break;
				}
			}
			// all other character
			else {
				str.append(cfml.getCurrent());
			}
		}
		if (!cfml.forwardIfCurrent(quoter)) throw new InterpreterException("Invalid String Literal Syntax Closing [" + quoter + "] not found");

		cfml.removeSpace();
		mode = STATIC;
		if (value != null) {
			if (str.isEmpty()) return value;
			return new Concat(value, str, limited);
		}
		return str;
	}

	/**
	 * Transfomiert einen numerische Wert. Die Laenge des numerischen Wertes interessiert nicht zu
	 * uebersetzungszeit, ein "Overflow" fuehrt zu einem Laufzeitfehler. Da die zu erstellende CFXD,
	 * bzw. dieser Transfomer, keine Vorwegnahme des Laufzeitsystems vornimmt. <br />
	 * EBNF:<br />
	 * <code>["+"|"-"] digit {digit} {"." digit {digit}};</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref number() throws PageException {
		// check first character is a number literal representation
		StringBuilder rtn = new StringBuilder(6);

		// get digit on the left site of the dot
		if (cfml.isCurrent('.')) rtn.append('0');
		else digit(rtn);
		// read dot if exist
		if (cfml.forwardIfCurrent('.')) {
			rtn.append('.');
			int before = cfml.getPos();
			digit(rtn);

			if (before < cfml.getPos() && cfml.forwardIfCurrent('e')) {
				Boolean expOp = null;
				if (cfml.forwardIfCurrent('+')) expOp = Boolean.TRUE;
				else if (cfml.forwardIfCurrent('-')) expOp = Boolean.FALSE;

				if (cfml.isCurrentDigit()) {
					if (expOp == Boolean.FALSE) rtn.append("e-");
					else if (expOp == Boolean.TRUE) rtn.append("e+");
					else rtn.append('e');
					digit(rtn);
				}
				else {
					if (expOp != null) cfml.previous();
					cfml.previous();
				}
			}
			// read right side of the dot
			if (before == cfml.getPos()) throw new InterpreterException("Number can't end with [.]");
		}

		// scientific notation
		else if (cfml.forwardIfCurrent('e')) {
			Boolean expOp = null;
			if (cfml.forwardIfCurrent('+')) expOp = Boolean.TRUE;
			else if (cfml.forwardIfCurrent('-')) expOp = Boolean.FALSE;

			if (cfml.isCurrentBetween('0', '9')) {
				rtn.append('e');
				if (expOp == Boolean.FALSE) rtn.append('-');
				else if (expOp == Boolean.TRUE) rtn.append('+');
				digit(rtn);
			}
			else {
				if (expOp != null) cfml.previous();
				cfml.previous();
			}
		}

		cfml.removeSpace();
		mode = STATIC;
		return new LNumber(rtn.toString());

	}

	/**
	 * Liest die reinen Zahlen innerhalb des CFMLString aus und gibt diese als Zeichenkette zurueck.
	 * <br />
	 * EBNF:<br />
	 * <code>"0"|..|"9";</code>
	 * 
	 * @param rtn
	 */
	private void digit(StringBuilder rtn) {

		while (cfml.isValidIndex()) {
			if (!cfml.isCurrentDigit()) break;
			rtn.append(cfml.getCurrentLower());
			cfml.next();
		}
	}

	/**
	 * Liest den folgenden idetifier ein und prueft ob dieser ein boolscher Wert ist. Im Gegensatz zu
	 * CFMX wird auch "yes" und "no" als bolscher <wert akzeptiert, was bei CFMX nur beim Umwandeln
	 * einer Zeichenkette zu einem boolschen Wert der Fall ist.<br />
	 * Wenn es sich um keinen bolschen Wert handelt wird der folgende Wert eingelesen mit seiner ganzen
	 * Hirarchie. <br />
	 * EBNF:<br />
	 * <code>"true" | "false" | "yes" | "no" | startElement  
	                  {("." identifier | "[" structElement "]" )[function] };</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref dynamic() throws PageException {

		// get First Element of the Variable
		int pos = cfml.getPos();
		String name = identifier(false);
		if (name == null) {
			if (!cfml.forwardIfCurrent('(')) return null;
			cfml.removeSpace();
			Ref ref = assignOp();

			if (!cfml.forwardIfCurrent(')')) throw new InterpreterException("Invalid Syntax Closing [)] not found");
			cfml.removeSpace();
			return limited ? ref : subDynamic(ref);
		}

		cfml.removeSpace();

		// Boolean constant
		if (name.equalsIgnoreCase("TRUE")) {
			cfml.removeSpace();
			return LBoolean.TRUE;
		}
		else if (name.equalsIgnoreCase("FALSE")) {
			cfml.removeSpace();
			return LBoolean.FALSE;
		}
		else if (!isJson && name.equalsIgnoreCase("YES")) {
			cfml.removeSpace();
			return LBoolean.TRUE;
		}
		else if (!isJson && name.equalsIgnoreCase("NO")) {
			cfml.removeSpace();
			return LBoolean.FALSE;
		}
		else if (allowNullConstant && name.equalsIgnoreCase("NULL")) {
			cfml.removeSpace();
			return new LString(null);
		}
		else if (!limited && name.equalsIgnoreCase("NEW")) {
			Ref res = newOp();
			if (res != null) return res;
		}
		return limited ? startElement(name) : subDynamic(startElement(name));

	}

	private Ref subDynamic(Ref ref) throws PageException {
		String name = null;

		// Loop over nested Variables
		while (cfml.isValidIndex()) {
			// .
			if (cfml.forwardIfCurrent('.')) {
				// Extract next Var String
				cfml.removeSpace();
				name = identifier(true);
				if (name == null) throw new InterpreterException("Invalid identifier");
				cfml.removeSpace();
				ref = new Variable(ref, name, limited);
			}
			// []
			else if (cfml.forwardIfCurrent('[')) {
				cfml.removeSpace();
				ref = new Variable(ref, assignOp(), limited);
				cfml.removeSpace();
				if (!cfml.forwardIfCurrent(']')) throw new InterpreterException("Invalid Syntax Closing []] not found");
			}
			// finish
			else {
				break;
			}

			cfml.removeSpace();

			if (cfml.isCurrent('(')) {
				if (!(ref instanceof Set)) throw new InterpreterException("invalid syntax " + ref.getTypeName() + " can't called as function");
				Set set = (Set) ref;
				ref = new UDFCall(set.getParent(pc), set.getKey(pc), functionArg(name, false, null, ')'));
			}
		}
		if (ref instanceof lucee.runtime.interpreter.ref.var.Scope) {
			lucee.runtime.interpreter.ref.var.Scope s = (lucee.runtime.interpreter.ref.var.Scope) ref;
			if (s.getScope() == Scope.SCOPE_ARGUMENTS || s.getScope() == Scope.SCOPE_LOCAL || s.getScope() == ScopeSupport.SCOPE_VAR) {
				ref = new Bind(s);
			}
		}
		return ref;
	}

	/**
	 * Extrahiert den Start Element einer Variale, dies ist entweder eine Funktion, eine Scope
	 * Definition oder eine undefinierte Variable. <br />
	 * EBNF:<br />
	 * <code>identifier "(" functionArg ")" | scope | identifier;</code>
	 * 
	 * @param name Einstiegsname
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref startElement(String name) throws PageException {

		// check function
		if (!limited && cfml.isCurrent('(')) {
			FunctionLibFunction function = fld.getFunction(name);
			Ref[] arguments = functionArg(name, true, function, ')');
			if (function != null) return new BIFCall(function, arguments);

			Ref ref = new lucee.runtime.interpreter.ref.var.Scope(Scope.SCOPE_UNDEFINED);
			return new UDFCall(ref, name, arguments);
		}
		// check scope
		return scope(name);
	}

	private Ref newOp() throws PageException {

		int start = cfml.getPos();
		String name = null;
		cfml.removeSpace();

		// first identifier
		name = identifier(true);
		Ref refName = null;
		if (name != null) {
			StringBuilder fullName = new StringBuilder();
			fullName.append(name);
			// Loop over addional identifier
			while (cfml.isValidIndex()) {
				if (cfml.forwardIfCurrent('.')) {
					cfml.removeSpace();
					name = identifier(true);
					if (name == null) throw new InterpreterException("invalid Component declaration");
					cfml.removeSpace();
					fullName.append('.');
					fullName.append(name);
				}
				else break;
			}
			refName = new LString(fullName.toString());
		}
		else {
			if (cfml.isCurrentQuoter()) refName = string();
			if (refName == null) {
				cfml.setPos(start);
				return null;
			}
		}
		cfml.removeSpace();

		if (cfml.isCurrent('(')) {
			FunctionLibFunction function = fld.getFunction("_createComponent");
			Ref[] arguments = functionArg("_createComponent", true, function, ')');
			Ref[] args = new Ref[arguments.length + 1];
			for (int i = 0; i < arguments.length; i++) {
				args[i] = arguments[i];
			}
			args[args.length - 1] = refName;
			BIFCall bif = new BIFCall(function, args);
			cfml.removeSpace();
			return bif;

		}
		throw new InterpreterException("invalid Component declaration ");

	}

	/**
	 * Liest einen CFML Scope aus, falls der folgende identifier keinem Scope entspricht, gibt die
	 * Variable null zurueck. <br />
	 * EBNF:<br />
	 * <code>"variable" | "cgi" | "url" | "form" | "session" | "application" | "arguments" | "cookie" | " client";</code>
	 * 
	 * @param idStr String identifier, wird aus Optimierungszwechen nicht innerhalb dieser Funktion
	 *            ausgelsen.
	 * @return CFXD Variable Element oder null
	 */
	private Ref scope(String idStr) {
		if (!limited && idStr.equals("var")) {
			String name = identifier(false);
			if (name != null) {
				cfml.removeSpace();
				return new Variable(new lucee.runtime.interpreter.ref.var.Scope(ScopeSupport.SCOPE_VAR), name, limited);
			}
		}
		int scope = limited ? Scope.SCOPE_UNDEFINED : VariableInterpreter.scopeString2Int(pc != null && pc.ignoreScopes(), idStr);
		if (scope == Scope.SCOPE_UNDEFINED) {
			return new Variable(new lucee.runtime.interpreter.ref.var.Scope(Scope.SCOPE_UNDEFINED), idStr, limited);
		}
		return new lucee.runtime.interpreter.ref.var.Scope(scope);

	}

	/**
	 * Liest einen Identifier aus und gibt diesen als String zurueck. <br />
	 * EBNF:<br />
	 * <code>(letter | "_") {letter | "_"|digit};</code>
	 * 
	 * @param firstCanBeNumber
	 * @return Identifier.
	 */
	private String identifier(boolean firstCanBeNumber) {
		if (!cfml.isCurrentLetter() && !cfml.isCurrentSpecial()) {
			if (!firstCanBeNumber) return null;
			else if (!cfml.isCurrentDigit()) return null;
		}
		boolean doUpper;
		PageSource ps = pc == null ? null : pc.getCurrentPageSource();
		if (ps != null) doUpper = !isJson && ps.getDialect() == CFMLEngine.DIALECT_CFML && ((MappingImpl) ps.getMapping()).getDotNotationUpperCase();
		else doUpper = !isJson && ((ConfigWebImpl) config).getDotNotationUpperCase(); // MUST .lucee should not be upper case

		StringBuilder sb = new StringBuilder();
		sb.append(doUpper ? cfml.getCurrentUpper() : cfml.getCurrent());
		do {
			cfml.next();
			if (!(cfml.isCurrentLetter() || cfml.isCurrentDigit() || cfml.isCurrentSpecial())) {
				break;
			}

			sb.append(doUpper ? cfml.getCurrentUpper() : cfml.getCurrent());
		}
		while (cfml.isValidIndex());
		return sb.toString();// cfml.substringLower(start,cfml.getPos()-start);
	}

	/**
	 * Liest die Argumente eines Funktonsaufruf ein und prueft ob die Funktion innerhalb der FLD
	 * (Function Library Descriptor) definiert ist. Falls sie existiert wird die Funktion gegen diese
	 * geprueft und ein build-in-function CFXD Element generiert, ansonsten ein normales funcion-call
	 * Element. <br />
	 * EBNF:<br />
	 * <code>[impOp{"," impOp}];</code>
	 * 
	 * @param name Identifier der Funktion als Zeichenkette
	 * @param checkLibrary Soll geprueft werden ob die Funktion innerhalb der Library existiert.
	 * @param flf FLD Function definition .
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref[] functionArg(String name, boolean checkLibrary, FunctionLibFunction flf, char end) throws PageException {

		// get Function Library
		checkLibrary = checkLibrary && flf != null;

		// Function Attributes
		List<Ref> arr = new ArrayList<Ref>();

		List<FunctionLibFunctionArg> arrFuncLibAtt = null;
		int libLen = 0;
		if (checkLibrary) {
			arrFuncLibAtt = flf.getArg();
			libLen = arrFuncLibAtt.size();
		}
		int count = 0;
		Ref ref;
		do {
			cfml.next();
			cfml.removeSpace();

			// finish
			if (cfml.isCurrent(end)) break;

			// too many Attributes
			boolean isDynamic = false;
			int max = -1;
			if (checkLibrary) {
				isDynamic = isDynamic(flf);
				max = flf.getArgMax();
				// Dynamic
				if (isDynamic) {
					if (max != -1 && max <= count) throw new InterpreterException("too many Attributes in function [" + name + "]");
				}
				// Fix
				else {
					if (libLen <= count) throw new InterpreterException("too many Attributes in function [" + name + "]");
				}
			}

			if (checkLibrary && !isDynamic) {
				// current attribues from library
				FunctionLibFunctionArg funcLibAtt = (FunctionLibFunctionArg) arrFuncLibAtt.get(count);
				short type = CFTypes.toShort(funcLibAtt.getTypeAsString(), false, CFTypes.TYPE_UNKNOW);
				if (type == CFTypes.TYPE_VARIABLE_STRING) {
					arr.add(functionArgDeclarationVarString());
				}
				else {
					ref = functionArgDeclaration();
					arr.add(new Casting(funcLibAtt.getTypeAsString(), type, ref));
				}
			}
			else {
				arr.add(functionArgDeclaration());
			}

			cfml.removeSpace();
			count++;
		}
		while (cfml.isCurrent(','));

		// end with ) ??
		if (!cfml.forwardIfCurrent(end)) {
			if (name.startsWith("_json")) throw new InterpreterException("Invalid Syntax Closing [" + end + "] not found");
			throw new InterpreterException("Invalid Syntax Closing [" + end + "] for function [" + name + "] not found");
		}

		// check min attributes
		if (checkLibrary && flf.getArgMin() > count) throw new InterpreterException("to less Attributes in function [" + name + "]");

		cfml.removeSpace();
		return (Ref[]) arr.toArray(new Ref[arr.size()]);
	}

	private boolean isDynamic(FunctionLibFunction flf) {
		return flf.getArgType() == FunctionLibFunction.ARG_DYNAMIC;
	}

	/**
	 * Sharps (#) die innerhalb von Expressions auftauchen haben in CFML keine weitere Beteutung und
	 * werden durch diese Methode einfach entfernt. <br />
	 * Beispiel:<br />
	 * <code>arrayLen(#arr#)</code> und <code>arrayLen(arr)</code> sind identisch. EBNF:<br />
	 * <code>"#" checker "#";</code>
	 * 
	 * @return CFXD Element
	 * @throws PageException
	 */
	private Ref sharp() throws PageException {
		if (!cfml.forwardIfCurrent('#')) return null;
		Ref ref;
		cfml.removeSpace();
		ref = assignOp();
		cfml.removeSpace();
		if (!cfml.forwardIfCurrent('#')) throw new InterpreterException("Syntax Error, Invalid Construct");
		cfml.removeSpace();
		return ref;
	}
}