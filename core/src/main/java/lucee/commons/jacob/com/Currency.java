package lucee.commons.jacob.com;

/**
 * Most COM bridges use java.lang.Long as their Java data type for COM Currency data. This is
 * because COM currency is a 64 bit number where the last 4 digits represent the milli-cents. We
 * wanted to support 64 bit Long values for x64 platforms so that meant we wanted to map Java.LONG
 * to COM.LONG even though it only works for 64 bit platforms. The end result was we needed a new
 * representation for Money so we have this.
 * <p>
 * In the future, this should convert to and from BigDecimal or Double
 */

public class Currency
		// Added EJP 31/8/2023
		implements Comparable<Currency> {
	Long embeddedValue = null;

	/**
	 * constructor that takes a long already in COM representation
	 *
	 * @param newValue New value.
	 */
	public Currency(long newValue) {
		embeddedValue = Long.valueOf(newValue);
	}

	/**
	 * constructor that takes a String already in COM representation
	 *
	 * @param newValue New value.
	 */
	public Currency(String newValue) {
		embeddedValue = Long.valueOf(newValue);
	}

	/**
	 * Return the currency as a primitive long.
	 *
	 * @return the currency as a primitive long
	 */
	public long longValue() {
		return embeddedValue.longValue();
	}

	/**
	 * Getter to the inner Long so that {@link Comparable#compareTo} can work
	 *
	 * @return the embedded Long value
	 */
	protected Long getLongValue() {
		return embeddedValue;
	}

	/**
	 * Compares the values of two currencies
	 *
	 * @param anotherCurrency Currency object to compare to.
	 * @return the usual compareTo results
	 */
	@Override
	public int compareTo(Currency anotherCurrency) {
		return embeddedValue.compareTo(anotherCurrency.getLongValue());
	}

	// /**
	// * standard comparison
	// *
	// * @param o
	// * must be Currency or Long
	// * @return the usual compareTo results
	// * Deleted EJP 31/8/2023 when I added 'implements Comparable<Currency>.
	// * The compiler will still generate this method as a bridge method.
	// */
	// public int compareTo(Object o) {
	// if (o instanceof Currency) {
	// return compareTo((Currency) o);
	// } else if (o instanceof Long) {
	// return embeddedValue.compareTo((Long) o);
	// } else
	// throw new IllegalArgumentException(
	// "Can only compare to Long and Currency not "
	// + o.getClass().getName());
	// }

	/*
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
			// Modified EJP 31/8/2023
		}
		else if (o instanceof Currency && compareTo((Currency) o) == 0) {
			return true;
		}
		else {
			return false;
		}
	}
}