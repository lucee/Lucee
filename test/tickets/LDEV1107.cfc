<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="image">
	<cfscript>
		function run( testResults , testBox ) {
			describe( "Test suite for LDEV-1107", function() {
				
				// png image 1 (Chrysanthemum) 
				it(title="checking PNGimageinfo3() for PNG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = PNGimageinfo3();
					expect(result.height).toBe(4032);
					expect(result.width).toBe(3024);
				});
				it(title="checking PNGimageRotation3() for PNG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = PNGimageRotation3();
					expect(result.height).toBe(3024);
					expect(result.width).toBe(4032);
				});


				// jpg image 2 (Desert)
				it(title="checking JPGimageinfo2() for JPG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = JPGimageinfo2();
					expect(result.height).toBe(4032);
					expect(result.width).toBe(3024);
				});
				it(title="checking ImageInfo() for JPG format, with height 4032 and width 3024", body = function( currentSpec ) {
					var result = JPGimageRotation2();
					expect(result.height).toBe(3024);
					expect(result.width).toBe(4032);
				});

				// png image 3 (truck)
				it(title="checking PNGimageinfo3(), when image rotate into 90 degree in clockwise direction", body = function( currentSpec ) {
					var result = PNGimageinfo3();
					expect(result.height).toBe(4032);
					expect(result.width).toBe(3024);
				});
				it(title="checking PNGimageRotation3(), when image rotate into 90 degree in clockwise direction", body = function( currentSpec ) {
					var result = PNGimageRotation3();
					expect(result.height).toBe(3024);
					expect(result.width).toBe(4032);
				});
			});
		}
		//private Function//
		private string function createURI(string calledName){
			var baseURI="/test/#listLast(getDirectoryFromPath(getCurrenttemplatepath()),"\/")#/";
			return baseURI&""&calledName;
		}
	</cfscript>

	<cffunction name="PNGimageinfo1" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/Chrysanthemum.png")>
		<cfimage action="read" source="#uri#" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>
	<cffunction name="PNGimageRotation1" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/Chrysanthemum.png")>
		<cfimage action="rotate" source="#uri#" angle="90" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>

	<cffunction name="JPGimageinfo2" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/Desert.jpg")>
		<cfimage action="read" source="#uri#" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>
	<cffunction name="JPGimageRotation2" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/Desert.jpg")>
		<cfimage action="rotate" source="#uri#" angle="90" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>

	<cffunction name="PNGimageinfo3" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/test1.png")>
		<cfimage action="read" source="#uri#" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>
	<cffunction name="PNGimageRotation3" returntype="Struct" access="private" localmode="true">
		<cfset uri = createURI("LDEV1107/test1.png")>
		<cfimage action="rotate" source="#uri#" angle="90" name="result">
		<cfset info=ImageInfo(result)>
		<cfreturn info>
	</cffunction>
</cfcomponent>