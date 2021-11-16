component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){}

	function afterAll(){}

	function run( testResults , testBox ) {
		describe( "tests for the type sruct", function() {

			it(title="test listener struct return a value", body=function(){
				var sct=structNew(onMissingKey:function(key,data){
    				return "notexistingvalue";
				});
				sct.a=1;
				
				expect(sct.a).toBe(1);
				expect(sct.notexistingkey).toBe("notexistingvalue");
				expect(structKeyList(sct)).toBe("a");
			});

			it(title="test listener struct return a value and store it", body=function(){
				var sct=structNew(onMissingKey:function(key,data){
					return data[key] = "notexistingvalue";
				});
				sct.a=1;
				
				expect(sct.a).toBe(1);
				expect(sct.notexistingkey).toBe("notexistingvalue");
				expect(listSort(structKeyList(sct),"textnocase")).toBe("a,notexistingkey");
			});

			it(title="test listener struct throwing an exception", body=function(){
				var sct=structNew(onMissingKey:function(key,data){
					throw "sorry but we cannot help!";
				});
				
				var msg="";
				try {
					var a=sct.notexistingkey;
				}
				catch(e) {
					msg=e.message;
				}
				expect(msg).toBe("sorry but we cannot help!");
			});

			it(title="shorthand notation", body=function() {
				var someKey = 42;
				var shortHandStruct = { someKvPair: "a", someKey, someOtherKvPair: "b" };

				expect(shortHandStruct.keyExists("someKey")).toBe(true);
				expect(shortHandStruct.someKey).toBe(42);

				var identity = (v) => v;

				expect(identity({identity}).identity).toBe(identity);
			})

		});
	}
}
