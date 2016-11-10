component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test suite for LDEV-1068", function() {
			it("Checking serializeJSON() for serializing query", function( currentSpec ){
				columnName = "name,age,sex";
				query = queryNew(columnName);
				queryAddRow( query, ['saravana',35,'male'] );
				queryAddRow( query, ['Bob',20, 'female'] );
				queryAddRow( query, ['pothy',25, 'male'] );
				json = serializeJSON(query,true);
				djson = deserializeJSON(json);
				keys = StructKeyList(djson.Data);
				caseCompare = compare(uCase(columnName),keys);
				expect(caseCompare).tobe(0);
			});
		});
	}
}