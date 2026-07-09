/*
 * Copyright (c) 1999-2004 Sourceforge JACOB Project.
 * All rights reserved. Originator: Dan Adler (http://danadler.com).
 * Get more information about JACOB at http://sourceforge.net/projects/jacob-project
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
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package lucee.commons.jacob.com;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * The multi-format data type used for all call backs and most communications between Java and COM.
 * It provides a single class that can handle all data types.
 * <p>
 * Just loading this class creates 3 variants that get added to the ROT
 * <p>
 * PROPVARIANT introduces new types so eventually Variant will need to be upgraded to support
 * PropVariant types. http://blogs.msdn.com/benkaras/archive/2006/09/13/749962.aspx
 * <p>
 * This object no longer implements Serializable because serialization is broken (and has been since
 * 2000/xp). The underlying marshalling/unmarshalling code is broken in the JNI layer.
 */
public class Variant extends JacobObject {

	/**
	 * Use this constant for optional parameters
	 */
	public final static lucee.commons.jacob.com.Variant DEFAULT;

	/**
	 * Same than {@link #DEFAULT}
	 */
	public final static lucee.commons.jacob.com.Variant VT_MISSING;

	/**
	 * Use for true/false variant parameters
	 */
	public final static lucee.commons.jacob.com.Variant VT_TRUE = new lucee.commons.jacob.com.Variant(true);

	/**
	 * Use for true/false variant parameters
	 */
	public final static lucee.commons.jacob.com.Variant VT_FALSE = new lucee.commons.jacob.com.Variant(false);

	/** variant's type is empty : equivalent to VB Nothing and VT_EMPTY */
	public static final short VariantEmpty = 0;

	/** variant's type is null : equivalent to VB Null and VT_NULL */
	public static final short VariantNull = 1;

	/** variant's type is short VT_I2 */
	public static final short VariantShort = 2;

	/** variant's type is int VT_I4, a Long in VC */
	public static final short VariantInt = 3;

	/** variant's type is float VT_R4 */
	public static final short VariantFloat = 4;

	/** variant's type is double VT_R8 */
	public static final short VariantDouble = 5;

	/** variant's type is currency VT_CY */
	public static final short VariantCurrency = 6;

	/** variant's type is date VT_DATE */
	public static final short VariantDate = 7;

	/** variant's type is string also known as VT_BSTR */
	public static final short VariantString = 8;

	/** variant's type is dispatch VT_DISPATCH */
	public static final short VariantDispatch = 9;

	/** variant's type is error VT_ERROR */
	public static final short VariantError = 10;

	/** variant's type is boolean VT_BOOL */
	public static final short VariantBoolean = 11;

	/** variant's type is variant it encapsulate another variant VT_VARIANT */
	public static final short VariantVariant = 12;

	/** variant's type is object VT_UNKNOWN */
	public static final short VariantObject = 13;

	/** variant's type is object VT_DECIMAL */
	public static final short VariantDecimal = 14;

	// VT_I1 = 16

	/** variant's type is byte VT_UI1 This is an UNSIGNED byte */
	public static final short VariantByte = 17;

	// VT_UI2 = 18
	// VT_UI4 = 19

	/**
	 * variant's type is 64 bit long integer VT_I8 - not yet implemented in Jacob because we have to
	 * decide what to do with Currency and because its only supported on XP and later. No win2k, NT or
	 * 2003 server.
	 */
	public static final short VariantLongInt = 20;

	// VT_UI8 = 21
	// VT_INT = 22
	// VT_UNIT = 23
	// VT_VOID = 24
	// VT_HRESULT = 25

	/**
	 * This value is for reference only and is not to be used by any callers
	 */
	public static final short VariantPointer = 26;

	// VT_SAFEARRAY = 27
	// VT_CARRARY = 28
	// VT_USERDEFINED = 29

	/** what is this? VT_TYPEMASK &amp;&amp; VT_BSTR_BLOB 0xfff */
	public static final short VariantTypeMask = 4095;

	/** variant's type is array VT_ARRAY 0x2000 */
	public static final short VariantArray = 8192;

	/** variant's type is a reference (to IDispatch?) VT_BYREF 0x4000 */
	public static final short VariantByref = 16384;

	/*
	 * Do the run time definition of DEFAULT and MISSING. Have to use static block because of the way
	 * the initialization is done via two calls instead of just a constructor for this type.
	 */
	static {
		lucee.commons.jacob.com.Variant vtMissing = new lucee.commons.jacob.com.Variant();
		vtMissing.putVariantNoParam();
		DEFAULT = vtMissing;
		VT_MISSING = vtMissing;
	}

	/**
	 * Pointer to MS struct.
	 */
	long m_pVariant = 0;

