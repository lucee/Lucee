component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1158", body=function(){
			it(title="checking LinkedhashMap object with init()", body=function(){
				var hashMap = createObject( "java", "java.util.LinkedHashMap" ).init();
				hashMap[ "foo" ] = "bar";
				var hashMapList = '';
				for( key in hashMap ){
					hashMapList = listAppend(hashMapList, "#key#:#hashMap[ key ] #" );
				}
				expect(hashMapList).toBe('foo:bar');
			});

			it(title="checking LinkedhashMap object without init()", body=function(){
				var hashMap = createObject( "java", "java.util.LinkedHashMap" );
				hashMap[ "foo" ] = "bar";
				var hashMapList = '';
				for( key in hashMap ){
					hashMapList = listAppend(hashMapList, "#key#:#hashMap[ key ] #" );
				}
				expect(hashMapList).toBe('foo:bar');
			});
		});
	}
}