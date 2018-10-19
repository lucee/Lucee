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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;

import lucee.commons.io.IOUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.lang.ExceptionUtil;

import org.osgi.framework.BundleException;

public class BundleFile extends BundleInfo {

    private File file;

    public BundleFile(Resource file) throws IOException, BundleException {
	this(toFileResource(file));
    }

    public BundleFile(File file) throws IOException, BundleException {
	super(file);
	this.file = file;
    }

    public InputStream getInputStream() throws IOException {
	return new FileInputStream(file);
    }

    public File getFile() {
	return file;
    }

    public boolean hasClass(String className) throws IOException {
	JarFile jar = new JarFile(file);
	try {
	    return jar.getEntry(className.replace('.', '/') + ".class") != null;
	}
	finally {
	    IOUtil.closeEL(jar);
	}
    }

    /**
     * only return a instance if the Resource is a valid bundle, otherwise it returns null
     * 
     * @param res
     * @return
     */
    public static BundleFile newInstance(Resource res) {

	try {
	    BundleFile bf = new BundleFile(res);
	    if (bf.isBundle()) return bf;
	}
	catch (Throwable t) {
	    ExceptionUtil.rethrowIfNecessary(t);
	}

	return null;
    }
}