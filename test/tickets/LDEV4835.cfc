component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ){
		describe( "LDEV-4835", function(){

			it( "create and test rest mapping on the fly", function(){
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var name="LDEV4835"&getTickCount();
				var dir=curr&name;
				try {
						directoryCreate(dir);
						RestInitApplication(dir, '/'&name, false, request.webadminpassword);
						
						var mappings=getPageContext().getConfig().getRestMappings();
						var has=false;
						loop array=mappings item="local.m" {
								if(m.getVirtual()=='/'&name) has=true; 
						}
				}
				finally {
						if(directoryExists(dir)) directoryDelete(dir, true);
				}
				expect(has).toBeTrue();
			});

		} );
	}
}
