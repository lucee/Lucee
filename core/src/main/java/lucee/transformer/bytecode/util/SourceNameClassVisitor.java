/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
package lucee.transformer.bytecode.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import lucee.commons.io.res.filter.ExtensionResourceFilter;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.config.Constants;
import lucee.runtime.functions.system.ExpandPath;
import lucee.runtime.type.util.ListUtil;

public class SourceNameClassVisitor extends ClassVisitor {

	private SourceInfo source;

	private ExtensionResourceFilter filter;

	public SourceNameClassVisitor(Config config, int arg0, boolean onlyCFC) {
		super(arg0);
		if (onlyCFC) {
			filter = new ExtensionResourceFilter(Constants.getComponentExtensions(), true, true);
		}
		else {
			filter = new ExtensionResourceFilter(Constants.getExtensions(), true, true);
			// filter.addExtension(config.getComponentExtension());
		}

	}

	@Override
	public void visitSource(String source, String debug) {
		super.visitSource(source, debug);
		if (!StringUtil.isEmpty(source)) {

			String name = ListUtil.last(source, "/\\");

			if (filter.accept(name)) {
				// older than 4.2.1.008
				if (StringUtil.isEmpty(debug)) {
					this.source = new SourceInfo(name, source);

				}
				else {
					// in that case source holds the absolute path
					String[] arr = ListUtil.listToStringArray(debug, ';');
					String str;
					int index;
					Map<String, String> map = new HashMap<String, String>();
					for (int i = 0; i < arr.length; i++) {
						str = arr[i].trim();
						index = str.indexOf(':');
						if (index == -1) map.put(str.toLowerCase(), "");
						else map.put(str.substring(0, index).toLowerCase(), str.substring(index + 1));

					}
					String rel = map.get("rel");
					String abs = map.get("abs");
					if (StringUtil.isEmpty(abs)) abs = source;

					this.source = new SourceInfo(name, rel, abs);
				}
			}
		}
	}

	public static SourceInfo getSourceInfo(Config config, Class clazz, boolean onlyCFC) throws IOException {
		String name = "/" + clazz.getName().replace('.', '/') + ".class";
		InputStream in = clazz.getResourceAsStream(name);
		ClassReader classReader = new ClassReader(in);
		SourceNameClassVisitor visitor = new SourceNameClassVisitor(config, 4, onlyCFC);
		classReader.accept(visitor, 0);
		if (visitor.source == null || visitor.source.name == null) return null;
		return visitor.source;

	}

	public static class SourceInfo {

		public final String name;
		public final String relativePath;
		private String absolutePath;

		public SourceInfo(String name, String relativePath) {
			this(name, relativePath, null);
		}

		public SourceInfo(String name, String relativePath, String absolutePath) {
			this.name = name;
			this.relativePath = relativePath;
			this.absolutePath = absolutePath;
		}

		public String absolutePath(PageContext pc) {
			if (!StringUtil.isEmpty(absolutePath)) return absolutePath;
			try {
				absolutePath = ExpandPath.call(pc, relativePath);
			}
			catch (Exception e) {
			}
			return absolutePath;
		}

		public String toString() {
			return new StringBuilder("absolute-path:" + absolutePath + ";relative-path:" + relativePath + ";name:" + name).toString();
		}
	}

}