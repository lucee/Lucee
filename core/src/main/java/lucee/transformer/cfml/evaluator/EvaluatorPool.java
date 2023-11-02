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
package lucee.transformer.cfml.evaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.expression.var.BIF;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.function.FunctionLibFunction;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

/**
 *
 * Wenn der CFML Transformer waehrend des uebersetzungsprozess auf einen Tag stoesst, prueft er
 * mithilfe der passenden TagLib, ob dieses Tag eine Evaluator definiert hat. Wenn ein Evaluator
 * definiert ist, kann der CFML Transformer diesen aber nicht sofort aufrufen, da zuerst das
 * komplette Dokument uebersetzt werden muss, bevor ein Evaluator aufgerufen werden kann. Hier kommt
 * der EvaluatorPool zum Einsatz, der CFMLTransfomer uebergibt den Evaluator den er von der TagLib
 * erhalten hat, an den EvaluatorPool weiter. Sobald der CFMLTransfomer den uebersetzungsprozess
 * abgeschlossen hat, ruft er dann den EvaluatorPool auf und dieser ruft dann alle Evaluatoren auf
 * die im uebergeben wurden.
 * 
 */
public final class EvaluatorPool {

	List<TagData> tags = new ArrayList<TagData>();
	List<FunctionData> functions = new ArrayList<FunctionData>();

	/**
	 * add a tag to the pool to evaluate at the end
	 */
	public void add(TagLibTag libTag, Tag tag, FunctionLib[] flibs, SourceCode cfml) {
		tags.add(new TagData(libTag, tag, flibs, cfml));
	}

	public void add(FunctionLibFunction flf, BIF bif, SourceCode cfml) {
		functions.add(new FunctionData(flf, bif, cfml));

	}

	/**
	 * Die Methode run wird aufgerufen sobald, der CFML Transformer den uebersetzungsprozess
	 * angeschlossen hat. Die metode run rauft darauf alle Evaluatoren auf die intern gespeicher wurden
	 * und loescht den internen Speicher.
	 * 
	 * @throws TemplateException
	 */
	public void run() throws TemplateException {
		{
			// tags
			Iterator<TagData> it = tags.iterator();
			while (it.hasNext()) {
				TagData td = it.next();
				SourceCode cfml = td.cfml;
				cfml.setPos(td.pos);
				try {
					if (td.libTag.getEvaluator() != null) td.libTag.getEvaluator().evaluate(td.tag, td.libTag, td.flibs);
				}
				catch (EvaluatorException e) {
					clear();// print.printST(e);
					throw new TemplateException(cfml, e);
				}
				catch (Throwable e) {
					ExceptionUtil.rethrowIfNecessary(e);
					clear();
					throw new TemplateException(cfml, e);
				}

			}
			tags.clear();
		}
		// functions
		Iterator<FunctionData> it = functions.iterator();
		while (it.hasNext()) {
			FunctionData td = it.next();
			SourceCode cfml = td.cfml;
			cfml.setPos(td.pos);
			try {
				if (td.flf.getEvaluator() != null) td.flf.getEvaluator().evaluate(td.bif, td.flf);
			}
			catch (EvaluatorException e) {
				clear();// print.printST(e);
				throw new TemplateException(cfml, e);
			}
			catch (Throwable e) {
				ExceptionUtil.rethrowIfNecessary(e);
				clear();
				throw new TemplateException(cfml, e);
			}

		}
		functions.clear();

	}

	/**
	 * internal class to store all tag related data
	 */
	static class TagData {
		private final TagLibTag libTag;
		private final Tag tag;
		private final FunctionLib[] flibs;
		private final SourceCode cfml;
		private final int pos;

		public TagData(TagLibTag libTag, Tag tag, FunctionLib[] flibs, SourceCode cfml) {
			this.libTag = libTag;
			this.tag = tag;
			this.flibs = flibs;
			this.cfml = cfml;
			this.pos = cfml.getPos();
		}
	}

	static class FunctionData {
		private final FunctionLibFunction flf;
		private final BIF bif;
		private final SourceCode cfml;
		private final int pos;

		public FunctionData(FunctionLibFunction flf, BIF bif, SourceCode cfml) {
			this.flf = flf;
			this.bif = bif;
			this.cfml = cfml;
			this.pos = cfml.getPos();
		}
	}

	/**
	 * clears the ppol
	 */
	public void clear() {
		tags.clear();
		functions.clear();
	}

	/*
	 * public static void getPool() { // TODO Auto-generated method stub
	 * 
	 * }
	 */

}