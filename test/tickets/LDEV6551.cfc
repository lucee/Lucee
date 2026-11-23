component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV6551" {

	function beforeAll() {
		variables.System = createObject( "java", "java.lang.System" );
		variables.propNames = [ "lucee.mvn.repo.releases", "lucee.mvn.repo.releases.0", "lucee.mvn.repo.releases.1" ];
	}

	function afterAll() {
		clearProps();
		resetRepos();
	}

	private function clearProps() {
		for ( var n in variables.propNames ) System.clearProperty( n );
	}

	private function configServer() {
		return getPageContext().getConfig().getConfigServer( request.SERVERADMINPASSWORD ?: "admin" );
	}

	private function resetRepos() {
		var cs = configServer();
		cs.resetMavenRepository();
		cs.resetMavenSnapshotRepository();
	}

	private array function releaseRepoUrls( struct props ) {
		clearProps();
		loop collection=arguments.props key="local.k" value="local.v" {
			System.setProperty( k, v );
		}
		resetRepos();
		var urls = [];
		for ( var rep in configServer().getMavenRepository() ) arrayAppend( urls, rep.getUrl() );
		clearProps();
		resetRepos();
		return urls;
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6551 lucee.mvn.repo.releases as system property / environment variable", function() {

			it( title="single URL", body=function( currentSpec ) {
				var urls = releaseRepoUrls( { "lucee.mvn.repo.releases": "https://single.example.invalid/r/" } );
				expect( urls ).toBe( [ "https://single.example.invalid/r/" ] );
			});

			it( title="comma separated list", body=function( currentSpec ) {
				var urls = releaseRepoUrls( { "lucee.mvn.repo.releases": "https://a.example.invalid/r/, https://b.example.invalid/r/" } );
				expect( urls ).toBe( [ "https://a.example.invalid/r/", "https://b.example.invalid/r/" ] );
			});

			it( title="JSON array", body=function( currentSpec ) {
				var urls = releaseRepoUrls( { "lucee.mvn.repo.releases": '["https://json.example.invalid/r/","https://json2.example.invalid/r/"]' } );
				expect( urls ).toBe( [ "https://json.example.invalid/r/", "https://json2.example.invalid/r/" ] );
			});

			it( title="indexed form (lucee.mvn.repo.releases.0 / .1)", body=function( currentSpec ) {
				var urls = releaseRepoUrls( {
					"lucee.mvn.repo.releases.0": "https://indexed.example.invalid/r/",
					"lucee.mvn.repo.releases.1": "https://indexed2.example.invalid/r/"
				} );
				expect( urls ).toBe( [ "https://indexed.example.invalid/r/", "https://indexed2.example.invalid/r/" ] );
			});

			it( title="not set: default repositories", body=function( currentSpec ) {
				var urls = releaseRepoUrls( {} );
				expect( arrayLen( urls ) ).toBeGT( 0 );
				expect( arrayToList( urls ) ).notToInclude( "example.invalid" );
			});
		});
	}
}
