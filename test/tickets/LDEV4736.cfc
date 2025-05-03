component extends = "org.lucee.cfml.test.LuceeTestCase" labels="directoryEvery"{

	function beforeAll() {
		variables.name = ListFirst(ListLast(getCurrentTemplatePath(), "\/"), ".");
		variables.parent = getDirectoryFromPath(getCurrentTemplatePath()) & name & Server.separator.file & "parent";
		variables.SEP = Server.separator.file;
		variables.path = parent&createUUID();
		variables.path2 = path&"#SEP#a";

		directoryCreate(path2);
		cffile (action="write" addnewline="yes" file="#path##SEP#b.txt" output="aaa" fixnewline="no");
		cffile (action="write" addnewline="yes" file="#path2##SEP#c.txt" output="aaa" fixnewline="no");
	}

	function run( testresults , testbox ) {
		describe( "Testcase for DirectoryEvery() function", function() {
			it( title = "Checking DirectoryEvery() with recurse=false", body = function ( currentSpec ) {
				savecontent variable="local.result" {
					DirectoryEvery( path, new LDEV4736.LDEV_4736(), false);
				};
				expect(result).toInclude("b.txt");
				expect(result).toInclude("a");
			});

			it( title = "Checking DirectoryEvery() with recurse=true", body = function ( currentSpec ) {
				savecontent variable="local.result" {
					DirectoryEvery( path, new LDEV4736.LDEV_4736(), true);
				};
				expect(result).toInclude("a");
				expect(result).toInclude("b.txt");
				expect(result).toInclude("c.txt");
			});
		});
	}

	function afterAll(){
		if(directoryExists(parent)) directorydelete(parent, true);
		directoryDelete(path, true);
	}
}