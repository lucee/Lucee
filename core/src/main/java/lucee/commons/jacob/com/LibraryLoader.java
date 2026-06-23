/*
 * Copyright (c) 1999-2007 Sourceforge JACOB Project.
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
import java.util.HashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Utility class to centralize the way in which the jacob JNI library is loaded.
 * <p>
 *
 * This supports defining the path or library name using system properties or a custom resource
 * file. If desired, jacob can auto-detect the correct version of the DLL for 32 or 64 bit windows,
 * as long as you have named them differently.
 *
 * <ol>
 * <li>If system property {@link #JACOB_DLL_PATH} is defined, the file located there will be loaded
 * as the jacob dll using System.load().</li>
 *
 * <li>If system property {@link #JACOB_DLL_NAME} is defined, the file located there will be loaded
 * as the jacob dll.</li>
 * <li>If system property {@link #JACOB_DLL_NAME_X86} and {@link #JACOB_DLL_NAME_X64} are defined,
 * the file located there will be loaded as the jacob dll, depending on the version of Windows.</li>
 *
 * <li>If {@link #JACOB_DLL_NAME} is defined in the {@code com.jacob.com.JacobLibraryLoader}
 * resource file, the specified dll will be loaded from the {@code java.library.path}.</li>
 * <li>If {@link #JACOB_DLL_NAME_X86} and {@link #JACOB_DLL_NAME_X64} are defined in the
 * {@code com.jacob.com.JacobLibraryLoader} resource file, the specified dll will be loaded from the
 * {@code java.library.path}, depending on the version of Windows.</li>
 *
 * <li>If none of the above are true, the default is to load the library named
 * "jacob-&lt;version&gt;-&lt;arch&gt;" (or "jacob-&lt;version&gt;-&lt;arch&gt;.dll") from the
 * {@code java.library.path}.</li>
 * </ol>
 *
 * The standard behavior for most applications is that {@code LoadLibrary()} will be called to load
 * the dll. {@code LoadLibary()} searches directories specified in the variable
 * {@code java.library.path}. This is why most test cases specify -Djava.library.path in their
 * command line arguments.
 * <p>
 * JACOB_DLL_PATH submitted sourceforge ticket 1493647 Added 1.11 <br>
 * JACOB_DLL_NAME, JACOB_DLL_NAME_32, JACOB_DLL_NAME_64 submitted sourceforge ticket 1845039 Added
 * 1.14M7
 *
 * @author Scott Dickerson (sjd78)
 * @author Jason Smith
 */
public final class LibraryLoader {
	/**
	 * Name of system property (currently <tt>jacob.dll.path</tt>) that may contain an absolute path to
	 * the JNI library.
	 */
	public static final String JACOB_DLL_PATH = "jacob.dll.path";

	/**
	 * Name of system property (currently <tt>jacob.dll.name</tt>) that may contain an alternate name
	 * for the JNI library (default is 'jacob').
	 */
	public static final String JACOB_DLL_NAME = "jacob.dll.name";

	/**
	 * Name of system property (currently <tt>jacob.dll.name</tt>) that may contain an alternate name
	 * for the JNI library (default is 'jacob'), 32 bit windows.
	 */
	public static final String JACOB_DLL_NAME_X86 = "jacob.dll.name.x86";

	/**
	 * Name of system property (currently <tt>jacob.dll.name</tt>) that may contain an alternate name
	 * for the JNI library (default is 'jacob'), 64 bit windows.
	 */
	public static final String JACOB_DLL_NAME_X64 = "jacob.dll.name.x64";

	/**
	 * Appended to "jacob" when building DLL name This string must EXACTLY match the string in the
	 * build.xml file
	 */
	public static final String DLL_NAME_MODIFIER_32_BIT = "x86";
	/**
	 * Appended to "jacob" when building DLL name This string must EXACTLY match the string in the
	 * build.xml file
	 */
	public static final String DLL_NAME_MODIFIER_64_BIT = "x64";

