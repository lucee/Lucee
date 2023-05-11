component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2158", function() {
			it(title = "Query sorting ignores leading hyphen", body = function( currentSpec ) {
				var testQuery = queryNew("getLtr", "CF_SQL_VARCHAR");
				var dataArr = listToArray("a,A,B,C,-A,-B,-C,- A,- B,- C,@A,@B,@C,*A,*B,*C,1A,1B,1C,-a,2A,/c");
				for (i = 1; i <= arrayLen(dataArr); i++)
					queryAddRow(testQuery, {getLtr: dataArr[i]});
				var testQuery = queryExecute("SELECT * FROM testQuery ORDER BY getLtr", {}, {dbType: "query"});

 //testQuery=duplicate(testQuery);
	//querySort(testQuery, "getLtr","asc");
	//writedump(testQuery);

				expect(testQuery.getLtr[1]).tobe('*A');
				expect(testQuery.getLtr[2]).tobe('*B');
				expect(testQuery.getLtr[3]).tobe('*C');
				expect(testQuery.getLtr[4]).tobe('- A');
				expect(testQuery.getLtr[5]).tobe('- B');
				expect(testQuery.getLtr[6]).tobe('- C');
				expect(testQuery.getLtr[7]).tobe('-A');
				expect(testQuery.getLtr[8]).tobe('-B');
				expect(testQuery.getLtr[9]).tobe('-C');
				expect(testQuery.getLtr[10]).tobe('-a');
				expect(testQuery.getLtr[11]).tobe('/c');
				expect(testQuery.getLtr[12]).tobe('1A');
				expect(testQuery.getLtr[13]).tobe('1B');
				expect(testQuery.getLtr[14]).tobe('1C');
				expect(testQuery.getLtr[15]).tobe('2A');
				expect(testQuery.getLtr[16]).tobe('@A');
				expect(testQuery.getLtr[17]).tobe('@B');
				expect(testQuery.getLtr[18]).tobe('@C');
				expect(testQuery.getLtr[19]).tobe('A');
				expect(testQuery.getLtr[20]).tobe('B');
				expect(testQuery.getLtr[21]).tobe('C');
				expect(testQuery.getLtr[22]).tobe('a');
			});

		});
	}
}
