/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.transformer.util;

import java.util.ArrayList;

import lucee.commons.digest.HashUtil;
import lucee.commons.io.SystemUtil;
import lucee.transformer.Position;

/**
 * this class is a Parser String optimized for the transfomer (CFML Parser)
 */
public class SourceCode {

	public static final short AT_LEAST_ONE_SPACE = 0;
	public static final short ZERO_OR_MORE_SPACE = 1;

	protected int pos = 0;

	protected final char[] text;
	protected final char[] lcText;
	protected final Integer[] lines; // TODO to int[]
	private final boolean writeLog;
	private final int dialect;
	private int hash;

	/**
	 * Constructor of the class
	 * 
	 * @param text
	 * @param charset
	 */
	public SourceCode(String strText, boolean writeLog, int dialect) {
		this.text = strText.toCharArray();
		this.hash = strText.hashCode();
		this.dialect = dialect;
		lcText = new char[text.length];

		ArrayList<Integer> arr = new ArrayList<Integer>();

		for (int i = 0; i < text.length; i++) {
			pos = i;
			if (text[i] == '\n') {
				arr.add(new Integer(i));
				lcText[i] = ' ';
			}
			else if (text[i] == '\r') {
				if (isNextRaw('\n')) {
					lcText[i++] = ' ';
				}
				arr.add(new Integer(i));
				lcText[i] = ' ';
			}
			else if (text[i] == '\t') lcText[i] = ' ';
			else lcText[i] = Character.toLowerCase(text[i]);
		}
		pos = 0;
		arr.add(new Integer(text.length));
		lines = arr.toArray(new Integer[arr.size()]);

		this.writeLog = writeLog;
	}

	public boolean hasPrevious() {
		return pos > 0;
	}

	/**
	 * returns if the internal pointer is not on the last positions
	 */
	public boolean hasNext() {
		return pos + 1 < lcText.length;
	}

	public boolean hasNextNext() {
		return pos + 2 < lcText.length;
	}

	/**
	 * moves the internal pointer to the next position, no check if the next position is still valid
	 */
	public void next() {
		pos++;
	}

	/**
	 * moves the internal pointer to the previous position, no check if the next position is still valid
	 */
	public void previous() {
		pos--;
	}

	/**
	 * returns the character of the current position of the internal pointer
	 */
	public char getCurrent() {
		return text[pos];
	}

	/**
	 * returns the lower case representation of the character of the current position
	 */
	public char getCurrentLower() {
		return lcText[pos];
	}

	/**
	 * returns the character at the given position
	 */
	public char charAt(int pos) {
		return text[pos];
	}

	/**
	 * returns the character at the given position as lower case representation
	 */
	public char charAtLower(int pos) {
		return lcText[pos];
	}

	public boolean isPrevious(char c) {
		if (!hasPrevious()) return false;
		return lcText[pos - 1] == c;
	}

	/**
	 * is the character at the next position the same as the character provided by the input parameter
	 */
	public boolean isNext(char c) {
		if (!hasNext()) return false;
		return lcText[pos + 1] == c;
	}

	public boolean isNext(char a, char b) {
		if (!hasNextNext()) return false;
		return lcText[pos + 1] == a && lcText[pos + 2] == b;
	}

	private boolean isNextRaw(char c) {
		if (!hasNext()) return false;
		return text[pos + 1] == c;
	}

	/**
	 * is the character at the current position (internal pointer) in the range of the given input
	 * characters?
	 * 
	 * @param left lower value.
	 * @param right upper value.
	 */
	public boolean isCurrentBetween(char left, char right) {
		if (!isValidIndex()) return false;
		return lcText[pos] >= left && lcText[pos] <= right;
	}

	/**
	 * returns if the character at the current position (internal pointer) is a valid variable character
	 */
	public boolean isCurrentVariableCharacter() {
		if (!isValidIndex()) return false;
		return isCurrentLetter() || isCurrentNumber() || isCurrent('$') || isCurrent('_');
	}

	/**
	 * returns if the current character is a letter (a-z,A-Z)
	 * 
	 * @return is a letter
	 */
	public boolean isCurrentLetter() {
		if (!isValidIndex()) return false;
		return lcText[pos] >= 'a' && lcText[pos] <= 'z';
	}

	/**
	 * returns if the current character is a number (0-9)
	 * 
	 * @return is a letter
	 */
	public boolean isCurrentNumber() {
		if (!isValidIndex()) return false;
		return lcText[pos] >= '0' && lcText[pos] <= '9';
	}

	/**
	 * retuns if the current character (internal pointer) is a valid special sign (_, $, Pound Symbol,
	 * Euro Symbol)
	 */
	public boolean isCurrentSpecial() {
		if (!isValidIndex()) return false;
		return lcText[pos] == '_' || lcText[pos] == '$' || lcText[pos] == SystemUtil.CHAR_EURO || lcText[pos] == SystemUtil.CHAR_POUND;
	}

