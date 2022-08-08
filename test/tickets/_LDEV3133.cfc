component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function beforeAll(){
		variables.uri = createURI("LDEV3133");
	}

	function run ( testResults , testbox ){
		describe("This testcase for LDEV-3133",function(){
			it(title = "Create struct with structNew",body = function( currentSpec ){
				animals=StructNew()
				animals.Aardwolf="Proteles cristata";
				animals.aardvark="Orycteropus afer";
				animals.Alligator="Mississippiensis";
				animals.albatross="Diomedeidae";
				expect(structKeyList(animals)).tobewithcase("AARDVARK,AARDWOLF,ALBATROSS,ALLIGATOR");
			});

			it(title = "Create struct with StructNew(casesensitive)",body = function( currentSpec ){
				animals=StructNew("casesensitive")
				animals.Aardwolf="Proteles cristata";
				animals.aardvark="Orycteropus afer";
				animals.AlliGator="Mississippiensis";
				animals.albatross="Diomedeidae";
				expect(structKeyList(animals)).tobewithcase("AlliGator,aardvark,albatross,Aardwolf");
				expect(structkeyarray(animals)[2]).tobewithcase("aardvark");
			});	

			it(title = "Create struct with StructNew(ordered-casesensitive)",body = function( currentSpec ){
				animals=StructNew("ordered-casesensitive")
				animals.Aardwolf="Proteles cristata"
				animals.aardvark="Orycteropus afer"
				animals.alligator="Mississippiensis"
				animals.Albatross="Diomedeidae"
				expect(structKeyList(animals)).tobewithcase("Aardwolf,aardvark,alligator,Albatross");
				expect(structkeyarray(animals)[2]).tobewithcase("aardvark");
			});	

			it(title = "Create casesensitive struct with Implicit notation",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 4}
				);
				expect(trim(result.fileContent)).tobewithcase("AlliGator,aardvark,albatross,Aardwolf");
			});

			it(title = "Create ordered-casesensitive struct with Implicit notation",body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 5}
				);
				expect(trim(result.fileContent)).tobewithcase("Aardwolf,aardvark,alliGator,Albatross");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}