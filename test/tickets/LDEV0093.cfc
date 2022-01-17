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
		describe( "test suite for LDEV0093", function() {
			it(title = "loosing meta data", body = function( currentSpec ) {
				
				var src=variables.path&"test.pdf";
				var trg=variables.path&"testcp.pdf";
				//create a blank PDF
				document format="PDF" filename=src overwrite=true{};
				//set the author
				info={ author:"ACME Ltd" };
				pdf action="setInfo" info=info source=src destination=src overwrite=true;
				// check author was set
				pdf action="getInfo" source=src name="local.infoBeforeWrite";
				//read pdf into a variable
				pdf action="read" source=src name="local.pdfObjectBeforeWrite";
				// write back to file
				pdf action="write" source=pdfObjectBeforeWrite destination=trg overwrite=true;
				// now check the author
				pdf action="getInfo" source=trg name="local.infoAfterWrite";
				

				expect(infoAfterWrite.author).toBe(infoBeforeWrite.author);
			});
		});
	}
}