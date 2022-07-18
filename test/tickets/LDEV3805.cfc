component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults,testBox ){
		describe("Testcase for LDEV-3805", function(){
			it( title="Duplicate the MAP instanceOf Serializable class with deepCopy true", body=function( currentSpec ){
				var syncMap = createObject( 'java', 'java.util.Collections' ).synchronizedMap({"test":{}});
				var dupMap = duplicate(syncMap);
				expect(dupMap.getClass().getName()).toBe(syncMap.getClass().getName());
			});
			it( title="Duplicate the MAP instanceOf Serializable class with deepCopy fasle", body=function( currentSpec ){
				var syncMap = createObject( 'java', 'java.util.Collections' ).synchronizedMap({"test":{}});
				var dupMap = duplicate(syncMap,false);
				expect(dupMap.getClass().getName()).toBe("java.util.HashMap");
			});
		}); 
	}
}