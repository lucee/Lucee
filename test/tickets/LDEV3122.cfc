component extends="org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll() {
		variables.path = "#getDirectoryFromPath(getCurrenttemplatepath())#LDEV3122";
		afterAll();
		if(!directoryExists(path)) directoryCreate(path)
	}
	
	function run( testResults , testBox ) {

		describe( "Testcase for LDEV-3122", function() {
			it( title="checking cffile nameconflict=makeunique without file exists", body=function() {
				filewrite("#path#\test.txt","LDEV-3122");
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
			
				file action="copy"  source="#path#\test.txt" destination=newDir nameconflict="makeunique";
				directory action="list" directory=newDir name="list";

				expect(list.name).tobe("test.txt");
			});

			it( title="checking cffile nameconflict=makeunique with file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				filewrite("#newDir#\testmakeunique.txt","LDEV-3122");

				file action="copy"  source="#newDir#\testmakeunique.txt" destination=newDir nameconflict="makeunique";
				directory action="list" directory=newDir name="list" listinfo="name";

				expect(find("testmakeunique_",serializejson(list))).tobeGT(0);
				expect(list.recordcount).tobe("2");
			});
			
			it( title="checking cffile nameconflict=forceunique without file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				filewrite("#path#\testforceunique.txt","LDEV-3122");

				file action="copy"  source="#path#\testforceunique.txt" destination=newDir nameconflict="forceunique";
				directory action="list" directory=newDir name="list" listinfo="name";

				expect(find("testforceunique_",serializejson(list))).tobeGT(0);
				expect(list.recordcount).tobe("1");
			});
			
			it( title="checking cffile nameconflict=forceunique with file exists", body=function() {
				var newDir = "#path#\test_#createUniqueID()#";
				directoryCreate(newDir)
				filewrite("#newDir#\testfileforceunique.txt","LDEV-3122");

				file action="copy"  source="#newDir#\testfileforceunique.txt" destination=newDir nameconflict="forceunique";
				directory action="list" directory=newDir name="list" listinfo="name";
			
				expect(find("testfileforceunique_",serializejson(list))).tobeGT(0);	
				expect(list.recordcount).tobe("2");
			});
		});

	}

	function afterAll() {
		if(directoryExists(path)) directoryDelete(path,true);
	}
}
