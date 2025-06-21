component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5660", body=function() {

			it(title="checking bundles defined in the core manifest are all mapped in the core BundleProvider", body = function( currentSpec ) {
				var bp = new component {
					import lucee.runtime.config.s3.BundleProvider;
					function getMappings(){
						return BundleProvider::getMappings();
					}
				};

				var mappings = bp.getMappings();

				checkOSGIBundlesMappings( mappings );
			});

			it(title="checking bundles defined in the core manifest are all mapped in the loader BundleProvider", body = function( currentSpec ) {
				var bp = new component {
					import lucee.loader.engine.BundleProvider;
					function getMappings(){
						return BundleProvider::getMappings();
					}
				};

				var mappings = bp.getMappings();

				checkOSGIBundlesMappings( mappings );
			});

		});
	}

	private function checkOSGIBundlesMappings( mappings ){
		var mfPath = expandpath("../core/src/main/java/META-INF/MANIFEST.MF");
		var manifestBundles = getBundlesFromManifest( mfPath );

		var missing = structFilter( manifestBundles, function( key, value ){
			return !structKeyExists( mappings, key );
		});

		expect( missing ).toBeEmpty( structKeyList( missing ) );
	}

	private struct function getBundlesFromManifest( mfPath ){
		var manifest = manifestRead(mfPath);
		var bundles = manifest.main["Require-Bundle"];
		var _bundles = listToArray(bundles);
		var manifestBundles = {};
		arrayEach(_bundles, function(el){
			var bundle = listToArray(el,";");
			manifestBundles[bundle[1]] = listLast(bundle[2], "=");
		});
		return manifestBundles;
	}

}