	/**
	 * public constructor, initializes and sets type to VariantEmpty
	 */
	public Variant() {
		this(null, false);
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(boolean in) {
		this(Boolean.valueOf(in));
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(byte in) {
		this(Byte.valueOf(in));
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(double in) {
		this(Double.valueOf(in));
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(float in) {
		this(Float.valueOf(in));
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(int in) {
		this(Integer.valueOf(in));
	};

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(long in) {
		this(Long.valueOf(in));
	}

	/**
	 * Convenience constructor that calls the main one with a byRef value of false
	 *
	 * @param in object to be made into variant
	 */
	public Variant(Object in) {
		this(in, false);
	}

	/**
	 * Constructor that accepts the data object and information about whether this is by reference or
	 * not. It calls the JavaVariantConverter to actually push the data into the newly created Variant.
	 *
	 * @param pValueObject The value object that will pushed down into windows memory. A null object
	 *            sets this to "empty"
	 * @param fByRef
	 */
	public Variant(Object pValueObject, boolean fByRef) {
		init();
		VariantUtilities.populateVariant(this, pValueObject, fByRef);
	}

	/**
	 * Constructor that accepts a primitive rather than an object
	 *
	 * @param in
	 */
	public Variant(short in) {
		this(Short.valueOf(in));
	}

	/**
	 * Cover for native method so we can cover it.
	 * <p>
	 * This cannot convert an object to a byRef. It can convert from byref to not byref
	 *
	 * @param in type to convert this variant too
	 * @return Variant returns this same object so folks can change when replacing calls toXXX() with
	 *         changeType().getXXX()
	 */
	public Variant changeType(short in) {
		changeVariantType(in);
		return this;
	}

	/**
	 * Converts variant to the passed in type by converting the underlying windows variant structure.
	 * private so folks use public java method
	 *
	 * @param in the desired resulting type
	 */
	private native void changeVariantType(short in);

	/**
	 * this returns null
	 *
	 * @return ?? comment says null?
	 */
	@Override
	public native Object clone();

	/**
	 * @deprecated No longer used
	 * @return null !
	 */
	@Deprecated
	public native Variant cloneIndirect();

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#finalize()
	 */
	@SuppressWarnings("removal")
	@Override
	protected void finalize() {
		safeRelease();
	}

	/**
	 *
	 * @return returns the value as a boolean, throws an exception if its not.
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public boolean getBoolean() {
		if (this.getvt() == VariantBoolean) {
			return getVariantBoolean();
		}
		throw new IllegalStateException("getBoolean() only legal on Variants of type VariantBoolean, not " + this.getvt());
	}

	/**
	 * public cover for native method
	 *
	 * @return the boolean from a booleanRef
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public boolean getBooleanRef() {
		if ((this.getvt() & VariantTypeMask) == VariantBoolean && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantBooleanRef();
		}
		throw new IllegalStateException("getBooleanRef() only legal on byRef Variants of type VariantBoolean, not " + this.getvt());
	}

	/**
	 *
	 * @return returns the value as a boolean, throws an exception if its not.
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public byte getByte() {
		if (this.getvt() == VariantByte) {
			return getVariantByte();
		}
		throw new IllegalStateException("getByte() only legal on Variants of type VariantByte, not " + this.getvt());
	}

	/**
	 * public cover for native method
	 *
	 * @return the byte from a booleanRef
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public byte getByteRef() {
		if ((this.getvt() & VariantTypeMask) == VariantByte && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantByteRef();
		}
		throw new IllegalStateException("getByteRef() only legal on byRef Variants of type VariantByte, not " + this.getvt());
	}

	/**
	 * MS Currency objects are 64 bit fixed point numbers with 15 digits to the left and 4 to the right
	 * of the decimal place.
	 *
	 * @return returns the currency value as a long, throws exception if not a currency type..
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public Currency getCurrency() {
		if (this.getvt() == VariantCurrency) {
			return new Currency(getVariantCurrency());
		}
		throw new IllegalStateException("getCurrency() only legal on Variants of type VariantCurrency, not " + this.getvt());
	}

	/**
	 * MS Currency objects are 64 bit fixed point numbers with 15 digits to the left and 4 to the right
	 * of the decimal place.
	 *
	 * @return returns the currency value as a long, throws exception if not a currency type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public Currency getCurrencyRef() {
		if ((this.getvt() & VariantTypeMask) == VariantCurrency && (this.getvt() & VariantByref) == VariantByref) {
			return new Currency(getVariantCurrencyRef());
		}
		throw new IllegalStateException("getCurrencyRef() only legal on byRef Variants of type VariantCurrency, not " + this.getvt());
	}

	/**
	 * @return double return the date (as a double) value held in this variant (fails on other types?)
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public double getDate() {
		if (this.getvt() == VariantDate) {
			return getVariantDate();
		}
		throw new IllegalStateException("getDate() only legal on Variants of type VariantDate, not " + this.getvt());
	}

	/**
	 *
	 * @return returns the date value as a double, throws exception if not a date type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public double getDateRef() {
		if ((this.getvt() & VariantTypeMask) == VariantDate && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantDateRef();
		}
		throw new IllegalStateException("getDateRef() only legal on byRef Variants of type VariantDate, not " + this.getvt());
	}

	/**
	 * return the BigDecimal value held in this variant (fails on other types)
	 *
	 * @return BigDecimal
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public BigDecimal getDecimal() {
		if (this.getvt() == VariantDecimal) {
			return (BigDecimal) (getVariantDec());
		}
		throw new IllegalStateException("getDecimal() only legal on Variants of type VariantDecimal, not " + this.getvt());
	}

	/**
	 * return the BigDecimal value held in this variant (fails on other types)
	 *
	 * @return BigDecimal
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public BigDecimal getDecimalRef() {
		if ((this.getvt() & VariantTypeMask) == VariantDecimal && (this.getvt() & VariantByref) == VariantByref) {
			return (BigDecimal) (getVariantDecRef());
		}
		throw new IllegalStateException("getDecimalRef() only legal on byRef Variants of type VariantDecimal, not " + this.getvt());
	}

	/**
	 * cover for {@link #toDispatch()} This method now matches other getXXX() methods. It throws an
	 * IllegalStateException if the object is not of type VariantDispatch
	 *
	 * @return this object as a dispatch
	 * @throws IllegalStateException if wrong variant type
	 */
	public Dispatch getDispatch() {
		if (this.getvt() == VariantDispatch) {
			return toDispatch();
		}
		throw new IllegalStateException("getDispatch() only legal on Variants of type VariantDispatch, not " + this.getvt());
	}

	/**
	 * Dispatch and dispatchRef are treated the same This is just a cover for toDispatch() with a flag
	 * check
	 *
	 * @return the results of toDispatch()
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public Dispatch getDispatchRef() {
		if ((this.getvt() & VariantTypeMask) == VariantDispatch && (this.getvt() & VariantByref) == VariantByref) {
			return toDispatch();
		}
		throw new IllegalStateException("getDispatchRef() only legal on byRef Variants of type VariantDispatch, not " + this.getvt());
	}

	/**
	 * @return double return the double value held in this variant (fails on other types?)
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public double getDouble() {
		if (this.getvt() == VariantDouble) {
			return getVariantDouble();
		}
		throw new IllegalStateException("getDouble() only legal on Variants of type VariantDouble, not " + this.getvt());
	}

	/**
	 *
	 * @return returns the double value, throws exception if not a Double type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public double getDoubleRef() {
		if ((this.getvt() & VariantTypeMask) == VariantDouble && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantDoubleRef();
		}
		throw new IllegalStateException("getDoubleRef() only legal on byRef Variants of type VariantDouble, not " + this.getvt());
	}

	/**
	 * Pointless method that was put here so that putEmpty() has a get method. This would have returned
	 * null if the value was VT_EMPTY or if it wasn't so it would have always returned the same value.
	 *
	 * @deprecated method never did anything
	 */
	@Deprecated
	public void getEmpty() {}

	/**
	 * @return double return the error value held in this variant (fails on other types?)
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public int getError() {
		if (this.getvt() == VariantError) {
			return getVariantError();
		}
		throw new IllegalStateException("getError() only legal on Variants of type VariantError, not " + this.getvt());
	}

	/**
	 *
	 * @return returns the error value as an int, throws exception if not a Error type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public int getErrorRef() {
		if ((this.getvt() & VariantTypeMask) == VariantError && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantErrorRef();
		}
		throw new IllegalStateException("getErrorRef() only legal on byRef Variants of type VariantError, not " + this.getvt());
	}

	/**
	 * @return returns the value as a float if the type is of type float
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public float getFloat() {
		if (this.getvt() == VariantFloat) {
			return getVariantFloat();
		}
		throw new IllegalStateException("getFloat() only legal on Variants of type VariantFloat, not " + this.getvt());
	}

	/**
	 *
	 * @return returns the float value, throws exception if not a Float type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public float getFloatRef() {
		if ((this.getvt() & VariantTypeMask) == VariantFloat && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantFloatRef();
		}
		throw new IllegalStateException("getFloatRef() only legal on byRef Variants of type VariantFloat, not " + this.getvt());
	}

	/**
	 * return the int value held in this variant if it is an int or a short. Throws for other types.
	 *
	 * @return int contents of the windows memory
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public int getInt() {
		if (this.getvt() == VariantInt) {
			return getVariantInt();
		}
		else if (this.getvt() == VariantShort) {
			return getVariantShort();
		}
		else {
			throw new IllegalStateException("getInt() only legal on Variants of type VariantInt, not " + this.getvt());
		}
	}

	/**
	 * get the content of this variant as an int
	 *
	 * @return int
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public int getIntRef() {
		if ((this.getvt() & VariantTypeMask) == VariantInt && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantIntRef();
		}
		throw new IllegalStateException("getIntRef() only legal on byRef Variants of type VariantInt, not " + this.getvt());
	}

	/**
	 * returns the windows time contained in this Variant to a Java Date. should return null if this is
	 * not a date Variant SF 959382
	 *
	 * @return java.util.Date returns the date if this is a VariantDate != 0, null if it is a
	 *         VariantDate == 0 and throws an IllegalStateException if this isn't a date.
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public Date getJavaDate() {
		Date returnDate = null;
		if (getvt() == VariantDate) {
			double windowsDate = getDate();
			if (windowsDate != 0) {
				returnDate = DateUtilities.convertWindowsTimeToDate(getDate());
			}
		}
		else {
			throw new IllegalStateException("getJavaDate() only legal on Variants of type VariantDate, not " + this.getvt());
		}
		return returnDate;
	}

	/**
	 * returns the windows time contained in this Variant to a Java Date should return null if this is
	 * not a date reference Variant SF 959382
	 *
	 * @return java.util.Date
	 */
	public Date getJavaDateRef() {
		double windowsDate = getDateRef();
		if (windowsDate == 0) {
			return null;
		}
		return DateUtilities.convertWindowsTimeToDate(windowsDate);
	}

	/**
	 * 64 bit Longs only available on x64. 64 bit long support added 1.14
	 *
	 * @return returns the value as a long, throws exception if not a Long type..
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public long getLong() {
		if (this.getvt() == VariantLongInt) {
			return getVariantLong();
		}
		throw new IllegalStateException("getLong() only legal on Variants of type VariantLongInt, not " + this.getvt());
	}

	/**
	 * 64 bit Longs only available on x64. 64 bit long support added 1.14
	 *
	 * @return returns the value as a long, throws exception if not a long type
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public long getLongRef() {
		if ((this.getvt() & VariantTypeMask) == VariantLongInt && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantLongRef();
		}
		throw new IllegalStateException("getLongRef() only legal on byRef Variants of type VariantLongInt, not " + this.getvt());
	}

	/**
	 * This method would have returned null if the type was VT_NULL. But because we return null if the
	 * data is not of the right type, this method should have always returned null
	 *
	 * @deprecated method never did anything
	 */
	@Deprecated
	public void getNull() {}

	/**
	 * return the int value held in this variant (fails on other types?)
	 *
	 * @return int
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public short getShort() {
		if (this.getvt() == VariantShort) {
			return getVariantShort();
		}
		throw new IllegalStateException("getShort() only legal on Variants of type VariantShort, not " + this.getvt());
	}

	/**
	 * get the content of this variant as an int
	 *
	 * @return int
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public short getShortRef() {
		if ((this.getvt() & VariantTypeMask) == VariantShort && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantShortRef();
		}
		throw new IllegalStateException("getShortRef() only legal on byRef Variants of type VariantShort, not " + this.getvt());
	}

	/**
	 *
	 * @return string contents of the variant, null if is of type null or empty
	 * @throws IllegalStateException if this variant is not of type String
	 */
	public String getString() {
		if (getvt() == Variant.VariantString) {
			return getVariantString();
		}
		else if (getvt() == Variant.VariantEmpty) {
			return null;
		}
		else if (getvt() == Variant.VariantNull) {
			return null;
		}
		else {
			throw new IllegalStateException("getString() only legal on Variants of type VariantString, not " + this.getvt());
		}
	}

	/**
	 * gets the content of the variant as a string ref
	 *
	 * @return String retrieved from the COM area.
	 * @throws IllegalStateException if variant is not of the requested type
	 */
	public String getStringRef() {
		if ((this.getvt() & VariantTypeMask) == VariantString && (this.getvt() & VariantByref) == VariantByref) {
			return getVariantStringRef();
		}
		throw new IllegalStateException("getStringRef() only legal on byRef Variants of type VariantString, not " + this.getvt());
	}

	/**
	 * Used to get the value from a windows type of VT_VARIANT or a jacob Variant type of
	 * VariantVariant. Added 1.12 pre 6 - VT_VARIANT support is at an alpha level
	 *
	 * @return Object a java Object that represents the content of the enclosed Variant
	 */
	public Object getVariant() {
		if ((this.getvt() & VariantTypeMask) == VariantVariant && (this.getvt() & VariantByref) == VariantByref) {
			if (JacobObject.isDebugEnabled()) {
				JacobObject.debug("About to call getVariantVariant()");
			}
			Variant enclosedVariant = new Variant();
			long enclosedVariantMemory = getVariantVariant();
			enclosedVariant.m_pVariant = enclosedVariantMemory;
			Object enclosedVariantAsJava = enclosedVariant.toJavaObject();
			// zero out the reference to the underlying windows memory so that
			// it is still only owned in one place by one java object
			// (this object of type VariantVariant)
			// enclosedVariant.putEmpty(); // don't know if this would have had
			// side effects
			if (JacobObject.isDebugEnabled()) {
				JacobObject.debug("Zeroing out enclosed Variant's ref to windows memory");
			}
			enclosedVariant.m_pVariant = 0;
			return enclosedVariantAsJava;
		}
		throw new IllegalStateException("getVariant() only legal on Variants of type VariantVariant, not " + this.getvt());
	}

	/**
	 * @deprecated superseded by SafeArray
	 * @return never returns anything
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public Variant[] getVariantArray() {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * @return the Variant Array that represents the data in the Variant
	 * @deprecated superseded by SafeArray
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public Variant[] getVariantArrayRef() {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 *
	 * @return the value in this Variant as a boolean, null if not a boolean
	 */
	private native boolean getVariantBoolean();

	private native boolean getVariantBooleanRef();

	/**
	 * @return the value in this Variant as a byte, null if not a byte
	 */
	private native byte getVariantByte();

	/**
	 * @return the value in this Variant as a byte, null if not a byte
	 */
	private native byte getVariantByteRef();

	/**
	 * @return the value in this Variant as a long, null if not a long
	 */
	private native long getVariantCurrency();

	/**
	 * @return the value in this Variant as a long, null if not a long
	 */
	private native long getVariantCurrencyRef();

	/**
	 * @return double return the date (as a double) value held in this variant (fails on other types?)
	 */
	private native double getVariantDate();

	/**
	 * get the content of this variant as a double representing a date
	 *
	 * @return double
	 */
	private native double getVariantDateRef();

	/**
	 * @return the value in this Variant as a decimal, null if not a decimal
	 */
	private native Object getVariantDec();

	/**
	 * @return the value in this Variant (byref) as a decimal, null if not a decimal
	 */
	private native Object getVariantDecRef();

	/**
	 * @return double get the content of this variant as a double
	 */
	private native double getVariantDouble();

	/**
	 * @return double get the content of this variant as a double
	 */
	private native double getVariantDoubleRef();

	private native int getVariantError();

	private native int getVariantErrorRef();

	/**
	 * @return returns the value as a float if the type is of type float
	 */
	private native float getVariantFloat();

	/**
	 * @return returns the value as a float if the type is of type float
	 */
	private native float getVariantFloatRef();

	/**
	 * @return the int value held in this variant (fails on other types?)
	 */
	private native int getVariantInt();

	/**
	 * @return the int value held in this variant (fails on other types?)
	 */
	private native int getVariantIntRef();

	/**
	 * @return the value in this Variant as a long, null if not a long
	 */
	private native long getVariantLong();

	/**
	 * @return the value in this Variant as a long, null if not a long
	 */
	private native long getVariantLongRef();

	/**
	 * get the content of this variant as a short
	 *
	 * @return short
	 */
	private native short getVariantShort();

	/**
	 * @return short the content of this variant as a short
	 */
	private native short getVariantShortRef();

	/**
	 * Native method that actually extracts a string value from the variant
	 *
	 * @return
	 */
	private native String getVariantString();

	/**
	 * @return String the content of this variant as a string
	 */
	private native String getVariantStringRef();

	/**
	 * Returns the variant type via a native method call
	 *
	 * @return short one of the VT_xx types
	 */
	private native short getVariantType();

	/**
	 * Returns the variant type via a native method call. Added 1.12 pre 6 - VT_VARIANT support is at an
	 * alpha level
	 *
	 * @return Variant one of the VT_Variant types
	 */
	private native long getVariantVariant();

	/**
	 * Reports the type of the underlying Variant object
	 *
	 * @return returns the variant type as a short, one of the Variantxxx values defined as statics in
	 *         this class. returns VariantNull if not initialized
	 * @throws IllegalStateException if there is no underlying windows data structure
	 */
	public short getvt() {
		if (m_pVariant != 0) {
			return getVariantType();
		}
		throw new IllegalStateException("uninitialized Variant");
	}

	/**
	 * initializes the COM Variant and puts its reference in this instance
	 */
	protected native void init();

	/**
	 *
	 * @return returns true if the variant is considered null
	 * @throws IllegalStateException if there is no underlying windows memory
	 */
	public boolean isNull() {
		getvt();
		return isVariantConsideredNull();
	}

	/**
	 * is the variant null or empty or error or null dispatch
	 *
	 * @return true if it is null or false if not
	 */
	private native boolean isVariantConsideredNull();

	/**
	 * sets the type to VT_ERROR and the error message to DISP_E_PARAMNOTFOIUND
	 *
	 * @deprecated replaced by putNoParam()
	 */
	@Deprecated
	public void noParam() {
		putNoParam();
	}

	/**
	 * returns true if the passed in Variant is a constant that should not be freed
	 *
	 * @param pVariant
	 * @return boolean that is true if Variant is a type of constant, VT_FALSE, VT_TRUE, VT_MISSING,
	 *         DEFAULT
	 */
	protected boolean objectIsAConstant(Variant pVariant) {
		if (pVariant == VT_FALSE || pVariant == VT_TRUE || pVariant == VT_MISSING || pVariant == DEFAULT) {
			return true;
		}
		return false;
	}

	/**
	 * puts a boolean into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	public void putBoolean(boolean in) {
		// verify we aren't released yet
		getvt();
		putVariantBoolean(in);
	}

	/**
	 * pushes a boolean into the variant by ref and sets the type of the variant to boolean
	 *
	 * @param in
	 */
	public void putBooleanRef(boolean in) {
		// verify we aren't released yet
		getvt();
		putVariantBooleanRef(in);
	}

	/**
	 * pushes a byte into the varaint and sets the type
	 *
	 * @param in
	 */
	public void putByte(byte in) {
		// verify we aren't released yet
		getvt();
		putVariantByte(in);
	};

	/**
	 * @deprecated superseded by SafeArray
	 * @param in doesn't matter because this method does nothing
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public void putByteArray(Object in) {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * pushes a byte into the variant by ref and sets the type
	 *
	 * @param in
	 */
	public void putByteRef(byte in) {
		// verify we aren't released yet
		getvt();
		putVariantByteRef(in);
	}

	/**
	 * @param in the object that would be wrapped by the Variant if this method was implemented
	 * @deprecated superseded by SafeArray
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public void putCharArray(Object in) {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * Puts a value in as a currency and sets the variant type. MS Currency objects are 64 bit fixed
	 * point numbers with 15 digits to the left and 4 to the right of the decimal place.
	 *
	 * @param in the long that will be put into the 64 bit currency object.
	 */
	public void putCurrency(Currency in) {
		// verify we aren't released yet
		getvt();
		putVariantCurrency(in.longValue());
	}

	/**
	 * Pushes a long into the variant as currency and sets the type. MS Currency objects are 64 bit
	 * fixed point numbers with 15 digits to the left and 4 to the right of the decimal place.
	 *
	 * @param in the long that will be put into the 64 bit currency object
	 */
	public void putCurrencyRef(Currency in) {
		// verify we aren't released yet
		getvt();
		putVariantCurrencyRef(in.longValue());
	}

	/**
	 * converts a java date to a windows time and calls putDate(double) SF 959382
	 *
	 * @param inDate a Java date to be converted
	 * @throws IllegalArgumentException if inDate = null
	 */
	public void putDate(Date inDate) {
		if (inDate == null) {
			throw new IllegalArgumentException("Cannot put null in as windows date");
			// do nothing
		}
		putDate(DateUtilities.convertDateToWindowsTime(inDate));
	}

	/**
	 * puts a windows date double into the variant and sets the type
	 *
	 * @param in
	 */
	public void putDate(double in) {
		// verify we aren't released yet
		getvt();
		putVariantDate(in);
	}

	/**
	 * converts a java date to a windows time and calls putDateRef(double) SF 959382
	 *
	 * @param inDate a Java date to be converted
	 * @throws IllegalArgumentException if inDate = null
	 */
	public void putDateRef(Date inDate) {
		if (inDate == null) {
			throw new IllegalArgumentException("Cannot put null in as windows date");
			// do nothing
		}
		putDateRef(DateUtilities.convertDateToWindowsTime(inDate));
	}

	/**
	 * set the content of this variant to a date (VT_DATE|VT_BYREF)
	 *
	 * @param in
	 */
	public void putDateRef(double in) {
		// verify we aren't released
		getvt();
		putVariantDateRef(in);
	}

	/**
	 * This actual does all the validating and massaging of the BigDecimalValues when converting them to
	 * MS Decimal types
	 *
	 * @param in number to be made into VT_DECIMAL
	 * @param byRef store by reference or not
	 * @param roundingBehavior one of the BigDecimal ROUND_xxx methods. Any method other than
	 *            ROUND_UNECESSARY means that the value will be rounded to fit
	 */
	private void putDecimal(BigDecimal in, boolean byRef) {
		// verify we aren't released
		getvt();
		// first validate the min and max
		VariantUtilities.validateDecimalMinMax(in);
		BigInteger allWordBigInt;
		allWordBigInt = in.unscaledValue();
		// Assume any required rounding has been done.
		VariantUtilities.validateDecimalScaleAndBits(in);
		// finally we can do what we actually came here to do
		int sign = in.signum();
		// VT_DECIMAL always has positive value with just the sign
		// flipped
		if (in.signum() < 0) {
			in = in.negate();
		}
		// ugh, reusing allWordBigInt but now should always be positive
		// and any round is applied
		allWordBigInt = in.unscaledValue();
		byte scale = (byte) in.scale();
		int lowWord = allWordBigInt.intValue();
		BigInteger middleWordBigInt = allWordBigInt.shiftRight(32);
		int middleWord = middleWordBigInt.intValue();
		BigInteger highWordBigInt = allWordBigInt.shiftRight(64);
		int highWord = highWordBigInt.intValue();
		if (byRef) {
			putVariantDecRef(sign, scale, lowWord, middleWord, highWord);
		}
		else {
			putVariantDec(sign, scale, lowWord, middleWord, highWord);
		}
	}

	/**
	 * EXPERIMENTAL 1.14 feature to support rounded decimals.
	 * <p>
	 * Set the value of this variant and set the type. This may throw exceptions more often than the
	 * caller expects because most callers don't manage the scale of their BigDecimal objects.
	 * <p>
	 * This default set method throws exceptions if precision or size is out of bounds
	 * <p>
	 * There are 12 bytes available for the integer number.
	 * <p>
	 * There is 1 byte for the scale.
	 *
	 * @param in the BigDecimal that will be converted to VT_DECIMAL
	 * @throws IllegalArgumentException if the scale is &gt; 28, the maximum for VT_DECIMAL or if there
	 *             are more than 12 bytes worth the digits
	 */
	public void putDecimal(BigDecimal in) {
		putDecimal(in, false);
	}

	/**
	 * Set the value of this variant and set the type. This may throw exceptions more often than the
	 * caller expects because most callers don't manage the scale of their BigDecimal objects.
	 * <p>
	 * This default set method throws exceptions if precision or size is out of bounds
	 * <p>
	 * There are 12 bytes available for the integer number.
	 * <p>
	 * There is 1 byte for the scale.
	 *
	 * @param in the BigDecimal that will be converted to VT_DECIMAL
	 * @throws IllegalArgumentException if the scale is &gt; 28, the maximum for VT_DECIMAL or if there
	 *             are more than 12 bytes worth the digits
	 */
	public void putDecimalRef(BigDecimal in) {
		putDecimal(in, true);
	}

	/**
	 * This acts a cover for putVariant Dispatch.
	 *
	 * @param in the Dispatch we're putting down in the COM variant space.
	 */
	public void putDispatch(Dispatch in) {
		putVariantDispatch(in);
	}

	/**
	 * Dispatch and dispatchRef are treated the same This is a cover for putVariantDispatch().
	 * putDispatch and putDispatchRef are treated the same because no one has written the COM code for
	 * putDispatchRef.
	 *
	 * @param in the Dispatch we're putting down in the COM variant space.
	 */
	public void putDispatchRef(Dispatch in) {
		putVariantDispatch(in);
	}

	/**
	 * wraps this Variant around the passed in double.
	 *
	 * @param in
	 */
	public void putDouble(double in) {
		// verify we aren't released yet
		getvt();
		putVariantDouble(in);
	}

	/**
	 * set the content of this variant to a double (VT_R8|VT_BYREF)
	 *
	 * @param in
	 */
	public void putDoubleRef(double in) {
		// verify we aren't released
		getvt();
		putVariantDoubleRef(in);
	}

	/**
	 * sets the type to VariantEmpty
	 *
	 */
	public void putEmpty() {
		// verify we aren't released yet
		getvt();
		putVariantEmpty();
	}

	/**
	 * puts an error code (I think) into the variant and sets the type
	 *
	 * @param in
	 */
	public void putError(int in) {
		// verify we aren't released yet
		getvt();
		putVariantError(in);
	}

	/**
	 * pushes an error code into the variant by ref and sets the type
	 *
	 * @param in
	 */
	public void putErrorRef(int in) {
		// verify we aren't released yet
		getvt();
		putVariantErrorRef(in);
	}

	/**
	 * fills the Variant with a float and sets the type to float
	 *
	 * @param in
	 */
	public void putFloat(float in) {
		// verify we haven't been released yet
		getvt();
		putVariantFloat(in);
	}

	/**
	 * pushes a float into the variant and sets the type
	 *
	 * @param in
	 */
	public void putFloatRef(float in) {
		// verify we aren't released yet
		getvt();
		putVariantFloatRef(in);
	}

	/**
	 * set the value of this variant and set the type
	 *
	 * @param in
	 */
	public void putInt(int in) {
		// verify we aren't released yet
		getvt();
		putVariantInt(in);
	}

	/**
	 * set the content of this variant to an int (VT_I4|VT_BYREF)
	 *
	 * @param in
	 */
	public void putIntRef(int in) {
		// verify we aren't released
		getvt();
		putVariantIntRef(in);
	}

	/**
	 * Puts a 64 bit Java Long into a 64 bit Variant Long. Only works on x64 systems otherwise throws an
	 * error. 64 bit long support added 1.14
	 *
	 * @param in the long that will be put into the 64 bit Long object.
	 */
	public void putLong(long in) {
		// verify we aren't released yet
		getvt();
		putVariantLong(in);
	}

	/**
	 * Puts a 64 bit Java Long into a 64 bit Variant Long. Only works on x64 systems otherwise throws an
	 * error. 64 bit long support added 1.14
	 *
	 * @param in the long that will be put into the 64 bit Long object.
	 */
	public void putLongRef(long in) {
		// verify we aren't released yet
		getvt();
		putVariantLongRef(in);
	}

	/**
	 * sets the type to VT_ERROR and the error message to DISP_E_PARAMNOTFOIUND
	 */
	public void putNoParam() {
		// verify we aren't released yet
		getvt();
		putVariantNoParam();
	}

	/**
	 * Sets the type to VariantDispatch and sets the value to null Equivalent to VB's nothing
	 */
	public void putNothing() {
		// verify we aren't released yet
		getvt();
		putVariantNothing();
	}

	/**
	 * Set this Variant's type to VT_NULL (the VB equivalent of NULL)
	 */
	public void putNull() {
		// verify we aren't released yet
		getvt();
		putVariantNull();
	}

	/**
	 * Puts an object into the Variant -- converts to Dispatch. Acts as a cover for
	 * putVariantDispatch(); This primarily exists to support jacobgen. This should be deprecated.
	 *
	 * @param in the object we are putting into the Variant, assumes a
	 * @see Variant#putDispatch(Dispatch)
	 * @deprecated should use putDispatch()
	 */
	@Deprecated
	public void putObject(Object in) {
		// this should verify in instanceof Dispatch
		putVariantDispatch(in);
	}

	/**
	 * Just a cover for putObject(). We shouldn't accept any old random object. This has been left in to
	 * support jacobgen. This should be deprecated.
	 *
	 * @param in
	 * @deprecated
	 */
	@Deprecated
	public void putObjectRef(Object in) {
		putObject(in);
	}

	/**
	 * have no idea...
	 *
	 * @param in
	 */
	public void putSafeArray(SafeArray in) {
		// verify we haven't been released yet
		getvt();
		putVariantSafeArray(in);
	}

	/**
	 * have no idea...
	 *
	 * @param in
	 */
	public void putSafeArrayRef(SafeArray in) {
		// verify we haven't been released yet
		getvt();
		putVariantSafeArrayRef(in);
	}

	/**
	 * set the content of this variant to a short (VT_I2)
	 *
	 * @param in
	 */
	public void putShort(short in) {
		// verify we aren't released
		getvt();
		putVariantShort(in);
	}

	/**
	 * set the content of this variant to a short (VT_I2|VT_BYREF)
	 *
	 * @param in
	 */
	public void putShortRef(short in) {
		// verify we aren't released
		getvt();
		putVariantShortRef(in);
	}

	/**
	 * put a string into the variant and set its type
	 *
	 * @param in
	 */
	public void putString(String in) {
		// verify we aren't released yet
		getvt();
		putVariantString(in);
	}

	/**
	 * set the content of this variant to a string (VT_BSTR|VT_BYREF)
	 *
	 * @param in
	 */
	public void putStringRef(String in) {
		// verify we aren't released
		getvt();
		putVariantStringRef(in);
	}

	/**
	 * Puts a variant into this variant making it type VT_VARIANT. Added 1.12 pre 6
	 *
	 * @param objectToBeWrapped A object that is to be referenced by this variant. If objectToBeWrapped
	 *            is already of type Variant, then it is used. If objectToBeWrapped is not Variant then
	 *            <code>new Variant(objectToBeWrapped)</code> is called and the result is passed into
	 *            the com layer
	 * @throws IllegalArgumentException if inVariant = null or if inVariant is a Varint
	 */
	public void putVariant(Object objectToBeWrapped) {
		if (objectToBeWrapped == null) {
			throw new IllegalArgumentException("Cannot put null in as a variant");
		}
		else if (objectToBeWrapped instanceof Variant) {
			throw new IllegalArgumentException("Cannot putVariant() only accepts non jacob objects.");
		}
		else {
			Variant inVariant = new Variant(objectToBeWrapped);
			putVariantVariant(inVariant);
			// This could be done in Variant.cpp
			if (JacobObject.isDebugEnabled()) {
				JacobObject.debug("Zeroing out enclosed Variant's ref to windows memory");
			}
			inVariant.m_pVariant = 0;
		}
	}

	/**
	 * @deprecated superseded by SafeArray
	 * @param in doesn't matter because this method does nothing
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public void putVariantArray(Variant[] in) {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * @param in the thing that would be come an array if this method was implemented
	 * @deprecated superseded by SafeArray
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public void putVariantArrayRef(Variant[] in) {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * puts a boolean into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantBoolean(boolean in);

	/**
	 * puts a boolean into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantBooleanRef(boolean in);

	/**
	 * puts a byte into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantByte(byte in);

	/**
	 * puts a byte into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantByteRef(byte in);

	/**
	 * puts a Currency into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantCurrency(long in);

	/**
	 * puts a Currency into the variant and sets it's type
	 *
	 * @param in the new value
	 */
	private native void putVariantCurrencyRef(long in);

	/**
	 * set the value of this variant
	 *
	 * @param in
	 */
	private native void putVariantDate(double in);

	/**
	 * set the content of this variant to a date (VT_DATE|VT_BYREF)
	 *
	 * @param in
	 */
	private native void putVariantDateRef(double in);

	/**
	 * private JNI method called by putDecimal
	 *
	 * @param signum sign
	 * @param scale BigDecimal's scale
	 * @param lo low 32 bits
	 * @param mid middle 32 bits
	 * @param hi high 32 bits
	 */
	private native void putVariantDec(int signum, byte scale, int lo, int mid, int hi);

	/**
	 * private JNI method called by putDecimalRef
	 *
	 * @param signum sign
	 * @param scale BigDecimal's scale
	 * @param lo low 32 bits
	 * @param mid middle 32 bits
	 * @param hi high 32 bits
	 */
	private native void putVariantDecRef(int signum, byte scale, int lo, int mid, int hi);

	/**
	 * the JNI implementation for putDispatch() so that we can screen the incoming dispatches in
	 * putDispatch() before this is invoked
	 *
	 * @param in should be a dispatch object
	 */
	private native void putVariantDispatch(Object in);

	private native void putVariantDouble(double in);

	/**
	 * set the content of this variant to a double (VT_R8|VT_BYREF)
	 *
	 * @param in
	 */
	private native void putVariantDoubleRef(double in);

	/**
	 * Sets the type to VariantEmpty. No values needed
	 */
	private native void putVariantEmpty();

	private native void putVariantError(int in);

	private native void putVariantErrorRef(int in);

	/**
	 * fills the Variant with a float and sets the type to float
	 *
	 * @param in
	 */
	private native void putVariantFloat(float in);

	private native void putVariantFloatRef(float in);

	/**
	 * set the value of this variant and set the type
	 *
	 * @param in
	 */
	private native void putVariantInt(int in);

	/**
	 * set the content of this variant to an int (VT_I4|VT_BYREF)
	 *
	 * @param in
	 */
	private native void putVariantIntRef(int in);

	private native void putVariantLong(long in);

	private native void putVariantLongRef(long in);

	/**
	 * sets the type to VT_ERROR and the error message to DISP_E_PARAMNOTFOIUND
	 */
	private native void putVariantNoParam();

	/**
	 * Sets the type to VariantDispatch and sets the value to null Equivalent to VB's nothing
	 */
	private native void putVariantNothing();

	/**
	 * Set this Variant's type to VT_NULL (the VB equivalent of NULL)
	 */
	private native void putVariantNull();

	private native void putVariantSafeArray(SafeArray in);

	private native void putVariantSafeArrayRef(SafeArray in);

	/**
	 * set the content of this variant to a short (VT_I2)
	 *
	 * @param in
	 */
	private native void putVariantShort(short in);

	/**
	 * set the content of this variant to a short (VT_I2|VT_BYREF)
	 *
	 * @param in
	 */
	private native void putVariantShortRef(short in);

	private native void putVariantString(String in);

	/**
	 * set the content of this variant to a string (VT_BSTR|VT_BYREF)
	 *
	 * @param in
	 */
	private native void putVariantStringRef(String in);

	/**
	 * All VariantVariant type variants are BYREF.
	 *
	 * Set the content of this variant to a string (VT_VARIANT|VT_BYREF).
	 *
	 * Added 1.12 pre 6 - VT_VARIANT support is at an alpha level
	 *
	 * @param in variant to be wrapped
	 *
	 */
	private native void putVariantVariant(Variant in);

	/**
	 * now private so only this object can access was: call this to explicitly release the com object
	 * before gc
	 *
	 */
	private native void release();

	/**
	 * This will release the "C" memory for the Variant unless this Variant is one of the constants in
	 * which case we don't want to release the memory.
	 * <p>
	 *
	 * @see com.jacob.com.JacobObject#safeRelease()
	 */
	@Override
	public void safeRelease() {
		// The well known constants should not be released.
		// Unfortunately this doesn't fix any other classes that are
		// keeping constants around in their static ivars.
		// those will still be busted.
		//
		// The only inconsistency here is that we leak
		// when this class is unloaded because we won't
		// free the memory even if the constants are being
		// finalized. this is not a big deal at all.
		// another way around this would be to create the constants
		// in their own thread so that they would never be released
		if (!objectIsAConstant(this)) {
			super.safeRelease();
			if (m_pVariant != 0) {
				release();
				m_pVariant = 0;
			}
			else {
				// looks like a double release
				// this should almost always happen due to gc
				// after someone has called ComThread.Release
				if (isDebugEnabled()) {
					debug("Variant: " + this.hashCode() + " double release");
					// Throwable x = new Throwable();
					// x.printStackTrace();
				}
			}
		}
		else {
			if (isDebugEnabled()) {
				debug("Variant: " + this.hashCode() + " don't want to release a constant");
			}
		}
	}

	/**
	 * this is supposed to cause the underlying variant object struct to be rebuilt from a previously
	 * serialized byte array.
	 *
	 * @param ba
	 */
	protected native void SerializationReadFromBytes(byte[] ba);

	/**
	 * this is supposed to create a byte array that represents the underlying variant object structure
	 */
	protected native byte[] SerializationWriteToBytes();

	/**
	 * @deprecated should be replaced by changeType() followed by getBoolean()
	 * @return the value of this variant as boolean (after possible conversion)
	 */
	@Deprecated
	public boolean toBoolean() {
		changeType(Variant.VariantBoolean);
		return getBoolean();
	}

	/**
	 * attempts to return the content of this variant as a double (after possible conversion)
	 *
	 * @deprecated should be replaced by changeType() followed by getByte()
	 * @return byte
	 */
	@Deprecated
	public byte toByte() {
		changeType(Variant.VariantByte);
		return getByte();
	}

	/**
	 * @deprecated superseded by SafeArray
	 * @return nothing because this method is not implemented
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public Object toByteArray() {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * @deprecated superseded by SafeArray
	 * @return never returns anything
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public Object toCharArray() {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * @deprecated should be replaced by changeType() followed by getCurrency
	 * @return the content of this variant as a long representing a monetary amount
	 */
	@Deprecated
	public Currency toCurrency() {
		changeType(Variant.VariantCurrency);
		return getCurrency();
	}

	/**
	 * @deprecated should use changeType() followed by getDate()
	 * @return the value of this variant as a date (after possible conversion)
	 */
	@Deprecated
	public double toDate() {
		changeType(VariantDate);
		return getDate();
	}

	/**
	 * @return the content of this variant as a Dispatch object (after possible conversion)
	 */
	public Dispatch toDispatch() {
		// now make the native call
		return toVariantDispatch();
	}

	/**
	 * @deprecated should call changeType() then getDouble()
	 * @return the content of this variant as a double (after possible conversion)
	 */
	@Deprecated
	public double toDouble() {
		changeType(Variant.VariantDouble);
		return getDouble();
	}

	/** @return the value of this variant as an enumeration (java style) */
	public native EnumVariant toEnumVariant();

	/**
	 * converts to an error type and returns the error
	 *
	 * @deprecated should use changeType() followed by getError()
	 * @return the error as an int (after conversion)
	 */
	@Deprecated
	public int toError() {
		changeType(Variant.VariantError);
		return getError();
	}

	/**
	 * attempts to return the contents of this variant as a float (after possible conversion)
	 *
	 * @deprecated should use changeType() and getFloat() instead
	 * @return float
	 */
	@Deprecated
	public float toFloat() {
		changeType(Variant.VariantFloat);
		return getFloat();
	}

	/**
	 * @deprecated should use changeType() followed by getInt()
	 * @return the value of this variant as an int (after possible conversion)
	 */
	@Deprecated
	public int toInt() {
		changeType(VariantInt);
		return getInt();
	}

	/**
	 * Returns the windows time contained in this Variant as a Java Date converts to a date like many of
	 * the other toXXX() methods SF 959382.
	 * <p>
	 * This method added 12/2005 for possible use by jacobgen instead of its conversion code
	 * <p>
	 * This does not convert the data
	 *
	 * @deprecated callers should use getDate()
	 * @return java.util.Date version of this variant if it is a date, otherwise null
	 *
	 */
	@Deprecated
	public Date toJavaDate() {
		changeType(Variant.VariantDate);
		return getJavaDate();
	}

	/**
	 * Convert a JACOB Variant value to a Java object (type conversions). provided in Sourceforge
	 * feature request 959381. See JavaVariantConverter..convertVariantTJavaObject(Variant) for more
	 * information.
	 *
	 * @return Corresponding Java object of the type matching the Variant type.
	 * @throws IllegalStateException if no underlying windows data structure
	 * @throws NotImplementedException if unsupported conversion is requested
	 * @throws JacobException if the calculated result was a JacobObject usually as a result of error
	 */
	public Object toJavaObject() throws JacobException {
		return VariantUtilities.variantToObject(this);
	}

	/**
	 * Acts a a cover for toDispatch. This primarily exists to support jacobgen.
	 *
	 * @deprecated this is a cover for toDispatch();
	 * @return Object returned by toDispatch()
	 * @see Variant#toDispatch() instead
	 */
	@Deprecated
	public Object toObject() {
		return toDispatch();
	}

	/**
	 * By default toSafeArray makes a deep copy due to the fact that this Variant owns the embedded
	 * SafeArray and will destroy it when it gc's calls toSafeArray(true).
	 *
	 * @return the object converted to a SafeArray
	 */
	public SafeArray toSafeArray() {
		// verify we haven't been released yet
		getvt();
		return toSafeArray(true);
	}

	/**
	 * This lets folk turn into a safe array without a deep copy. Should this API be public?
	 *
	 * @param deepCopy
	 * @return SafeArray constructed
	 */
	public SafeArray toSafeArray(boolean deepCopy) {
		// verify we haven't been released yet
		getvt();
		return toVariantSafeArray(deepCopy);
	}

	/**
	 * I don't know what this is. Is it some legacy (pre 1.8) thing?
	 *
	 * @deprecated
	 * @return this object as a dispatch object by calling toDispatch()
	 */
	@Deprecated
	public Object toScriptObject() {
		return toDispatch();
	}

	/**
	 * attempts to return the contents of this Variant as a short (after possible conversion)
	 *
	 * @deprecated callers should use changeType() followed by getShort()
	 * @return short
	 */
	@Deprecated
	public short toShort() {
		this.changeType(Variant.VariantShort);
		return getShort();
	}

	/**
	 * This method now correctly implements java toString() semantics Attempts to return the content of
	 * this variant as a string
	 * <ul>
	 * <li>"not initialized" if not initialized
	 * <li>"null" if VariantEmpty,
	 * <li>"null" if VariantError
	 * <li>"null" if VariantNull
	 * <li>"null" if Variant type didn't convert. This can happen for date conversions where the
	 * returned value was 0.
	 * <li>the value if we know how to describe one of that type
	 * <li>three question marks if can't convert
	 * </ul>
	 *
	 * @return String value conversion,
	 * @throws IllegalStateException if there is no underlying windows data structure
	 */
	@Override
	public String toString() {
		try {
			// see if we are in a legal state
			getvt();
		}
		catch (IllegalStateException ise) {
			return "";
		}
		if (getvt() == VariantEmpty || getvt() == VariantError || getvt() == VariantNull) {
			return "null";
		}
		if (getvt() == VariantString) {
			return getString();
		}
		try {
			Object foo = toJavaObject();
			// rely on java objects to do the right thing
			if (foo == null) {
				return "null";
			}
			return foo.toString();
		}
		catch (NotImplementedException nie) {
			// some types do not generate a good description yet
			return "Description not available for type: " + getvt();
		}
	}

	/**
	 * Exists to support jacobgen. This would be deprecated if it weren't for jacobgen
	 *
	 * @deprecated superseded by "this"
	 * @return this same object
	 */
	@Deprecated
	public Variant toVariant() {
		return this;
	}

	/**
	 * @deprecated superseded by SafeArray
	 * @return nothing because this method is not implemented
	 * @throws com.jacob.com.NotImplementedException
	 */
	@Deprecated
	public Variant[] toVariantArray() {
		throw new NotImplementedException("Not implemented");
	}

	/**
	 * native method used by toDispatch()
	 *
	 * @return
	 */
	private native Dispatch toVariantDispatch();

	private native SafeArray toVariantSafeArray(boolean deepCopy);

	/*
	 * =====================================================================
	 *
	 *
	 * =====================================================================
	 */

	/**
	 * Clear the content of this variant
	 */
	public native void VariantClear();

}