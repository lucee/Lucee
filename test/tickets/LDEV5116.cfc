// this is simply crappy code, and will never be supported by lucee, WONT FIX
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-5116", function() {
			it(title = "", body = function( currentSpec ) {
				var BundleProvider=createObject("java","lucee.runtime.config.s3.BundleProvider");
				var BundleDefinition=createObject("java","lucee.runtime.osgi.OSGiUtil$BundleDefinition");
				var bd=BundleDefinition.init("com.mysql.cj",nullValue());
				var uri=BundleProvider.getInstance().getBundleAsURL(bd, true);
				expect( fileExists( uri.toString() ) ).tobeTrue();
			});
		});
	}
}