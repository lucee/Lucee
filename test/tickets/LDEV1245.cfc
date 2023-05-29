component extends="org.lucee.cfml.test.LuceeTestCase"{

	function beforeAll(){
		application action="update" NULLSupport=true;
	}

	function afterAll(){
		application action="update" NULLSupport=false;
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1245", body=function(){
			it(title="checking full Null Support enable with null value in Query",  body=function(){
				
				var tmpQry = queryNew( 'column_with_null' );
				queryAddRow( tmpQry, { column_with_null: javacast( 'null', '' ) } );
				var hasError = "";
				try {
					var result = tmpQry.toString();
				} catch ( any e ){
					hasError = e.message;
				}
				expect(hasError).toBe("");
			});
		});
	}

}