	/**
	 * is the current character (internal pointer) the same as the given
	 */
	public boolean isCurrent(char c) {
		if (!isValidIndex()) return false;
		return lcText[pos] == c;
	}

	/**
	 * forward the internal pointer plus one if the next character is the same as the given input
	 */
	public boolean forwardIfCurrent(char c) {
		if (isCurrent(c)) {
			pos++;
			return true;
		}
		return false;
	}

	/**
	 * returns if the current character (internal pointer) and the following are the same as the given
	 * input
	 */
	public boolean isCurrent(String str) {
		if (pos + str.length() > lcText.length) return false;
		for (int i = str.length() - 1; i >= 0; i--) {
			if (str.charAt(i) != lcText[pos + i]) return false;
		}
		return true;
	}

	/**
	 * forwards if the current character (internal pointer) and the following are the same as the given
	 * input
	 */
	public boolean forwardIfCurrent(String str) {
		boolean is = isCurrent(str);
		if (is) pos += str.length();
		return is;
	}

	/**
	 * @param str string to check against current position
	 * @param startWithSpace if true there must be whitespace at the current position
	 * @return does the criteria match?
	 */
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

	/**
	 * @param str string to check against current position
	 * @param startWithSpace if true there must be whitespace at the current position
	 * @param followedByNoVariableCharacter the character following the string must be a none variable
	 *            character (!a-z,A-Z,0-9,_$) (not eaten)
	 * @return does the criteria match?
	 */
	public boolean forwardIfCurrent(String str, boolean startWithSpace, boolean followedByNoVariableCharacter) {

		int start = pos;
		if (startWithSpace && !removeSpace()) return false;

		if (!forwardIfCurrent(str)) {
			pos = start;
			return false;
		}
		if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
			pos = start;
			return false;
		}
		return true;
	}

	/**
	 * forwards if the current character (internal pointer) and the following are the same as the given
	 * input, followed by a none word character
	 */
	public boolean forwardIfCurrentAndNoWordAfter(String str) {
		int c = pos;
		if (forwardIfCurrent(str)) {
			if (!isCurrentBetween('a', 'z') && !isCurrent('_')) return true;
		}
		pos = c;
		return false;
	}

	/**
	 * forwards if the current character (internal pointer) and the following are the same as the given
	 * input, followed by a none word character or a number
	 */
	public boolean forwardIfCurrentAndNoVarExt(String str) {
		int c = pos;
		if (forwardIfCurrent(str)) {
			if (!isCurrentBetween('a', 'z') && !isCurrentBetween('0', '9') && !isCurrent('_')) return true;
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

	public boolean forwardIfCurrent(String first, String second, String third, boolean startWithSpace, boolean followedByNoVariableCharacter) {
		int start = pos;

		if (startWithSpace && !removeSpace()) return false;

		if (!forwardIfCurrent(first, second, third)) {
			pos = start;
			return false;
		}
		if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
			pos = start;
			return false;
		}
		return true;
	}

	public boolean forwardIfCurrent(String first, String second, boolean startWithSpace, boolean followedByNoVariableCharacter) {
		int start = pos;

		if (startWithSpace && !removeSpace()) return false;

		if (!forwardIfCurrent(first, second)) {
			pos = start;
			return false;
		}
		if (followedByNoVariableCharacter && isCurrentVariableCharacter()) {
			pos = start;
			return false;
		}
		return true;
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

	public boolean hasNLBefore() {
		int index = 0;
		while (pos - (++index) >= 0) {
			if (text[pos - index] == '\n') return true;
			if (text[pos - index] == '\r') return true;
			if (lcText[pos - index] != ' ') return false;
		}
		return false;
	}

	/**
	 * Stellt den Zeiger nach vorne, wenn er sich innerhalb von Leerzeichen befindet, bis die
	 * Leerzeichen fertig sind.
	 * 
	 * @return Gibt zurueck ob der Zeiger innerhalb von Leerzeichen war oder nicht.
	 */
	public boolean removeSpace() {
		int start = pos;
		while (pos < lcText.length && lcText[pos] == ' ') {
			pos++;
		}
		return (start < pos);
	}

	public void revertRemoveSpace() {
		while (hasSpaceBefore()) {
			previous();
		}
	}

	public String removeAndGetSpace() {
		int start = pos;
		while (pos < lcText.length && lcText[pos] == ' ') {
			pos++;
		}
		return substring(start, pos - start);
	}

	/**
	 * Stellt den internen Zeiger an den Anfang der naechsten Zeile, gibt zurueck ob eine weitere Zeile
	 * existiert oder ob es bereits die letzte Zeile war.
	 * 
	 * @return Existiert eine weitere Zeile.
	 */
	public boolean nextLine() {
		while (isValidIndex() && text[pos] != '\n' && text[pos] != '\r') {
			next();
		}
		if (!isValidIndex()) return false;

		if (text[pos] == '\n') {
			next();
			return isValidIndex();
		}
		if (text[pos] == '\r') {
			next();
			if (isValidIndex() && text[pos] == '\n') {
				next();
			}
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
		return substring(start, lcText.length - start);
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
		return substringLower(start, lcText.length - start);
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
	public SourceCode subCFMLString(int start) {
		return subCFMLString(start, text.length - start);
	}

	/**
	 * return a subset of the current SourceCode
	 * 
	 * @param start start position of the new subset.
	 * @param count length of the new subset.
	 * @return subset of the SourceCode as new SourcCode
	 */
	public SourceCode subCFMLString(int start, int count) {
		return new SourceCode(String.valueOf(text, start, count), writeLog, dialect);

	}

	/**
	 * Gibt den CFMLString als String zurueck.
	 * 
	 * @see java.lang.Object#toString()
	 */
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
	 * Gibt die aktuelle Zeile zurueck in der der Zeiger des CFMLString steht.
	 * 
	 * @return Zeilennummer
	 */
	public int getLine() {
		return getLine(pos);
	}

	public Position getPosition() {
		return getPosition(pos);
	}

	public Position getPosition(int pos) {
		int line = 0;
		int posAtStart = 0;
		for (int i = 0; i < lines.length; i++) {
			if (pos <= lines[i].intValue()) {
				line = i + 1;
				if (i > 0) posAtStart = lines[i - 1].intValue();
				break;
			}
		}
		if (line == 0) throw new RuntimeException("syntax error");

		int column = pos - posAtStart;

		return new Position(line, column, pos);
	}

	/**
	 * Gibt zurueck in welcher Zeile die angegebene Position ist.
	 * 
	 * @param pos Position von welcher die Zeile erfragt wird
	 * @return Zeilennummer
	 */
	public int getLine(int pos) {
		for (int i = 0; i < lines.length; i++) {
			if (pos <= lines[i].intValue()) return i + 1;
		}
		return lines.length;
	}

	/**
	 * Gibt die Stelle in der aktuelle Zeile zurueck, in welcher der Zeiger steht.
	 * 
	 * @return Position innerhalb der Zeile.
	 */
	public int getColumn() {
		return getColumn(pos);
	}

	/**
	 * Gibt die Stelle in der Zeile auf die pos zeigt zurueck.
	 * 
	 * @param pos Position von welcher die Zeile erfragt wird
	 * @return Position innerhalb der Zeile.
	 */
	public int getColumn(int pos) {
		int line = getLine(pos) - 1;
		if (line == 0) return pos + 1;
		return pos - lines[line - 1].intValue();
	}

	/**
	 * Gibt die Zeile auf welcher der Zeiger steht als String zurueck.
	 * 
	 * @return Zeile als Zeichenkette
	 */
	public String getLineAsString() {
		return getLineAsString(getLine(pos));
	}

	/**
	 * Gibt die angegebene Zeile als String zurueck.
	 * 
	 * @param line Zeile die zurueck gegeben werden soll
	 * @return Zeile als Zeichenkette
	 */
	public String getLineAsString(int line) {
		int index = line - 1;
		if (lines.length <= index) return null;
		int max = lines[index].intValue();
		int min = 0;
		if (index != 0) min = lines[index - 1].intValue() + 1;

		if (min < max && max - 1 < lcText.length) return this.substring(min, max - min);
		return "";
	}

	/**
	 * Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
	 * 
	 * @return Gibt zurueck ob der Zeiger auf dem letzten Zeichen steht.
	 */
	public boolean isLast() {
		return pos == lcText.length - 1;
	}

	/**
	 * Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
	 * 
	 * @return Gibt zurueck ob der Zeiger nach dem letzten Zeichen steht.
	 */
	public boolean isAfterLast() {
		return pos >= lcText.length;
	}

	/**
	 * Gibt zurueck ob der Zeiger einen korrekten Index hat.
	 * 
	 * @return Gibt zurueck ob der Zeiger einen korrekten Index hat.
	 */
	public boolean isValidIndex() {
		return pos < lcText.length && pos > -1;
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

	public int indexOfNext(String str) {
		char[] carr = str.toCharArray();
		outer: for (int i = pos; i < lcText.length; i++) {
			if (lcText[i] == carr[0]) {
				// print.e("- "+lcText[i]);
				for (int y = 1; y < carr.length; y++) {
					// print.e("-- "+y);
					if (lcText.length <= i + y || lcText[i + y] != carr[y]) {
						// print.e("ggg");
						continue outer;
					}
				}
				return i;
			}
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
		return lcText.length;
	}

	/**
	 * Prueft ob das uebergebene Objekt diesem Objekt entspricht.
	 * 
	 * @param o Object zum vergleichen.
	 * @return Ist das uebergebene Objekt das selbe wie dieses.
	 */
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof SourceCode)) return false;
		return o.toString().equals(this.toString());
	}

	public boolean getWriteLog() {
		return writeLog;
	}

	public String getText() {
		return new String(text);
	}

	public String id() {
		return HashUtil.create64BitHashAsString(getText());
	}

	public int getDialect() {
		return dialect;
	}

	@Override
	public int hashCode() {
		return hash;
	}
}