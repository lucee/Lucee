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
package lucee.runtime.op.validators;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.op.Caster;

/**
 * logic to determine if a credit card number is valid. no GUI, just calculation.
 *
 */
public final class ValidateCreditCard {

	/**
	 * enum for Amex
	 */
	static final int AMEX = 1;

	/**
	 * true if debugging output wanted
	 */
	static final boolean DEBUGGING = true;

	/**
	 * enum for Diner's club
	 */
	static final int DINERS = 2; // includes Carte Blanche

	/**
	 * enum for Discover Card
	 */
	static final int DISCOVER = 3;

	/**
	 * enum for Enroute
	 */
	static final int ENROUTE = 4;

	/**
	 * enum for JCB
	 */
	static final int JCB = 5;

	/**
	 * enum for Mastercard
	 */
	static final int MASTERCARD = 6;

	/**
	 * enum for insufficient digits
	 */
	static final int NOT_ENOUGH_DIGITS = -3;

	/**
	 * enum for too many digits
	 */
	static final int TOO_MANY_DIGITS = -2;

	/**
	 * enum for unknown vendor
	 */
	static final int UNKNOWN_VENDOR = -1;

	/**
	 * enum for Visa
	 */
	static final int VISA = 7;

	/**
	 * Used to speed up findMatchingRange by caching the last hit.
	 */
	private static int cachedLastFind = 0;

	/**
	 * ranges of credit card number that belong to each company. buildRanges initialises.
	 */
	private static LCR[] ranges;

	/**
	 * used by vendorToString to describe the enumerations
	 */
	private static final String[] vendors = { "Error: not enough digits", "Error: too many digits", "Error: unknown credit card company", "dummy", "Amex", "Diners/Carte Blanche",
			"Discover", "enRoute", "JCB", "MasterCard", "Visa" };

	// -------------------------- STATIC METHODS --------------------------

	static {
		// now that all enum constants defined
		buildRanges();
	}

	/**
	 * build table of which ranges of credit card number belong to which vendor
	 */
	private static void buildRanges() {
		// careful, no lead zeros allowed
		// low high len vendor mod-10?
		ranges = new LCR[] { new LCR(4000000000000L, 4999999999999L, 13, VISA, true), new LCR(30000000000000L, 30599999999999L, 14, DINERS, true),
				new LCR(36000000000000L, 36999999999999L, 14, DINERS, true), new LCR(38000000000000L, 38999999999999L, 14, DINERS, true),
				new LCR(180000000000000L, 180099999999999L, 15, JCB, true), new LCR(201400000000000L, 201499999999999L, 15, ENROUTE, false),
				new LCR(213100000000000L, 213199999999999L, 15, JCB, true), new LCR(214900000000000L, 214999999999999L, 15, ENROUTE, false),
				new LCR(340000000000000L, 349999999999999L, 15, AMEX, true), new LCR(370000000000000L, 379999999999999L, 15, AMEX, true),
				new LCR(3000000000000000L, 3999999999999999L, 16, JCB, true), new LCR(4000000000000000L, 4999999999999999L, 16, VISA, true),
				new LCR(5100000000000000L, 5599999999999999L, 16, MASTERCARD, true), new LCR(6011000000000000L, 6011999999999999L, 16, DISCOVER, true) }; // end table
		// initialisation
	}

	/**
	 * Finds a matching range in the ranges array for a given creditCardNumber.
	 *
	 * @param creditCardNumber number on card.
	 *
	 * @return index of matching range, or NOT_ENOUGH_DIGITS or UNKNOWN_VENDOR on failure.
	 */
	protected static int findMatchingRange(long creditCardNumber) {
		if (creditCardNumber < 1000000000000L) {
			return NOT_ENOUGH_DIGITS;
		}
		if (creditCardNumber > 9999999999999999L) {
			return TOO_MANY_DIGITS;
		}
		// check the cached index first, where we last found a number.
		if (ranges[cachedLastFind].low <= creditCardNumber && creditCardNumber <= ranges[cachedLastFind].high) {
			return cachedLastFind;
		}
		for (int i = 0; i < ranges.length; i++) {
			if (ranges[i].low <= creditCardNumber && creditCardNumber <= ranges[i].high) {
				// we have a match
				cachedLastFind = i;
				return i;
			}
		} // end for
		return UNKNOWN_VENDOR;
	} // end findMatchingRange

