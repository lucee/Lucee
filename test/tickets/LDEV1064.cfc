component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1064", function() {
			it( title='checking testAttributeName with attributeCollection', body=function( currentSpec ) {
				var attr = {
					name : "qDir1"
				};
				directory directory=expandPath('.') attributeCollection=attr;
				expect(qDir1).toBetypeof("Query");	// writeDump(qDir2);
			});

			it( title='checking testAttributeAlias with attributeCollection', body=function( currentSpec ) {
				var attr = {
					variable : "qDir2"
				};
				directory directory=expandPath('.') attributeCollection=attr;
				expect(qDir2).toBetypeof("Query");	// writeDump(qDir2);
			});
		});
	}
}