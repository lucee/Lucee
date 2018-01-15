component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV1609";
		directoryCreate(variables.Dir);
		fileWrite("#variables.Dir#/test.txt", "testName");
	}

	function afterAll(){
		directoryDelete(variables.Dir, true);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1609", body=function() {
			it( title='Checking cffile action ="write" with attribute nameConflict="skip" ',body=function( currentSpec ) {
				cffile( action="write", file="#variables.Dir#/test.txt", output="testName1", charset="utf-8", nameconflict="skip" ){
				}
				expect(trim(fileRead("#variables.Dir#/test.txt"))).toBe('testName');
			});

			it( title='Checking cffile action ="write" with attribute nameConflict="error" ',body=function( currentSpec ) {
				cffile( action="write", file="#variables.Dir#/test.txt", output="testName2", charset="utf-8", nameconflict="error" ){
				}
				expect(trim(fileRead("#variables.Dir#/test.txt"))).toBe('testName');
			});
		});
	}
}