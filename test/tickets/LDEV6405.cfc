component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, testBox) {
		describe(title="LDEV-6405 ExtensionLister strategies", body=function() {

			it(title="GroupMetadataExtensionLister parses artifact ids from group metadata XML", body=function(currentSpec) {
				var xml = '<metadata><groupId>org.lucee</groupId><artifacts><artifact><artifactId>redis-extension</artifactId><latest>4.1.0.0-SNAPSHOT</latest><release>4.0.0.2</release></artifact><artifact><artifactId>activation</artifactId></artifact><artifact><artifactId>s3-extension</artifactId></artifact></artifacts><lastUpdated>20260612153000</lastUpdated></metadata>';
				var artifacts = createObject("java", "lucee.runtime.config.maven.extensionlist.GroupMetadataExtensionLister").parse(xml);
				expect(artifacts.size()).toBe(2);
				expect(artifacts.contains("redis-extension")).toBeTrue();
				expect(artifacts.contains("s3-extension")).toBeTrue();
			});

			it(title="GroupMetadataExtensionLister returns empty set for missing artifacts element", body=function(currentSpec) {
				var xml = '<metadata><groupId>org.lucee</groupId><lastUpdated>20260612153000</lastUpdated></metadata>';
				var artifacts = createObject("java", "lucee.runtime.config.maven.extensionlist.GroupMetadataExtensionLister").parse(xml);
				expect(artifacts.size()).toBe(0);
			});

			it(title="EmptyExtensionLister returns empty set without network access", body=function(currentSpec) {
				var Repository = createObject("java", "lucee.runtime.config.maven.MavenUpdateProvider$Repository");
				var repo = Repository.init("Sonatype Snapshots", "https://central.sonatype.com/repository/maven-snapshots/", 1, 0, 0);
				var artifacts = createObject("java", "lucee.runtime.config.maven.extensionlist.EmptyExtensionLister").list(repo, "org.lucee");
				expect(artifacts.size()).toBe(0);
			});

			it(title="SolrSearchExtensionLister builds search URL from repository URL", body=function(currentSpec) {
				var searchUrl = createObject("java", "lucee.runtime.config.maven.extensionlist.SolrSearchExtensionLister").buildSearchUrl("http://localhost:8856/", "org.lucee", 0);
				var expectedPrefix = "http://localhost:8856/solrsearch/select?q=g%3Aorg.lucee";
				expect(left(searchUrl, len(expectedPrefix))).toBe(expectedPrefix);
			});

			it(title="SolrSearchExtensionLister parses artifact ids from JSON", body=function(currentSpec) {
				var json = '{"response":{"numFound":2,"docs":[{"a":"s3-extension","g":"org.lucee"},{"a":"../bad","g":"org.lucee"}]}}';
				var page = createObject("java", "lucee.runtime.config.maven.extensionlist.SolrSearchExtensionLister").parse(json);
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
