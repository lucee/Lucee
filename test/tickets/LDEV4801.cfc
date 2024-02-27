component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	public function beforeAll(){
		variables.size = 1000;
		variables.str = ExtensionList().toJson();
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4801 ( improve arrayToList )", body=function() {
			it(title="Checking arrayToList() 2k, empty delim", body = function( currentSpec ) {
				var arr = [];
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr, "" );
			});

			it(title="Checking arrayToList() 2k, single delim", body = function( currentSpec ) {
				var arr = [];
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr, "1" );
			});

			it(title="Checking arrayToList() 2k, double delim", body = function( currentSpec ) {
				var arr = [];
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr,"12" );
			});
		});
	}

}
