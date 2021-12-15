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

import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.transformer.util.PageSourceCode;
import lucee.transformer.util.SourceCode;

/**
 * Die Klasse TemplateException wird durch den CFMLTransformer geworfen, wenn dieser auf einen
 * grammatikalischen Fehler in dem zu verarbeitenden CFML Code stoesst oder wenn ein Tag oder eine
 * Funktion von der Definition innerhalb der Tag- bzw. der Funktions- Library abweicht.
 */
public final class CFMLTransformerException extends Exception {
	private SourceCode sc;
	// private String htmlMessage;

	/**
	 * Konstruktor mit einem CFMLString und einer anderen Exception.
	 * 
	 * @param cfml
	 * @param e
	 */
	public CFMLTransformerException(SourceCode sc, Exception e) {
		this(sc, StringUtil.isEmpty(e.getMessage()) ? (Caster.toClassName(e)) : e.getMessage());
	}

	/**
	 * Konstruktor ohne Message, nur mit CFMLString.
	 * 
	 * @param cfml
	 * 
	 *            public TemplateException(CFMLString cfml) { this(cfml,"Error while transforming CFML
	 *            File"); }
	 */

	/**
	 * Hauptkonstruktor, mit CFMLString und message.
	 * 
	 * @param cfml CFMLString
	 * @param message Fehlermeldung
	 */
	public CFMLTransformerException(SourceCode sc, String message) {
		super(message);
		this.sc = sc;

	}

	/**
	 * Gibt eine detaillierte Fehlermeldung zurueck. ueberschreibt toString Methode von
	 * java.lang.Objekt, alias fuer getMessage().
	 * 
	 * @return Fehlermeldung als Plain Text Ausgabe
	 */
	@Override
	public String toString() {
		boolean hasCFML = sc != null;
		StringBuffer sb = new StringBuffer();
		sb.append("Error\n");
		sb.append("----------------------------------\n");
		if (hasCFML && sc instanceof PageSourceCode) {
			sb.append("File: " + ((PageSourceCode) sc).getPageSource().getDisplayPath() + "\n");
		}
		if (hasCFML) {
			int line = sc.getLine();

			int counter = 0;
			sb.append("Line: " + line + "\n");
			sb.append("Column: " + sc.getColumn() + "\n");
			sb.append("Type: Syntax\n");
			sb.append("Code Outprint: \n");
			line = (line - 2 < 1) ? 1 : line - 2;
			int lineDescLen = (((line + 5) + "").length());
			for (int i = line;; i++) {
				if (i > 0) {
					String strLine = sc.getLineAsString(i);
					if (strLine == null) break;
					String desc = (("" + i).length() < lineDescLen) ? "0" + i : "" + i;
					sb.append(desc + ": " + strLine + "\n");
					counter++;
				}
				if (counter == 5) break;
			}
			sb.append("\n");
		}
		sb.append("Message:\n");
		sb.append("" + super.getMessage() + "\n");
		return sb.toString();
	}

	/**
	 * Gibt die Zeilennummer zurueck
	 * 
	 * @return Zeilennummer
	 */
	public int getLine() {
		return sc.getLine();
	}

	/**
	 * Gibt die Column der aktuellen Zeile zurueck
	 * 
	 * @return Column der Zeile
	 */
	public int getColumn() {
		return sc.getColumn();
	}

	/**
	 * Returns the value of cfml.
	 * 
	 * @return value cfml
	 */
	public SourceCode getCfml() {
		return sc;
	}

}