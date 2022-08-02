component extends="org.lucee.cfml.test.LuceeTestCase" labels="component"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-421", function() {
			it(title="Getting the properties of a component with getComponentMetadata()", body = function( currentSpec ) {
				var uri = new LDEV0421.test();
				var props = getComponentMetadata(uri).properties;
				expect(props[1].name).toBe("id");
				expect(props[2].name).toBe("FirstName");
				expect(props[3].name).toBe("MiddleName");
				expect(props[4].name).toBe("LastName");
				expect(props[5].name).toBe("Email");
			});
		});
	}
}