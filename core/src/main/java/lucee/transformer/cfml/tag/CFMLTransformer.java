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
package lucee.transformer.cfml.tag;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.commons.lang.types.RefBoolean;
import lucee.commons.lang.types.RefBooleanImpl;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.MappingImpl;
import lucee.runtime.PageSource;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.config.Constants;
import lucee.runtime.config.Identification;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageExceptionImpl;
import lucee.runtime.exp.TemplateException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ArrayUtil;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.bytecode.Body;
import lucee.transformer.bytecode.BodyBase;
import lucee.transformer.bytecode.Page;
import lucee.transformer.bytecode.Statement;
import lucee.transformer.bytecode.statement.PrintOut;
import lucee.transformer.bytecode.statement.StatementBase;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.bytecode.util.ASMUtil;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.ExprTransformer;
import lucee.transformer.cfml.TransfomerSettings;
import lucee.transformer.cfml.attributes.AttributeEvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorException;
import lucee.transformer.cfml.evaluator.EvaluatorPool;
import lucee.transformer.cfml.evaluator.impl.ProcessingDirectiveException;
import lucee.transformer.cfml.expression.SimpleExprTransformer;
import lucee.transformer.cfml.script.AbstrCFMLScriptTransformer;
import lucee.transformer.cfml.script.AbstrCFMLScriptTransformer.ComponentTemplateException;
import lucee.transformer.cfml.script.CFMLScriptTransformer;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.CustomTagLib;
import lucee.transformer.library.tag.TagLib;
import lucee.transformer.library.tag.TagLibException;
import lucee.transformer.library.tag.TagLibFactory;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.library.tag.TagLibTagAttr;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * <pre>
		EBNF (Extended Backus-Naur Form) 
		
		transform	= {body}
		body		= [comment] ("</" | "<" tag body | literal body);
		comment		= "<!---" {?-"--->"} "--->";
		literal		= ("<" | {?-"#"-"<"} "<" | {"#" expression "#"} "<" ) | ({?-"<"} "<")
				  (* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig was die Tag-Lib vorgibt, 
				     dass Expression geparst werden sollen oder nicht. *)
		tag		= name-space identifier spaces attributes ("/>" | ">" [body "</" identifier spaces ">"]);
				  (* Ob dem Tag ein Body und ein End-Tag folgt ist abhaengig von Definition des body-content in Tag-Lib, gleices gilt fuer appendix *)
		name-space	= < tagLib[].getNameSpaceAndSeperator() >;
			          (* Vergleicht Zeichen mit den Namespacedefinitionen der Tag Libraries. *)
		attributes	= ({spaces attribute} "/>" | {spaces attribute} ">") | attribute-value;
				  (* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig von der Tag Attribute Definition in der Tag Lib. *)
		attribute	= attribute-name  spaces "=" spaces attribute-value;
		attribute-name	= ("expression"|'expression'|expression) | identifier;
			          (* Ruft identifier oder den Expression Transformer auf je nach Attribute Definition in der Tag Lib. *)
		attribute-value	= expression; 
		identifier     	= (letter | "_") {letter | "_"|digit};
		letter			= "a".."z"|"A".."Z";
		digit			= "0".."9";
		expression      = <ExprTransfomer.expression()>; (* Ruft den Expression Transformer auf. *)
		spaces         = {space};
		space          = "\s"|"\t"|"\f"|"\t"|"\n";
		
		{"x"}= 0 bis n mal "x"
		["x"]= 0 bis 1 mal "x"
		("x" | "y")"z" = "xz" oder "yz"
 * </pre>
 * 
 *
 */
public final class CFMLTransformer {

	public static short TAG_LIB_GLOBAL = 0;
	public static short TAG_LIB_PAGE = 1;
	private boolean codeIsland;
	private boolean done;

	public CFMLTransformer() {
		this(false);
	}

	public CFMLTransformer(boolean codeIsland) {
		this.codeIsland = codeIsland;
	}

