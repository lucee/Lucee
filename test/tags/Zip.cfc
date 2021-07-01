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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="zip" {

	public function setUp() {
		
		variables.currFile=getCurrentTemplatePath();
		variables.currDir=getDirectoryFromPath(currFile);

		variables.root=currDir & "zip/";
		variables.dir=root & "a/b/c/";
		variables.file1=dir & "a.txt"
		variables.dir1=root & "a/";
		variables.file2=root & "a/b.txt";
		variables.target=root & "test.zip";
		variables.targetStored=root & "testStored.zip";
		variables.unzip=root & "unzip";

		if (directoryExists(root)) directoryDelete(root,true);

		directoryCreate(dir);
		directoryCreate(unzip);
		fileWrite(file1,"file 1");
		fileWrite(file2,"file 2");
	}

	function afterAll(){
		if ( directoryExists( root ) ) 
			directoryDelete( root , true );
	}

	public function test() {
		try {
			if(fileExists(target)) fileDelete(target);
			// zip
			zip action="zip" file=target {
				zipparam entryPath = "/1/2.cfm" source =variables.file1;
				zipparam source =variables.file1;
				zipparam source =variables.dir1;
				zipparam prefix="n/m" source =variables.dir1;
			}

			// list
			zip action="list" file=target name="local.qry";
			assertEquals(6,qry.recordcount);
			assertEquals('1/2.cfm,a.txt,b.txt,b/c/a.txt,n/m/b.txt,n/m/b/c/a.txt',listSort(valueList(qry.name),'textnocase'));
			
			// read
			zip action="read" entrypath="1/2.cfm" variable="local.entry" file=target;
			assertEquals('file 1',entry);

			// unzip
			zip action="unzip" showdirectory=true file=target destination=unzip;
			var qry=directoryList(path:unzip,listInfo:'query',recurse:true,type:'file');
			queryAddColumn(qry,"relpath");
			loop query=qry {
				qry.relpath = qry.directory & "/" & qry.name;
				qry.relpath = replace(qry.relpath, "\", "/" , "all"); // normalize paths
				qry.relpath = mid( qry.relpath , len( unzip ) + 2); // strip out base dir
			}
			assertEquals(6,qry.recordcount);
			assertEquals('1/2.cfm,a.txt,b.txt,b/c/a.txt,n/m/b.txt,n/m/b/c/a.txt',listSort(valueList(qry.relpath),'textnocase'));

			// remove
			zip action="delete" entrypath="/n/m/" file=target;
			zip action="list" file=target name="local.qry";
			assertEquals(4,qry.recordcount);
			assertEquals('1/2.cfm,a.txt,b.txt,b/c/a.txt',listSort(valueList(qry.name),'textnocase'));
			
			// zip no compression
			if( fileExists(targetStored)) fileDelete(targetStored);
			zip action="zip" file=targetStored compressionMethod="store"{
				zipparam entryPath = "/1/2.cfm" source =variables.file1;
				zipparam source =variables.file1;
				zipparam source =variables.dir1;
				zipparam prefix="n/m" source =variables.dir1;
			}
			// without compression, a zip file using store should be larger than a standard zip file
			assertTrue(getFileInfo(targetStored).size GT getFileInfo(target).size);
		

		}
		finally {
			if ( directoryExists( root ) ) 
				directoryDelete( root , true );
		}
	}
	
	public function testInvalidEntryName() {
		var curr = getDirectoryFromPath( getCurrentTemplatePath() );
		var trg=curr & "zip/"
		trg2 = trg & "sub/sub/";
		if ( directoryExists( trg ) ) 
			directoryDelete( trg, true );
		directoryCreate( trg );
		directoryCreate( trg2 );

		try{
			// create the test zip
			zip action="zip" file="#trg#test.zip"{
				zipparam entrypath="../../invalidpath.txt" content="test an invalid path";
			}

			// unzip the created zip
			zip action="unzip" file="#trg#test.zip" destination=trg2;

			// is the file in the right place
			assertTrue(fileExists("#trg2#invalidpath.txt"));
			assertFalse(fileExists("#trg#invalidpath.txt"));
		}
		finally {
			if (directoryExists( trg ) ) 
				directoryDelete( trg, true );
		}
	}

	public function testZipExecutableFlag() { // LDEV-2117

		if ( isNotSupported() ) return;  // this is unix only

		var curr = getDirectoryFromPath( getCurrentTemplatePath() );
		var src = curr & "zip_exe";
		var dest = curr & "unzip_exe";

		try {
			if ( directoryExists( src ) ) 
				directoryDelete( src, true );
			directoryCreate( src );
			if ( directoryExists( dest ) ) 
				directoryDelete( dest, true );
			directoryCreate( dest );

			var testFile = src & "/test.txt";
			FileWrite( testFile, "why do you want to execute me?");
			fileSetAccessMode( testFile, "744");

			var testFile2 = src & "/test2.txt";
			FileWrite( testFile2, "why don't you want to execute me?");

			var info = FileInfo( testFile );
			// systemOutput( info , true );
			expect( info.execute ).toBeTrue();
			expect( info.mode ).toBe( 744 );

			// systemOutput( info, true );
			// systemOutput( directoryList( path=src, listinfo="query" ), true );

			// create the test zip
			zip action="zip" file="#src#/test.zip" {
				zipparam source=testFile;
				zipparam source=testFile2;
			}

			zip action="list" file="#src#/test.zip" name="local.qry";
			// systemOutput( local.qry, true );

			expect( local.qry.recordcount ).toBe( 2 );
			// expect( local.qry.mode[1] ).toBe( 744 ); // cfzip doesn't list mode

			// unzip the created zip
			zip action="unzip" file="#src#/test.zip" destination=dest;

			info = FileInfo( dest & "/test.txt" );
			expect( info.execute ).toBeTrue();
			expect( info.mode ).toBe( 744 );

			info = FileInfo( dest & "/test2.txt" );
			expect( info.execute ).toBeFalse();
		}
		finally {
			if ( directoryExists( src ) )
				directoryDelete( src, true );
			if ( directoryExists( dest ) )
				directoryDelete( dest, true );
		}
	}

	function isNotSupported() {
		var isWindows = find("Windows", server.os.name );
		if (isWindows > 0 ) return true;
		else return false;
	}
	
} 
</cfscript>
