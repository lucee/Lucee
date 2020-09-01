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
package lucee.runtime.functions.system;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.FileWrapper;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ManifestRead {
	public static Struct call(PageContext pc, String str) throws PageException {
		Manifest manifest = null;
		// is it a file?
		Resource res = null;
		try {
			res = ResourceUtil.toResourceExisting(pc, str);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
		}

		// is a file!
		if (res != null) {
			try {
				// is it a jar?
				ZipFile zip = null;
				try {
					zip = new ZipFile(FileWrapper.toFile(res));
				}
				catch (Exception e) {/* no jar or invalid jar */}

				// it is a jar
				if (zip != null) {
					InputStream is = null;
					try {
						ZipEntry ze = zip.getEntry("META-INF/MANIFEST.MF");
						if (ze == null) throw new ApplicationException("zip file [" + str + "] has no entry with name [META-INF/MANIFEST.MF]");

						is = zip.getInputStream(ze);
						manifest = new Manifest(is);

					}
					finally {
						IOUtil.close(is);
						IOUtil.closeEL(zip);
					}
				}
				// is it a Manifest file?
				else {
					InputStream is = null;
					try {
						manifest = new Manifest(is = res.getInputStream());
					}
					finally {
						IOUtil.close(is);
					}
				}

			}
			catch (Throwable t) {
				ExceptionUtil.rethrowIfNecessary(t);
				throw Caster.toPageException(t);
			}
		}

		// was not a file
		if (manifest == null) {
			try {
				manifest = new Manifest(new ByteArrayInputStream(str.getBytes()));
			}
			catch (IOException e) {
				throw Caster.toPageException(e);
			}
		}

		Struct sct = new StructImpl();
		// set the main attributes
		set(sct, "main", manifest.getMainAttributes());

		// all the others
		Set<Entry<String, Attributes>> set = manifest.getEntries().entrySet();
		if (set.size() > 0) {
			Iterator<Entry<String, Attributes>> it = set.iterator();

			Struct sec = new StructImpl();
			sct.setEL("sections", sec);
			Entry<String, Attributes> e;
			while (it.hasNext()) {
				e = it.next();
				set(sec, e.getKey(), e.getValue());
			}
		}
		return sct;
	}

	private static void set(Struct parent, String key, Attributes attrs) throws PageException {
		Struct sct = new StructImpl();
		parent.set(key, sct);

		Iterator<Entry<Object, Object>> it = attrs.entrySet().iterator();
		Entry<Object, Object> e;
		while (it.hasNext()) {
			e = it.next();
			sct.setEL(Caster.toString(e.getKey()), StringUtil.unwrap(Caster.toString(e.getValue())));
		}
	}
}