component extends = "org.lucee.cfml.test.LuceeTestCase"{
	cfapplication(mappings = { "/cfcMap" : getDirectoryFromPath(getCurrentTemplatePath()) & "LDEV3900/otherCfc"});
	function run( testResults , testBox ) {
		describe( "Test case for LDEV-3900", function() {
			it(title="check import component definition using component object", body=function( currentSpec ) {
				try {
					var result = new LDEV3900.SomeCfc().doitUsingImport();
				}
				catch(any e) {
					var result= e.message;
				}
				expect(result).toBe("prop");
			});
			it(title="check import component definition with structCopy the component object", body=function( currentSpec ) {
				try {
					var result = structCopy(new LDEV3900.SomeCfc()).doitUsingImport();
				}
				catch(any e) {
					var result= e.message;
				}
				expect(result).toBe("prop");
			});
			it(title="check import component definition with duplicate the component object", body=function( currentSpec ) {
				try {
					var result = duplicate(new LDEV3900.SomeCfc()).doitUsingImport();
				}
				catch(any e) {
					var result= e.message;
				}
				expect(result).toBe("prop");
			});
		});
	}
}