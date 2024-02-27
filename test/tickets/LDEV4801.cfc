component extends="org.lucee.cfml.test.LuceeTestCase" labels="array" {

	public function beforeAll(){
		variables.size = 2000;
	}

	public function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-4801 ( improve arrayToList )", body=function() {
			it(title="Checking arrayToList() 5k, empty delim", body = function( currentSpec ) {
				var arr = [];
				var str = ExtensionList().toJson();
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr,"" );
			});

			it(title="Checking arrayToList() 5k, single delim", body = function( currentSpec ) {
				var arr = [];
				var str = ExtensionList().toJson();
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr, "1" );
			});

			it(title="Checking arrayToList() 5k, double delim", body = function( currentSpec ) {
				var arr=[];
				var str = ExtensionList().toJson();
				ArraySet( arr, 1, size, str );
				var y = ArrayToList( arr,"12" );
			});
		});
	}

}
