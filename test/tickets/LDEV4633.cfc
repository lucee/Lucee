component extends="org.lucee.cfml.test.LuceeTestCase" labels="query" {

	function run( testResults , testBox ) {
		describe( title = "Testcase for metadata to be writeable", body = function() {
			var EmailMarketing = new LDev4633.EmailMarketing();

			// get metadata from bean
			var metadata = getMetaData(EmailMarketing);

			// we should have a single property that is "hoisted" from the 'EmailMarketingBase.cfc' into the 'EmailMakreting.cfc'
			expect(metadata.properties.len()).toBe(1, "We should have one property in the metatdata");

		});

		describe( title = "Testcase testing caching of metadata", body = function() {
			var q=new Query();
			var md=getMetadata(q);
			md.susi="Sorglos";
			
			expect(structKeyExists(md,"susi")).toBeTrue();
			// getting the metadata again, should still have the same cached data
			expect(structKeyExists(getMetadata(q),"susi")).toBeTrue();

		});
	}
}
