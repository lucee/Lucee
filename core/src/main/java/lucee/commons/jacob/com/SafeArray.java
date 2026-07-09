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

import java.util.Date;

/**
 * This creates an array wrapper around Variant objects(?). This supports 1, 2 and n-dimensional
 * arrays. It exists in this form because n-dimensional arrays were a later addition.
 */
public class SafeArray extends JacobObject {
	/** The super secret long that is actually the pointer to windows memory */
	long m_pV = 0;

	/**
	 * Constructor. Why does this exist? Yeah, someone will post on sourceforge about this comment.
	 *
	 */
	public SafeArray() {}

	/**
	 * Constructor.
	 *
	 * @param vt - type of array
	 */
	public SafeArray(int vt) {
		init(vt, new int[] { 0 }, new int[] { -1 });
	}

	/**
	 * Constructor for a single dimensional array whose lower bounds is 0 and whose upper bound is
	 * specified as a parameter
	 *
	 * @param vt - type of the array
	 * @param celems - length of the array
	 */
	public SafeArray(int vt, int celems) {
		init(vt, new int[] { 0 }, new int[] { celems });
	}

	/**
	 * Creates a two dimensional SafeArray whose base indexes are 0.
	 *
	 * @param vt - Type of the array
	 * @param celems1 - length of the array in first dimension
	 * @param celems2 - length of the array in second dimension
	 */
	public SafeArray(int vt, int celems1, int celems2) {
		init(vt, new int[] { 0, 0 }, new int[] { celems1, celems2 });
	}

	/**
	 * Constructor with support for N-dimensional array support
	 * <p>
	 * You create an N-D SafeArray by: SafeArray sa = new SafeArray(Variant.VariantVariant, new int[]
	 * {0,0,0,0}, new int[] {4,4,4,4}); Where the 1st array is lower bounds and 2nd has the lengths of
	 * each dimension *
	 *
	 * @param vt
	 * @param lbounds
	 * @param celems
	 */
	public SafeArray(int vt, int lbounds[], int celems[]) {
		init(vt, lbounds, celems);
	}

	/**
	 * Convert a string to a VT_UI1 array.
	 *
	 * @param s - source string
	 */
	public SafeArray(String s) {
		// https://sourceforge.net/p/jacob-project/patches/41/
		/*
		 * char[] ca = s.toCharArray(); init(Variant.VariantByte, new int[] { 0 }, new int[] { ca.length });
		 * fromCharArray(ca);
		 */
		byte[] ba = s.getBytes();
		init(Variant.VariantByte, new int[] { 0 }, new int[] { ba.length });
		fromByteArray(ba);

	}

	/**
	 * Convert a VT_UI1 array to string. Is this broken for unicode?
	 *
	 * @return variant byte as a string
	 */
	public String asString() {
		if (getvt() != Variant.VariantByte) {
			return null;
		}
		// https://sourceforge.net/p/jacob-project/patches/41/
		/*
		 * char ja[] = toCharArray(); return new String(ja);
		 */
		byte ba[] = toByteArray();
		return new String(ba);
	}

	@Override
	public native Object clone();

	/**
	 * now private so only this object can access. Was: call this to explicitly release the com object
	 * before gc
	 *
	 */
	private native void destroy();

