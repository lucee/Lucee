component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for ComponentListPackage Function", body=function() {
			it( title='checking ComponentListPackage()',body=function( currentSpec ) {
				var compList = "debug,dbdriver,gdriver,mailservers,cdriver";
				for(comp in compList){
					var ctList = ComponentListPackage("lucee-server.admin.#comp#");
					var drList = directoryList(path=expandPath("{lucee-server}\context\admin\#comp#"), type="file");
					assertEquals(arrayLen(drList), arrayLen(ctList));
				}
			});
		});
	}
}
