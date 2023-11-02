component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1188", function() {
			
			it( title='Checking a bad json 1', body=function( currentSpec ) {
				var result=ValidateJson(readJson("god.json"),readJson("schema.json"));
				expect(result.isValid).toBe(true);
			});
			it( title='Checking a bad json 1', body=function( currentSpec ) {
				var result=ValidateJson(readJson("bad.json"),readJson("schema.json"));
				expect(result.isValid).toBe(false);
				expect(len(result.errors)).toBe(1);
				expect(result.errors[1].level).toBe("Error");
			});
			it( title='Checking a bad json 2', body=function( currentSpec ) {
				var result=ValidateJson(readJson("bad2.json"),readJson("schema.json"));
				expect(result.isValid).toBe(false);
				expect(len(result.errors)).toBe(1);
				expect(result.errors[1].level).toBe("Error");
			});

		});
	}

	function readJson(fileName) {
		var dir=getDirectoryFromPath(getCurrentTemplatePath())&"LDEV1188/";
		return fileRead(dir&fileName);
	}
}