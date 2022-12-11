<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase" labels="ftp" {
	
	
	//public function afterTests(){}
	
	public function testSFTP() {
		var sftp=getSFTPCredentials();
		if(!structCount(sftp)) return;
		//return; //disable failing test
		_test(
			secure: true,
			host: sftp.server,
			user: sftp.username,
			pass: sftp.password,
			port: sftp.port,
			base: sftp.base_path
		);
	}

	public function testFTP() {
		var ftp=getFTPCredentials();
		if(!structCount(ftp)) return;
		//return; //disable failing test
		_test(
			secure: false,
			host: ftp.server,
			user: ftp.username,
			pass: ftp.password,
			port: ftp.port,
			base: ftp.base_path
		);
	}

	private void function _test(required boolean secure, required string host, required number port,
		required string user, required string pass, required string base){

		//systemOutput(arguments, true);
		
		ftp action = "open"
			connection="ftpConn"
			passive = "true"
			timeout = 5
			secure = arguments.secure
			username = arguments.user
			password = arguments.pass
			server = arguments.host
			stopOnError = true
			port = arguments.port;

		var folderName="folder"&getTickCount();
		if(right(base,1)!="/")base=base&"/";
		var dir=base&folderName&"/";
		var fileName="test.txt";
		var file=dir&fileName;

		var fileName2="test2.txt";
		var file2=dir&fileName2;

		var subdir=dir&"sub/";
		var subfile=subdir&fileName;
		
		// list the inital state
		ftp action="listdir" directory=base connection="ftpConn" name="local.initialState"; // passive not sticky LDEV-977
		
		// print working directory
		ftp action="getcurrentdir" directory=base connection="ftpConn" result="local.pwd1";
		pwd1=pwd1.returnValue;
		///////// TODO does not work with sftp assertTrue(pwd1==base || pwd1&"/"==base);
			
		
		//try{

			// we create a directory
			ftp action="createdir" directory=dir connection="ftpConn";
			ftp action="listdir" directory=base connection="ftpConn" name="local.list2";
			assertEquals(initialState.recordcount+1,list2.recordcount);

			// change working directory
			ftp action="changedir" directory=dir connection="ftpConn";
			ftp action="getcurrentdir" directory=base connection="ftpConn" result="local.pwd2";
			pwd2=pwd2.returnValue;
			assertTrue(pwd2==dir || pwd2&"/"==dir);

			// we add a file
			ftp action="putFile"  localfile=getCurrentTemplatePath() remoteFile=file connection= "ftpConn";
			ftp action="listdir" directory=dir connection="ftpConn" name="local.list3";  // passive not sticky LDEV-977;
			assertEquals(list3.recordcount,1);
			assertEquals(list3.name,fileName);
			assertEquals(list3.isDirectory,false);
			assertEquals(list3.name,fileName);
			assertEquals(list3.path,file);
			assertEquals(list3.type,"file");

			// we read the file
			var src=getCurrentTemplatePath();
			var localFile=src&"."&getTickcount()&".rf";
			try {
				ftp action="getFile"  localfile=localFile remoteFile=file connection= "ftpConn";
				var srcContent=fileRead(src);
				var localFileContent=fileRead(localFile);
				assertEquals(srcContent,localFileContent);
			}
			finally {
				try {fileDelete(localFile);}catch(local.ee){}
			}

			// we rename the file
			ftp action="rename"  existing=file new=file2 connection= "ftpConn";
			ftp action="listdir" directory=dir connection="ftpConn" name="local.list4";
			assertEquals(list4.recordcount,1);
			assertEquals(list4.name,fileName2);

			// exists dir
			ftp action="existsdir" directory=dir connection="ftpConn" result="local.exist1";
			assertTrue(exist1.returnValue);
			ftp action="existsdir" directory=subdir connection="ftpConn" result="local.exist2";
			assertFalse(exist2.returnValue);

			//exists file
			ftp action="existsfile" remotefile=file2 connection="ftpConn" result="local.exist3";
			assertTrue(exist3.returnValue);
			ftp action="existsfile" remotefile=file connection="ftpConn" result="local.exist4";
			assertFalse(exist4.returnValue);

			ftp action="listdir" directory=base connection="ftpConn" name="local.list22";
			debug(list22);

			// we delete the file again
			ftp action="remove"  item=file2 connection= "ftpConn";
			ftp action="listdir" directory=dir connection="ftpConn" name="local.list4";
			assertEquals(list4.recordcount,0);

			// we add again a file and directory to be sure we can delete a folder with content
			ftp action="createdir" directory=subdir connection="ftpConn";
			ftp action="putFile"  localfile=getCurrentTemplatePath() remoteFile="#subfile#-normal" connection="ftpConn";
			ftp action="putFile"  localfile=getCurrentTemplatePath() remoteFile="#subfile#-ascii" connection="ftpConn" transferMode="ASCII";
			ftp action="putFile"  localfile=getCurrentTemplatePath() remoteFile="#subfile#-auto" connection="ftpConn" transferMode="auto"; // default
			// LDEV-3528  transferMode=“binary” causes "Connection is not open" error with ftp
			if ( arguments.secure )
				ftp action="putFile"  localfile=getCurrentTemplatePath() remoteFile="#subfile#-binary" connection="ftpConn" transferMode="binary";
			debug(cfftp);

		//}
		//finally {
			ftp action="listdir" directory=subdir connection="ftpConn" name="local.listSubdir" recurse=true;
			expect( listSubdir.recordcount ).toBe( arguments.secure? 4 : 3 );
			
			ftp action="removedir" directory=subdir connection="ftpConn" recurse=true;
			ftp action="existsDir" directory=subdir connection="ftpConn" result="local.listSubdirExists";
			expect( listSubdirExists.returnvalue ).toBeFalse();
			
			// delete the folder we did for testing
			ftp action="removedir" directory=dir connection="ftpConn" recurse=true;
			ftp action="listdir" directory=base connection="ftpConn" name="local.finalState";
			expect( finalState.recordcount ) .toBe( initialState.recordcount );
		//}

	}

	private struct function getFTPCredentials() {
		return server.getTestService("ftp");
	}

	private struct function getSFTPCredentials() {
		return server.getTestService("sftp");
	}
} 
</cfscript>