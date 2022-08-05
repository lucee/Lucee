<!--- 
 *
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	
	function beforeAll() {
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#file-tests";
		afterAll();
		if(!directoryExists(path)) directoryCreate(path)
	}

	function testTouchOfNotExistingFile() localmode=true {
		name = "#path#\testnottouch.txt";

		assertFalse(fileExists(name));

		file action="touch" file=name;
		assertTrue(fileExists(name));

		file action="info" file=name variable="res";
		assertEquals(0,res.size);
	}

	function testTouchOfExistingFile() localmode=true {
		name = "#path#\testtouch.txt";

		assertFalse(fileExists(name));

		fileWrite(name,'Susi');

		file action="touch" file=name;
		assertTrue(fileExists(name));
		
		file action="info" file=name variable="res2";
		assertEquals(4,res2.size);
	}

	function testfileAction() localmode=true {
		
		testFile = "#path#\test.txt";

		// file write
		file action="write" file=testFile output="susi" addnewline="no";
		assertEquals("susi",fileRead(testFile));
		
		// file append
		file action="append" file=testFile output="john" addnewline="no";
		assertEquals("susijohn",fileRead(testFile));

		// file read
		file action="read" file=testFile variable="appendRes";
		assertEquals("susijohn",trim(appendRes));

		// file readBinary
		file action="readBinary" file=testFile variable="readBinaryRes";
		assertTrue(isBinary(readBinaryRes));

		// file info
		file action="info" file=testFile variable="res2";
		assertEquals(8,res2.size);

		// file copy
		file action="copy"  source=testFile destination=path;
		assertTrue(fileExists(testFile));

		//file rename
		file action="rename" source=testFile destination="#path#\testFile.txt";
		assertTrue(fileExists("#path#\testFile.txt"));

		// file move
		file action="move" source="#path#\testFile.txt" destination="#path#\movefile.txt";
		assertTrue(fileExists("#path#\movefile.txt"));
		assertFalse(fileExists(testFile));
		
		// file delete
		file action="delete" file="#path#\movefile.txt";
		assertFalse(fileExists("#path#\movefile.txt"));	
	}

	function afterAll() {
		if(directoryExists(path)) directoryDelete(path,true);
	}
} 
</cfscript>