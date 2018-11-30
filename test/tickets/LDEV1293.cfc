component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){
		variables.Qry = queryNew("id,en,mi", "integer,varchar,varchar", [
		    [1,"one","tahi"],
		    [2,"two","rua"],
		    [3,"three","toru"],
		    [4,"four","wha"]
		]);

		variables.remapTemplate = queryNew("value,english,maori");
		variables.reMappedQry = Qry.map(function(row){
		return {value=row.id, english=row.en, maori=row.mi};
		}, remapTemplate);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1293", body=function() {
			it(title="checking the reMapped query", body = function( currentSpec ) {
				var testQuery = queryNew("value,english,maori", "integer,varchar,varchar", [
						[1,"one","tahi"],
						[2,"two","rua"],
						[3,"three","toru"],
						[4,"four","wha"]
					]);
				expect(reMappedQry.columnList).toBe(testQuery.columnList);
			});

			it(title="checking remap template", body = function( currentSpec ) {
				var dupRemapTemplate = queryNew("value,english,maori");
				expect(remapTemplate.columnList).toBe(dupRemapTemplate.columnList);
			});
		});
	}
}