	/**
	 * Startmethode zum transfomieren einer CFML Datei. <br />
	 * EBNF:<br />
	 * <code>{body}</code>
	 * 
	 * @param config
	 * @param ps CFML File
	 * @param tlibs Tag Library Deskriptoren, nach denen innerhalb der CFML Datei geprueft werden soll.
	 * @param flibs Function Library Deskriptoren, nach denen innerhalb der Expressions der CFML Datei
	 *            geprueft werden soll.
	 * @param returnValue if true the method returns the value of the last expression executed inside
	 *            when you call the method "call"
	 * @return uebersetztes CFXD Dokument Element.
	 * @throws TemplateException
	 * @throws IOException
	 */
	public Page transform(Factory factory, ConfigImpl config, PageSource ps, TagLib[] tlibs, FunctionLib[] flibs, boolean returnValue, boolean ignoreScopes)
			throws TemplateException, IOException {
		Page p;
		SourceCode sc;

		boolean writeLog = config.getExecutionLogEnabled();

		Charset charset = config.getTemplateCharset();
		boolean dotUpper = ps.getDialect() == CFMLEngine.DIALECT_CFML && ((MappingImpl) ps.getMapping()).getDotNotationUpperCase();

		// parse regular
		while (true) {
			try {
				sc = new PageSourceCode(ps, charset, writeLog);

				// script files (cfs)
				if (Constants.isCFMLScriptExtension(ListUtil.last(ps.getRealpath(), '.'))) {
					boolean isCFML = ps.getDialect() == CFMLEngine.DIALECT_CFML;
					TagLibTag scriptTag = CFMLTransformer.getTLT(sc, isCFML ? Constants.CFML_SCRIPT_TAG_NAME : Constants.LUCEE_SCRIPT_TAG_NAME, config.getIdentification());

					sc.setPos(0);
					SourceCode original = sc;

					// try inside a cfscript
					String text = "<" + scriptTag.getFullName() + ">" + original.getText() + "\n</" + scriptTag.getFullName() + ">";
					sc = new PageSourceCode(ps, text, charset, writeLog);
				}

				p = transform(factory, config, sc, tlibs, flibs, ps.getResource().lastModified(), dotUpper, returnValue, ignoreScopes);
				break;
			}
			catch (ProcessingDirectiveException pde) {
				if (pde.getWriteLog() != null) writeLog = pde.getWriteLog().booleanValue();
				if (pde.getDotNotationUpperCase() != null) dotUpper = pde.getDotNotationUpperCase().booleanValue();
				if (!StringUtil.isEmpty(pde.getCharset())) charset = pde.getCharset();
			}
		}

		// could it be a component?
		boolean isCFML = ps.getDialect() == CFMLEngine.DIALECT_CFML;
		boolean isCFMLCompExt = isCFML && Constants.isCFMLComponentExtension(ResourceUtil.getExtension(ps.getResource(), ""));

		boolean possibleUndetectedComponent = false;

		// we don't have a component or interface
		if (p.isPage()) {
			if (isCFML) possibleUndetectedComponent = isCFMLCompExt;
			else if (Constants.isLuceeComponentExtension(ResourceUtil.getExtension(ps.getResource(), ""))) {
				Expression expr;
				Statement stat;
				PrintOut po;
				LitString ls;
				List<Statement> statements = p.getStatements();

				// check the root statements for component
				Iterator<Statement> it = statements.iterator();
				String str;
				while (it.hasNext()) {
					stat = it.next();
					if (stat instanceof PrintOut && (expr = ((PrintOut) stat).getExpr()) instanceof LitString) {
						ls = (LitString) expr;
						str = ls.getString();
						if (str.indexOf(Constants.LUCEE_COMPONENT_TAG_NAME) != -1 || str.indexOf(Constants.LUCEE_INTERFACE_TAG_NAME) != -1
								|| str.indexOf(Constants.CFML_COMPONENT_TAG_NAME) != -1 // cfml name is supported as alias
						) {
							possibleUndetectedComponent = true;
							break;
						}
					}
				}
			}
		}

		if (possibleUndetectedComponent) {
			Page _p;

			TagLibTag scriptTag = CFMLTransformer.getTLT(sc, isCFML ? Constants.CFML_SCRIPT_TAG_NAME : Constants.LUCEE_SCRIPT_TAG_NAME, config.getIdentification());

			sc.setPos(0);
			SourceCode original = sc;

			// try inside a cfscript
			String text = "<" + scriptTag.getFullName() + ">" + original.getText() + "\n</" + scriptTag.getFullName() + ">";
			sc = new PageSourceCode(ps, text, charset, writeLog);

			try {
				while (true) {
					if (sc == null) {
						sc = new PageSourceCode(ps, charset, writeLog);
						text = "<" + scriptTag.getFullName() + ">" + sc.getText() + "\n</" + scriptTag.getFullName() + ">";
						sc = new PageSourceCode(ps, text, charset, writeLog);
					}
					try {
						_p = transform(factory, config, sc, tlibs, flibs, ps.getResource().lastModified(), dotUpper, returnValue, ignoreScopes);
						break;
					}
					catch (ProcessingDirectiveException pde) {
						if (pde.getWriteLog() != null) writeLog = pde.getWriteLog().booleanValue();
						if (pde.getDotNotationUpperCase() != null) dotUpper = pde.getDotNotationUpperCase().booleanValue();
						if (!StringUtil.isEmpty(pde.getCharset())) charset = pde.getCharset();
						sc = null;
					}
				}
			}
			catch (ComponentTemplateException e) {
				throw e.getTemplateException();
			}
			// we only use that result if it is a component now
			if (_p != null && !_p.isPage()) return _p;
		}

		if (isCFMLCompExt && !p.isComponent() && !p.isInterface()) {
			String msg = "template [" + ps.getDisplayPath() + "] must contain a component or an interface.";
			if (sc != null) throw new TemplateException(sc, msg);
			throw new TemplateException(msg);
		}

		return p;
	}

	public static TagLibTag getTLT(SourceCode cfml, String name, Identification id) throws TemplateException {
		TagLib tl;
		try {
			// this is already loaded, oherwise we where not here
			tl = TagLibFactory.loadFromSystem(cfml.getDialect(), id);
			return tl.getTag(name);
		}
		catch (TagLibException e) {
			throw new TemplateException(cfml, e);
		}
	}

	/**
	 * Startmethode zum transfomieren einer CFMLString. <br />
	 * EBNF:<br />
	 * <code>{body}</code>
	 * 
	 * @param config
	 * @param sc CFMLString
	 * @param tlibs Tag Library Deskriptoren, nach denen innerhalb der CFML Datei geprueft werden soll.
	 * @param flibs Function Library Deskriptoren, nach denen innerhalb der Expressions der CFML Datei
	 *            geprueft werden soll.
	 * @param sourceLastModified
	 * @param dotNotationUpperCase
	 * @param returnValue if true the method returns the value of the last expression executed inside
	 *            when you call the method "call"
	 * @return uebersetztes CFXD Dokument Element.
	 * @throws TemplateException
	 */
	public Page transform(Factory factory, ConfigImpl config, SourceCode sc, TagLib[] tlibs, FunctionLib[] flibs, long sourceLastModified, Boolean dotNotationUpperCase,
			boolean returnValue, boolean ignoreScope) throws TemplateException {
		boolean dnuc;
		if (dotNotationUpperCase == null) {
			if (sc instanceof PageSourceCode)
				dnuc = sc.getDialect() == CFMLEngine.DIALECT_CFML && ((MappingImpl) ((PageSourceCode) sc).getPageSource().getMapping()).getDotNotationUpperCase();
			else dnuc = sc.getDialect() == CFMLEngine.DIALECT_CFML && config.getDotNotationUpperCase();
		}
		else dnuc = dotNotationUpperCase;

		TagLib[][] _tlibs = new TagLib[][] { null, new TagLib[0] };
		_tlibs[TAG_LIB_GLOBAL] = tlibs;
		// reset page tlds
		if (_tlibs[TAG_LIB_PAGE].length > 0) {
			_tlibs[TAG_LIB_PAGE] = new TagLib[0];
		}

		Page page = new Page(factory, config, sc, null, ConfigWebUtil.getEngine(config).getInfo().getFullVersionInfo(), sourceLastModified, sc.getWriteLog(),
				sc.getDialect() == CFMLEngine.DIALECT_LUCEE || config.getSuppressWSBeforeArg(), config.getDefaultFunctionOutput(), returnValue, ignoreScope);

		TransfomerSettings settings = new TransfomerSettings(dnuc, sc.getDialect() == CFMLEngine.DIALECT_CFML && factory.getConfig().getHandleUnQuotedAttrValueAsString(),
				ignoreScope);
		Data data = new Data(factory, page, sc, new EvaluatorPool(), settings, _tlibs, flibs, config.getCoreTagLib(sc.getDialect()).getScriptTags(), false);
		transform(data, page);
		return page;

	}

