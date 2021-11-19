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
package lucee.runtime.config.component;

import lucee.commons.io.res.Resource;
import lucee.runtime.config.Constants;
import lucee.runtime.config.ConfigFactory;

public class ComponentFactory {

	/**
	 * this method deploy all components for org.lucee.cfml
	 * 
	 * @param dir components directory
	 * @param doNew redeploy even the file exist, this is set to true when a new version is started
	 */
	public static void deploy(Resource dir, boolean doNew) {
		String path = "/resource/component/" + (Constants.DEFAULT_PACKAGE.replace('.', '/')) + "/";

		delete(dir, "Base");
		deploy(dir, path, doNew, "HelperBase");
		deploy(dir, path, doNew, "Feed");
		deploy(dir, path, doNew, "Ftp");
		deploy(dir, path, doNew, "Http");
		deploy(dir, path, doNew, "Mail");
		deploy(dir, path, doNew, "Query");
		deploy(dir, path, doNew, "Result");
		deploy(dir, path, doNew, "Administrator");

		// orm
		{
			Resource ormDir = dir.getRealResource("orm");
			String ormPath = path + "orm/";
			if (!ormDir.exists()) ormDir.mkdirs();
			deploy(ormDir, ormPath, doNew, "IEventHandler");
			deploy(ormDir, ormPath, doNew, "INamingStrategy");
		}
		// test
		{
			Resource testDir = dir.getRealResource("test");
			String testPath = path + "test/";
			if (!testDir.exists()) testDir.mkdirs();

			deploy(testDir, testPath, doNew, "LuceeTestSuite");
			deploy(testDir, testPath, doNew, "LuceeTestSuiteRunner");
			deploy(testDir, testPath, doNew, "LuceeTestCase");
		}

	}

	private static void deploy(Resource dir, String path, boolean doNew, String name) {
		Resource f = dir.getRealResource(name + ".cfc");
		if (!f.exists() || doNew) ConfigFactory.createFileFromResourceEL(path + name + ".cfc", f);
	}

	private static void delete(Resource dir, String name) {
		Resource f = dir.getRealResource(name + ".cfc");
		if (f.exists()) f.delete();
	}
}