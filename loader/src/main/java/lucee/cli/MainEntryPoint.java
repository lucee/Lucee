/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
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
 **/
package lucee.cli;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class MainEntryPoint {

	public static void main(final String[] args) throws Throwable {
		File libDir = new File("./").getCanonicalFile();
		System.out.println(libDir);

		// Fix for tomcat
		if (libDir.getName().equals(".") || libDir.getName().equals("..")) libDir = libDir.getParentFile();

		File[] children = libDir.listFiles(new ExtFilter());
		if (children.length < 2) {
			libDir = new File(libDir, "lib");
			children = libDir.listFiles(new ExtFilter());
		}

		final URL[] urls = new URL[children.length];
		System.out.println("Loading Jars");
		for (int i = 0; i < children.length; i++) {
			urls[i] = new URL("jar:file://" + children[i] + "!/");
			System.out.println("- " + urls[i]);
		}
		System.out.println();
		URLClassLoader cl = null;
		try {
			cl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
			final Class<?> cli = cl.loadClass("lucee.cli.CLI");
			final Method main = cli.getMethod("main", new Class[] { String[].class });
			main.invoke(null, new Object[] { args });
		}
		finally {
			if (cl != null) try {
				cl.close();
			}
			catch (final IOException ioe) {}
		}
	}

	public static class ExtFilter implements FilenameFilter {

		private final String ext = ".jar";

		@Override
		public boolean accept(final File dir, final String name) {
			return name.toLowerCase().endsWith(ext);
		}

	}
}