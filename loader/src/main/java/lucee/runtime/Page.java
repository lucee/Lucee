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
package lucee.runtime;

import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.lang.ref.SoftReference;

import lucee.commons.io.res.Resource;
import lucee.loader.engine.CFMLEngine;
import lucee.loader.engine.CFMLEngineFactory;
import lucee.runtime.component.ImportDefintion;
import lucee.runtime.exp.PageException;
import lucee.runtime.type.Struct;
import lucee.runtime.type.UDF;
import lucee.runtime.type.UDFProperties;
import lucee.runtime.util.IO;

/**
 * abstract Method for all generated Page Object
 */
public abstract class Page implements Serializable {

	private static final long serialVersionUID = 7844636300784565040L;

	private static final ImportDefintion[] NO_IMPORTS = new ImportDefintion[0];
	private static final CIPage[] NO_SUB_PAGES = new CIPage[0];

	public static boolean FALSE = false;
	public static boolean TRUE = true;

	private PageSource pageSource;
	private byte loadType;

	private Resource staticTextLocation;

	/**
	 * return version definition of the page
	 * 
	 * @return version
	 */
	public long getVersion() {
		return -1;
	}

	/**
	 * method to invoke a page
	 * 
	 * @param pc PageContext
	 * @throws Throwable throwable
	 * @return null
	 */
	public Object call(final PageContext pc) throws Throwable {
		return null;
	}

	/**
	 * return when the source file last time was modified
	 * 
	 * @return last modification of source file
	 */
	public long getSourceLastModified() {
		return 0;
	}

	/**
	 * return the time when the file was compiled
	 * 
	 * @return compile time
	 */
	public long getCompileTime() {
		return 0;
	}

	public String str(PageContext pc, int off, int len) throws IOException, PageException {
		if (staticTextLocation == null) {
			PageSource ps = getPageSource();
			Mapping m = ps.getMapping();
			staticTextLocation = m.getClassRootDirectory();
			staticTextLocation = staticTextLocation.getRealResource(ps.getJavaName() + ".txt");
		}
		CFMLEngine e = CFMLEngineFactory.getInstance();
		IO io = e.getIOUtil();

		Reader reader = io.getReader(staticTextLocation, e.getCastUtil().toCharset("UTF-8"));
		char[] carr = new char[len];
		try {
			if (off > 0) reader.skip(off);
			reader.read(carr);
		}
		finally {
			io.closeSilent(reader);
		}

		// print.e(carr);
		return new String(carr);
	}

	/**
	 * @param pageSource page source
	 */
	public void setPageSource(final PageSource pageSource) {
		this.pageSource = pageSource;
	}

	/**
	 * @return Returns the pageResource.
	 */
	public PageSource getPageSource() {
		return pageSource;
	}

	/**
	 * @return gets the load type
	 */
	public byte getLoadType() {
		return loadType;
	}

	/**
	 * @param loadType sets the load type
	 */
	public void setLoadType(final byte loadType) {
		this.loadType = loadType;
	}

	public Object udfCall(final PageContext pageContext, final UDF udf, final int functionIndex) throws Throwable {
		return null;
	}

	public void threadCall(final PageContext pageContext, final int threadIndex) throws Throwable {}

	public Object udfDefaultValue(final PageContext pc, final int functionIndex, final int argumentIndex, final Object defaultValue) {
		return null;
	}

	public ImportDefintion[] getImportDefintions() {
		return NO_IMPORTS;
	}

	public CIPage[] getSubPages() {
		return NO_SUB_PAGES;
	}

	public transient SoftReference<Struct> metaData;

	public UDFProperties[] udfs;
}