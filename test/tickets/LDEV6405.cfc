component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, testBox) {
		describe(title="LDEV-6405 ExtensionLister strategies", body=function() {

			it(title="MavenCentralSearchExtensionLister parses artifact ids from JSON", body=function(currentSpec) {
				var json = '{"response":{"numFound":2,"docs":[{"a":"s3-extension","g":"org.lucee"},{"a":"../bad","g":"org.lucee"}]}}';
				var page = createObject("java", "lucee.runtime.config.maven.extensionlist.MavenCentralSearchExtensionLister").parse(json);
				expect(page.numFound).toBe(2);
				expect(page.artifacts.size()).toBe(1);
				expect(page.artifacts.contains("s3-extension")).toBeTrue();
			});

			it(title="luceeExtension lists org.lucee extensions via new discovery", body=function(currentSpec) {
				var artifacts = luceeExtension("org.lucee");
				expect(isArray(artifacts)).toBeTrue();
				expect(arrayLen(artifacts)).toBeGT(20);
				expect(arrayContains(artifacts, "s3-extension")).toBeTrue();
			});

		});
	}

}
