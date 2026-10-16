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

import java.util.Enumeration;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An implementation of IEnumVariant based on code submitted by Thomas Hallgren
 * (mailto:Thomas.Hallgren@eoncompany.com)
 */
public class EnumVariant extends JacobObject implements Enumeration<Variant>, Iterable<Variant> {
	/** pointer to windows memory */
	private long m_pIEnumVARIANT;

	private final Variant[] m_recBuf = new Variant[1];

	// this only gets called from JNI
	//
	protected EnumVariant(long pIEnumVARIANT) {
		m_pIEnumVARIANT = pIEnumVARIANT;
	}

	/**
	 * @param disp
	 */
	public EnumVariant(Dispatch disp) {
		int[] hres = new int[1];
		// SF 3377279
		// Added Dispatch.Method to the invoke flags to call _NewEnum. There are
		// some
		// non-conforming legacy implementations that expose _NewEnum as a
		// method.
		Variant evv = Dispatch.invokev(disp, DispatchIdentifier.DISPID_NEWENUM, Dispatch.Get | Dispatch.Method, new Variant[0], hres);
		if (evv.getvt() != Variant.VariantObject)
			// The DISPID_NEWENUM did not result in a valid object
			throw new ComFailException("Can't obtain EnumVARIANT");

		EnumVariant tmp = evv.toEnumVariant();
		m_pIEnumVARIANT = tmp.m_pIEnumVARIANT;
		tmp.m_pIEnumVARIANT = 0;
	}

	/**
	 * Implements java.util.Enumeration
	 *
	 * @return boolean true if there are more elements in this enumeration
	 */
	@Override
	public boolean hasMoreElements() {
		{
			if (m_recBuf[0] == null) {
				if (this.Next(m_recBuf) <= 0) return false;
			}
			return true;
		}
	}

	/**
	 * Implements java.util.Enumeration
	 *
	 * @return next element in the enumeration
	 */
	@Override
	public Variant nextElement() {
		Variant last = m_recBuf[0];
		if (last == null) {
			if (this.Next(m_recBuf) <= 0) throw new java.util.NoSuchElementException();
			last = m_recBuf[0];
		}
		m_recBuf[0] = null;
		return last;
	}

	/**
	 * Get next element in collection or null if at end
	 *
	 * @return Variant that is next in the collection
	 * @deprecated use nextElement() instead
	 */
	@Deprecated
	public Variant Next() {
		if (hasMoreElements()) return nextElement();
		return null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation resets the EnumVariant, so that <code>iterator()</code> can be called
	 * multiple times, as can {@link #stream}.
	 */
	@Override
	public Iterator<Variant> iterator() {
		Reset();
		return new Iterator<Variant>() {
			@Override
			public boolean hasNext() {
				return hasMoreElements();
			}

			@Override
			public Variant next() {
				return nextElement();
			}
		};
	}

	/**
	 * Get a Stream from this object.
	 *
	 * @return Stream&lt;Variant&gt;.
	 */
	public Stream<Variant> stream() {
		return StreamSupport.stream(this.spliterator(), false);
	}

	/**
	 * This should be private and wrapped to protect JNI layer.
	 *
	 * @param receiverArray
	 * @return Returns the next variant object pointer as an int from windows layer
	 */
	public native int Next(Variant[] receiverArray);

	/**
	 * This should be private and wrapped to protect JNI layer.
	 *
	 * @param count number to skip
	 */
	public native void Skip(int count);

	/**
	 * This should be private and wrapped to protect JNI layer
	 */
	public native void Reset();

	/**
	 * now private so only this object can access was: call this to explicitly release the com object
	 * before gc
	 *
	 */
	private native void release();

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

	/*
	 * (non-Javadoc)
	 *
	 * @see com.jacob.com.JacobObject#safeRelease()
	 */
	@Override
	public void safeRelease() {
		super.safeRelease();
		if (m_pIEnumVARIANT != 0) {
			this.release();
			m_pIEnumVARIANT = 0;
		}
		else {
			// looks like a double release
			if (isDebugEnabled()) {
				debug(this.getClass().getName() + ":" + this.hashCode() + " double release");
			}
		}
	}

}