component extends="org.lucee.cfml.test.LuceeTestCase" labels="component"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-421", function() {
			it(title="Getting the properties of a component with getMetaData()", body = function( currentSpec ) {
				var uri = new test.testcases.LDEV0421.test();
				var props = getComponentMetadata(uri).properties;
				expect(props[1].name).toBE("id");
				expect(props[2].name).toBE("FirstName");
				expect(props[3].name).toBE("MiddleName");
				expect(props[4].name).toBE("LastName");
				expect(props[5].name).toBE("Email");
			});
		});
	}
}