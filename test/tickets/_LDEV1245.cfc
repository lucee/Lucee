component extends="org.lucee.cfml.test.LuceeTestCase"{

	// skip closure
	function isNotSupported() {
		var isNotSupport=getCredentials();
		if( isNotSupport == true ){
			isNotSupport = false;
		} else{
			isNotSupport = true;
		}
		return isNotSupport;
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1245", skip=isNotSupported(), body=function(){
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

	// Private functions
	private boolean function getCredentials() {
		var fullNullSupport = false;
		if( structKeyExists(server.system.environment, "lucee.full.null.support")){
			fullNullSupport = server.system.environment["lucee.full.null.support"];
		}
		return fullNullSupport;
	}
}


