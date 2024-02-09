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


			it(title="literal struct valid", body=function() {
				var x={susi="Sorglos",peter="Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

				var x={"susi"="Sorglos","peter"="Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

				var x={'susi'="Sorglos",'peter'="Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

				var x={susi:"Sorglos",peter:"Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

				var x={"susi":"Sorglos","peter":"Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

				var x={'susi':"Sorglos",'peter':"Lustig"};
				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");

			});

			it(title="short hand literal struct valid", body=function(){
				var susi="Sorglos";
				var peter="Lustig";
				var x={susi,peter};

				expect(structCount(x)).toBe(2);
				expect(x.susi).toBe("Sorglos");
				expect(x.peter).toBe("Lustig");
			});


			it(title="short hand literal struct invalid 1", body=function(){
				// we need to make this in a separate file because this creates a template exception (comppiler)
				var uri=createURI("Struct/invalid1.cfm");
				expect(function() {
					_InternalRequest(template:uri);
				}).toThrow(type="Template");
			});
			it(title="short hand literal struct invalid 2", body=function(){
				// we need to make this in a separate file because this creates a template exception (comppiler)
				var uri=createURI("Struct/invalid2.cfm");
				expect(function() {
					_InternalRequest(template:uri);
				}).toThrow(type="Template");
			});
			it(title="short hand literal struct invalid 3", body=function(){
				// we need to make this in a separate file because this creates a template exception (comppiler)
				var uri=createURI("Struct/invalid3.cfm");
				expect(function() {
					_InternalRequest(template:uri);
				}).toThrow(type="Template");
			});


		});
	}

	private string function createURI(string calledName){
		var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}