	/**
	 * {@inheritDoc}
	 */
	@SuppressWarnings("removal")
	@Override
	protected void finalize() {
		safeRelease();
	}

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromBooleanArray(boolean ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromByteArray(byte ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromCharArray(char ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromDoubleArray(double ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromFloatArray(float ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromIntArray(int ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromLongArray(long ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromShortArray(short ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromStringArray(String ja[]);

	/**
	 * populate the safe array from the passed in array of data
	 *
	 * @param ja
	 */
	public native void fromVariantArray(Variant ja[]);

	/**
	 * boolean access
	 *
	 * @param sa_idx
	 * @return boolean representation
	 */
	public native boolean getBoolean(int sa_idx);

	/**
	 * get boolean value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native boolean getBoolean(int indices[]);

	/**
	 * boolean access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return boolean representation
	 */
	public native boolean getBoolean(int sa_idx1, int sa_idx2);

	/**
	 * boolean access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getBooleans(int sa_idx, int nelems, boolean ja[], int ja_start);

	/**
	 * byte access
	 *
	 * @param sa_idx
	 * @return byte representaton
	 */
	public native byte getByte(int sa_idx);

	/**
	 * get byte value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native byte getByte(int indices[]);

	/**
	 * byte access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return byte representation
	 */
	public native byte getByte(int sa_idx1, int sa_idx2);

	/**
	 * Fills byte array from contents of this array
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getBytes(int sa_idx, int nelems, byte ja[], int ja_start);

	/**
	 * char access
	 *
	 * @param sa_idx
	 * @return single character rpeesentation
	 */
	public native char getChar(int sa_idx);

	/**
	 * get char value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native char getChar(int indices[]);

	/**
	 * char access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return single character representation
	 */
	public native char getChar(int sa_idx1, int sa_idx2);

	/**
	 * char access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getChars(int sa_idx, int nelems, char ja[], int ja_start);

	/**
	 * double access
	 *
	 * @param sa_idx
	 * @return double stored in array
	 */
	public native double getDouble(int sa_idx);

	/**
	 * get double value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native double getDouble(int indices[]);

	/**
	 * double access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return double stored in array
	 */
	public native double getDouble(int sa_idx1, int sa_idx2);

	/**
	 * double access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getDoubles(int sa_idx, int nelems, double ja[], int ja_start);

	/**
	 * @return the size of each element?
	 */
	public native int getElemSize();

	/**
	 * @return The ??features of the array?
	 */
	public native int getFeatures();

	/**
	 * float access
	 *
	 * @param sa_idx
	 * @return float held in array at location
	 */
	public native float getFloat(int sa_idx);

	/**
	 * get float value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native float getFloat(int indices[]);

	/**
	 * float access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return float held in array at location
	 */
	public native float getFloat(int sa_idx1, int sa_idx2);

	/**
	 * float access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getFloats(int sa_idx, int nelems, float ja[], int ja_start);

	/**
	 * get int from an single dimensional array
	 *
	 * @param sa_idx - array index
	 * @return int stored in array
	 */
	public native int getInt(int sa_idx);

	/**
	 * get int value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native int getInt(int indices[]);

	/**
	 * get int from 2 dimensional array
	 *
	 * @param sa_idx1 - array index first dimension
	 * @param sa_idx2 - array index of second dimension
	 * @return int stored in array
	 */
	public native int getInt(int sa_idx1, int sa_idx2);

	/**
	 * retrieves a group of ints from a single dimensional array
	 *
	 * @param sa_idx - the index in the array to start the get
	 * @param nelems - number of elements to retrieve
	 * @param ja - the structure to be filled with the ints
	 * @param ja_start - the start point in the java int array to start filling
	 */
	public native void getInts(int sa_idx, int nelems, int ja[], int ja_start);

	/**
	 * get int from an single dimensional array
	 *
	 * @param sa_idx - array index
	 * @return long stored in array
	 */
	public native long getLong(int sa_idx);

	/**
	 * get long value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native long getLong(int indices[]);

	/**
	 * get long from 2 dimensional array
	 *
	 * @param sa_idx1 - array index first dimension
	 * @param sa_idx2 - array index of second dimension
	 * @return long stored in array
	 */
	public native long getLong(int sa_idx1, int sa_idx2);

	/**
	 * retrieves a group of longs from a single dimensional array
	 *
	 * @param sa_idx - the index in the array to start the get
	 * @param nelems - number of elements to retrieve
	 * @param ja - the structure to be filled with the longs
	 * @param ja_start - the start point in the java longs array to start filling
	 */
	public native void getLongs(int sa_idx, int nelems, long ja[], int ja_start);

	/**
	 * @return The lower bounds of the array?
	 */
	public native int getLBound();

	/**
	 * @param dim the dimension we are checking in a multidimensional array
	 * @return The lower bounds of the array?
	 */
	public native int getLBound(int dim);

	/**
	 * @return The number of dimensions in this array
	 */
	public native int getNumDim();

	/**
	 * not implemented.
	 *
	 * @return 0
	 */
	public int getNumLocks() {
		return 0;
	}

	/**
	 * short access
	 *
	 * @param sa_idx
	 * @return short stored in array
	 */
	public native short getShort(int sa_idx);

	/**
	 * get short value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native short getShort(int indices[]);

	/**
	 * short access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return short stored in array
	 */
	public native short getShort(int sa_idx1, int sa_idx2);

	/**
	 * short access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getShorts(int sa_idx, int nelems, short ja[], int ja_start);

	/**
	 * string access
	 *
	 * @param sa_idx
	 * @return String stored in array
	 *
	 */
	public native String getString(int sa_idx);

	/**
	 * get String value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native String getString(int indices[]);

	/**
	 * string access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return String stored in array
	 */
	public native String getString(int sa_idx1, int sa_idx2);

	/**
	 * string access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getStrings(int sa_idx, int nelems, String ja[], int ja_start);

	/**
	 * @return The upper bounds of the array?
	 */
	public native int getUBound();

	/**
	 * @param dim the dimension we are checking in a multidimensional array
	 * @return The upper bounds of the array?
	 */
	public native int getUBound(int dim);

	/**
	 * variant access
	 *
	 * @param sa_idx
	 * @return Variant held in location in the array?
	 */
	public native Variant getVariant(int sa_idx);

	/**
	 * get Variant value from N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @return the value at the specified location
	 */
	public native Variant getVariant(int indices[]);

	/**
	 * variant access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @return Variant held in a location in the array?
	 */
	public native Variant getVariant(int sa_idx1, int sa_idx2);

	/**
	 * variant access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void getVariants(int sa_idx, int nelems, Variant ja[], int ja_start);

	/**
	 * @return the Variant type
	 */
	public native int getvt();

	protected native void init(int vt, int lbounds[], int celems[]);

	// Don't directly expose the private native
	// Should all the other native methods have the same protection
	/**
	 * Does anyone want to document this?
	 */
	public void reinit() {
		reinit0();
	}

	// exposing private native is apparently frowned upon
	// and should be implmemented like this
	private native void reinit0();

	/**
	 * Does anyone want to document this?
	 *
	 * @param vt - the variant type?
	 */
	public native void reinterpretType(int vt);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void safeRelease() {
		super.safeRelease();
		if (m_pV != 0) {
			destroy();
			m_pV = 0;
		}
		else {
			// looks like a double release
			if (isDebugEnabled()) {
				debug(this.getClass().getName() + ":" + this.hashCode() + " double release");
			}
		}
	}

	/**
	 * boolean access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setBoolean(int sa_idx, boolean c);

	/**
	 * set boolean value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setBoolean(int indices[], boolean c);

	/**
	 * boolean access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setBoolean(int sa_idx1, int sa_idx2, boolean c);

	/**
	 * boolean access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setBooleans(int sa_idx, int nelems, boolean ja[], int ja_start);

	/**
	 * byte access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setByte(int sa_idx, byte c);

	/**
	 * set byte value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setByte(int indices[], byte c);

	/**
	 * byte access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setByte(int sa_idx1, int sa_idx2, byte c);

	/**
	 * fills array with passed in bytes
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setBytes(int sa_idx, int nelems, byte ja[], int ja_start);

	/**
	 * char access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setChar(int sa_idx, char c);

	/**
	 * set char value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setChar(int indices[], char c);

	/**
	 * char access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setChar(int sa_idx1, int sa_idx2, char c);

	/**
	 * char access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setChars(int sa_idx, int nelems, char ja[], int ja_start);

	/**
	 * double access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setDouble(int sa_idx, double c);

	/**
	 * set double value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setDouble(int indices[], double c);

	/**
	 * double access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setDouble(int sa_idx1, int sa_idx2, double c);

	/**
	 * double access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setDoubles(int sa_idx, int nelems, double ja[], int ja_start);

	/**
	 * float access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setFloat(int sa_idx, float c);

	/**
	 * set float value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setFloat(int indices[], float c);

	/**
	 * float access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setFloat(int sa_idx1, int sa_idx2, float c);

	/**
	 * float access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setFloats(int sa_idx, int nelems, float ja[], int ja_start);

	/**
	 * sets the int value of an element in a single dimensional array
	 *
	 * @param sa_idx - index into the array
	 * @param c - the value to be set
	 */
	public native void setInt(int sa_idx, int c);

	/**
	 * set int value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setInt(int indices[], int c);

	/**
	 * sets the int value of a 2 dimensional array
	 *
	 * @param sa_idx1 - index on the first dimension
	 * @param sa_idx2 - index on the second dimension
	 * @param c - the value to be set
	 */
	public native void setInt(int sa_idx1, int sa_idx2, int c);

	/**
	 * sets a group of ints into a single dimensional array
	 *
	 * @param sa_idx - the index of the start of the array to put into
	 * @param nelems - number of elements to be copied
	 * @param ja - the new int values to be put into the array
	 * @param ja_start -the start index in the array that we are copying into SafeArray
	 */
	public native void setInts(int sa_idx, int nelems, int ja[], int ja_start);

	/**
	 * sets the long value of an element in a single dimensional array
	 *
	 * @param sa_idx - index into the array
	 * @param c - the value to be set
	 */
	public native void setLong(int sa_idx, long c);

	/**
	 * set long value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setLong(int indices[], long c);

	/**
	 * sets the long value of a 2 dimensional array
	 *
	 * @param sa_idx1 - index on the first dimension
	 * @param sa_idx2 - index on the second dimension
	 * @param c -the value to be set
	 */
	public native void setLong(int sa_idx1, int sa_idx2, long c);

	/**
	 * sets a group of longs into a single dimensional array
	 *
	 * @param sa_idx - the index of the start of the array to put into
	 * @param nelems - the new long values to be put into the array
	 * @param ja_start - the start index in the array that we are copying into SafeArray
	 */
	public native void setLongs(int sa_idx, int nelems, long ja[], int ja_start);

	/**
	 * short access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setShort(int sa_idx1, int sa_idx2, short c);

	/**
	 * short access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setShort(int sa_idx, short c);

	/**
	 * set short value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setShort(int indices[], short c);

	/**
	 * short access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setShorts(int sa_idx, int nelems, short ja[], int ja_start);

	/**
	 * puts a string into an element in a two dimensional array.
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setString(int sa_idx1, int sa_idx2, String c);

	/*
	 * ================================================================ The beginning of N-dimensional
	 * array support ================================================================
	 */

	/**
	 * puts a string into an element in a single dimensional safe array
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setString(int sa_idx, String c);

	/**
	 * set Stringvalue in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param c
	 */
	public native void setString(int indices[], String c);

	/**
	 * string access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setStrings(int sa_idx, int nelems, String ja[], int ja_start);

	/**
	 * variant access
	 *
	 * @param sa_idx1
	 * @param sa_idx2
	 * @param c
	 */
	public native void setVariant(int sa_idx1, int sa_idx2, Variant c);

	/**
	 * variant access
	 *
	 * @param sa_idx
	 * @param c
	 */
	public native void setVariant(int sa_idx, Variant c);

	/**
	 * set Variant value in N-dimensional array
	 *
	 * @param indices - length must equal Dimension of SafeArray
	 * @param v
	 */
	public native void setVariant(int indices[], Variant v);

	/**
	 * get windows time from an single dimensional array
	 *
	 * @param sa_idx - array index
	 * @return date stored in array
	 */
	public native double getDate(int sa_idx);

	/**
	 * returns the Java Date contained in this array
	 *
	 * @param sa_idx1 - array index
	 * @return Java date stored in array
	 * @throws IllegalStateException if array is not of the requested type
	 */
	public Date getJavaDate(int sa_idx1) {
		Date returnDate = null;
		if (this.getvt() == Variant.VariantDate) {
			double windowsDate = this.getDate(sa_idx1);
			if (windowsDate != 0.0D) {
				returnDate = DateUtilities.convertWindowsTimeToDate(windowsDate);
			}
			return returnDate;
		}
		else {
			throw new IllegalStateException("getJavaDate() only legal on SafeArray of type VariantDate, not " + this.getvt());
		}
	}

	/**
	 * variant access
	 *
	 * @param sa_idx
	 * @param nelems
	 * @param ja
	 * @param ja_start
	 */
	public native void setVariants(int sa_idx, int nelems, Variant ja[], int ja_start);

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return boolean[] array of booleans contained in this collection
	 */
	public native boolean[] toBooleanArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return byte[] byte array contained in this collection
	 */
	public native byte[] toByteArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return char[] character array contained in this collection
	 */
	public native char[] toCharArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return double[] double array contained in this collection
	 */
	public native double[] toDoubleArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return float[] array of float contained in this collection
	 */
	public native float[] toFloatArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return int[] int array contained in this collection
	 */
	public native int[] toIntArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return long[] long array contained in this collection
	 */
	public native long[] toLongArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return short[] short array contained in this collection
	 */
	public native short[] toShortArray();

	/**
	 * Standard toString() Warning, this creates new Variant objects!
	 *
	 * @return String contents of variant
	 */
	@Override
	public String toString() {
		String s = "";
		int ndim = getNumDim();
		if (ndim == 1) {
			int ldim = getLBound();
			int udim = getUBound();
			for (int i = ldim; i <= udim; i++) {
				Variant v = getVariant(i);

				if (((v.getvt() & Variant.VariantTypeMask) | Variant.VariantArray) == v.getvt()) {
					return s + "[" + v.toSafeArray().toString() + "]";
				}
				else {
					s += " " + v.toString();
				}
			}
		}
		else if (ndim == 2) {
			int ldim1 = getLBound(1);
			int udim1 = getUBound(1);

			int ldim2 = getLBound(2);
			int udim2 = getUBound(2);

			for (int i = ldim1; i <= udim1; i++) {
				for (int j = ldim2; j <= udim2; j++) {
					Variant v = getVariant(i, j);
					s += " " + v.toString();
				}
				s += "\n";
			}
		}
		return s;
	}

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return String[] String array contained in this collection
	 */
	public native String[] toStringArray();

	/**
	 * Retrieves the data from the array cast to a Java data type
	 *
	 * @return Variant[] array of variants contained in this collection
	 */
	public native Variant[] toVariantArray();

}