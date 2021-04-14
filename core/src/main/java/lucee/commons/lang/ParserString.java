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
package lucee.commons.lang;

import lucee.commons.io.SystemUtil;

/**
 * Der CFMLString ist eine Hilfe fuer die Transformer, er repraesentiert den CFML Code und bietet
 * Methoden an, um alle noetigen Informationen auszulesen und Manipulationen durchzufuehren. Dies
 * um, innerhalb des Transformer, wiederkehrende Zeichenketten-Manipulationen zu abstrahieren.
 *
 */
public final class ParserString {

	/**
	 * Mindestens einen Space
	 */
	public static final short AT_LEAST_ONE_SPACE = 0;

	/**
	 * Mindestens ein Space
	 */
	public static final short ZERO_OR_MORE_SPACE = 1;

	/**
	 * Field <code>pos</code>
	 */
	protected int pos = 0;
	/**
	 * Field <code>text</code>
	 */
	protected char[] text;
	/**
	 * Field <code>lcText</code>
	 */
	protected char[] lcText;

	/**
	 * Diesen Konstruktor kann er CFML Code als Zeichenkette uebergeben werden.
	 * 
	 * @param text CFML Code
	 */
	public ParserString(String text) {
		init(text);
	}

	/**
	 * Gemeinsame Initialmethode der drei Konstruktoren, diese erhaelt den CFML Code als char[] und
	 * uebertraegt ihn, in die interen Datenhaltung.
	 * 
	 * @param str
	 */
	protected void init(String str) {
		int len = str.length();
		text = new char[len];
		lcText = new char[len];

		for (int i = 0; i < len; i++) {
			char c = str.charAt(i);
			text[i] = c;
			if (c == '\n' || c == '\r' || c == '\t') {
				lcText[i] = ' ';
			}
			else lcText[i] = ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) ? c : Character.toLowerCase(c);
		}
	}

	/**
	 * Gibt zurueck ob, ausgehend von der aktuellen Position des internen Zeigers im Text, noch ein
	 * Zeichen vorangestellt ist.
	 * 
	 * @return boolean Existiert ein weieters Zeichen nach dem Zeiger.
	 */
	public boolean hasNext() {
		return pos + 1 < text.length;
	}

	public boolean hasNextNext() {
		return pos + 2 < text.length;
	}

	public boolean hasPrevious() {
		return pos - 1 >= 0;
	}

	public boolean hasPreviousPrevious() {
		return pos - 2 >= 0;
	}

	/**
	 * Stellt den internen Zeiger auf die naechste Position. ueberlappungen ausserhalb des Index des
	 * Textes werden ignoriert.
	 */
	public void next() {
		pos++;
	}

	/**
	 * Stellt den internen Zeiger auf die vorhergehnde Position. ueberlappungen ausserhalb des Index des
	 * Textes werden ignoriert.
	 */
	public void previous() {
		pos--;
	}

	/**
	 * Gibt das Zeichen (Character) an der aktuellen Position des Zeigers aus.
	 * 
	 * @return char Das Zeichen auf dem der Zeiger steht.
	 */
	public char getCurrent() {
		return text[pos];
	}

	/**
	 * Gibt das Zeichen (Character) an der naechsten Position des Zeigers aus.
	 * 
	 * @return char Das Zeichen auf dem der Zeiger steht plus 1.
	 */
	public char getNext() {
		return text[pos + 1];
	}

	/**
	 * Gibt das Zeichen, als Kleinbuchstaben, an der aktuellen Position des Zeigers aus.
	 * 
	 * @return char Das Zeichen auf dem der Zeiger steht als Kleinbuchstaben.
	 */
	public char getCurrentLower() {
		return lcText[pos];
	}

	/**
	 * Gibt das Zeichen, als Grossbuchstaben, an der aktuellen Position des Zeigers aus.
	 * 
	 * @return char Das Zeichen auf dem der Zeiger steht als Grossbuchstaben.
	 */
	public char getCurrentUpper() {
		return Character.toUpperCase(text[pos]);
	}

	/**
	 * Gibt das Zeichen, als Kleinbuchstaben, an der naechsten Position des Zeigers aus.
	 * 
	 * @return char Das Zeichen auf dem der Zeiger steht plus 1 als Kleinbuchstaben.
	 */
	public char getNextLower() {
		return lcText[pos];
	}

	/**
	 * Gibt das Zeichen an der angegebenen Position zurueck.
	 * 
	 * @param pos Position des auszugebenen Zeichen.
	 * @return char Das Zeichen an der angegebenen Position.
	 */
	public char charAt(int pos) {
		return text[pos];
	}

	/**
	 * Gibt das Zeichen, als Kleinbuchstaben, an der angegebenen Position zurueck.
	 * 
	 * @param pos Position des auszugebenen Zeichen.
	 * @return char Das Zeichen an der angegebenen Position als Kleinbuchstaben.
	 */
	public char charAtLower(int pos) {
		return lcText[pos];
	}

	/**
	 * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
	 * 
	 * @param c Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean isNext(char c) {
		if (!hasNext()) return false;
		return lcText[pos + 1] == c;
	}

	public boolean isPrevious(char c) {
		if (!hasPrevious()) return false;
		return lcText[pos - 1] == c;
	}

	/**
	 * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
	 * 
	 * @param c Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean isCurrentIgnoreSpace(char c) {
		if (!hasNext()) return false;
		int start = getPos();
		removeSpace();

		boolean is = isCurrent(c);
		setPos(start);
		return is;
	}

	/**
	 * Gibt zurueck ob das naechste Zeichen das selbe ist wie das Eingegebene.
	 * 
	 * @param c Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean isCurrentIgnoreSpace(String str) {
		if (!hasNext()) return false;
		int start = getPos();
		removeSpace();

		boolean is = isCurrent(str);
		setPos(start);
		return is;
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen zwischen den Angegebenen liegt.
	 * 
	 * @param left Linker (unterer) Wert.
	 * @param right Rechter (oberer) Wert.
	 * @return Gibt zurueck ob das aktuelle Zeichen zwischen den Angegebenen liegt.
	 */
	public boolean isCurrentBetween(char left, char right) {
		if (!isValidIndex()) return false;
		return lcText[pos] >= left && lcText[pos] <= right;
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
	 * 
	 * @return Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
	 */
	public boolean isCurrentDigit() {
		if (!isValidIndex()) return false;
		return (lcText[pos] >= '0' && lcText[pos] <= '9');
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
	 * 
	 * @return Gibt zurueck ob das aktuelle Zeichen eine Zahl ist.
	 */
	public boolean isCurrentQuoter() {
		if (!isValidIndex()) return false;
		return lcText[pos] == '"' || lcText[pos] == '\'';
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
	 * 
	 * @return Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
	 */
	public boolean isCurrentLetter() {
		if (!isValidIndex()) return false;
		return lcText[pos] >= 'a' && lcText[pos] <= 'z';
	}

	public boolean isCurrentNumber() {
		if (!isValidIndex()) return false;
		return lcText[pos] >= '0' && lcText[pos] <= '9';
	}

	public boolean isCurrentWhiteSpace() {
		if (!isValidIndex()) return false;
		return (lcText[pos] == ' ' || lcText[pos] == '\t' || lcText[pos] == '\b' || lcText[pos] == '\r' || lcText[pos] == '\n');
		// return lcText[pos]>='a' && lcText[pos]<='z';
	}

	public boolean forwardIfCurrentWhiteSpace() {
		boolean rtn = false;
		while (isCurrentWhiteSpace()) {
			pos++;
			rtn = true;
		}
		return rtn;
	}

	public boolean isNextWhiteSpace() {
		if (!hasNext()) return false;
		return (lcText[pos + 1] == ' ' || lcText[pos + 1] == '\t' || lcText[pos + 1] == '\b' || lcText[pos + 1] == '\r' || lcText[pos + 1] == '\n');
	}

	public boolean isNextNextWhiteSpace() {
		if (!hasNextNext()) return false;
		return (lcText[pos + 2] == ' ' || lcText[pos + 2] == '\t' || lcText[pos + 2] == '\b' || lcText[pos + 2] == '\r' || lcText[pos + 2] == '\n');
	}

	public boolean isPreviousWhiteSpace() {
		if (!hasPrevious()) return false;
		return (lcText[pos - 1] == ' ' || lcText[pos - 1] == '\t' || lcText[pos - 1] == '\b' || lcText[pos - 1] == '\r' || lcText[pos - 1] == '\n');
	}

	public boolean isPreviousPreviousWhiteSpace() {
		if (!hasPreviousPrevious()) return false;
		return (lcText[pos - 2] == ' ' || lcText[pos - 2] == '\t' || lcText[pos - 2] == '\b' || lcText[pos - 2] == '\r' || lcText[pos - 2] == '\n');
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen ein Special Buchstabe ist (_,<euro>,$,<pound>).
	 * 
	 * @return Gibt zurueck ob das aktuelle Zeichen ein Buchstabe ist.
	 */
	public boolean isCurrentSpecial() {
		if (!isValidIndex()) return false;
		return lcText[pos] == '_' || lcText[pos] == '$' || lcText[pos] == SystemUtil.SYMBOL_EURO || lcText[pos] == SystemUtil.SYMBOL_POUND;
	}

	/**
	 * Gibt zurueck ob das aktuelle Zeichen das selbe ist wie das Eingegebene.
	 * 
	 * @param c char Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean isCurrent(char c) {
		if (!isValidIndex()) return false;
		return lcText[pos] == c;
	}

	public boolean isLast(char c) {
		if (lcText.length == 0) return false;
		return lcText[lcText.length - 1] == c;
	}

	/**
	 * Stellt den Zeiger eins nach vorn, wenn das aktuelle Zeichen das selbe ist wie das Eingegebene,
	 * gibt zurueck ob es das selbe Zeichen war oder nicht.
	 * 
	 * @param c char Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean forwardIfCurrent(char c) {
		if (isCurrent(c)) {
			pos++;
			return true;
		}
		return false;
	}

	/**
	 * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind, wie in der angegebenen
	 * Zeichenkette.
	 * 
	 * @param str String Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean isCurrent(String str) {
		if (pos + str.length() > text.length) return false;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (str.charAt(i) != lcText[pos + i]) return false;
		}
		return true;
	}

	/**
	 * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind, wie in der angegebenen
	 * Zeichenkette, wenn ja wird der Zeiger um die Laenge des String nach vorne gesetzt.
	 * 
	 * @param str String Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean forwardIfCurrent(String str) {
		boolean is = isCurrent(str);
		if (is) pos += str.length();
		return is;
	}

	public boolean forwardIfCurrent(String str, boolean startWithSpace) {
		if (!startWithSpace) return forwardIfCurrent(str);

		int start = pos;
		if (!removeSpace()) return false;

		if (!forwardIfCurrent(str)) {
			pos = start;
			return false;
		}
		return true;
	}

	public boolean forwardIfCurrent(String first, String second, String third, boolean startWithSpace) {
		if (!startWithSpace) return forwardIfCurrent(first, second, third);
		int start = pos;

		if (!removeSpace()) return false;

		if (!forwardIfCurrent(first, second, third)) {
			pos = start;
			return false;
		}
		return true;
	}

	/**
	 * Gibt zurueck ob das aktuelle und die folgenden Zeichen die selben sind gefolgt nicht von einem
	 * word character, wenn ja wird der Zeiger um die Laenge des String nach vorne gesetzt.
	 * 
	 * @param str String Zeichen zum Vergleich.
	 * @return boolean
	 */
	public boolean forwardIfCurrentAndNoWordAfter(String str) {
		int c = pos;
		if (forwardIfCurrent(str)) {
			if (!isCurrentLetter() && !isCurrent('_')) return true;
		}
		pos = c;
		return false;
	}

	public boolean forwardIfCurrentAndNoWordNumberAfter(String str) {
		int c = pos;
		if (forwardIfCurrent(str)) {
			if (!isCurrentLetter() && !isCurrentLetter() && !isCurrent('_')) return true;
		}
		pos = c;
		return false;
	}

	public boolean forwardIfCurrentAndNoWordNumberAfter(String str, String str2) {
		int c = pos;
		if (forwardIfCurrent(str, str2)) {
			if (!isCurrentLetter() && !isCurrentLetter() && !isCurrent('_')) return true;
		}
		pos = c;
		return false;
	}

	public boolean forwardIfCurrentAndNoWordNumberAfter(String str, String str2, String str3) {
		int c = pos;
		if (forwardIfCurrent(str, str2, str3)) {
			if (!isCurrentLetter() && !isCurrentLetter() && !isCurrent('_')) return true;
		}
		pos = c;
		return false;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
	 * 
	 * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
	 *         entsprechen.
	 */
	public boolean isCurrent(String first, char second) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;
		removeSpace();
		boolean rtn = isCurrent(second);
		pos = start;
		return rtn;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
	 * 
	 * @param first Erstes Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweites Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
	 *         entsprechen.
	 */
	public boolean isCurrent(char first, char second) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;
		removeSpace();
		boolean rtn = isCurrent(second);
		pos = start;
		return rtn;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
	 * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
	 * 
	 * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
	 */
	public boolean forwardIfCurrent(String first, char second) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;
		removeSpace();
		boolean rtn = forwardIfCurrent(second);
		if (!rtn) pos = start;
		return rtn;
	}

	public boolean forwardIfCurrent(String first, String second, char third) {
		int start = pos;
		if (!forwardIfCurrent(first, second)) return false;
		removeSpace();
		boolean rtn = forwardIfCurrent(third);
		if (!rtn) pos = start;
		return rtn;
	}

	/**
	 * Gibt zurueck ob ein Wert folgt und vor und hinterher Leerzeichen folgen.
	 * 
	 * @param before Definition der Leerzeichen vorher.
	 * @param val Gefolgter Wert der erartet wird.
	 * @param after Definition der Leerzeichen nach dem Wert.
	 * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
	 */
	public boolean forwardIfCurrent(short before, String val, short after) {
		int start = pos;
		// space before
		if (before == AT_LEAST_ONE_SPACE) {
			if (!removeSpace()) return false;
		}
		else removeSpace();

		// value
		if (!forwardIfCurrent(val)) {
			setPos(start);
			return false;
		}

		// space after
		if (after == AT_LEAST_ONE_SPACE) {
			if (!removeSpace()) {
				setPos(start);
				return false;
			}
		}
		else removeSpace();
		return true;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
	 * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
	 * 
	 * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
	 */
	public boolean forwardIfCurrent(char first, char second) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;
		removeSpace();
		boolean rtn = forwardIfCurrent(second);
		if (!rtn) pos = start;
		return rtn;
	}

	public boolean forwardIfCurrent(char first, char second, char third) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;

		removeSpace();
		boolean rtn = forwardIfCurrent(second);
		if (!rtn) {
			pos = start;
			return rtn;
		}

		removeSpace();
		rtn = forwardIfCurrent(third);
		if (!rtn) pos = start;

		return rtn;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second.
	 * 
	 * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob die eingegebenen Werte dem Inhalt beim aktuellen Stand des Zeigers
	 *         entsprechen.
	 */
	public boolean isCurrent(String first, String second) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;
		removeSpace();
		boolean rtn = isCurrent(second);
		pos = start;
		return rtn;
	}

	/**
	 * Gibt zurueck ob first den folgenden Zeichen entspricht, gefolgt von Leerzeichen und second, wenn
	 * ja wird der Zeiger um die Laenge der uebereinstimmung nach vorne gestellt.
	 * 
	 * @param first Erste Zeichen zum Vergleich (Vor den Leerzeichen).
	 * @param second Zweite Zeichen zum Vergleich (Nach den Leerzeichen).
	 * @return Gibt zurueck ob der Zeiger vorwaerts geschoben wurde oder nicht.
	 */
	public boolean forwardIfCurrent(String first, String second) {
		int start = pos;

		if (!forwardIfCurrent(first)) return false;

		if (!removeSpace()) {
			pos = start;
			return false;
		}
		boolean rtn = forwardIfCurrent(second);
		if (!rtn) pos = start;
		return rtn;
	}

	public boolean forwardIfCurrent(String first, String second, String third) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;

		if (!removeSpace()) {
			pos = start;
			return false;
		}

		if (!forwardIfCurrent(second)) {
			pos = start;
			return false;
		}

		if (!removeSpace()) {
			pos = start;
			return false;
		}

		boolean rtn = forwardIfCurrent(third);
		if (!rtn) pos = start;
		return rtn;
	}

	public boolean forwardIfCurrent(String first, String second, String third, String forth) {
		int start = pos;
		if (!forwardIfCurrent(first)) return false;

		if (!removeSpace()) {
			pos = start;
			return false;
		}

		if (!forwardIfCurrent(second)) {
			pos = start;
			return false;
		}

		if (!removeSpace()) {
			pos = start;
			return false;
		}

		if (!forwardIfCurrent(third)) {
			pos = start;
			return false;
		}

		if (!removeSpace()) {
			pos = start;
			return false;
		}

		boolean rtn = forwardIfCurrent(forth);
		if (!rtn) pos = start;
		return rtn;

	}

	/**
	 * Gibt zurueck ob sich vor dem aktuellen Zeichen Leerzeichen befinden.
	 * 
	 * @return Gibt zurueck ob sich vor dem aktuellen Zeichen Leerzeichen befinden.
	 */
	public boolean hasSpaceBefore() {
		return pos > 0 && lcText[pos - 1] == ' ';
	}

	/**
	 * Stellt den Zeiger nach vorne, wenn er sich innerhalb von Leerzeichen befindet, bis die
	 * Leerzeichen fertig sind.
	 * 
	 * @return Gibt zurueck ob der Zeiger innerhalb von Leerzeichen war oder nicht.
	 */
	public boolean removeSpace() {
		int start = pos;
		while (pos < text.length && lcText[pos] == ' ') {
			pos++;
		}
		return (start < pos);
	}

	public void revertRemoveSpace() {
		while (hasSpaceBefore()) {
			previous();
		}
	}

	/**
	 * Stellt den internen Zeiger an den Anfang der naechsten Zeile, gibt zurueck ob eine weitere Zeile
	 * existiert oder ob es bereits die letzte Zeile war.
	 * 
	 * @return Existiert eine weitere Zeile.
	 */
	public boolean nextLine() {
		while (isValidIndex() && text[pos] != '\n') {
			next();
		}
		if (isValidIndex() && text[pos] == '\n') {
			next();
			return isValidIndex();
		}
		return false;
	}

	/**
	 * Gibt eine Untermenge des CFMLString als Zeichenkette zurueck, ausgehend von start bis zum Ende
	 * des CFMLString.
	 * 
	 * @param start Von wo aus die Untermege ausgegeben werden soll.
	 * @return Untermenge als Zeichenkette
	 */
	public String substring(int start) {
		return substring(start, text.length - start);
	}

	/**
	 * Gibt eine Untermenge des CFMLString als Zeichenkette zurueck, ausgehend von start mit einer
	 * maximalen Laenge count.
	 * 
	 * @param start Von wo aus die Untermenge ausgegeben werden soll.
	 * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
	 * @return Untermenge als Zeichenkette.
	 */
	public String substring(int start, int count) {
		return String.valueOf(text, start, count);
	}

	/**
	 * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
	 * start bis zum Ende des CFMLString.
	 * 
	 * @param start Von wo aus die Untermenge ausgegeben werden soll.
	 * @return Untermenge als Zeichenkette in Kleinbuchstaben.
	 */
	public String substringLower(int start) {
		return substringLower(start, text.length - start);
	}

	/**
	 * Gibt eine Untermenge des CFMLString als Zeichenkette in Kleinbuchstaben zurueck, ausgehend von
	 * start mit einer maximalen Laenge count.
	 * 
	 * @param start Von wo aus die Untermenge ausgegeben werden soll.
	 * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
	 * @return Untermenge als Zeichenkette in Kleinbuchstaben.
	 */
	public String substringLower(int start, int count) {
		return String.valueOf(lcText, start, count);
	}

	/**
	 * Gibt eine Untermenge des CFMLString als CFMLString zurueck, ausgehend von start bis zum Ende des
	 * CFMLString.
	 * 
	 * @param start Von wo aus die Untermenge ausgegeben werden soll.
	 * @return Untermenge als CFMLString
	 */
	public ParserString subCFMLString(int start) {
		return subCFMLString(start, text.length - start);
	}

	/**
	 * Gibt eine Untermenge des CFMLString als CFMLString zurueck, ausgehend von start mit einer
	 * maximalen Laenge count.
	 * 
	 * @param start Von wo aus die Untermenge ausgegeben werden soll.
	 * @param count Wie lange die zurueckgegebene Zeichenkette maximal sein darf.
	 * @return Untermenge als CFMLString
	 */
	public ParserString subCFMLString(int start, int count) {
		return new ParserString(String.valueOf(text, start, count));
		/*
		 * NICE die untermenge direkter ermiiteln, das problem hierbei sind die lines
		 * 
		 * int endPos=start+count; int LineFrom=-1; int LineTo=-1; for(int i=0;i<lines.length;i++) { if() }
		 * 
		 * return new CFMLString( 0, String.valueOf(text,start,count).toCharArray(),
		 * String.valueOf(lcText,start,count).toCharArray(), lines);
		 */
	}

	@Override
	public String toString() {
		return new String(this.text);
	}

	/**
	 * Gibt die aktuelle Position des Zeigers innerhalb des CFMLString zurueck.
	 * 
	 * @return Position des Zeigers
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * Setzt die Position des Zeigers innerhalb des CFMLString, ein ungueltiger index wird ignoriert.
	 * 
	 * @param pos Position an die der Zeiger gestellt werde soll.
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}

	/**
	 * Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
	 * 
	 * @return Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
	 */
	public boolean isLast() {
		return pos == text.length - 1;
	}

	/**
	 * Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
	 * 
	 * @return Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
	 */
	public boolean isAfterLast() {
		return pos >= text.length;
	}

	/**
	 * Gibt zurueck ob der Zeiger einen korrekten Index hat.
	 * 
	 * @return Gibt zurueck ob der Zeiger einen korrekten Index hat.
	 */
	public boolean isValidIndex() {
		return pos < text.length;
	}

	/**
	 * Gibt zurueck, ausgehend von der aktuellen Position, wann das naechste Zeichen folgt das gleich
	 * ist wie die Eingabe, falls keines folgt wird -1 zurueck gegeben. Gross- und Kleinschreibung der
	 * Zeichen werden igoriert.
	 * 
	 * @param c gesuchtes Zeichen
	 * @return Zeichen das gesucht werden soll.
	 */
	public int indexOfNext(char c) {
		for (int i = pos; i < lcText.length; i++) {
			if (lcText[i] == c) return i;
		}
		return -1;
	}

	/**
	 * Gibt das letzte Wort das sich vor dem aktuellen Zeigerstand befindet zurueck, falls keines
	 * existiert wird null zurueck gegeben.
	 * 
	 * @return Word vor dem aktuellen Zeigerstand.
	 */
	public String lastWord() {
		int size = 1;
		while (pos - size > 0 && lcText[pos - size] == ' ') {
			size++;
		}
		while (pos - size > 0 && lcText[pos - size] != ' ' && lcText[pos - size] != ';') {
			size++;
		}
		return this.substring((pos - size + 1), (pos - 1));
	}

	/**
	 * Gibt die Laenge des CFMLString zurueck.
	 * 
	 * @return Laenge des CFMLString.
	 */
	public int length() {
		return text.length;
	}

	/**
	 * Prueft ob das uebergebene Objekt diesem Objekt entspricht.
	 * 
	 * @param o Object zum vergleichen.
	 * @return Ist das uebergebene Objekt das selbe wie dieses.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ParserString)) return false;
		return o.toString().equals(this.toString());
	}

}