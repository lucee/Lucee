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
 * A class that implements a Single Threaded Apartment. Users will subclass this and override
 * OnInit() and OnQuit() where they will create and destroy a COM component that wants to run in an
 * STA other than the main STA.
 */
public class STA extends Thread {
	/**
	 * referenced by STA.cpp
	 */
	public int threadID;

	/**
	 * constructor for STA
	 */
	public STA() {
		start(); // start the thread
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		// init COM
		ComThread.InitSTA();
		if (OnInit()) {
			// this call blocks in the win32 message loop
			// until quitMessagePump is called
			doMessagePump();
		}
		OnQuit();
		// uninit COM
		ComThread.Release();
	}

	/**
	 * Override this method to create and initialize any COM component that you want to run in this
	 * thread. If anything fails, return false to terminate the thread.
	 * 
	 * @return always returns true
	 */
	public boolean OnInit() {
		return true;
	}

	/**
	 * Override this method to destroy any resource before the thread exits and COM in uninitialized
	 */
	public void OnQuit() {
		// there is nothing to see here
	}

	/**
	 * calls quitMessagePump
	 */
	public void quit() {
		quitMessagePump();
	}

	/**
	 * run a message pump for the main STA
	 */
	public native void doMessagePump();

	/**
	 * quit message pump for the main STA
	 */
	public native void quitMessagePump();

	/**
	 * STA isn't a subclass of JacobObject so a reference to it doesn't load the DLL without this
	 */
	static {
		LibraryLoader.loadJacobLibrary();
	}
}