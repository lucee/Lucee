<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run( testResults , testBox ) {
			describe("Test suite for LDEV-506", function() {
				it("simple function call, for cached UDF", function() {
					try{
						result = cachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(15);
				});

				it("calling cached UDF created by objectload()", function() {
					loadObj = getObject();
					try{
						result = loadObj.cachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(15);
				});

				it("simple function call, for non cached UDF", function() {
					try{
						result = nonCachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(150);
				});

				it("calling non-cached UDF created by objectload()", function() {
					loadObj = getObject();
					try{
						result = loadObj.nonCachedFunction();
					} catch ( any e){
						result = e.message;
					}
					expect(result).toBe(150);
				});
			});
		}

		private function getObject(){
			functions={ cachedFunction:cachedFunction, nonCachedFunction:nonCachedFunction};
			savedObj = objectsave(functions);
			objLoad = objectload(savedObj);
			return objLoad;
		}
	</cfscript>
	<!--- Private Function  --->
	<cffunction name="cachedFunction" cachedwithin="request" localmode="modern" access="private">
		<cfset a = 10>
		<cfset b = 5>
		<cfset c = a + b>
		<cfreturn c>
	</cffunction>

	<cffunction name="nonCachedFunction"  localmode="modern" access="private">
		<cfset a = 100>
		<cfset b = 50>
		<cfset c = a + b>
		<cfreturn c>
	</cffunction>
</cfcomponent>
