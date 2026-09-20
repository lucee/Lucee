component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5660", body=function() {
			it(title="checking bundles defined in the core manifest are all mapped in the CORE BundleProvider", body = function( currentSpec ) {
				var bp = new component {
					import lucee.runtime.config.s3.BundleProvider;
					function getMappings(){
						return BundleProvider::getMappings();
					}
				};

				var mappings = bp.getMappings();

				checkOSGIBundlesMappings( mappings );
			});

			// the method BundleProvider::getMappings() now need a `JarFile` as argument that points to the lucee core 
			// and then loads the mappings from the same place as the test below, we can make this work, but maybe not worth the effort
			// because it does the same
			it(title="checking bundles defined in the core manifest are all mapped in the LOADER BundleProvider", body = function( currentSpec ) {
				var bp = new component {
					import lucee.loader.engine.BundleProvider;
					function getMappings(){

						var pc= getPageContext();
						var bi=bundleInfo(pc);
						var jf= new java.util.jar.JarFile(bi.location);
						return BundleProvider::getMappings(jf);
					}
				};

				var mappings = bp.getMappings();

				checkOSGIBundlesMappings( mappings );
			});

		});
	}

	private function checkOSGIBundlesMappings( mappings ){
		// LDEV-5841: expandPath now resolves correctly from current template directory
		// Navigate from test/tickets/LDEV5567 -> ../../../ (project root) -> core/...
		var mfPath = expandpath("../../../core/src/main/java/META-INF/MANIFEST.MF");
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
