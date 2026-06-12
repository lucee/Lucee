component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, testBox) {
		describe(title="LDEV-6317 Maven release repository defaults", body=function() {

			it(title="POM default repositories include Google Central mirror and exclude deprecated Sonatype and Google Maven repos", body=function(currentSpec) {
				var repos = createObject("java", "lucee.runtime.mvn.POM").REPOSITORIES;
				var urls = [];
				for (var repo in repos) {
					arrayAppend(urls, repo.getUrl());
				}
				expect(arrayContainsNoCase(urls, "https://maven-central.storage-download.googleapis.com/maven2/")).toBeTrue();
				expect(arrayContainsNoCase(urls, "https://repo1.maven.org/maven2/")).toBeTrue();
				expect(arrayContainsNoCase(urls, "https://oss.sonatype.org/content/repositories/releases/")).toBeFalse();
				expect(arrayContainsNoCase(urls, "https://maven.google.com/")).toBeFalse();
			});

		});
	}

}
