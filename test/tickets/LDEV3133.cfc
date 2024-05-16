component extends = "org.lucee.cfml.test.LuceeTestCase" labels="struct" {
	function beforeAll(){
		variables.uri = createURI("LDEV3133");
	}

	function run ( testResults , testbox ){
		describe("This testcase for LDEV-3133",function(){
			it(title = "Create struct with structNew",body = function( currentSpec ){
				animals=StructNew();
				animals.Aardwolf="Proteles cristata";
				animals.aardvark="Orycteropus afer";
				animals.Alligator="Mississippiensis";
				animals.albatross="Diomedeidae";
				expect(structKeyList(animals)).toBeWithCase("AARDVARK,AARDWOLF,ALBATROSS,ALLIGATOR");
			});

			// only supported with server setting (i.e. unquoted)
			it(title = "Create struct with StructNew(casesensitive)", skip=true, body = function( currentSpec ){
				animals=StructNew("casesensitive")
				animals.Aardwolf="Proteles cristata";
				animals.aardvark="Orycteropus afer";
				animals.AlliGator="Mississippiensis";
				animals.albatross="Diomedeidae";
				expect(structKeyList(animals)).toBeWithCase("AlliGator,aardvark,albatross,Aardwolf");
				expect(structkeyarray(animals)[2]).toBeWithCase("aardvark");
			});

			it(title = "Create struct with StructNew(casesensitive)", body = function( currentSpec ){
				animals=StructNew("casesensitive")
				animals["Aardwolf"]="Proteles cristata";
				animals["aardwolf"]="Proteles cristata";
				expect(structKeyList(animals)).toIncludeWithCase("Aardwolf");
				expect(structKeyList(animals)).toIncludeWithCase("aardwolf");
			});

			// only supported with server setting (i.e. unquoted)
			it(title = "Create struct with StructNew(ordered-casesensitive)",skip=true, body = function( currentSpec ){
				animals=StructNew("ordered-casesensitive");
				animals.Aardwolf="Proteles cristata";
				animals.aardvark="Orycteropus afer";
				animals.alligator="Mississippiensis";
				animals.Albatross="Diomedeidae";
				expect(structKeyList(animals)).toBeWithCase("Aardwolf,aardvark,alligator,Albatross");
				expect(structkeyarray(animals)[2]).toBeWithCase("aardvark");
			});

			it(title = "Create struct with StructNew(ordered-casesensitive)", body = function( currentSpec ){
				animals=StructNew("ordered-casesensitive")
				animals["Aardwolf"]="Proteles cristata";
				animals["aardwolf"]="Proteles cristata";
				expect(structKeyList(animals)).toBeWithCase("Aardwolf,aardwolf");
			});

			it(title = "Create casesensitive struct with Implicit notation LDEV-4505", skip=true, body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 4}
				);
				expect(trim(result.fileContent)).toBeWithCase("AlliGator,aardvark,albatross,Aardwolf");
			});

			it(title = "Create ordered-casesensitive struct with Implicit notation LDEV-4505", skip=true, body = function( currentSpec ){
				local.result = _InternalRequest(
					template :  "#uri#/test.cfm",
					forms = {scene = 5}
				);
				expect(trim(result.fileContent)).toBeWithCase("Aardwolf,aardvark,alliGator,Albatross");
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}

}