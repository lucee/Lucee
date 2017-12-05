component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1606", body=function() {
			it( title='Checking cfzipparam tag with attribute filter',body=function( currentSpec ) {
				cfzip( action="unzip", file="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\test.zip", destination="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\" ){
					cfzipparam (filter="*.xml");
				}
				cfdirectory(action="list", directory="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\test",  name="result");
				expect(result.name).toBe('test.xml');
			});

			it( title='Checking cfzip tag with attribute filter',body=function( currentSpec ) {
				cfzip( action="unzip", file="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\test2.zip", destination="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\", filter="*.xml" ){
				}
				cfdirectory(action="list", directory="#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1606\test2",  name="result");
				expect(result.name).toBe('test.xml');
			});
		});
	}
}