	public static boolean isValid(String strCreditCardNumber) {
		return isValid(toLong(strCreditCardNumber, 0L));
	}

	private static long toLong(String strCreditCardNumber, long defaultValue) {

		if (strCreditCardNumber == null) return defaultValue;

		// strip commas, spaces, + AND -
		StringBuffer sb = new StringBuffer(strCreditCardNumber.length());
		for (int i = 0; i < strCreditCardNumber.length(); i++) {
			char c = strCreditCardNumber.charAt(i);
			if (!StringUtil.isWhiteSpace(c) && c != ',' && c != '+' && c != '-') sb.append(c);
		}
		long num = (long) Caster.toDoubleValue(sb.toString(), 0L);
		if (num == 0L) return defaultValue;

		return num;
	}

	/**
	 * Determine if the credit card number is valid, i.e. has good prefix and checkdigit. Does _not_ ask
	 * the credit card company if this card has been issued or is in good standing.
	 *
	 * @param creditCardNumber number on card.
	 *
	 * @return true if card number is good.
	 */
	public static boolean isValid(long creditCardNumber) {
		int i = findMatchingRange(creditCardNumber);
		if (i < 0) {
			return false;
		}
		// else {
		// we have a match
		if (ranges[i].hasCheckDigit) {
			// there is a checkdigit to be validated
			/*
			 * Manual method MOD 10 checkdigit 706-511-227 7 0 6 5 1 1 2 2 7 2 * 2 * 2 * 2
			 * --------------------------------- 7 + 0 + 6 +1+0+ 1 + 2 + 2 + 4 = 23 23 MOD 10 = 3 10 - 3 = 7 --
			 * the check digit Note digits of multiplication results must be added before sum. Computer Method
			 * MOD 10 checkdigit 706-511-227 7 0 6 5 1 1 2 2 7 Z Z Z Z --------------------------------- 7 + 0 +
			 * 6 + 1 + 1 + 2 + 2 + 4 + 7 = 30 30 MOD 10 had better = 0
			 */
			long number = creditCardNumber;
			int checksum = 0;
			// work right to left
			for (int place = 0; place < 16; place++) {
				int digit = (int) (number % 10);
				number /= 10;
				if ((place & 1) == 0) {
					// even position (0-based from right), just add digit
					checksum += digit;
				}
				else { // odd position (0-based from right), must double
						// and add
					checksum += z(digit);
				}
				if (number == 0) {
					break;
				}
			} // end for
				// good checksum should be 0 mod 10
			return (checksum % 10) == 0;
		}
		return true; // no checksum needed

		// } // end if have match
	} // end isValid

	/**
	 * Determine the credit card company. Does NOT validate checkdigit.
	 *
	 * @param creditCardNumber number on card.
	 *
	 * @return credit card vendor enumeration constant.
	 */
	public static int recognizeVendor(long creditCardNumber) {
		int i = findMatchingRange(creditCardNumber);
		if (i < 0) {
			return i;
		}
		return ranges[i].vendor;

	} // end recognize

	// From http://www.icverify.com/
	// Vendor Prefix len checkdigit
	// MASTERCARD 51-55 16 mod 10
	// VISA 4 13, 16 mod 10
	// AMEX 34,37 15 mod 10
	// Diners Club/
	// Carte Blanche
	// 300-305 14
	// 36 14
	// 38 14 mod 10
	// Discover 6011 16 mod 10
	// enRoute 2014 15
	// 2149 15 any
	// JCB 3 16 mod 10
	// JCB 2131 15
	// 1800 15 mod 10

	public static String toCreditcard(String strCreditCardNumber) throws ExpressionException {
		long number = toLong(strCreditCardNumber, -1);
		if (number == -1) throw new ExpressionException("invalid creditcard number [" + strCreditCardNumber + "]");
		return toPrettyString(number);
	}

	public static String toCreditcard(String strCreditCardNumber, String defaultValue) {
		long number = toLong(strCreditCardNumber, -1);
		if (number == -1) return defaultValue;
		return toPrettyString(number);
	}

