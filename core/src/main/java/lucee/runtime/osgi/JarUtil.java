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
package lucee.runtime.osgi;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

import lucee.commons.io.IOUtil;
import lucee.commons.io.log.LogUtil;
import lucee.commons.io.res.Resource;
import lucee.runtime.engine.ThreadLocalPageContext;

public class JarUtil {

	public static final String[] DEFAULT_IGNORES = new String[] { "java.*"

	};

	/**
	 * 
	 * @param res
	 * @param ignores ".*" add the end includes all sub directories
	 * @return
	 * @throws IOException
	 */
	public static Set<String> getExternalImports(Resource res, String[] ignores) throws IOException {
		InputStream is = res.getInputStream();
		try {
			return getExternalImports(is, ignores);
		}
		finally {
			IOUtil.close(is);
		}
	}

	public static Set<String> getExternalImports(InputStream is, String[] ignores) {
		Set<String> imports = new HashSet<>();
		Set<String> classNames = new HashSet<>();
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(is);
			ZipEntry entry;
			String name;
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory() || !entry.getName().endsWith(".class")) continue;
				name = entry.getName();
				name = name.replace('/', '.');
				name = name.substring(0, name.length() - 6);
				classNames.add(name);
				_getExternalImports(imports, zis, ignores);
			}
		}
		catch (IOException ioe) {
			LogUtil.log(ThreadLocalPageContext.get(), JarUtil.class.getName(), ioe);
		}
		finally {
			try {
				IOUtil.close(zis);
			}
			catch (IOException ioe) {
				LogUtil.log(ThreadLocalPageContext.get(), JarUtil.class.getName(), ioe);
			}
		}

		// remove all class from this jar
		Iterator<String> it = classNames.iterator();
		String cn;
		while (it.hasNext()) {
			cn = it.next();
			imports.remove(cn);
		}

		// create package set
		Set<String> importPackages = new HashSet<>();
		it = imports.iterator();
		int index;
		while (it.hasNext()) {
			cn = it.next();
			index = cn.lastIndexOf('.');
			if (index == -1) continue; // no package
			importPackages.add(cn.substring(0, index));
		}
		return importPackages;
	}

	private static void _getExternalImports(Set<String> imports, InputStream src, String[] ignores) throws IOException {
		final ClassReader reader = new ClassReader(src);
		final Remapper remapper = new Collector(imports, ignores);
		final ClassVisitor inner = new EmptyVisitor();
		final RemappingClassAdapter visitor = new RemappingClassAdapter(inner, remapper);
		reader.accept(visitor, 0);
	}

	public static class Collector extends Remapper {

		private final Set<String> imports;
		private final String[] ignores;

		public Collector(final Set<String> imports, String[] ignores) {
			this.imports = imports;
			this.ignores = ignores;
		}

		@Override
		public String mapDesc(final String desc) {
			if (desc.startsWith("L")) {
				this.addType(desc.substring(1, desc.length() - 1));
			}
			return super.mapDesc(desc);
		}

		@Override
		public String[] mapTypes(final String[] types) {
			for (final String type: types) {
				this.addType(type);
			}
			return super.mapTypes(types);
		}

		@Override
		public String mapType(final String type) {
			this.addType(type);
			return type;
		}

		private void addType(final String type) {
			String className = type.replace('/', '.');
			int index = className.lastIndexOf('.');
			if (index == -1) return;// class with no package
			String ignore, pack;

			for (int i = 0; i < DEFAULT_IGNORES.length; i++) {
				ignore = DEFAULT_IGNORES[i];
				// also ignore sub directories
				if (ignore.endsWith(".*")) {
					ignore = ignore.substring(0, ignore.length() - 1);
					if (className.startsWith(ignore)) return;
				}
				else {
					pack = className.substring(0, index);
					if (pack.equals(ignore)) return;
				}
			}

			for (int i = 0; i < ignores.length; i++) {
				ignore = ignores[i];
				// also ignore sub directories
				if (ignore.endsWith(".*")) {
					ignore = ignore.substring(0, ignore.length() - 1);
					if (className.startsWith(ignore)) return;
				}
				else {
					pack = className.substring(0, index);
					if (pack.equals(ignore)) return;
				}

			}
			this.imports.add(className);
		}
	}

	private static class EmptyVisitor extends ClassVisitor implements Opcodes {
		public EmptyVisitor() {
			super(ASM4);
		}
	}

}