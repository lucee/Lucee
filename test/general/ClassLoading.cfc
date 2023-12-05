component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( "test suite for classloading", function() {
			it(title="checking OSGi Bundle loading with no version defintion", body=function(){
				createObject(
					type:"java",
					class:"org.lucee.mockup.osgi.Test",
					bundleName:"lucee.mockup"
				);
			});
		});
	}
}
