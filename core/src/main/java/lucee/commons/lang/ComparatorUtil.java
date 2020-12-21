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
package lucee.commons.lang;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import lucee.runtime.type.comparator.NumberComparator;
import lucee.runtime.type.comparator.TextComparator;

public class ComparatorUtil {

	public static final int SORT_TYPE_TEXT = 1;
	public static final int SORT_TYPE_TEXT_NO_CASE = 2;
	public static final int SORT_TYPE_NUMBER = 3;

	public static Comparator toComparator(int sortType, boolean orderAsc, Locale l, Comparator defaultValue) {
		// check sortorder
		// text
		if (sortType == SORT_TYPE_TEXT) {
			if (l != null) return toCollator(l, Collator.IDENTICAL, orderAsc);
			return new TextComparator(orderAsc, false);
		}
		// text no case
		else if (sortType == SORT_TYPE_TEXT_NO_CASE) {
			if (l != null) return toCollator(l, Collator.TERTIARY, orderAsc);
			return new TextComparator(orderAsc, true);
		}
		// numeric
		else if (sortType == SORT_TYPE_NUMBER) {
			return new NumberComparator(orderAsc);
		}
		else {
			return defaultValue;
		}
	}

	private static Comparator toCollator(Locale l, int strength, boolean orderAsc) {
		Collator c = Collator.getInstance(l);
		c.setStrength(strength);
		c.setDecomposition(Collator.CANONICAL_DECOMPOSITION);
		if (!orderAsc) return new Inverter(c);
		return c;
	}

	private static class Inverter<T> implements Comparator<T> {

		private Collator c;

		public Inverter(Collator c) {
			this.c = c;
		}

		@Override
		public int compare(T o1, T o2) {
			return c.compare(o2, o1);
		}
	}
}