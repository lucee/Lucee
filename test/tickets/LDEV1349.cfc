component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1349", function() {
			it( title='checking path conversion', body=function( currentSpec ) {
			 	
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
			 	
			 	local.app=getApplicationSettings();
			 	app.mappings["/l1349"]=curr&"LDEV-1349/aaaaaaaaaaaaaaaaa/";
			 	application action="update" mappings=app.mappings;

			 	local.app=getApplicationSettings();
			 	savecontent variable="local.result" {
				 	include "/l1349/Index.cfm";
				 	include "/l1349/../Index.cfm";
				 }
				assertEquals( "ba", result );
			});

			
		});
	}
}