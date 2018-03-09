component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "Test suite Mappings", function() {
			it( title='test sub mappings for non existing files', body=function( currentSpec ) {
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var parent=getDirectoryFromPath(mid(curr,1,len(curr)-1));
				var org = getApplicationSettings().mappings;
				var mappings = duplicate(org);
				try {
					mappings[ '/' ] = parent;
					mappings[ '/diff' ] = curr;//parent&"other";
		
					application action="update" mappings="#mappings#";
					expect(expandPath("/diff/susi.cfm")).toBe(curr&"susi.cfm");
				}
				finally {
					application action="update" mappings="#org#";
				}
			});

			it( title='test sub mappings for existing files', body=function( currentSpec ) {
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var p=getDirectoryFromPath(mid(curr,1,len(curr)-1));
				var rel=replace(getCurrentTemplatePath(),p,'');
				var pp=getDirectoryFromPath(mid(p,1,len(p)-1));
				var org = getApplicationSettings().mappings;
				var mappings = duplicate(org);
				
				try {
					mappings[ '/' ] = p;
					mappings[ '/diff' ] = curr;//parent&"other";

					application action="update" mappings="#mappings#";

					expect(expandPath("/"&rel)).toBe(getCurrentTemplatePath());
					expect(expandPath("/diff/"&rel)).toBe(curr&rel);

					// /Users/mic/Test/test2/webapps/ROOT/test/testcases/testcases/Mappings.cfc
				}
				finally {
					application action="update" mappings="#org#";
				}
			});


		});
	}

}