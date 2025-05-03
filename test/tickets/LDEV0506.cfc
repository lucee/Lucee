<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe("Test suite for LDEV-506", function() {
				it("simple function call, for cached UDF", function() {
					try{
						var result = cachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(15);
				});

				it("calling cached UDF created by objectload()", function() {
					var loadObj = getObject();
					try{
						var result = loadObj.cachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(15);
				});

				it("simple function call, for non cached UDF", function() {
					try{
						var result = nonCachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(150);
				});

				it("calling non-cached UDF created by objectload()", function() {
					var loadObj = getObject();
					try{
						var result = loadObj.nonCachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(150);
				});
			});
		}

		private function getObject(){
			var functions={ cachedFunction:cachedFunction, nonCachedFunction:nonCachedFunction};
			var savedObj = objectsave(functions);
			var objLoad = objectload(savedObj);
			return objLoad;
		}
	</cfscript>
	<!--- Private Function  --->
	<cffunction name="cachedFunction" cachedwithin="request" localmode="modern" access="private">
		<cfset var a = 10>
		<cfset var b = 5>
		<cfset var c = a + b>
		<cfreturn c>
	</cffunction>

	<cffunction name="nonCachedFunction"  localmode="modern" access="private">
		<cfset var a = 100>
		<cfset var b = 50>
		<cfset var c = a + b>
		<cfreturn c>
	</cffunction>
</cfcomponent>
