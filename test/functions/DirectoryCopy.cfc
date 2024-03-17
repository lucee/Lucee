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
		variables.name = ListFirst(ListLast(getCurrentTemplatePath(),"\/"),".");
		variables.dir = getDirectoryFromPath(getCurrentTemplatePath())&name&"/";
	}

	public function afterAll() {
		directorydelete(dir,true);
	}

	public function testdirectoryCopy() localMode="modern" {
		//  inital create 
		cfdirectory( mode=777, directory="#dir#inc", action="create" );
		cffile( file="#dir#/inc/test1.txt", action="write" output="hello1");
		cffile( file="#dir#/inc/abra.txt", action="write" output="hello1");
		cfdirectory( mode=777, directory="#dir#inc/empty", action="create" )
		cfdirectory( mode=777, directory="#dir#inc/testempty", action="create" );
		cfdirectory( mode=777, directory="#dir#inc/sub", action="create" );
		cffile( file="#dir#inc/sub/test3.txt", action="write" output="hello2");

		// copy not recursive
		directoryCopy("#dir#inc","#dir#inc2");
		directory directory="#dir#inc2" action="list" name="qry" recurse="yes";
		assertEquals("abra.txt,test1.txt",listSort(valueList(qry.name),'textnocase'));

		// copy not recursive with filter "test*"
		directoryCopy("#dir#inc","#dir#inc4",false,"test*");
		directory directory="#dir#inc4" action="list" name="qry" recurse="yes";
		assertEquals("test1.txt",listSort(valueList(qry.name),'textnocase'));

		//  copy recursive
		directoryCopy("#dir#inc","#dir#inc3",true);
		directory directory="#dir#inc3" action="list" name="qry" recurse="yes";
		assertEquals("abra.txt,empty,sub,test1.txt,test3.txt,testempty",listSort(valueList(qry.name),'textnocase'));

		// copy recursive with filter "test*"
		directoryCopy("#dir#inc","#dir#inc5",true,"test*"); 
		directory directory="#dir#inc5" action="list" name="qry" recurse="yes";
		assertEquals("sub,test1.txt,test3.txt,testempty",listSort(valueList(qry.name),'textnocase'));
	}
}