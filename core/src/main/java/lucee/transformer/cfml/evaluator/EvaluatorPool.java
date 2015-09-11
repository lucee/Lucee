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

import java.util.Vector;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.bytecode.statement.tag.Tag;
import lucee.transformer.library.function.FunctionLib;
import lucee.transformer.library.tag.TagLibTag;
import lucee.transformer.util.SourceCode;

/**
 *
 * Wenn der CFML Transformer waehrend des uebersetzungsprozess auf einen Tag stoesst, 
 * prueft er mithilfe der passenden TagLib, 
 * ob dieses Tag eine Evaluator definiert hat. 
 * Wenn ein Evaluator definiert ist, kann der CFML Transformer diesen aber nicht sofort aufrufen, 
 * da zuerst das komplette Dokument uebersetzt werden muss, 
 * bevor ein Evaluator aufgerufen werden kann.
 * Hier kommt der EvaluatorPool zum Einsatz, 
 * der CFMLTransfomer uebergibt den Evaluator den er von der TagLib erhalten hat, 
 * an den EvaluatorPool weiter. 
 * Sobald der CFMLTransfomer den uebersetzungsprozess abgeschlossen hat, 
 * ruft er dann den EvaluatorPool auf und dieser ruft dann alle Evaluatoren auf die im uebergeben wurden. 

 */
public final class EvaluatorPool {
	
	Vector v=new Vector();
	
	/**
	 * Diese Methode wird aufgerufen um eine neue Methode in den Pool zu spielen.
	 * @param libTag  Die Definition des Tag aus der TLD.
	 * @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 * @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 * @param cfml CFMLString des aktuellen uebersetzungsprozess.
	 */
	public void add(TagLibTag libTag,Tag tag, FunctionLib[] flibs, SourceCode cfml) {
		v.add(new EvaluatorData(libTag,tag,flibs,cfml));
	}

	/**
	 * Die Methode run wird aufgerufen sobald, der CFML Transformer den uebersetzungsprozess angeschlossen hat.
	 * Die metode run rauft darauf alle Evaluatoren auf die intern gespeicher wurden und loescht den internen Speicher.
	 * @throws TemplateException
	 */
	public void run() throws TemplateException  {
		int size=v.size();
		for(int i=0;i<size;i++) {
			EvaluatorData ec=(EvaluatorData)v.elementAt(i);
			SourceCode cfml=ec.getCfml();
			cfml.setPos(ec.getPos());
			try {
				if(ec.getLibTag().getEvaluator()!=null)ec.getLibTag().getEvaluator().evaluate(
						ec.getTag(),
						ec.getLibTag(),
						ec.getFlibs());
			} catch (EvaluatorException e) {
			    v.clear();//print.printST(e);
				throw new TemplateException(cfml,e);
			}catch (Throwable e) {
			    v.clear();
				throw new TemplateException(cfml,e);
			}
			
		}
		v.clear();
	}

	/**
	 *
	 *
	 * Die interne Klasse EvaluatorData dient zum Zwischenspeichern aller Daten 
	 * die benoetigt werden einen einzelnen Evaluator aufzurufen. 
	 */
	class EvaluatorData {
		TagLibTag libTag;
		Tag tag; 
		FunctionLib[] flibs; 
		SourceCode cfml;
		int pos;
		
		/**
		* Konstruktor von EvaluatorData.
		* @param libTag  Die Definition des Tag aus der TLD.
	 	* @param cfxdTag Das konkrete Tag innerhalb der kompletten CFXD.
	 	* @param flibs Saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors.
	 	* @param cfml CFMLString des aktuellen uebersetzungsprozess.
	 	*/
		public EvaluatorData(TagLibTag libTag,Tag tag, FunctionLib[] flibs, SourceCode cfml) {
			this.libTag=libTag;
			this.tag=tag;
			this.flibs=flibs;
			this.cfml=cfml;
			this.pos=cfml.getPos();
		}
		
		/**
		 * Gibt den aktuellen CFMLString zurueck.
		 * @return CFMLString des aktuellen uebersetzungsprozess.
		 */
		public SourceCode getCfml() {
			return cfml;
		}

		/**
		 * Gibt den zu verarbeitenden Tag zurueck.
		 * @return Das konkrete Tag innerhalb der kompletten CFXD.
		 */
		public Tag getTag() {
			return tag;
		}

		/**
		 * Gibt saemtliche Function Library Deskriptoren des aktuellen Tag Libray Deskriptors zurueck. 
		 * @return Saemtliche Function Library Deskriptoren.
		 */
		public FunctionLib[] getFlibs() {
			return flibs;
		}

		/**
		 * Die Definition des aktuellen tags aus der TLD
		 * @return den aktuellen TagLibTag.
		 */
		public TagLibTag getLibTag() {
			return libTag;
		}

		/**
		 * Die Position des zu verarbeitenden Tag innerhalb der CFML Seite.
		 * @return Position des Tag.
		 */
		public int getPos() {
			return pos;
		}

	}

    /**
     * clears the ppol
     */
    public void clear() {
        v.clear();
    }

	/*public static void getPool() {
		// TODO Auto-generated method stub
		
	}*/

}