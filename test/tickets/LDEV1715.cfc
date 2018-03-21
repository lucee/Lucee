component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test suite for LDEV-1715", body=function(){
			it(title="Checking getmetaDataFunction with abstract modifier, abstract extends in other cfc", body=function(){
				var result = getComponentMetadata("LDEV1715.app1.abstractComponent");
				expect(ArrayLen(result.functions)).toBe("2");
			});

			it(title="Checking getmetaDataFunction with abstract modifier, without extend abstract", body=function(){
				var result = getComponentMetadata("LDEV1715.app2.abstractComponent");
				expect(ArrayLen(result.functions)).toBe("2");
			});
		});
	}
}