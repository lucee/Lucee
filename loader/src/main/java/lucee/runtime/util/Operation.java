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
package lucee.runtime.util;

import java.util.Date;

import lucee.runtime.exp.PageException;

/**
 * class to compare objects and primitive value types
 */
public interface Operation {

	/**
	 * compares two Objects
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return different of objects as int
	 * @throws PageException Page Exception
	 */
	public int compare(Object left, Object right) throws PageException;

	/**
	 * compares an Object with a String
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Object left, String right) throws PageException;

	/**
	 * compares an Object with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Object left, double right) throws PageException;

	/**
	 * compares an Object with a boolean
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Object left, boolean right) throws PageException;

	/**
	 * compares an Object with a Date
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Object left, Date right) throws PageException;

	/**
	 * compares a String with an Object
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(String left, Object right) throws PageException;

	/**
	 * compares a String with a String
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(String left, String right);

	/**
	 * compares a String with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(String left, double right);

	/**
	 * compares a String with a boolean
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(String left, boolean right);

	/**
	 * compares a String with a Date
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(String left, Date right) throws PageException;

	/**
	 * compares a double with an Object
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(double left, Object right) throws PageException;

	/**
	 * compares a double with a String
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(double left, String right);

	/**
	 * compares a double with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(double left, double right);

	/**
	 * compares a double with a boolean
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(double left, boolean right);

	/**
	 * compares a double with a Date
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(double left, Date right);

	/**
	 * compares a boolean with an Object
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(boolean left, Object right) throws PageException;

	/**
	 * compares a boolean with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(boolean left, double right);

	/**
	 * compares a boolean with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(boolean left, String right);

	/**
	 * compares a boolean with a boolean
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(boolean left, boolean right);

	/**
	 * compares a boolean with a Date
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(boolean left, Date right);

	/**
	 * compares a Date with an Object
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Date left, Object right) throws PageException;

	/**
	 * compares a Date with a String
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 * @throws PageException Page Exception
	 */
	public int compare(Date left, String right) throws PageException;

	/**
	 * compares a Date with a double
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(Date left, double right);

	/**
	 * compares a Date with a boolean
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(Date left, boolean right);

	/**
	 * compares a Date with a Date
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return difference as int
	 */
	public int compare(Date left, Date right);

	/**
	 * Method to compare to different values, return true of objects are same otherwise false
	 * 
	 * @param left left value to compare
	 * @param right right value to compare
	 * @param caseSensitive check case sensitive or not
	 * @return is same or not
	 * @throws PageException Page Exception
	 */
	public boolean equals(Object left, Object right, boolean caseSensitive) throws PageException;

	public boolean equalsComplexEL(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance);

	public boolean equalsComplex(Object left, Object right, boolean caseSensitive, boolean checkOnlyPublicAppearance) throws PageException;

	/**
	 * check if left is inside right
	 * 
	 * @param left string to check
	 * @param right substring to find in string
	 * @return return if substring has been found
	 * @throws PageException Page Exception
	 */
	public boolean ct(Object left, Object right) throws PageException;

	/**
	 * Equivalence: Return True if both operands are True or both are False. The EQV operator is the
	 * opposite of the XOR operator. For example, True EQV True is True, but True EQV False is False.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result of operation
	 * @throws PageException Page Exception
	 */
	public boolean eqv(Object left, Object right) throws PageException;

	/**
	 * Implication: The statement A IMP B is the equivalent of the logical statement "If A Then B." A
	 * IMP B is False only if A is True and B is False. It is True in all other cases.
	 * 
	 * @param left value to check
	 * @param right value to check
	 * @return result
	 * @throws PageException Page Exception
	 */
	public boolean imp(Object left, Object right) throws PageException;

	/**
	 * check if left is not inside right
	 * 
	 * @param left string to check
	 * @param right substring to find in string
	 * @return return if substring NOT has been found
	 * @throws PageException Page Exception
	 */
	public boolean nct(Object left, Object right) throws PageException;

	/**
	 * calculate the exponent of the left value
	 * 
	 * @param left value to get exponent from
	 * @param right exponent count
	 * @return return exponent value
	 * @throws PageException Page Exception
	 */
	public double exponent(Object left, Object right) throws PageException;

	/**
	 * concat to Strings
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return concatenated String
	 */
	public String concat(String left, String right);

	/**
	 * plus operation
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return result of the operations
	 */
	public double plus(double left, double right);

	/**
	 * minus operation
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return result of the operations
	 */
	public double minus(double left, double right);

	/**
	 * modulus operation
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return result of the operations
	 */
	public double modulus(double left, double right);

	/**
	 * divide operation
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return result of the operations
	 */
	public double divide(double left, double right);

	/**
	 * multiply operation
	 * 
	 * @param left Left Object
	 * @param right Right Object
	 * @return result of the operations
	 */
	public double multiply(double left, double right);

}