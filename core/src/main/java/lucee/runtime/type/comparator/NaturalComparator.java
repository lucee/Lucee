/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
package lucee.runtime.type.comparator;

import java.util.Comparator;

import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

/**
 * comparator implementation for natural (human) sorting, a run of digits is compared by its numeric
 * value, so "file2" is sorted before "file10" (LDEV-1784).
 * <ul>
 * <li>digits are the ASCII digits 0-9, a run of digits has no size limit (no overflow)</li>
 * <li>everything else is compared character by character, with or without regard to case</li>
 * <li>if two digit runs have the same value, they are ordered by the number of leading zeros (fewer
 * zeros first), but only when the rest of the text is equal, so "a01b" is still sorted before
 * "a2"</li>
 * <li>if one text is the start of the other, the shorter one is sorted first</li>
 * </ul>
 */
public final class NaturalComparator implements Comparator {

	private final boolean isAsc;
	private final boolean ignoreCase;

	/**
	 * constructor of the class
	 * 
	 * @param isAsc ascending or descending
	 * @param ignoreCase ignore case or not
	 */
	public NaturalComparator(boolean isAsc, boolean ignoreCase) {
		this.isAsc = isAsc;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public int compare(Object oLeft, Object oRight) {
		try {
			if (isAsc) return compare(Caster.toString(oLeft), Caster.toString(oRight), ignoreCase);
			return compare(Caster.toString(oRight), Caster.toString(oLeft), ignoreCase);
		}
		catch (PageException e) {
			throw new PageRuntimeException(new ExpressionException("can only sort arrays with simple values", e.getMessage()));
		}
	}

	/**
	 * natural compare of 2 strings in ascending order
	 */
	public static int compare(String left, String right, boolean ignoreCase) {
		final int lenL = left.length();
		final int lenR = right.length();
		int l = 0, r = 0;
		// result for digit runs with the same value but a different number of leading zeros
		int zeros = 0;

		while (l < lenL && r < lenR) {
			char cl = left.charAt(l);
			char cr = right.charAt(r);

			if (isDigit(cl) && isDigit(cr)) {
				// end of the digit runs
				int endL = l + 1;
				while (endL < lenL && isDigit(left.charAt(endL)))
					endL++;
				int endR = r + 1;
				while (endR < lenR && isDigit(right.charAt(endR)))
					endR++;

				// skip leading zeros
				int startL = l;
				while (startL < endL - 1 && left.charAt(startL) == '0')
					startL++;
				int startR = r;
				while (startR < endR - 1 && right.charAt(startR) == '0')
					startR++;

				// more significant digits is the bigger number
				int sigL = endL - startL;
				int sigR = endR - startR;
				if (sigL != sigR) return sigL < sigR ? -1 : 1;

				// same amount of digits, the first difference decides
				for (int i = 0; i < sigL; i++) {
					char dl = left.charAt(startL + i);
					char dr = right.charAt(startR + i);
					if (dl != dr) return dl < dr ? -1 : 1;
				}

				// same value, remember the difference in leading zeros for the case the rest is equal as well
				if (zeros == 0) {
					int zerosL = startL - l;
					int zerosR = startR - r;
					if (zerosL != zerosR) zeros = zerosL < zerosR ? -1 : 1;
				}
				l = endL;
				r = endR;
				continue;
			}

			if (cl != cr) {
				if (ignoreCase) {
					cl = fold(cl);
					cr = fold(cr);
				}
				if (cl != cr) return cl < cr ? -1 : 1;
			}
			l++;
			r++;
		}

		// one string is the start of the other, the shorter first
		boolean restL = l < lenL;
		boolean restR = r < lenR;
		if (restL != restR) return restL ? 1 : -1;
		return zeros;
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	// same folding as String.compareToIgnoreCase
	private static char fold(char c) {
		return Character.toLowerCase(Character.toUpperCase(c));
	}

	@Override
	public String toString() {
		return "NaturalComparator:isAsc" + isAsc + ";ignoreCase:" + ignoreCase;
	}
}
