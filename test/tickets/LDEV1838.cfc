component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1838", body=function() {
			it( title='writable',body=function( currentSpec ) {
				
				var settings=getApplicationSettings();
				var org=settings.CGIReadOnly;
					
				try {
					application action="update" CGIReadOnly=false;
					var o=objectSave(cgi);
					var cgi2=objectLoad(o);
					expect(cgi2.script_name).toBe(cgi.script_name);
				}
				finally {
					application action="update" CGIReadOnly=org;
				}

			});
			it( title='reaonly',body=function( currentSpec ) {
				
				var settings=getApplicationSettings();
				var org=settings.CGIReadOnly;
					
				try {
					application action="update" CGIReadOnly=true;
					var o=objectSave(cgi);
					var cgi2=objectLoad(o);
					expect(cgi2.script_name).toBe(cgi.script_name);
				}
				finally {
					application action="update" CGIReadOnly=org;
				}

			});
		});
	}
}