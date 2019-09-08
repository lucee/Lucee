/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.instrumentation;

import lucee.commons.lang.StringUtil;

/**
 * Utilities for dealing with different Java vendors.
 */
public enum JavaVendor {
	IBM("com.ibm.tools.attach.VirtualMachine"), SUN("com.sun.tools.attach.VirtualMachine"),
	// When in doubt, try the Sun implementation.
	OTHER("com.sun.tools.attach.VirtualMachine");

	static {
		String vendor = System.getProperty("java.vendor");
		if (StringUtil.containsIgnoreCase(vendor, "SUN MICROSYSTEMS")) {
			_vendor = SUN;
		}
		else if (StringUtil.containsIgnoreCase(vendor, "IBM")) {
			_vendor = IBM;
		}
		else {
			_vendor = OTHER;
		}
	}

	private static final JavaVendor _vendor;
	private String _virtualMachineClass = null;

	private JavaVendor(String vmClass) {
		_virtualMachineClass = vmClass;
	}

	/**
	 * This static worker method returns the current Vendor.
	 */
	public static JavaVendor getCurrentVendor() {
		return _vendor;
	}

	/**
	 * This static worker method returns <b>true</b> if the current implementation is IBM.
	 */
	public boolean isIBM() {
		return _vendor == IBM;
	}

	/**
	 * This static worker method returns <b>true</b> if the current implementation is Sun.
	 */
	public boolean isSun() {
		return _vendor == SUN;
	}

	public String getVirtualMachineClassName() {
		return _virtualMachineClass;
	}
}