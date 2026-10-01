component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function run( testResults, testBox ) {
		describe( "LDEV-5671 safe navigation operator must not swallow exceptions", function() {

			it( title="an exception thrown by a function on the left side of ?. propagates", body=function( currentSpec ) {
				expect( function() {
					var r = ldev5671_throwError()?.test;
				}).toThrow( regex="LDEV-5671 Oops", message="?. swallowed the exception thrown by ldev5671_throwError()" );
				expect( function() {
					var r = ldev5671_throwError( dummy=1 )?.test;
				}).toThrow( regex="LDEV-5671 Oops", message="?. swallowed the exception thrown by ldev5671_throwError( dummy=1 )" );
			});

			it( title="an exception thrown by a component method on the left side of ?. propagates", body=function( currentSpec ) {
				expect( function() {
					var r = this.ldev5671_throwErrorPublic()?.test;
				}).toThrow( regex="LDEV-5671 Oops", message="?. swallowed the exception thrown by this.ldev5671_throwErrorPublic()" );
			});

			it( title="an exception thrown by a closure member on the left side of ?. propagates", body=function( currentSpec ) {
				var obj = { fn: function() {
					throw "LDEV-5671 Oops";
				} };
				expect( function() {
					var r = obj.fn()?.test;
				}).toThrow( regex="LDEV-5671 Oops", message="?. swallowed the exception thrown by obj.fn()" );
			});

			it( title="?. still returns null for a null or undefined left side", body=function( currentSpec ) {
				expect( isNull( ldev5671_returnNull()?.test ) ).toBeTrue();
				var n = nullValue();
				expect( isNull( n?.test ) ).toBeTrue();
				expect( isNull( n?.test() ) ).toBeTrue();
				expect( isNull( ldev5671_notExisting?.test ) ).toBeTrue();
				expect( isNull( ldev5671_notExisting?.test() ) ).toBeTrue();
			});

			it( title="?. still returns the value for a non-null left side", body=function( currentSpec ) {
				expect( ldev5671_returnStruct()?.test ).toBeTrue();
				expect( isNull( ldev5671_returnStruct()?.notExisting ) ).toBeTrue();
			});

		});
	}

	private function ldev5671_throwError() {
		throw "LDEV-5671 Oops";
		return { test: true };
	}

	public function ldev5671_throwErrorPublic() {
		throw "LDEV-5671 Oops";
		return { test: true };
	}

	private function ldev5671_returnNull() {
		return;
	}

	private function ldev5671_returnStruct() {
		return { test: true };
	}
}
