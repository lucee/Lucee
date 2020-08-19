component extends="org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll() {
		variables.path = getDirectoryFromPath(getCurrentTemplatePath())&"LDEV0093/";
		if(!directoryExists(variables.path))
			directoryCreate(variables.path);	
	}
	function afterAll() {
		if(directoryExists(variables.path))
			directoryDelete(variables.path,true);	
	}


	function run( testResults , testBox ) {
		describe( "test suite for LDEV2424", function() {
			it(title = "loosing meta data", body = function( currentSpec ) {
				
				var src=variables.path&"test.pdf";
				var trg=variables.path&"testcp.pdf";
				//create a blank PDF
				document format="PDF" filename=src overwrite=true{};
				//set the author
				info={ author:"ACME Ltd" };
				pdf action="setInfo" info=info source=src destination=src overwrite=true;
				// check author was set
				pdf action="getInfo" source=src name="local.info";
				
				expect(local.info.PageRotation[1]).toBe(0);
				expect(local.info.Pagesize[1].Height>0).toBe(true);
				expect(local.info.Pagesize[1].Width>0).toBe(true);
			});
		});
	}
}