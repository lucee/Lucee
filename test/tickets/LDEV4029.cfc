component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function testHttpsMaven() {
		var metaDataURL = "https://repo1.maven.org/maven2/org/apache/tika/tika-core/maven-metadata.xml";
		cfhttp( url=metaDataURL, result="local.res");
		expect(	res.status_code ).toBe( 200 );
	}

}
