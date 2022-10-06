component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "Testcase for LDEV-3671", function() {

			it(title = "Checking getMetaData()" , body = function( currentSpec ) {
				var obj = new LDEV3671.test3671();

				var func = getMetaData(obj).functions[1];
				assertEquals('testFunc', func.name);
				assertEquals("public", func.access);
				assertEquals("final", func.modifier);
				assertEquals("any", func.returntype);
				assertEquals("false", func.static);
				assertEquals("true", func.output);

				var param = func.parameters[1];
				assertEquals('s', param.name);
				assertEquals(true, param.required);
				assertEquals('string', param.type);
			});
		});
	}
}
