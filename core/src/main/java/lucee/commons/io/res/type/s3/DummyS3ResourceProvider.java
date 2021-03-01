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
package lucee.commons.io.res.type.s3;

import java.io.IOException;
import java.util.Map;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.ResourceProvider;
import lucee.commons.io.res.ResourceProviderPro;
import lucee.commons.io.res.Resources;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;

public final class DummyS3ResourceProvider implements ResourceProviderPro {

	private static final String S3 = "17AB52DE-B300-A94B-E058BD978511E39E";
	private static boolean tryToInstall = true;

	private static final long serialVersionUID = 3685913246889089664L;

	@Override
	public ResourceProvider init(String scheme, Map<String, String> arguments) {
		return this;
	}

	@Override
	public Resource getResource(String path) {
		throw notInstalledEL();
	}

	@Override
	public String getScheme() {
		return "s3";
	}

	@Override
	public Map<String, String> getArguments() {
		throw notInstalledEL();
	}

	@Override
	public void setResources(Resources resources) {}

	@Override
	public void unlock(Resource res) {}

	@Override
	public void lock(Resource res) throws IOException {}

	@Override
	public void read(Resource res) throws IOException {}

	@Override
	public boolean isCaseSensitive() {
		throw notInstalledEL();
	}

	@Override
	public boolean isModeSupported() {
		throw notInstalledEL();
	}

	@Override
	public boolean isAttributesSupported() {
		throw notInstalledEL();
	}

	@Override
	public char getSeparator() {
		throw notInstalledEL();
	}

	private PageException notInstalled() {
		return new ApplicationException("No S3 Resource installed!", "Check out the Extension Store in the Lucee Administrator for \"S3\".");
	}

	private PageRuntimeException notInstalledEL() {
		return new PageRuntimeException(notInstalled());
	}

}