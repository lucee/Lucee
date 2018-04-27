component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1831", body=function() {
			it( title='unload/load cfc',body=function( currentSpec ) {
				
				var curr=getDirectoryFromPath(getCurrentTemplatePath());
				var dir=curr&"LDEV1831";

				var settings=getApplicationSettings();
				var cfcsOrg=settings.componentpaths;
				var cfcs=duplicate(cfcsOrg);
				arrayAppend(cfcs,dir);
				// adding component path
				application action="update" componentpaths = cfcs; 
				try {
					var t=new a.b.Test1831();
					expect(t.getName()).toBe('Susi');
					var o=objectSave(t);
					
					// remove component path
					application action="update" componentpaths = cfcsOrg; 
					
					t2=objectLoad(o);
					expect(t2.getName()).toBe('Susi');
				}
				finally {
					application action="update" componentpaths = cfcsOrg; 
				}
			});
		});
	}
}