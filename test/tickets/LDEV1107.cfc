<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-1107", function() {
				it(title="checking ImageInfo() for PNG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = PNGimageinfo();
					expect(result.height).toBe(4032);
					expect(result.width).toBe(3024);
				});

				it(title="checking ImageInfo() for JPG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = JPGimageinfo();
					expect(result.height).toBe(4032);
					expect(result.width).toBe(3024);
				});
			});
		}
		//private Function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>

	<cffunction name="PNGimageinfo" returntype="Struct" access="private">
		<cfset uri = createURI("LDEV1107/Chrysanthemum.png")>
		<cfimage action="read" source="#uri#" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>

	<cffunction name="JPGimageinfo" returntype="Struct" access="private">
		<cfset uri = createURI("LDEV1107/Desert.jpg")>
		<cfimage action="read" source="#uri#" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>
</cfcomponent>