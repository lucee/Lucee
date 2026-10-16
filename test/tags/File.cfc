<!--- 
 *
 * Copyright (c) 2016, Lucee Association Switzerland. All rights reserved.
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
		
		srcFile = "#path#\test.txt";

		// file write
		file action="write" file=srcFile output="susi" addnewline="no";
		assertEquals("susi",fileRead(srcFile));

		// file append
		file action="append" file=srcFile output="john" addnewline="no";
		assertEquals("susijohn",fileRead(srcFile));

		// file read
		file action="read" file=srcFile variable="appendRes";
		assertEquals("susijohn",trim(appendRes));

		// file readBinary
		file action="readBinary" file=srcFile variable="readBinaryRes";
		assertTrue(isBinary(readBinaryRes));

		// file info
		file action="info" file=srcFile variable="res2";
		assertEquals(8,res2.size);

		// file copy
		trgCopy = "#path#\test-copy.txt";
		file action="copy" source=srcFile destination=trgCopy;
		assertTrue(fileExists(srcFile));
		assertTrue(fileExists(trgCopy));

		//file rename
		trgRename = "#path#\test-rename.txt";
		file action="rename" source=trgCopy destination=trgRename;
		assertFalse(fileExists(trgCopy));
		assertTrue(fileExists(trgRename));

		// file move
		trgMove = "#path#\test-move.txt";
		file action="move" source=trgRename destination=trgMove;
		assertTrue(fileExists(trgMove));
		assertFalse(fileExists(trgRename));

		// file delete
		file action="delete" file=trgMove;
		assertFalse(fileExists(trgMove));
		file action="delete" file=srcFile;
		assertFalse(fileExists(srcFile));
	}

	function afterAll() {
		if(directoryExists(path)) directoryDelete(path,true);
	}
} 
</cfscript>