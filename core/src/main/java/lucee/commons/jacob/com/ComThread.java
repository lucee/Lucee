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
 * Represents a COM level thread This is an abstract class because all the methods are static and no
 * instances are ever created.
 */
public abstract class ComThread {
	private static final int MTA = 0x0;

	private static final int STA = 0x2;

	/**
	 * Comment for <code>haveSTA</code>
	 */
	public static boolean haveSTA = false;

	/**
	 * Comment for <code>mainSTA</code>
	 */
	public static MainSTA mainSTA = null;

	/**
	 * Initialize the current java thread to be part of the Multi-threaded COM Apartment
	 */
	public static synchronized void InitMTA() {
		InitMTA(false);
	}

	/**
	 * Initialize the current java thread to be an STA
	 */
	public static synchronized void InitSTA() {
		InitSTA(false);
	}

	/**
	 * Initialize the current java thread to be part of the Multi-threaded COM Apartment, if
	 * createMainSTA is true, create a separate MainSTA thread that will house all Apartment Threaded
	 * components
	 * 
	 * @param createMainSTA
	 */
	public static synchronized void InitMTA(boolean createMainSTA) {
		Init(createMainSTA, MTA);
	}

	/**
	 * Initialize the current java thread to be an STA COM Apartment, if createMainSTA is true, create a
	 * separate MainSTA thread that will house all Apartment Threaded components
	 * 
	 * @param createMainSTA
	 */
	public static synchronized void InitSTA(boolean createMainSTA) {
		Init(createMainSTA, STA);
	}

	/**
	 * 
	 */
	public static synchronized void startMainSTA() {
		mainSTA = new MainSTA();
		haveSTA = true;
	}

	/**
	 * 
	 */
	public static synchronized void quitMainSTA() {
		if (mainSTA != null) mainSTA.quit();
	}

	/**
	 * Initialize the current java thread to be part of the MTA/STA COM Apartment
	 * 
	 * @param createMainSTA
	 * @param mode
	 */
	public static synchronized void Init(boolean createMainSTA, int mode) {
		if (createMainSTA && !haveSTA) {
			// if the current thread is going to be in the MTA and there
			// is no STA thread yet, then create a main STA thread
			// to avoid COM creating its own
			startMainSTA();
		}
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: before Init: " + mode);
		}
		doCoInitialize(mode);
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: after Init: " + mode);
		}
		ROT.addThread();
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: after ROT.addThread: " + mode);
		}
	}

	/**
	 * Call CoUninitialize to release this java thread from COM
	 */
	public static synchronized void Release() {
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: before clearObjects");
		}
		ROT.clearObjects();
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: before UnInit");
		}
		doCoUninitialize();
		if (JacobObject.isDebugEnabled()) {
			JacobObject.debug("ComThread: after UnInit");
		}
	}

	/**
	 * @deprecated the java model leave the responsibility of clearing up objects to the Garbage
	 *             Collector. Our programming model should not require that the user specifically remove
	 *             object from the thread.
	 * 
	 *             This will remove an object from the ROT
	 * @param o
	 */
	@Deprecated
	public static synchronized void RemoveObject(JacobObject o) {
		ROT.removeObject(o);
	}

	/**
	 * @param threadModel
	 */
	public static native void doCoInitialize(int threadModel);

	/**
	 * 
	 */
	public static native void doCoUninitialize();

	/**
	 * load the Jacob DLL. We do this in case COMThread is called before any other reference to one of
	 * the JacboObject subclasses is made.
	 */
	static {
		LibraryLoader.loadJacobLibrary();
	}
}