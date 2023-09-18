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
				expect(len(mappings)).toBe(3);
				expect(mappings[1].virtual).toBe("/lucee-server");
				expect(directoryExists(mappings[1].getPhysical())).toBeTrue();

				expect(mappings[3].virtual).toBe("/");
				expect(directoryExists(mappings[3].getPhysical())).toBeTrue();
			});

			it( title='test default mappings', body=function( currentSpec ) {
				
				var pc=getPageContext();
				var c=pc.getConfig();

				// validate component mappings
				var componentMappings=c.getComponentMappings();
				expect(len(componentMappings)).toBe(2);
				expect(componentMappings[1].getStrPhysical()).toBe("{lucee-web}/components/");
				expect(directoryExists(componentMappings[1].getPhysical())).toBeTrue();
				expect(componentMappings[2].getStrPhysical()).toBe("{lucee-server}/components/");
				expect(directoryExists(componentMappings[2].getPhysical())).toBeTrue();
			});

			it( title='test default mappings', body=function( currentSpec ) {				
				var pc=getPageContext();
				var c=pc.getConfig();

				// validate custom tag mappings
				var customTagMappings=c.getCustomTagMappings();
				expect(len(customTagMappings)).toBe(1);
				expect(customTagMappings[1].getStrPhysical()).toBe("{lucee-config}/customtags/");
				expect(directoryExists(customTagMappings[1].getPhysical())).toBeTrue();
			});


		});
	}

}