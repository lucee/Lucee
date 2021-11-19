component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.Dir = "#GetDirectoryFromPath(getCurrentTemplatePath())#LDEV2159";
		directoryCreate(variables.Dir);
	}

	function afterAll(){
		directoryDelete(variables.Dir, true);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-2159", body=function() {
			it( title='cffile write nameconflict skip - writes an empty file" ',body=function( currentSpec ) {
				cffile( action="write", file="#variables.Dir#/test.txt", output="testName", charset="utf-8", nameconflict="skip" ){
				}
				expect(trim(fileRead("#variables.Dir#/test.txt"))).toBe('testName');
			});
		});
	}
} 