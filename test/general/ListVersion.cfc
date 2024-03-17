component extends="org.lucee.cfml.test.LuceeTestCase"  skip="true"{
	function beforeAll(){
		
	}

	function run( testResults , testBox ) {
		describe( "test listing external versions", function() {


			it( title='test version listing', body=function( currentSpec ) {
				var versions=LuceeVersionsList();
				expect(isArray(versions)).toBeTrue();
				expect(arrayLen(versions)>100).toBeTrue();
			});

			it( title='test Maven specific version listing', body=function( currentSpec ) {
				var versions=LuceeVersionsListMvn();
				expect(isArray(versions)).toBeTrue();
				expect(arrayLen(versions)>100).toBeTrue();
			});

			it( title='test S3 specific version listing', body=function( currentSpec ) {
				var versions=LuceeVersionsListS3();
				expect(isQuery(versions)).toBeTrue();
				expect(versions.recordcount>100).toBeTrue();
			});
		});
	}

}