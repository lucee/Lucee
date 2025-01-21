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
		variables.SEP = Server.separator.file;
		variables.parent = getTempDirectory() & "directoryDelete" & sep;
		if (directoryExists(parent))
			directoryDelete(parent,true);
		directoryCreate(parent);
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

	public function testDirectoryNonEmptyDelete() localMode="modern" {	
		dirEmpty = parent & "/notEmpty/";
		directoryCreate(dirEmpty);
		getTempFile(dirEmpty,"empty","txt");
		var error = "";
		try {
			directoryDelete(dirEmpty);
		} catch (e){
			error = e.message;
		}
		expect( error ).notToBe( "" );
	}

	public function testDeleteDirectoryMissing() localMode="modern" {	
		dirMissing = parent & "/missingDir/";
		directoryCreate(dirMissing);
		var handle = FileOpen( dirMissing );
		var res = handle.getResource();
		directoryDelete(dirMissing, true);

		var error = "";
		try {
			res.remove(false); // call directly to avoid exists check in directoryDelete
		} catch (e){
			error = e.message;
		} finally {
			FileClose(handle);
		}
		expect( error ).notToBe( "" );
	}

	public function testDirectoryNonEmptyDeleteLocked() localMode="modern" {
		if (!isWindows()) return;
		dirEmpty = parent & "/notEmptyResLocked/";
		directoryCreate(dirEmpty);
		var src = getTempFile(dirEmpty,"empty-locked","txt");
		var error = "";
		try {
			var f = createObject("java", "java.io.File").init(src);
			var fos =  createObject("java", "java.io.FileOutputStream").init(f); // lock file
			directoryDelete(dirEmpty, true);
		} catch (e){
			error = e.message;
		} finally {
			fos.close();
		}
		expect( error ).notToBe( "" );
	}

	private function isWindows(){
		return (server.os.name contains "windows");
	}

}