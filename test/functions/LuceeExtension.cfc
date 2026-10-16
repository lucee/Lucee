component extends="org.lucee.cfml.test.LuceeTestCase" {
	
	function run( testResults , testBox ) {
		describe( title="Test suite for LuceeExtension()", body=function() {

			it(title="list extensions with no group defined (org.lucee should be used)", body = function( currentSpec ) {
				var artifacts = luceeExtension();
				expect(isArray(artifacts)).toBe(true);
				expect(arrayLen(artifacts)>0).toBe(true);

			});

			it(title="list extensions with with group org.lucee", body = function( currentSpec ) {
				var artifacts = luceeExtension("org.lucee");
				expect(isArray(artifacts)).toBe(true);
				expect(arrayLen(artifacts)>0).toBe(true);

			});

			it(title="list versions", body = function( currentSpec ) {
				var artifacts = luceeExtension("org.lucee");
				var max=5;
				loop array=artifacts item="local.artifact" {
					if(--max==0) break;
					var versions = luceeExtension("org.lucee", local.artifact);
					expect(isArray(versions)).toBe(true);
					expect(arrayLen(versions)>0).toBe(true);
					debug("versions for #local.artifact# : #serializeJSON(versions)#");
				}
			});

			it(title="get data for a specific version", body = function( currentSpec ) {
				var artifacts = luceeExtension("org.lucee");
				var max=5;
				loop array=artifacts item="local.artifact" {
					if(--max==0) break;
					var versions = luceeExtension("org.lucee", local.artifact);
					var last = luceeExtension("org.lucee", local.artifact, versions[len(versions)]);
					expect(isStruct(last)).toBe(true);
					expect(structCount(last)>0).toBe(true);
					debug("data for #local.artifact# version #versions[len(versions)]# : #serializeJSON(last)#");
				}
			});
		});
	}
}

