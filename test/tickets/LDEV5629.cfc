component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {

		describe( title="Test case LDEV-5629 invoke with tag based cfc", body=function() {

			it(title="checking cfinvokeargument preserves types", body=function( currentSpec ) {
				```
				<cfinvoke component="LDEV5629.ldev5629_tag" method="testNumbers" returnvariable="local.result">
					<cfinvokeargument name="tStr" value="string to test" >
					<cfinvokeargument name="tNum" value=1234 >
				</cfinvoke>
				```
				expect( result.tStr.getClass().getName() ).toBe( "java.lang.String" );
				expect( result.tNum.getClass().getName() ).toBe( "java.lang.Double" );

			});

			it(title="checking invoke preserves types", body=function( currentSpec ) {

				var obj= new LDEV5629.ldev5629_tag();
				var result = invoke( obj, "testNumbers", {tStr="a string to test", tNum=1234});
				
				expect( result.tStr.getClass().getName() ).toBe( "java.lang.String" );
				expect( result.tNum.getClass().getName() ).toBe( "java.lang.Double" );
			});
		});

		describe( title="Test case LDEV-5629 invoke with script based cfc", body=function() {

			it(title="checking cfinvokeargument preserves types", body=function( currentSpec ) {
				```
				<cfinvoke component="LDEV5629.ldev5629_script" method="testNumbers" returnvariable="local.result">
					<cfinvokeargument name="tStr" value="string to test" >
					<cfinvokeargument name="tNum" value=1234 >
				</cfinvoke>
				```
				expect( result.tStr.getClass().getName() ).toBe( "java.lang.String" );
				expect( result.tNum.getClass().getName() ).toBe( "java.lang.Double" );

			});

			it(title="checking invoke preserves types", body=function( currentSpec ) {

				var obj= new LDEV5629.ldev5629_script();
				var result = invoke( obj, "testNumbers", {tStr="a string to test", tNum=1234});
				
				expect( result.tStr.getClass().getName() ).toBe( "java.lang.String" );
				expect( result.tNum.getClass().getName() ).toBe( "java.lang.Double" );
			});
		});
	}

}
