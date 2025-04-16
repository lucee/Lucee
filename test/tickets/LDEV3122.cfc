component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.path = getTempDirectory() & "LDEV-3122/";
		afterAll();
		if(!directoryExists(path)) directoryCreate(path)
	}

	function run( testResults , testBox ) {

		describe( "Testcase for LDEV-3122", function() {
			it( title="checking CFFILE nameConflict=makeunique without file exists", body=function() {
				fileWrite("#path#\test.txt","LDEV-3122");
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)

				file action="copy"  source="#path#\test.txt" destination=newDir nameConflict="makeUnique";
				directory action="list" directory=newDir name="local.list";

				expect( list.name ).toBe( "test.txt" );
			});

			it( title="checking CFFILE nameConflict=makeUnique with file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				fileWrite("#newDir#\testMakeUnique.txt","LDEV-3122");

				file action="copy"  source="#newDir#\testMakeUnique.txt" destination=newDir nameConflict="makeUnique";
				directory action="list" directory=newDir name="local.list" listInfo="name";

				expect( serializeJson( list ) ).toInclude( "testMakeUnique-" );
				expect( list.recordcount ).toBe("2");
			});

			it( title="checking CFFILE nameConflict=forceunique without file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				fileWrite("#path#\testForceUnique.txt","LDEV-3122");

				file action="copy"  source="#path#\testForceUnique.txt" destination=newDir nameConflict="forceUnique";
				directory action="list" directory=newDir name="local.list" listInfo="name";

				expect( serializeJson( list ) ).toInclude( "testForceUnique-" );
				expect( list.recordcount ).toBe("1");
			});

			it( title="checking CFFILE nameConflict=forceunique with file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				fileWrite("#newDir#\testFileForceUnique.txt","LDEV-3122");

				file action="copy"  source="#newDir#\testFileForceUnique.txt" destination=newDir nameConflict="forceUnique";
				directory action="list" directory=newDir name="local.list" listInfo="name";

				expect( serializeJson( list ) ).toInclude( "testFileForceUnique-");
				expect( list.recordcount ).toBe("2");
			});
		});

	}

	function afterAll() {
		if(directoryExists(path)) directoryDelete(path,true);
	}
}