	public void transform(Data data, Body parent) throws TemplateException {
		try {
			do {
				body(data, parent);

				if (done || data.srcCode.isAfterLast()) break;
				if (data.srcCode.forwardIfCurrent("</")) {
					int pos = data.srcCode.getPos();
					TagLib tagLib = nameSpace(data);
					if (tagLib == null) {
						parent.addPrintOut(data.factory, "</", null, null);
					}
					else {
						String name = identifier(data.srcCode, true, true);
						if (tagLib.getIgnoreUnknowTags()) {
							TagLibTag tlt = tagLib.getTag(name);
							if (tlt == null) {
								data.srcCode.setPos(pos);
								parent.addPrintOut(data.factory, "</", null, null);
							}
						}
						else throw new TemplateException(data.srcCode, "no matching start tag for end tag [" + tagLib.getNameSpaceAndSeparator() + name + "]");

					}
				}
				else throw new TemplateException(data.srcCode, "Error while transforming CFML File");
			}
			while (true);

			// call-back of evaluators
			int pos = data.srcCode.getPos();
			data.ep.run();
			data.srcCode.setPos(pos);
			return;
		}
		catch (TemplateException e) {
			data.ep.clear();
			throw e;
		}
	}

	/**
	 * Liest den Body eines Tag ein. Kommentare, Tags und Literale inkl. Expressions. <br />
	 * EBNF:<br />
	 * <code>[comment] ("</" | "<" tag body | literal body);</code>
	 * 
	 * @param body CFXD Body Element dem der Inhalt zugeteilt werden soll.
	 * @param parseExpression Definiert ob Expressions innerhalb von Literalen uebersetzt werden sollen
	 *            oder nicht.
	 * @param transformer Expression Transfomer zum uebersetzten von Expression.
	 * @throws TemplateException
	 */
	public void body(Data data, Body body) throws TemplateException {
		boolean parseLiteral = true;

		// Comment
		comment(data.srcCode, false);
		// Tag
		// is Tag Beginning
		if (data.srcCode.isCurrent('<')) {
			// return if end tag and inside tag
			if (data.srcCode.isNext('/')) {
				// lucee.print.ln("early return");
				return;
			}
			parseLiteral = !tag(data, body);
		}
		// no Tag
		if (parseLiteral) {
			literal(data, body);
		}
		// not at the end

		if (!done && data.srcCode.isValidIndex()) body(data, body);
	}

	/**
	 * Liest einen Kommentar ein, Kommentare werden nicht in die CFXD uebertragen sondern verworfen.
	 * Komentare koennen auch Kommentare enthalten. <br />
	 * EBNF:<br />
	 * <code>"<!---" {?-"--->"} "--->";</code>
	 * 
	 * @throws TemplateException
	 */

	private static void comment(SourceCode cfml, boolean removeSpace) throws TemplateException {
		if (!removeSpace) {
			comment(cfml);
		}
		else {
			cfml.removeSpace();
			if (comment(cfml)) cfml.removeSpace();
		}

	}

	public static boolean comment(SourceCode cfml) throws TemplateException {
		if (!cfml.forwardIfCurrent("<!---")) return false;

		int start = cfml.getPos();
		short counter = 1;
		while (true) {
			if (cfml.isAfterLast()) {
				cfml.setPos(start);
				throw new TemplateException(cfml, "no end comment found");
			}
			else if (cfml.forwardIfCurrent("<!---")) {
				counter++;
			}
			else if (cfml.forwardIfCurrent("--->")) {
				if (--counter == 0) {
					comment(cfml);
					return true;
				}
			}
			else {
				cfml.next();
			}
		}
	}

