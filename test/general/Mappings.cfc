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
				}
				finally {
					application action="update" mappings="#org#";
				}
			});


			it( title='test default mappings', body=function( currentSpec ) {
				var pc=getPageContext();
				var c=pc.getConfig();

				// validate mappings
				var mappings=c.getMappings();
				expect(len(mappings)>2).toBeTrue();
				//expect(mappings[1].virtual).toBe("/lucee-server");
				//expect(directoryExists(mappings[1].getPhysical())).toBeTrue();

				expect(mappings[len(mappings)].virtual).toBe("/");
				expect(directoryExists(mappings[len(mappings)].getPhysical())).toBeTrue();
			});

			it( title='test default mappings', body=function( currentSpec ) {
				
				var pc=getPageContext();
				var c=pc.getConfig();

				// validate component mappings
				var componentMappings=c.getComponentMappings();
				expect(len(componentMappings)>0).toBeTrue();
				expect(componentMappings[1].getStrPhysical()).toBe("{lucee-config}/components/");
				expect(directoryExists(componentMappings[1].getPhysical())).toBeTrue();
			});

			it( title='test default mappings', body=function( currentSpec ) {				
				var pc=getPageContext();
				var c=pc.getConfig();

				// validate custom tag mappings
				var customTagMappings=c.getCustomTagMappings();
				expect(len(customTagMappings)>0).toBeTrue();
				expect(customTagMappings[1].getStrPhysical()).toBe("{lucee-config}/customtags/");
				expect(directoryExists(customTagMappings[1].getPhysical())).toBeTrue();
			});


		});
	}

}