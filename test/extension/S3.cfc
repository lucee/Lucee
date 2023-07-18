component extends="org.lucee.cfml.test.LuceeTestCase" labels="s3"	{

	function beforeAll() skip="isNotSupported"{
		if(isNotSupported()) return;
		var s3Details = getCredentials();
		id = s3Details.BUCKET_PREFIX & "s3ext-" & lcase(hash(CreateGUID()));
		root = "s3://#s3Details.ACCESS_KEY_ID#:#s3Details.SECRET_KEY#@/";
	}

	function afterAll() skip="isNotSupported"{
	}

	public function run( testResults , testBox ) {
		describe( title="Test the S3 extension", body=function() {

			it(title="create/delete a empty bucket", skip=isNotSupported(), body=function( currentSpec ){
				SystemOutput(root,1,1);
				var bucketName = "#id#-1";
				var bucketPath=root&bucketName;
				try {
					expect(directoryExists(bucketPath)).toBeFalse();
					directoryCreate(bucketPath);
					expect(directoryExists(bucketPath)).toBeTrue();
					directoryDelete(bucketPath);
					expect(directoryExists(bucketPath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="create/delete a bucket with a file", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-2";
				var bucketPath=root&bucketName;
				try {
					expect(directoryExists(bucketPath)).toBeFalse();
					directoryCreate(bucketPath);
					fileWrite(bucketPath&"/foo.txt", "Bar");
					expect(directoryExists(bucketPath)).toBeTrue();
					var failed=false;
					try {
						directoryDelete(bucketPath);
					}
					catch(e) {
						failed=true;
					}
					expect(failed).toBeTrue();
					
					directoryDelete(bucketPath,true);
					expect(directoryExists(bucketPath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="create/delete a empty folder", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-folder1";
				var bucketPath=root&bucketName;
				var folderPath=bucketPath&"/folder1";
				try {
					directoryCreate(bucketPath);
					expect(directoryExists(bucketPath)).toBeTrue();
					expect(directoryExists(folderPath)).toBeFalse();
					directoryCreate(folderPath);
					expect(directoryExists(folderPath)).toBeTrue();
					directoryDelete(folderPath);
					expect(directoryExists(folderPath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="create/delete a folder with a file", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-folder2";
				var bucketPath=root&bucketName;
				var folderPath=bucketPath&"/folder1";
				try {
					directoryCreate(bucketPath);
					expect(directoryExists(bucketPath)).toBeTrue();
					expect(directoryExists(folderPath)).toBeFalse();
					directoryCreate(folderPath);
					fileWrite(folderPath&"/foo.txt", "Bar");
					expect(directoryExists(folderPath)).toBeTrue();
					var failed=false;
					try {
						directoryDelete(folderPath);
					}
					catch(e) {
						failed=true;
					}
					expect(failed).toBeTrue();
					directoryDelete(folderPath,true);
					expect(directoryExists(folderPath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="create/delete a file", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-file";
				var bucketPath=root&bucketName;
				var filePath=bucketPath&"/foo.txt";
				try {
					directoryCreate(bucketPath);
					expect(directoryExists(bucketPath)).toBeTrue();
					expect(fileExists(filePath)).toBeFalse();
					fileWrite(filePath, "Bar");
					expect(fileExists(filePath)).toBeTrue();
					fileDelete(filePath);
					expect(fileExists(filePath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="copy a empty bucket", skip=isNotSupported(), body=function( currentSpec ){
				SystemOutput(root,1,1);
				var bucketName = "#id#-b1";
				var bucketPathSrc=root&bucketName;
				var bucketPathTrg=bucketPathSrc&"copied";
				try {
					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
					
					directoryCreate(bucketPathSrc);

					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeFalse();

					directoryCopy(bucketPathSrc, bucketPathTrg);

					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeTrue();

					directoryDelete(bucketPathSrc);
					directoryDelete(bucketPathTrg);

					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPathSrc)) directoryDelete(bucketPathSrc,true);
					if(directoryExists(bucketPathTrg)) directoryDelete(bucketPathTrg,true);
				}
			});


			it(title="copy a bucket with content", skip=isNotSupported(), body=function( currentSpec ){
				SystemOutput(root,1,1);
				var bucketName = "#id#-b2";
				var bucketPathSrc=root&bucketName;
				var folderPathSrc=bucketPathSrc&"/folder1";
				var filePathSrc=folderPathSrc&"/foo.txt";
				var bucketPathTrg=bucketPathSrc&"copied";
				var folderPathTrg=bucketPathTrg&"/folder1";
				var filePathTrg=folderPathTrg&"/foo.txt";
				
				try {
					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
					directoryCreate(bucketPathSrc);
					directoryCreate(folderPathSrc);
					fileWrite(filePathSrc, "Bar");
					
					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(folderPathSrc)).toBeTrue();
					expect(fileExists(filePathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
					
					directoryCopy(bucketPathSrc, bucketPathTrg,true);

					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(folderPathSrc)).toBeTrue();
					expect(fileExists(filePathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeTrue();
					expect(directoryExists(folderPathTrg)).toBeTrue();
					expect(fileExists(filePathTrg)).toBeTrue();

					directoryDelete(bucketPathSrc,true);
					directoryDelete(bucketPathTrg,true);

					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPathSrc)) directoryDelete(bucketPathSrc,true);
					if(directoryExists(bucketPathTrg)) directoryDelete(bucketPathTrg,true);
				}
			});

			it(title="list buckets", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-list1";
				var bucketPath=root&bucketName;
				try {
					directoryCreate(bucketPath);

					var has=false;
					loop array=directoryList(root) item="local.item" {
						if(find(bucketName, item)) {
							has=true;
							break;
						}
					}
					expect(has).toBeTrue();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			it(title="list content of a bucket", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-list2";
				var bucketPath=root&bucketName;
				var folderPath=bucketPath&"/folder1";
				var filePath=folderPath&"/foo.txt";
				try {
					directoryCreate(bucketPath);
					directoryCreate(folderPath);
					fileWrite(filePath, "Bar");
					
					expect(len(directoryList(bucketPath))).toBe(1);
					expect(len(directoryList(bucketPath,true))).toBe(2);
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});

			xit(title="move a bucket with content", skip=isNotSupported(), body=function( currentSpec ){
				SystemOutput(root,1,1);
				var bucketName = "#id#-move";
				var bucketPathSrc=root&bucketName;
				var folderPathSrc=bucketPathSrc&"/folder1";
				var filePathSrc=folderPathSrc&"/foo.txt";
				var bucketPathTrg=bucketPathSrc&"moved";
				var folderPathTrg=bucketPathTrg&"/folder1";
				var filePathTrg=folderPathTrg&"/foo.txt";
				
				try {
					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
					directoryCreate(bucketPathSrc);
					directoryCreate(folderPathSrc);
					fileWrite(filePathSrc, "Bar");
					
					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(folderPathSrc)).toBeTrue();
					expect(fileExists(filePathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeFalse();
					
					directoryRename(bucketPathSrc, bucketPathTrg,true);

					expect(directoryExists(bucketPathSrc)).toBeFalse();
					expect(directoryExists(bucketPathTrg)).toBeTrue();
					expect(directoryExists(folderPathTrg)).toBeTrue();
					expect(fileExists(filePathTrg)).toBeTrue();

					directoryDelete(bucketPathTrg,true);

					expect(directoryExists(bucketPathTrg)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPathSrc)) directoryDelete(bucketPathSrc,true);
					if(directoryExists(bucketPathTrg)) directoryDelete(bucketPathTrg,true);
				}
			});			

			it(title="file append/write", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-append";
				var bucketPath=root&bucketName;
				var filePath=bucketPath&"/foo.txt";
				try {
					directoryCreate(bucketPath);
					expect(directoryExists(bucketPath)).toBeTrue();
					expect(fileExists(filePath)).toBeFalse();
					fileWrite(filePath, "Bar");
					expect(fileExists(filePath)).toBeTrue();
					expect(fileRead(filePath)).toBe("Bar");
					fileAppend(filePath, "!!!");
					expect(fileRead(filePath)).toBe("Bar!!!");
					expect(createObject("java","java.lang.String").init(fileReadBinary(filePath))).toBe("Bar!!!");
					
					fileDelete(filePath);
					expect(fileExists(filePath)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPath)) directoryDelete(bucketPath,true);
				}
			});


			it(title="file copy/delete", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-append2";
				var bucketPathSrc=root&bucketName&"src";
				var bucketPathTrg=root&bucketName&"trg";
				var filePathSrc=bucketPathSrc&"/foosrc.txt";
				var filePathTrg1=bucketPathSrc&"/footrg.txt";
				var filePathTrg2=bucketPathTrg&"/footrg.txt";
				
				
				try {
					directoryCreate(bucketPathSrc);
					directoryCreate(bucketPathTrg);
					fileWrite(filePathSrc, "Bar");
					
					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeTrue();
					expect(fileExists(filePathSrc)).toBeTrue();
					expect(fileExists(filePathTrg1)).toBeFalse();

					// copy within bucket
					fileCopy(filePathSrc, filePathTrg1);
					expect(fileExists(filePathTrg1)).toBeTrue();

					// copy to other bucket
					fileCopy(filePathSrc, filePathTrg2);
					expect(fileExists(filePathTrg2)).toBeTrue();

					fileDelete(filePathSrc);
					fileDelete(filePathTrg1);
					fileDelete(filePathTrg2);
					expect(fileExists(filePathSrc)).toBeFalse();
					expect(fileExists(filePathTrg1)).toBeFalse();
					expect(fileExists(filePathTrg2)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPathSrc)) directoryDelete(bucketPathSrc,true);
					if(directoryExists(bucketPathTrg)) directoryDelete(bucketPathTrg,true);
				}
			});


			it(title="file move/delete", skip=isNotSupported(), body=function( currentSpec ){
				var bucketName = "#id#-append-3";
				var bucketPathSrc=root&bucketName&"src";
				var bucketPathTrg=root&bucketName&"trg";
				var filePathSrc=bucketPathSrc&"/foosrc.txt";
				var filePathTrg1=bucketPathSrc&"/footrg.txt";
				var filePathTrg2=bucketPathTrg&"/footrg.txt";
				
				
				try {
					directoryCreate(bucketPathSrc);
					directoryCreate(bucketPathTrg);
					fileWrite(filePathSrc, "Bar");
					
					expect(directoryExists(bucketPathSrc)).toBeTrue();
					expect(directoryExists(bucketPathTrg)).toBeTrue();
					expect(fileExists(filePathSrc)).toBeTrue();
					expect(fileExists(filePathTrg1)).toBeFalse();

					// copy within bucket
					fileMove(filePathSrc, filePathTrg1);
					expect(fileExists(filePathSrc)).toBeFalse();
					expect(fileExists(filePathTrg1)).toBeTrue();

					// copy to other bucket
					fileMove(filePathTrg1, filePathTrg2);
					expect(fileExists(filePathTrg1)).toBeFalse();
					expect(fileExists(filePathTrg2)).toBeTrue();

					fileDelete(filePathTrg2);
					expect(fileExists(filePathTrg2)).toBeFalse();
				}
				finally {
					if(directoryExists(bucketPathSrc)) directoryDelete(bucketPathSrc,true);
					if(directoryExists(bucketPathTrg)) directoryDelete(bucketPathTrg,true);
				}
			});
			

			it(title="check having a file and directory with the same name", skip=true, body=function( currentSpec ) {
				if(isNotSupported()) return;

				var bucketName = "#id#-same";
				var base = root & bucketName;
				
				if( directoryExists(base))
					directoryDelete(base, true);
				try{
				assertFalse(directoryExists(base));
				assertFalse(fileExists(base));
				directoryCreate(base);
				assertTrue(directoryExists(base));
				assertFalse(fileExists(base));

				var sub=base & "/a";
				if(!fileExists(sub))
					fileWrite(sub, "");

				assertFalse(directoryExists(sub));
				assertTRue(fileExists(sub));

				// because previous file is empty it is accepted as directory
				var subsub=sub & "/foo.txt";
				if(!fileExists(subsub))
					fileWrite(subsub, "hello there");

				assertFalse(directoryExists(subsub));
				assertTrue(fileExists(subsub));

				assertTrue(directoryExists(sub));
				assertFalse(fileExists(sub));

				var children = directoryList(sub, true,'query');
				assertEquals(1,children.recordcount);
				}
				finally {
					if( directoryExists(base))
						directoryDelete(base, true);
				}
			});
		});
	}


	public boolean function isNotSupported() {
		return structCount(getCredentials())==0;
	}

	private struct function getCredentials() {
		return server.getTestService("s3");
	}
}