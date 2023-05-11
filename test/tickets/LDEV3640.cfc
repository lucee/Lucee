component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {


	function run( testResults , testBox ) {

		describe( 'QofQ nulls' , function(){

			it( 'Stay null when unioned' , function() {
				var qs_result = queryNew("col" , "int", [1]);
				```
				<cfquery name="qs_result" dbtype="query">
						select null as test
						from qs_result
						union
						select null as test
						from qs_result
				</cfquery>
				```

				expect( isNull( qs_result.getColumn('test').get(1,nullValue()) ) ).toBeTrue();
			});

			it( 'Stay null when grouped' , function() {
				var qs_result = queryNew("col,col2" , "string,string", [
					['test',nullValue()],
					['foo',nullValue()],
					['test',nullValue()]
				]);
				```
				<cfquery name="qs_result" dbtype="query">
					select col,col2,isnull(col2,42) as test
					from qs_result
					group by col,col2
				</cfquery>
				```

				expect( isNull( qs_result.getColumn('col2').get(1,nullValue()) ) ).toBeTrue();
				expect( qs_result.test ).toBe( 42 );

			});

			it( 'Stay null when aggregated' , function() {

				var qs_result = queryNew("dnum_auto,amount_local" , "integer,integer", [[10,10],[20,20]]);
				```
				<cfquery name="testquery" dbtype="query">
					select sum(amount_local) as amount_local
					from qs_result
					where dnum_auto = 1000
				</cfquery>
				```
				
				expect( testquery.recordCount ).toBe( 1 );
				expect( testquery.amount_local ).toBe( '' );
				expect( isNull( testquery.getColumn('amount_local').get(1,nullValue()) ) ).toBeTrue();

				```
				<cfquery name="testquery2" dbtype="query">
					select sum(amount_local) as amount_local
					from testquery 
				</cfquery>
				```

				expect( testquery2.recordCount ).toBe( 1 );
				expect( testquery2.amount_local ).toBe( '' );
				expect( isNull( testquery2.getColumn('amount_local').get(1,nullValue()) ) ).toBeTrue();
			});

		});

	}
	
	
} 