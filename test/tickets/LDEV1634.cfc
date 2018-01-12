component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1634", function() {
			it( title='checking InputBaseN function', body=function( currentSpec ) {
				var string = "40390719E3C0";
				var InputBasefn = "#InputBaseN(string, 16)#";
				var Longobj = "#CreateObject('java', 'java.lang.Long').parseLong('#string#', 16)#";
				expect(InputBasefn).toBe(Longobj);
			});
		});
	}
}
