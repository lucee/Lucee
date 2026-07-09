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

/**
 * If you need to pass a COM Dispatch object between STA threads, you have to marshall the
 * interface. This class is used as follows: the STA that creates the Dispatch object must construct
 * an instance of this class. Another thread can then call toDispatch() on that instance and get a
 * Dispatch pointer which has been marshalled. WARNING: You can only call toDispatch() once! If you
 * need to call it multiple times (or from multiple threads) you need to construct a separate
 * DispatchProxy instance for each such case!
 */
public class DispatchProxy extends JacobObject {
	/**
	 * Comment for <code>m_pStream</code>
	 */
	public long m_pStream;

	/**
	 * Marshals the passed in dispatch into the stream
	 * 
	 * @param localDispatch
	 */
	public DispatchProxy(Dispatch localDispatch) {
		MarshalIntoStream(localDispatch);
	}

	/**
	 * 
	 * @return Dispatch the dispatch retrieved from the stream
	 */
	public Dispatch toDispatch() {
		return MarshalFromStream();
	}

	private native void MarshalIntoStream(Dispatch d);

	private native Dispatch MarshalFromStream();

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
	public void finalize() {
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
		if (m_pStream != 0) {
			release();
			m_pStream = 0;
		}
		else {
			// looks like a double release
			if (isDebugEnabled()) {
				debug(this.getClass().getName() + ":" + this.hashCode() + " double release");
			}
		}
	}
}