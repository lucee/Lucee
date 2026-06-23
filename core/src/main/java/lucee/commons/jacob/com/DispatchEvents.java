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
 * This class creates the scaffolding for event callbacks. Every instance of tis acts as a wrapper
 * around some java object that wants callbacks from the microsoft side. It represents the
 * connection between Java and COM for callbacks.
 * <p>
 * The callback mechanism will take any event that it receives and try and find a java method with
 * the same name that accepts the Variant... as a parameter. It will then wrap the call back data in
 * the Variant array and call the java method of the object that this DispatchEvents object was
 * initialized with.
 * <p>
 * Instances of this class are created with "sink object" that will receive the event messages. The
 * sink object is wrapped in an Invocation handler that actually receives the messages and then
 * forwards them on to the "sink object". The constructors recognize when an instance of
 * InvocationProxy is passed in and do not create a new InvocationProxy as a wrapper. They instead
 * use the passed in InvocationProxy.
 * 
 */
public class DispatchEvents extends JacobObject {

	/**
	 * pointer to an MS data struct. The COM layer knows the name of this variable and puts the windows
	 * memory pointer here.
	 */
	long m_pConnPtProxy = 0;

	/**
	 * the wrapper for the event sink. This object is the one that will be sent a message when an event
	 * occurs in the MS layer. Normally, the InvocationProxy will forward the messages to a wrapped
	 * object that it contains.
	 */
	InvocationProxy mInvocationProxy = null;

	/**
	 * This is the most commonly used constructor.
	 * <p>
	 * Creates the event callback linkage between the the MS program represented by the Dispatch object
	 * and the Java object that will receive the callback.
	 * <p>
	 * Can be used on any object that implements IProvideClassInfo.
	 * 
	 * @param sourceOfEvent Dispatch object who's MS app will generate callbacks
	 * @param eventSink Java object that wants to receive the events
	 */
	public DispatchEvents(Dispatch sourceOfEvent, Object eventSink) {
		this(sourceOfEvent, eventSink, null);
	}

	/**
	 * None of the samples use this constructor.
	 * <p>
	 * Creates the event callback linkage between the the MS program represented by the Dispatch object
	 * and the Java object that will receive the callback.
	 * <p>
	 * Used when the program doesn't implement IProvideClassInfo. It provides a way to find the TypeLib
	 * in the registry based on the programId. The TypeLib is looked up in the registry on the path
	 * HKEY_LOCAL_MACHINE/SOFTWARE/Classes/CLSID/(CLID drived from progid)/ProgID/Typelib
	 * 
	 * @param sourceOfEvent Dispatch object who's MS app will generate callbacks
	 * @param eventSink Java object that wants to receive the events
	 * @param progId program id in the registry that has a TypeLib subkey. The progrId is mapped to a
	 *            CLSID that is they used to look up the key to the Typelib
	 */
	public DispatchEvents(Dispatch sourceOfEvent, Object eventSink, String progId) {
		this(sourceOfEvent, eventSink, progId, null);
	}

	/**
	 * Creates the event callback linkage between the the MS program represented by the Dispatch object
	 * and the Java object that will receive the callback.
	 * <p>
	 * This method was added because Excel doesn't implement IProvideClassInfo and the registry entry
	 * for Excel.Application doesn't include a typelib key.
	 * 
	 * <pre>
	 * DispatchEvents de = new DispatchEvents(someDispatch, someEventHAndler, &quot;Excel.Application&quot;, &quot;C:\\Program Files\\Microsoft Office\\OFFICE11\\EXCEL.EXE&quot;);
	 * </pre>
	 * 
	 * @param sourceOfEvent Dispatch object who's MS app will generate callbacks
	 * @param eventSink Java object that wants to receive the events
	 * @param progId , mandatory if the typelib is specified
	 * @param typeLib The location of the typelib to use
	 */
	public DispatchEvents(Dispatch sourceOfEvent, Object eventSink, String progId, String typeLib) {
		if (JacobObject.isDebugEnabled()) {
			System.out.println("DispatchEvents: Registering " + eventSink + "for events ");
		}
		if (eventSink instanceof InvocationProxy) {
			mInvocationProxy = (InvocationProxy) eventSink;
		}
		else {
			mInvocationProxy = getInvocationProxy(eventSink);
		}
		if (mInvocationProxy != null) {
			init3(sourceOfEvent, mInvocationProxy, progId, typeLib);
		}
		else {
			if (JacobObject.isDebugEnabled()) {
				JacobObject.debug("Cannot register null event sink for events");
			}
			throw new IllegalArgumentException("Cannot register null event sink for events");
		}
	}

	/**
	 * Returns an instance of the proxy configured with pTargetObject as its target
	 * 
	 * @param pTargetObject
	 * @return InvocationProxy an instance of the proxy this DispatchEvents will send to the COM layer
	 */
	protected InvocationProxy getInvocationProxy(Object pTargetObject) {
		InvocationProxy newProxy = new InvocationProxyAllVariants();
		newProxy.setTarget(pTargetObject);
		return newProxy;
	}

	/**
	 * hooks up a connection point proxy by progId event methods on the sink object will be called by
	 * name with a signature of <name>(Variant[] args)
	 * 
	 * You must specify the location of the typeLib.
	 * 
	 * @param src dispatch that is the source of the messages
	 * @param sink the object that will receive the messages
	 * @param progId optional program id. most folks don't need this either
	 * @param typeLib optional parameter for those programs that don't register their type libs (like
	 *            Excel)
	 */
	private native void init3(Dispatch src, Object sink, String progId, String typeLib);

	/**
	 * now private so only this object can asccess was: call this to explicitly release the com object
	 * before gc
	 * 
	 */
	private native void release();

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
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
		if (mInvocationProxy != null) {
			mInvocationProxy.setTarget(null);
		}
		mInvocationProxy = null;
		super.safeRelease();
		if (m_pConnPtProxy != 0) {
			release();
			m_pConnPtProxy = 0;
		}
		else {
			// looks like a double release
			if (isDebugEnabled()) {
				debug("DispatchEvents:" + this.hashCode() + " double release");
			}
		}
	}

}
