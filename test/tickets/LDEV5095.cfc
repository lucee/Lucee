component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5095', body=function(){

			it( title='missing property on string throws missing method?', body=function() {
				var str="lucee";
				try {
					var test = str.lucee;
				} catch ( e ) {
					// throw lucee.runtime.exp.NativeException: No matching method for String.isLUCEE() found. there are no methods with this name.
					expect( e.type ).toBe( "expression", e.stacktrace );
				}
			});

		});
	}

}