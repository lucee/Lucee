<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.
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
		variables.parent=getDirectoryFromPath(getCurrentTemplatePath()) & name & Server.separator.file;
	}

	public function afterAll() {
		directorydelete(parent,true);
	}

	public function testDirectoryList() localMode="modern" {
		var SEP = Server.separator.file;
		lock name="testdirectoryList" timeout="1" throwontimeout="no" type="exclusive" {
			path = parent&createUUID();
			path2 = path&"#SEP#a";
			directoryCreate(path2);
			cffile( fixnewline=false, output="aaa", file="#path##SEP#b.txt", addnewline=true, action="write" );
			cffile( fixnewline=false, output="aaa", file="#path2##SEP#c.txt", addnewline=true, action="write" );

			//  recursive false 
			dir = directoryList(path);
			assertEquals(2,arrayLen(dir));
			assertEquals("#path##SEP#a,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));

			//  recursive true 
			dir = directoryList(path,true);
			assertEquals(3,arrayLen(dir));
			assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));

			//  type:directory 
			dir = directoryList(path:path,type:'directory');
			assertEquals(1,arrayLen(dir));
			assertEquals("#path##SEP#a",arrayToList(dir));
			
			//  type:file 
			dir = directoryList(path:path,type:'file');
			assertEquals(1,arrayLen(dir));
			assertEquals("#path##SEP#b.txt",arrayToList(dir));
			
			//  list info 
			dir = directoryList(path,true,"name");
			assertEquals(3,arrayLen(dir));
			assertEquals("a,b.txt,c.txt",listSort(arrayToList(dir),'textnocase'));
			dir = directoryList(path,true,"path");
			assertEquals(3,arrayLen(dir));
			assertEquals("#path##SEP#a,#path##SEP#a#SEP#c.txt,#path##SEP#b.txt",listSort(arrayToList(dir),'textnocase'));
			dir = directoryList(path,true,"query");
			directoryDelete(path,true);
		}
	}

}