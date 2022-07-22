component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){
		variables.uri = createURI("LDEV3590");
		systemOutput( "--------------", true );
	}

	function run ( testResults , testBox ) {
		describe("This testcase for LDEV-3590",function(){
			
			it(title="test onApplicationStart",body =function( currentSpec ){
				var result=testThread("#variables.uri#/app/index.cfm");
				expect( trim( result ) ).toBe( "test,test,test" );
			});

			it(title="test onSessionStart",body =function( currentSpec ){
				var result=testThread( "#variables.uri#/sess/index.cfm" );
				expect( trim( result ) ).toBe( "test,test,test" );
			});

			it(title="test onApplicationStart and onSessionStart",body =function( currentSpec ){
				var result = testThread( "#variables.uri#/appsess/index.cfm" );
				expect( trim( result ) ).toBe( "test;test,test;test,test;test" );
			});

		});
	}

	private string function testThread( required folder ) {
		var appName= "a" & createUniqueID();
		var names = "";
		loop times=3 {
			var name = appName & createUniqueID();
			names = listAppend( names , name );	
			thread name=name appName=appName folder=folder {
				thread.result = _InternalRequest(
					template: folder
					,url: { "appName": appName }
				).filecontent;
				systemOutput( "thread key list: " & structKeyList(thread), true );
				systemOutput( "thread.result: " & thread.result, true );
			}
		}
		thread action="join" name=names;

		var results = "";
		systemOutput( "folder: " & folder, true );
		systemOutput( "names: " & names, true );

		systemOutput( cfthread, true );

		loop struct=cfthread index="local.name" item="local.threadz" {
			systemOutput( "name: " & name, true );
			if ( listFind( names, threadz.name ) != 0 ) {
				//continue;
				systemOutput(  structKeyList( threadz ), true) ;
				results = listAppend( results , threadz.result );
			}
		}
		
		systemOutput( "results: " & results, true );
		systemOutput( "--------------", true );
		return results;
	}

	private string function createURI(string calledName){
		var baseURI = "/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
		return baseURI&""&calledName;
	}
}