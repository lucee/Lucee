component extends="org.lucee.cfml.test.LuceeTestCase"{
	
	function run( testResults , testBox ) {
		describe( title="Test suite for sameFormFieldsAsArray LDEV-2795", body=function() {
			it( title='sameFormFieldsAsArray=true',body=function( currentSpec ) {
				var uri = createURI( "LDEV2795" );
				var result = _InternalRequest(
					template:"#uri#/enabled/index.cfm",
					form: "a=&b=1&a=&b=2"
				);
				var r = DeserializeJson( result.filecontent.trim() );
				expect( r.a ).toBeArray();
				expect( r.b ).toBeArray();
				expect( ArrayLen( r.a ) ).toBe( 2 );
				expect( ArrayLen( r.b ) ).toBe( 2 );
				expect( r.a[ 1 ] ).toBe( "" );
				expect( r.a[ 2 ] ).toBe( "" );
				expect( result.filecontent.trim() ).toBe( '{"a":["",""],"b":["1","2"],"fieldnames":"a,b"}' );
			});

			it( title='sameFormFieldsAsArray=false', body=function( currentSpec ) {
				var uri = createURI( "LDEV2795" );
				var result = _InternalRequest(
					template:"#uri#/disabled/index.cfm",
					form: "a=&b=1&a=&b=2"
				);
				var r = DeserializeJson( result.filecontent.trim() );
				expect( r.a ).toBeString();
				expect( r.b ).toBeString();
				expect( ListLen(r.b) ).toBe(2);
				expect( r.a ).toBe( "," );
				expect( result.filecontent.trim() ).toBe( '{"a":",","b":"1,2","fieldnames":"a,b"}' );
			});
		});
	}
	private string function createURI(string calledName){
		var baseURI="/test/#listLast( getDirectoryFromPath( getCurrentTemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}
