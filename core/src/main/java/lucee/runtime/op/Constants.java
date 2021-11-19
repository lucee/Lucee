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
package lucee.runtime.op;

/**
 * Constant Values
 */
public final class Constants {

	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

	/**
	 * Field <code>INTEGER_ZERO</code> equals Integer.valueOf(0)
	 */
	public static final Integer INTEGER_0 = Integer.valueOf(0);
	/**
	 * Field <code>INTEGER_ONE</code> equals Integer.valueOf(1)
	 */
	public static final Integer INTEGER_1 = Integer.valueOf(1);

	public static final Integer INTEGER_MINUS_ONE = Integer.valueOf(-1);
	/**
	 * Field <code>INTEGER_TWO</code> equals Integer.valueOf(8)
	 */
	public static final Integer INTEGER_2 = Integer.valueOf(2);
	/**
	 * Field <code>INTEGER_THREE</code> equals Integer.valueOf(3)
	 */
	public static final Integer INTEGER_3 = Integer.valueOf(3);
	/**
	 * Field <code>INTEGER_FOUR</code> equals Integer.valueOf(4)
	 */
	public static final Integer INTEGER_4 = Integer.valueOf(4);
	/**
	 * Field <code>INTEGER_FIVE</code> equals Integer.valueOf(5)
	 */
	public static final Integer INTEGER_5 = Integer.valueOf(5);
	/**
	 * Field <code>INTEGER_SIX</code> equals Integer.valueOf(6)
	 */
	public static final Integer INTEGER_6 = Integer.valueOf(6);
	/**
	 * Field <code>INTEGER_SEVEN</code> equals Integer.valueOf(7)
	 */
	public static final Integer INTEGER_7 = Integer.valueOf(7);
	/**
	 * Field <code>INTEGER_EIGHT</code> equals Integer.valueOf(8)
	 */
	public static final Integer INTEGER_8 = Integer.valueOf(8);
	/**
	 * Field <code>INTEGER_NINE</code> equals Integer.valueOf(9)
	 */
	public static final Integer INTEGER_9 = Integer.valueOf(9);
	/**
	 * Field <code>INTEGER_NINE</code> equals Integer.valueOf(9)
	 */
	public static final Integer INTEGER_10 = Integer.valueOf(10);
	public static final Integer INTEGER_11 = Integer.valueOf(11);
	public static final Integer INTEGER_12 = Integer.valueOf(12);

	public static final short SHORT_VALUE_ZERO = (short) 0;
	public static final Short SHORT_ZERO = Short.valueOf((short) 0);
	public static final Long LONG_ZERO = Long.valueOf(0);
	public static final Double DOUBLE_ZERO = new Double(0);

	/**
	 * return an Integer object with same value
	 * 
	 * @param i
	 * @return Integer Object
	 * @deprecated use Integer.valueOf() instead
	 */
	@Deprecated
	public static Integer Integer(int i) {
		// if(i>-1 && i<100) return INTEGER[i];
		return Integer.valueOf(i);
	}

	/**
	 * return a Boolean object with same value
	 * 
	 * @param b
	 * @return Boolean Object
	 * @deprecated use Boolean.valueOf() instead
	 */
	@Deprecated
	public static Boolean Boolean(boolean b) {
		return b ? Boolean.TRUE : Boolean.FALSE;
	}

}