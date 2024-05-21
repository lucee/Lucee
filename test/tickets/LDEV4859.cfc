component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='LDEV-4859' , body=function(){
			it( title='test createObject' , body=function() {
				expect(function(){
					var nioPath = createObject("java", "java.nio.file.Paths").get( getTempFile( getTempDirectory(), "ldev4859" ), [] );
					var nioAttributes = createObject("java", "java.nio.file.attribute.BasicFileAttributes");
					var nioFiles = createObject("java", "java.nio.file.Files");
					var fileAttr = nioFiles.readAttributes(nioPath, nioAttributes.getClass(), []);
					var created = fileAttr.creationTime().toString();

					// issue happens when the reference get read from cache, so we repeat this so it comes from cache
					var nioPath = createObject("java", "java.nio.file.Paths").get( getTempFile( getTempDirectory(), "ldev4859" ), [] );
					var nioAttributes = createObject("java", "java.nio.file.attribute.BasicFileAttributes");
					var nioFiles = createObject("java", "java.nio.file.Files");
					var fileAttr = nioFiles.readAttributes(nioPath, nioAttributes.getClass(), []);
					var created = fileAttr.creationTime().toString();



				}).notToThrow();
			});
		});
	}

}