	/**
	 * Convert a creditCardNumber as long to a formatted String. Currently it breaks 16-digit numbers
	 * into groups of 4.
	 *
	 * @param creditCardNumber number on card.
	 *
	 * @return String representation of the credit card number.
	 */
	private static String toPrettyString(long creditCardNumber) {
		String plain = Long.toString(creditCardNumber);
		// int i = findMatchingRange(creditCardNumber);
		int length = plain.length();

		switch (length) {
		case 12:
			// 12 pattern 3-3-3-3
			return plain.substring(0, 3) + ' ' + plain.substring(3, 6) + ' ' + plain.substring(6, 9) + ' ' + plain.substring(9, 12);

		case 13:
			// 13 pattern 4-3-3-3
			return plain.substring(0, 4) + ' ' + plain.substring(4, 7) + ' ' + plain.substring(7, 10) + ' ' + plain.substring(10, 13);

		case 14:
			// 14 pattern 2-4-4-4
			return plain.substring(0, 2) + ' ' + plain.substring(2, 6) + ' ' + plain.substring(6, 10) + ' ' + plain.substring(10, 14);

		case 15:
			// 15 pattern 3-4-4-4
			return plain.substring(0, 3) + ' ' + plain.substring(3, 7) + ' ' + plain.substring(7, 11) + ' ' + plain.substring(11, 15);

		case 16:
			// 16 pattern 4-4-4-4
			return plain.substring(0, 4) + ' ' + plain.substring(4, 8) + ' ' + plain.substring(8, 12) + ' ' + plain.substring(12, 16);

		case 17:
			// 17 pattern 1-4-4-4-4
			return plain.substring(0, 1) + ' ' + plain.substring(1, 5) + ' ' + plain.substring(5, 9) + ' ' + plain.substring(9, 13) + ' ' + plain.substring(13, 17);

		default:
			// 0..11, 18+ digits long
			// plain
			return plain;
		} // end switch
	} // end toPrettyString

	/**
	 * Converts a vendor index enumeration to the equivalent words. It will trigger an
	 * ArrayIndexOutOfBoundsException if you feed it an illegal value.
	 *
	 * @param vendorEnum e.g. AMEX, UNKNOWN_VENDOR, TOO_MANY_DIGITS
	 *
	 * @return equivalent string in words, e.g. "Amex" "Error: unknown vendor".
	 */
	public static String vendorToString(int vendorEnum) {
		return vendors[vendorEnum - NOT_ENOUGH_DIGITS];
	} // end vendorToString

	/**
	 * used in computing checksums, doubles and adds resulting digits.
	 *
	 * @param digit the digit to be doubled, and digit summed.
	 *
	 * @return // 0->0 1->2 2->4 3->6 4->8 5->1 6->3 7->5 8->7 9->9
	 */
	private static int z(int digit) {
		if (digit == 0) {
			return 0;
		}
		return (digit * 2 - 1) % 9 + 1;
	}
} // end class CreditCard

/**
 * Describes a single Legal Card Range
 */
class LCR {

	// ------------------------------ FIELDS ------------------------------

	/**
	 * does this range have a MOD-10 checkdigit?
	 */
	public boolean hasCheckDigit;

	/**
	 * low and high bounds on range covered by this vendor
	 */
	public long high;

	/**
	 * how many digits in this type of number.
	 */
	public int length;

	/**
	 * low bounds on range covered by this vendor
	 */
	public long low;

	/**
	 * enumeration credit card service
	 */
	public int vendor;

	// --------------------------- CONSTRUCTORS ---------------------------

	/**
	 * public constructor
	 *
	 * @param low lowest credit card number in range.
	 * @param high highest credit card number in range
	 * @param length length of number in digits
	 * @param vendor enum constant for vendor
	 * @param hasCheckDigit true if uses mod 10 check digit.
	 */
	public LCR(long low, long high, int length, int vendor, boolean hasCheckDigit) {
		this.low = low;
		this.high = high;
		this.length = length;
		this.vendor = vendor;
		this.hasCheckDigit = hasCheckDigit;
	} // end public constructor
} // end class LCR