	/**
	 * Load the jacob dll either from an absolute path or by a library name, both of which may be
	 * defined in various ways.
	 *
	 * @throws UnsatisfiedLinkError if the library does not exist.
	 */
	public static void loadJacobLibrary() {
		// In some cases, a library that uses Jacob won't be able to set system
		// properties
		// prior to Jacob being loaded. The resource bundle provides an
		// alternate way to
		// override DLL name or path that will be loaded with Jacob regardless
		// of other
		// initialization order.
		ResourceBundle resources = null;
		Set<String> keys = new HashSet<String>();
		try {
			resources = ResourceBundle.getBundle(LibraryLoader.class.getName(), Locale.getDefault(), LibraryLoader.class.getClassLoader());
			for (Enumeration<String> i = resources.getKeys(); i.hasMoreElements();) {
				String key = i.nextElement();
				keys.add(key);
			}
		}
		catch (MissingResourceException e) {
			// Do nothing. Expected.
		}

		// First, check for a defined PATH. System property overrides resource
		// bundle.
		String path = System.getProperty(JACOB_DLL_PATH);
		if (path == null && resources != null && keys.contains(JACOB_DLL_PATH)) {
			path = (String) resources.getObject(JACOB_DLL_PATH);
		}

		if (path != null) {
			JacobObject.debug("Loading library " + path + " using System.loadLibrary ");
			System.load(path);
		}
		else {
			// Path was not defined, so use the OS mechanism for loading
			// libraries.
			// Check for a defined NAME. System property overrides resource
			// bundle.
			String name = null;

			if (System.getProperty(JACOB_DLL_NAME) != null) {
				name = System.getProperty(JACOB_DLL_NAME);
			}
			else if (System.getProperty(JACOB_DLL_NAME_X86) != null && shouldLoad32Bit()) {
				name = System.getProperty(JACOB_DLL_NAME_X86);
			}
			else if (System.getProperty(JACOB_DLL_NAME_X64) != null && !shouldLoad32Bit()) {
				name = System.getProperty(JACOB_DLL_NAME_X64);
			}
			else if (resources != null && keys.contains(JACOB_DLL_NAME)) {
				name = resources.getString(JACOB_DLL_NAME);
			}
			else if (resources != null && keys.contains(JACOB_DLL_NAME_X86) && shouldLoad32Bit()) {
				name = resources.getString(JACOB_DLL_NAME_X86);
			}
			else if (resources != null && keys.contains(JACOB_DLL_NAME_X64) && !shouldLoad32Bit()) {
				name = resources.getString(JACOB_DLL_NAME_X64);
			}
			else {
				// No alternate NAME or PATH was defined, so use the default.
				// We will almost always end up here.
				name = getPreferredDLLName();
			}

			JacobObject.debug("Loading library " + name + " using System.loadLibrary ");
			// System.out.println("Loading " + name);
			System.loadLibrary(name);
		}
	}

	/**
	 * Developer note: This method MUST be synchronized with the DLL names created as part of the build
	 * process in build.xml
	 * <p>
	 * The DLL name is "jacob &lt;PLATFORM&gt; .release"
	 *
	 * @return the preferred name of the DLL adjusted for this platform and version without the ".dll"
	 *         extension
	 */
	public static String getPreferredDLLName() {
		if (shouldLoad32Bit()) {
			return "jacob" + "-" + JacobReleaseInfo.getBuildVersion() + "-" + DLL_NAME_MODIFIER_32_BIT;
		}
		else {
			return "jacob" + "-" + JacobReleaseInfo.getBuildVersion() + "-" + DLL_NAME_MODIFIER_64_BIT;
		}
	}

	/**
	 * Detects whether this is a 32-bit JVM.
	 *
	 * @return {@code true} if this is a 32-bit JVM.
	 */
	protected static boolean shouldLoad32Bit() {
		// This guesses whether we are running 32 or 64 bit Java.
		// This works for Sun and IBM JVMs version 5.0 or later.
		// May need to be adjusted for non-Sun JVMs.

		String bits = System.getProperty("sun.arch.data.model", "?");
		if (bits.equals("32")) return true;
		else if (bits.equals("64")) return false;

		// this works for jRocket
		String arch = System.getProperty("java.vm.name", "?");
		if (arch.toLowerCase().indexOf("64-bit") >= 0) return false;

		return true;
	}
} // LibraryLoader
