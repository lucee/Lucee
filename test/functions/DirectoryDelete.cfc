<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 --->
 component extends="org.lucee.cfml.test.LuceeTestCase" {

	public function beforeAll() {
		variables.name=ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.parent=getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
	}

	public function afterAll() {
		directorydelete(parent,true);
	}

	public function testDirectoryDelete() localMode="modern" {
		//  begin old test code 
		lock name="testdirectoryDelete" timeout="1" throwontimeout="no" type="exclusive" {
			dir = parent&createUUID();
			directoryCreate(dir);
			directorydelete(dir);
			try {
				directorydelete(dir);
				fail("must throw:does !exist");
			} catch (any cfcatch) {
			}
			dir2 = dir&"/a/b/c/";
			directoryCreate(dir2);
			try {
				directorydelete(dir);
				fail("must throw:The specified directory ... could !be deleted.");
			} catch (any cfcatch) {
			}
			try {
				directorydelete(dir,false);
				fail("must throw:The specified directory ... could !be deleted.");
			} catch (any cfcatch) {
			}
			directorydelete(dir,true);
		}
	}

}