	/**
	 * Liest Literale Zeichenketten ein die sich innerhalb und auserhalb von tgas befinden, beim
	 * Einlesen wird unterschieden ob Expression geparsst werden muessen oder nicht, dies ist abhaengig,
	 * von der Definition des Tag in dem man sich allenfalls befindet, innerhalb der TLD.
	 * 
	 * @param parent uebergeordnetes Element.
	 * @param parseExpression Definiert on Expressions geparset werden sollen oder nicht.
	 * @param transformer Expression Transfomer zum uebersetzen der Expressions innerhalb des Literals.
	 * @throws TemplateException
	 * 
	 *             <br />
	 *             EBNF:<br />
	 *             <code>("<" | {?-"#"-"<"} "<" | {"#" expression "#"} "<" ) | ({?-"<"} "<")
			(* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig ob die Tag-Lib vorgibt, 
			 dass Expression geparst werden sollen oder nicht. *)</code>
	 */
	private void literal(Data data, Body parent) throws TemplateException {
		while (codeIsland && data.srcCode.isCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
			int start = data.srcCode.getPos();

			if (data.srcCode.forwardIfCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
				parent.addPrintOut(data.factory, AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR, data.srcCode.getPosition(start), data.srcCode.getPosition());
			}
			else {
				done = true;
				return;
			}
		}
		boolean _end = false;
		// with expression
		if (data.parseExpression) {
			if (data.srcCode.isAfterLast()) return;
			// data.cfml.getCurrent()
			StringBuilder text = new StringBuilder();
			int count = 0;
			while (data.srcCode.isValidIndex()) {
				count++;
				// #
				if (data.srcCode.isCurrent('#')) {
					data.srcCode.next();
					if (data.srcCode.isCurrent('#')) {
						text.append('#');
					}
					else {
						if (text.length() > 0) {
							Position end = data.srcCode.getPosition();
							Position start = data.srcCode.getPosition(end.pos - text.length());

							parent.addPrintOut(data.factory, text.toString(), start, end);
							start = end;
							text = new StringBuilder();
						}
						Position end = data.srcCode.getPosition();
						Position start = data.srcCode.getPosition(end.pos - text.length());

						PrintOut po;
						parent.addStatement(po = new PrintOut(data.transformer.transform(data), start, end));
						po.setEnd(data.srcCode.getPosition());

						if (!data.srcCode.isCurrent('#')) throw new TemplateException(data.srcCode, "missing terminating [#] for expression");
					}
				}
				else if (data.srcCode.isCurrent('<') && count > 1) {
					break;
				}
				else if (codeIsland && data.srcCode.isCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) && count > 1) {
					// int start = data.srcCode.getPos();
					if (data.srcCode.forwardIfCurrent(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR)) {
						text.append(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR);
						// parent.addPrintOut(data.factory,AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR,
						// data.srcCode.getPosition(start),data.srcCode.getPosition());
						data.srcCode.previous();
					}
					else {
						_end = true;
						break;
					}
				}
				else text.append(data.srcCode.getCurrent());
				data.srcCode.next();
			}
			if (text.length() > 0) {
				Position end = data.srcCode.getPosition();
				Position start = data.srcCode.getPosition(end.pos - text.length());

				parent.addPrintOut(data.factory, text.toString(), start, end);
				if (_end) {
					done = true;
					return;
					// throw new CodeIslandEnd();
				}
			}
		}
		// no expression
		else {
			int start = data.srcCode.getPos();
			data.srcCode.next();

			int end = data.srcCode.indexOfNext('<');
			int endIsland = codeIsland ? data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) : -1;
			int endIslandEsc = codeIsland ? data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR) : -1;

			// next is escaped
			while (endIsland != -1 && endIslandEsc != -1 && endIsland == endIslandEsc && (end == -1 || endIsland < end)) {
				String txt = data.srcCode.substring(start, endIsland - start) + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR;
				int s = data.srcCode.getPos();
				data.srcCode.setPos(endIsland + (AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR.length() * 2));
				parent.addPrintOut(data.factory, txt, data.srcCode.getPosition(s), data.srcCode.getPosition());

				// now we need to check again
				start = data.srcCode.getPos();
				// data.srcCode.next();
				end = data.srcCode.indexOfNext('<');
				endIsland = data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR);
				endIslandEsc = data.srcCode.indexOfNext(AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR + AbstrCFMLScriptTransformer.TAG_ISLAND_INDICATOR);
			}

			String text;
			if (endIsland != -1 && (end == -1 || endIsland < end)) {
				text = data.srcCode.substring(start, endIsland - start);
				data.srcCode.setPos(endIsland);
				_end = true;
			}
			else if (end != -1) {
				text = data.srcCode.substring(start, end - start);
				data.srcCode.setPos(end);
			}
			else {
				text = data.srcCode.substring(start);
				data.srcCode.setPos(data.srcCode.length());
			}
			Position e = data.srcCode.getPosition();
			Position s = data.srcCode.getPosition(start);

			parent.addPrintOut(data.factory, text, s, e);
			if (_end) {
				done = true;
				return;
				// throw new CodeIslandEnd();
			}
		}
	}

	private Object p(Data data) {
		try {
			return data.srcCode.getPosition();
		}
		catch (Exception e) {
			return data.srcCode.getPos();
		}
	}

	/**
	 * Liest einen Tag ein, prueft hierbei ob das Tag innerhalb einer der geladenen Tag-Lib existiert,
	 * ansonsten wird ein Tag einfach als literal-string aufgenommen. <br />
	 * EBNF:<br />
	 * <code>name-space identifier spaces attributes ("/>" | ">" [body "</" identifier spaces ">"]);(* Ob dem Tag ein Body und ein End-Tag folgt ist abhaengig von Definition des body-content in Tag-Lib, gleices gilt fuer appendix *)</code>
	 * 
	 * @param parent uebergeornetes Tag
	 * @param parseExpression sollen Expresson innerhalb des Body geparste werden oder nicht.
	 * @return Gibt zurueck ob es sich um ein Tag as einer Tag-Lib handelte oder nicht.
	 * @throws TemplateException
	 */
	private boolean tag(Data data, Body parent) throws TemplateException {
		boolean startingParseExpression = data.parseExpression;
		ExprTransformer startingTransformer = data.transformer;
		try {
			boolean hasBody = false;

			Position line = data.srcCode.getPosition();
			// int column=data.cfml.getColumn();
			int start = data.srcCode.getPos();
			data.srcCode.next();

			// read in namespace of tag
			TagLib tagLib = nameSpace(data);

			// return if no matching tag lib
			if (tagLib == null) {
				data.srcCode.previous();
				return false;
			}

			// Get matching tag from tag lib
			String strNameNormal = identifier(data.srcCode, false, true);
			if (strNameNormal == null) {
				data.srcCode.setPos((data.srcCode.getPos() - tagLib.getNameSpaceAndSeparator().length()) - 1);
				return false;
			}

			String strName = strNameNormal.toLowerCase();
			String appendix = null;
			TagLibTag tagLibTag = tagLib.getTag(strName);

			// get taglib
			if (tagLibTag == null) {
				tagLibTag = tagLib.getAppendixTag(strName);
				if (tagLibTag == null) {
					if (tagLib.getIgnoreUnknowTags()) {
						data.srcCode.setPos(start);
						return false;
					}
					throw new TemplateException(data.srcCode, "undefined tag [" + tagLib.getNameSpaceAndSeparator() + strName + "]");
				}
				appendix = StringUtil.removeStartingIgnoreCase(strNameNormal, tagLibTag.getName());
			}

			if (tagLibTag.getStatus() == TagLib.STATUS_UNIMPLEMENTED) {
				throw new TemplateException(data.srcCode, "the tag [" + tagLibTag.getFullName() + "] is not implemented yet.");
			}

			// CFXD Element
			Tag tag;
			try {
				tag = tagLibTag.getTag(data.factory, line, data.srcCode.getPosition());
			}
			catch (Exception e) {
				throw new TemplateException(data.srcCode, e);
			}
			parent.addStatement(tag);

			// get tag from tag library
			if (appendix != null) {
				tag.setAppendix(appendix);
				tag.setFullname(tagLibTag.getFullName().concat(appendix));
			}
			else {
				tag.setFullname(tagLibTag.getFullName());
			}
			// if(tag.getFullname().equalsIgnoreCase("cfcomponent"))data.page.setIsComponent(true); // MUST to
			// hardcoded, to better
			// else if(tag.getFullname().equalsIgnoreCase("cfinterface"))data.page.setIsInterface(true); // MUST
			// to hardcoded, to better

			tag.setTagLibTag(tagLibTag);
			comment(data.srcCode, true);

			// Tag Translator Evaluator
			if (tagLibTag.hasTTE()) {
				data.ep.add(tagLibTag, tag, data.flibs, data.srcCode);
			}

			// get Attributes
			attributes(data, tagLibTag, tag);

			if (tagLibTag.hasAttributeEvaluator()) {
				try {
					tagLibTag = tagLibTag.getAttributeEvaluator().evaluate(tagLibTag, tag);
				}
				catch (AttributeEvaluatorException e) {

					throw new TemplateException(data.srcCode, e);
				}
			}

			// End of begin Tag
			// TODO muss erlaubt sein
			if (data.srcCode.forwardIfCurrent('>')) {
				hasBody = tagLibTag.getHasBody();
			}
			else if (data.srcCode.forwardIfCurrent('/', '>')) {
				if (tagLibTag.getHasBody()) tag.setBody(new BodyBase(data.factory));
			}
			else {
				throw createTemplateException(data.srcCode, "tag [" + tagLibTag.getFullName() + "] is not closed", tagLibTag);
			}

			// Body
			if (hasBody) {

				// get Body
				if (tagLibTag.isTagDependent()) {
					// get TagDependentBodyTransformer
					TagDependentBodyTransformer tdbt = null;
					try {
						tdbt = tagLibTag.getBodyTransformer();
					}
					catch (TagLibException e) {
						throw new TemplateException(data.srcCode, e);
					}
					if (tdbt == null) throw createTemplateException(data.srcCode, "Tag dependent body Transformer is invalid for Tag [" + tagLibTag.getFullName() + "]", tagLibTag);

					// tag.setBody(tdbt.transform(data.factory,data.root,data.ep,data.tlibs,data.flibs,
					// tagLibTag.getFullName(),data.scriptTags,data.srcCode,data.settings));

					tag.setBody(tdbt.transform(data, tagLibTag.getFullName()));

					// get TagLib of end Tag
					if (!data.srcCode.forwardIfCurrent("</")) {
						// MUST this is a patch, do a more proper implementation
						TemplateException te = new TemplateException(data.srcCode, "invalid construct");
						if (tdbt instanceof CFMLScriptTransformer && ASMUtil.containsComponent(tag.getBody())) {
							throw new CFMLScriptTransformer.ComponentTemplateException(te);
						}
						throw te;
					}

					TagLib tagLibEnd = nameSpace(data);
					// same NameSpace
					if (!(tagLibEnd != null && tagLibEnd.getNameSpaceAndSeparator().equals(tagLib.getNameSpaceAndSeparator())))
						throw new TemplateException(data.srcCode, "invalid construct");
					// get end Tag
					String strNameEnd = identifier(data.srcCode, true, true).toLowerCase();

					// not the same name Tag
					if (!strName.equals(strNameEnd)) {
						data.srcCode.setPos(start);
						throw new TemplateException(data.srcCode, "Start and End Tag has not the same Name [" + tagLib.getNameSpaceAndSeparator() + strName + "-"
								+ tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "]");
					}
					data.srcCode.removeSpace();
					if (!data.srcCode.forwardIfCurrent('>'))
						throw new TemplateException(data.srcCode, "End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "] not closed");
				}
				else {
					// get body of Tag
					BodyBase body = new BodyBase(data.factory);
					body.setParent(tag);
					// tag.setBody(body);
					// parseExpression=(tagLibTag.getParseBody())?true:parseExpression;
					if (tagLibTag.getParseBody()) data.parseExpression = true;

					while (true) {

						// Load Expession Transformer from TagLib
						data.transformer = startingTransformer;
						if (data.parseExpression) {
							try {
								data.transformer = tagLibTag.getTagLib().getExprTransfomer();
							}
							catch (TagLibException e) {
								throw new TemplateException(data.srcCode, e);
							}
						}

						// call body

						body(data, body);

						// no End Tag
						if (done || data.srcCode.isAfterLast()) {

							if (tagLibTag.isBodyReq()) {
								data.srcCode.setPos(start);
								throw createTemplateException(data.srcCode, "No matching end tag found for tag [" + tagLibTag.getFullName() + "]", tagLibTag);
							}
							body.moveStatmentsTo(parent);
							return executeEvaluator(data, tagLibTag, tag);
						}

						// Invalid Construct
						int posBeforeEndTag = data.srcCode.getPos();
						if (!data.srcCode.forwardIfCurrent('<', '/'))
							throw createTemplateException(data.srcCode, "Missing end tag for [" + tagLibTag.getFullName() + "]", tagLibTag);

						// get TagLib of end Tag
						int _start = data.srcCode.getPos();
						TagLib tagLibEnd = nameSpace(data);

						// same NameSpace
						if (tagLibEnd != null) {
							String strNameEnd = "";
							// lucee.print.ln(data.cfml.getLine()+" - "+data.cfml.getColumn()+" -
							// "+tagLibEnd.getNameSpaceAndSeperator()+".equals("+tagLib.getNameSpaceAndSeperator()+")");
							if (tagLibEnd.getNameSpaceAndSeparator().equals(tagLib.getNameSpaceAndSeparator())) {

								// get end Tag
								strNameEnd = identifier(data.srcCode, true, true).toLowerCase();
								// not the same name Tag

								// new part
								data.srcCode.removeSpace();
								if (strName.equals(strNameEnd)) {
									if (!data.srcCode.forwardIfCurrent('>'))
										throw new TemplateException(data.srcCode, "End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "] not closed");
									break;
								}

							}
							// new part
							if (tagLibTag.isBodyReq()) {
								TagLibTag endTag = tagLibEnd.getTag(strNameEnd);
								if (endTag != null && !endTag.getHasBody()) throw new TemplateException(data.srcCode,
										"End Tag [" + tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "] is not allowed, for this tag only a Start Tag is allowed");
								data.srcCode.setPos(start);
								if (tagLibEnd.getIgnoreUnknowTags() && (tagLibEnd.getTag(strNameEnd)) == null) {
									data.srcCode.setPos(_start);
								}
								else throw new TemplateException(data.srcCode, "Start and End Tag has not the same Name [" + tagLib.getNameSpaceAndSeparator() + strName + "-"
										+ tagLibEnd.getNameSpaceAndSeparator() + strNameEnd + "]");
							}
							else {
								body.moveStatmentsTo(parent);
								data.srcCode.setPos(posBeforeEndTag);
								return executeEvaluator(data, tagLibTag, tag);
							}
							/// new part
						}
						body.addPrintOut(data.factory, "</", null, null);

					}
					tag.setBody(body);

				}
			}
			if (tag instanceof StatementBase) ((StatementBase) tag).setEnd(data.srcCode.getPosition());
			// Tag Translator Evaluator

			return executeEvaluator(data, tagLibTag, tag);
		}
		finally {
			data.parseExpression = startingParseExpression;
			data.transformer = startingTransformer;
		}
	}

	private boolean executeEvaluator(Data data, TagLibTag tagLibTag, Tag tag) throws TemplateException {
		if (tagLibTag.hasTTE()) {
			try {
				TagLib lib = tagLibTag.getEvaluator().execute(data.config, tag, tagLibTag, data.flibs, data);
				if (lib != null) {
					// set
					for (int i = 0; i < data.tlibs[TAG_LIB_PAGE].length; i++) {
						if (data.tlibs[TAG_LIB_PAGE][i].getNameSpaceAndSeparator().equalsIgnoreCase(lib.getNameSpaceAndSeparator())) {
							boolean extIsCustom = data.tlibs[TAG_LIB_PAGE][i] instanceof CustomTagLib;
							boolean newIsCustom = lib instanceof CustomTagLib;
							// TagLib + CustomTagLib (visa/versa)
							if (extIsCustom) {
								((CustomTagLib) data.tlibs[TAG_LIB_PAGE][i]).append(lib);
								return true;
							}
							else if (newIsCustom) {
								((CustomTagLib) lib).append(data.tlibs[TAG_LIB_PAGE][i]);
								data.tlibs[TAG_LIB_PAGE][i] = lib;
								return true;
							}
						}
					}
					// TODO make sure longer namespace ar checked firts to support subsets, same for core libs
					// insert
					TagLib[] newTlibs = new TagLib[data.tlibs[TAG_LIB_PAGE].length + 1];
					for (int i = 0; i < data.tlibs[TAG_LIB_PAGE].length; i++) {
						newTlibs[i] = data.tlibs[TAG_LIB_PAGE][i];
					}
					newTlibs[data.tlibs[TAG_LIB_PAGE].length] = lib;
					data.tlibs[TAG_LIB_PAGE] = newTlibs;
				}
			}
			catch (EvaluatorException e) {
				throw new TemplateException(data.srcCode, e);
			}
		}
		return true;
	}

	/**
	 * Vergleicht folgende Zeichen mit den Namespacedefinitionen der Tag Libraries, gibt eine Tag-Lib
	 * zurueck falls eine passt, ansonsten null. <br />
	 * EBNF:<br />
	 * <code>< tagLib[].getNameSpaceAndSeperator() >(* Vergleicht Zeichen mit den Namespacedefinitionen der Tag Libraries. *) </code>
	 * 
	 * @return TagLib Passende Tag Lirary oder null.
	 */
	public static TagLib nameSpace(Data data) {
		boolean hasTag = false;
		int start = data.srcCode.getPos();
		TagLib tagLib = null;

		// loop over NameSpaces
		for (int i = 1; i >= 0; i--) {
			for (int ii = 0; ii < data.tlibs[i].length; ii++) {
				tagLib = data.tlibs[i][ii];
				char[] c = tagLib.getNameSpaceAndSeperatorAsCharArray();
				// Loop over char of NameSpace and Sepearator
				hasTag = true;
				for (int y = 0; y < c.length; y++) {
					if (!(data.srcCode.isValidIndex() && c[y] == data.srcCode.getCurrentLower())) {
						// hasTag=true;
						// } else {
						hasTag = false;
						data.srcCode.setPos(start);
						break;
					}
					data.srcCode.next();
				}
				if (hasTag) return tagLib;// break;
			}
			// if(hasTag) return tagLib;
		}
		return null;
	}

	/**
	 * Liest die Attribute eines Tags ein, dies Abhaengig von der Definition innerhalb der Tag-Lib.
	 * Hierbei unterscheiden wir vier verschiedene Arten von Attributen:<br>
	 * <ul>
	 * <li>FIX: Definierte Attribute Fix, fuer jedes Attribut ist definiert ob es required ist oder
	 * nicht (gleich wie JSP).</li>
	 * <li>DYNAMIC: Die Attribute des Tag sind frei, keine Namen sind vorgegeben. Es kann aber definiert
	 * sein wieviele Attribute maximal und minimal verwendetet werden duerfen.</li>
	 * <li>FULLDYNAMIC: Gleich wie DYNAMIC, jedoch kann der Name des Attribut auch ein dynamischer Wert
	 * sein (wie bei cfset).</li>
	 * <li>NONAME: Ein Tag welches nur ein Attribut besitzt ohne Name, sondern einfach nur mit einem
	 * Attribut Wert</li>
	 * </ul>
	 * <br />
	 * EBNF:<br />
	 * <code>({spaces attribute} "/>" | {spaces attribute} ">") | attribute-value;(* Welcher Teil der "oder" Bedingung ausgefuehrt wird, ist abhaengig von der Tag Attribute Definition in der Tag Lib. *)</code>
	 * 
	 * @param tag
	 * @param parent
	 * @throws TemplateException
	 */
	public static void attributes(Data data, TagLibTag tag, Tag parent) throws TemplateException {
		int type = tag.getAttributeType();
		int start = data.srcCode.getPos();
		// Tag with attribute names
		if (type != TagLibTag.ATTRIBUTE_TYPE_NONAME) {
			try {
				int min = tag.getMin();
				int max = tag.getMax();
				int count = 0;
				ArrayList<String> args = new ArrayList<String>();
				RefBoolean allowDefaultValue = new RefBooleanImpl(tag.getDefaultAttribute() != null);
				while (data.srcCode.isValidIndex()) {
					data.srcCode.removeSpace();
					// if no more attributes break
					if (data.srcCode.isCurrent('/') || data.srcCode.isCurrent('>')) break;

					parent.addAttribute(attribute(data, tag, args, allowDefaultValue));
					count++;
				}

				// set default values
				if (tag.hasDefaultValue()) {
					Map<String, TagLibTagAttr> hash = tag.getAttributes();
					Iterator<Entry<String, TagLibTagAttr>> it = hash.entrySet().iterator();
					Entry<String, TagLibTagAttr> e;
					TagLibTagAttr att;
					while (it.hasNext()) {
						e = it.next();
						att = e.getValue();
						if (!parent.containsAttribute(att.getName()) && att.hasDefaultValue()) {

							Attribute attr = new Attribute(tag.getAttributeType() == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC, att.getName(),
									data.factory.toExpression(data.factory.createLitString(Caster.toString(att.getDefaultValue(), null)), att.getType()), att.getType());
							attr.setDefaultAttribute(true);
							parent.addAttribute(attr);
						}
					}
				}

				boolean hasAttributeCollection = args.contains("attributecollection");

				// to less attributes
				if (!hasAttributeCollection && min > count)
					throw createTemplateException(data.srcCode, "the tag [" + tag.getFullName() + "] must have at least [" + min + "] attributes", tag);

				// too much attributes
				if (!hasAttributeCollection && max > 0 && max < count)
					throw createTemplateException(data.srcCode, "the tag [" + tag.getFullName() + "] can have a maximum of [" + max + "] attributes", tag);

				// not defined attributes
				if (type == TagLibTag.ATTRIBUTE_TYPE_FIXED || type == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
					// Map<String, TagLibTagAttr> hash = tag.getAttributes();
					Iterator<TagLibTagAttr> it = tag.getAttributes().values().iterator();

					while (it.hasNext()) {

						TagLibTagAttr att = it.next();
						if (att.isRequired() && !contains(args, att) && att.getDefaultValue() == null) {
							if (!hasAttributeCollection)
								throw createTemplateException(data.srcCode, "attribute [" + att.getName() + "] is required for tag [" + tag.getFullName() + "]", tag);
							parent.addMissingAttribute(att);
						}
					}
				}
			}
			catch (TemplateException te) {
				data.srcCode.setPos(start);
				// if the tag supports a non name attribute try this
				TagLibTagAttr sa = tag.getSingleAttr();
				if (sa != null) attrNoName(parent, tag, data, sa);
				else throw te;
			}
		}
		// tag without attributes name
		else {
			attrNoName(parent, tag, data, null);
		}
	}

	private static boolean contains(ArrayList<String> names, TagLibTagAttr attr) {

		Iterator<String> it = names.iterator();
		String name;
		String[] alias;
		while (it.hasNext()) {
			name = it.next();

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

	private static void attrNoName(Tag parent, TagLibTag tag, Data data, TagLibTagAttr attr) throws TemplateException {
		if (attr == null) attr = tag.getFirstAttribute();
		String strName = "noname";
		String strType = "any";
		boolean pe = true;
		if (attr != null) {
			strName = attr.getName();
			strType = attr.getType();
			pe = attr.getRtexpr();
		}
		// LitString.toExprString("",-1);
		Attribute att = new Attribute(false, strName, attributeValue(data, tag, strType, pe, true, data.factory.createNull()), strType);
		parent.addAttribute(att);
	}

	/**
	 * Liest ein einzelnes Atribut eines tag ein (nicht NONAME). <br />
	 * EBNF:<br />
	 * <code>attribute-name  spaces "=" spaces attribute-value;</code>
	 * 
	 * @param tag Definition des Tag das dieses Attribut enthaelt.
	 * @param args Container zum Speichern einzelner Attribute Namen zum nachtraeglichen Prufen gegen
	 *            die Tag-Lib.
	 * @return Element Attribute Element.
	 * @throws TemplateException
	 */
	private static Attribute attribute(Data data, TagLibTag tag, ArrayList<String> args, RefBoolean allowDefaultValue) throws TemplateException {
		Expression value = null;

		// Name
		StringBuffer sbType = new StringBuffer();
		RefBoolean dynamic = new RefBooleanImpl(false);
		boolean isDefaultValue = false;
		boolean[] parseExpression = new boolean[2];
		parseExpression[0] = true;
		parseExpression[1] = false;
		String name = attributeName(data.srcCode, dynamic, args, tag, sbType, parseExpression, allowDefaultValue.toBooleanValue());

		// mixed in a noname attribute
		if (StringUtil.isEmpty(name)) {
			allowDefaultValue.setValue(false);
			TagLibTagAttr attr = tag.getDefaultAttribute();
			if (attr == null) throw new TemplateException(data.srcCode, "Invalid Identifier.");
			name = attr.getName();
			sbType.append(attr.getType());
			isDefaultValue = true;
		}

		comment(data.srcCode, true);

		if (isDefaultValue || data.srcCode.forwardIfCurrent('=')) {
			comment(data.srcCode, true);
			// Value
			value = attributeValue(data, tag, sbType.toString(), parseExpression[0], false, data.factory.createLitString(""));
		}
		// default value boolean true
		else {
			TagLibTagAttr attr = tag.getAttribute(name);
			if (attr != null) value = attr.getUndefinedValue(data.factory);
			else value = tag.getAttributeUndefinedValue(data.factory);

			if (sbType.toString().length() > 0) {
				value = data.factory.toExpression(value, sbType.toString());
			}
		}
		comment(data.srcCode, true);

		return new Attribute(dynamic.toBooleanValue(), name, value, sbType.toString());
	}

	/**
	 * Liest den Namen eines Attribut ein, je nach Attribut-Definition innerhalb der Tag-Lib, wird der
	 * Name ueber den identifier oder den Expression Transformer eingelesen.
	 * <ul>
	 * <li>FIX und DYNAMIC --> identifier</li>
	 * <li>FULLDYNAMIC --> Expression Transformer</li>
	 * </ul>
	 * <br />
	 * EBNF:<br />
	 * <code>("expression"|'expression'|expression) | identifier;(* Ruft identifier oder den Expression Transformer auf je nach Attribute Definition in der Tag Lib. *)</code>
	 * 
	 * @param dynamic
	 * @param args Container zum Speichern einzelner Attribute Namen zum nachtraeglichen Prufen gegen
	 *            die Tag-Lib.
	 * @param tag Aktuelles tag aus der Tag-Lib
	 * @param sbType Die Methode speichert innerhalb von sbType den Typ des Tags, zur Interpretation in
	 *            der attribute Methode.
	 * @param parseExpression Soll der Wert des Attributes geparst werden
	 * @return Attribute Name
	 * @throws TemplateException
	 */
	private static String attributeName(SourceCode cfml, RefBoolean dynamic, ArrayList<String> args, TagLibTag tag, StringBuffer sbType, boolean[] parseExpression,
			boolean allowDefaultValue) throws TemplateException {

		String _id = identifier(cfml, !allowDefaultValue, true);
		if (StringUtil.isEmpty(_id)) {
			return null;
		}

		int typeDef = tag.getAttributeType();
		String id = StringUtil.toLowerCase(_id);
		if (args.contains(id)) throw createTemplateException(cfml, "you can't use the same tag attribute [" + id + "] twice", tag);
		args.add(id);

		if ("attributecollection".equals(id)) {
			dynamic.setValue(tag.getAttribute(id, true) == null);
			sbType.append("struct");
			parseExpression[0] = true;
			parseExpression[1] = true;
		}
		else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED || typeDef == TagLibTag.ATTRIBUTE_TYPE_MIXED) {
			TagLibTagAttr attr = tag.getAttribute(id, true);
			if (attr == null) {
				if (typeDef == TagLibTag.ATTRIBUTE_TYPE_FIXED) {
					String names = tag.getAttributeNames();
					if (StringUtil.isEmpty(names)) throw createTemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]", tag);

					try {
						names = ListUtil.sort(names, "textnocase", null, null);
					}
					catch (Throwable t) {
						ExceptionUtil.rethrowIfNecessary(t);
					}
					throw createTemplateException(cfml, "Attribute [" + id + "] is not allowed for tag [" + tag.getFullName() + "]", "valid attribute names are [" + names + "]",
							tag);
				}
				dynamic.setValue(true);
			}
			else {
				id = attr.getName();
				sbType.append(attr.getType());
				parseExpression[0] = attr.getRtexpr();
			}
		}
		else if (typeDef == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) {
			dynamic.setValue(true);
		}
		return id;
	}

	/**
	 * Liest den Wert eines Attribut, mithilfe des innerhalb der Tag-Lib definierten Expression
	 * Transformer, ein. <br />
	 * EBNF:<br />
	 * <code>expression;</code>
	 * 
	 * @param tag
	 * @param type
	 * @param parseExpression
	 * @param isNonName
	 * @return Element Eingelesener uebersetzer Wert des Attributes.
	 * @throws TemplateException
	 */
	public static Expression attributeValue(Data data, TagLibTag tag, String type, boolean parseExpression, boolean isNonName, Expression noExpression) throws TemplateException {
		Expression expr;
		try {
			ExprTransformer transfomer = null;
			if (parseExpression) {
				transfomer = tag.getTagLib().getExprTransfomer();
			}
			else {
				if (data.getSimpleExprTransformer() == null) {
					data.setSimpleExprTransformer(new SimpleExprTransformer('#'));
					// set.setSpecialChar();
				}
				transfomer = data.getSimpleExprTransformer();
			}
			if (isNonName) {
				int pos = data.srcCode.getPos();
				try {
					expr = transfomer.transform(data);
				}
				catch (TemplateException ete) {
					if (data.srcCode.getPos() == pos) expr = noExpression;
					else throw ete;
				}
			}
			else {
				if (data.settings.handleUnQuotedAttrValueAsString) {
					boolean alt = data.allowLowerThan;
					data.allowLowerThan = true;
					try {
						expr = transfomer.transformAsString(data);
					}
					finally {
						data.allowLowerThan = alt;
					}

				}
				else expr = transfomer.transform(data);
			}
			if (type.length() > 0) {
				expr = data.factory.toExpression(expr, type);
			}
		}
		catch (TagLibException e) {
			throw new TemplateException(data.srcCode, e);
		}
		return expr;
	}

	/**
	 * Liest einen Identifier ein und gibt diesen als String zurueck. <br />
	 * EBNF:<br />
	 * <code>(letter | "_") {letter | "_"|digit};</code>
	 * 
	 * @param throwError throw error or return null if name is invalid
	 * @return Identifier String.
	 * @throws TemplateException
	 */
	public static String identifier(SourceCode cfml, boolean throwError, boolean allowColon) throws TemplateException {
		int start = cfml.getPos();

		if (!cfml.isCurrentBetween('a', 'z') && !cfml.isCurrent('_')) {
			if (throwError) throw new TemplateException(cfml, "Invalid Identifier, the following character cannot be part of an identifier [" + cfml.getCurrent() + "]");
			return null;
		}
		do {
			cfml.next();
			if (!(cfml.isCurrentBetween('a', 'z') || cfml.isCurrentBetween('0', '9') || cfml.isCurrent('_') || (allowColon && cfml.isCurrent(':')) || cfml.isCurrent('-'))) {
				break;
			}
		}
		while (cfml.isValidIndex());
		return cfml.substring(start, cfml.getPos() - start);
	}

	public static TemplateException createTemplateException(SourceCode cfml, String msg, String detail, TagLibTag tag) {
		TemplateException te = new TemplateException(cfml, msg, detail);
		setAddional(te, tag);
		return te;
	}

	public static TemplateException createTemplateException(SourceCode cfml, String msg, TagLibTag tag) {
		TemplateException te = new TemplateException(cfml, msg);
		setAddional(te, tag);
		return te;
	}

	public static TemplateException setAddional(TemplateException te, TagLibTag tlt) {
		setAddional((PageExceptionImpl) te, tlt);
		return te;
	}

	public static ApplicationException setAddional(ApplicationException ae, TagLibTag tlt) {
		setAddional((PageExceptionImpl) ae, tlt);
		return ae;
	}

	private static void setAddional(PageExceptionImpl pe, TagLibTag tlt) {
		Map<String, TagLibTagAttr> attrs = tlt.getAttributes();
		Iterator<Entry<String, TagLibTagAttr>> it = attrs.entrySet().iterator();
		Entry<String, TagLibTagAttr> entry;
		TagLibTagAttr attr;

		// Pattern
		StringBuilder pattern = new StringBuilder("<");
		pattern.append((tlt.getFullName()));
		StringBuilder req = new StringBuilder();
		StringBuilder opt = new StringBuilder();
		StringBuilder tmp;

		pattern.append(" ");
		int c = 0;
		while (it.hasNext()) {
			entry = it.next();
			attr = entry.getValue();
			tmp = attr.isRequired() ? req : opt;

			tmp.append(" ");
			if (!attr.isRequired()) tmp.append("[");
			if (c++ > 0) pattern.append(" ");
			tmp.append(attr.getName());
			tmp.append("=\"");
			tmp.append(attr.getType());
			tmp.append("\"");
			if (!attr.isRequired()) tmp.append("]");
		}

		if (req.length() > 0) pattern.append(req);
		if (opt.length() > 0) pattern.append(opt);

		if (tlt.getAttributeType() == TagLibTag.ATTRIBUTE_TYPE_MIXED || tlt.getAttributeType() == TagLibTag.ATTRIBUTE_TYPE_DYNAMIC) pattern.append(" ...");
		pattern.append(">");
		if (tlt.getHasBody()) {
			if (tlt.isBodyReq()) {
				pattern.append("</");
				pattern.append(tlt.getFullName());
				pattern.append(">");
			}
			else if (tlt.isBodyFree()) {
				pattern.append("[</");
				pattern.append(tlt.getFullName());
				pattern.append(">]");
			}
		}

		pe.setAdditional(KeyConstants._Pattern, pattern);

		// Documentation
		StringBuilder doc = new StringBuilder(tlt.getDescription());
		req = new StringBuilder();
		opt = new StringBuilder();

		doc.append("\n");

		it = attrs.entrySet().iterator();
		while (it.hasNext()) {
			entry = it.next();
			attr = entry.getValue();
			tmp = attr.isRequired() ? req : opt;

			tmp.append("* ");
			tmp.append(attr.getName());
			tmp.append(" (");
			tmp.append(attr.getType());
			tmp.append("): ");
			tmp.append(attr.getDescription());
			tmp.append("\n");
		}

		if (req.length() > 0) doc.append("\nRequired:\n").append(req);
		if (opt.length() > 0) doc.append("\nOptional:\n").append(opt);

		pe.setAdditional(KeyConstants._Documentation, doc);
	}

}
