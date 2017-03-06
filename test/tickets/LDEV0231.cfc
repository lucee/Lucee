<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function beforeAll(){
			// runs before all testcases
		}

		function afterAll(){
			// runs after all testcases
		}

		function run( testResults , testBox ){
			describe( title="Test suite for checking cfschedule result attribute in Lucee", body=function(){
				beforeEach(function( currentSpec ){
					// runs before each spec in this suite group
					hasError = false;
				});

				afterEach(function( currentSpec ){
					// Runs after each spec in this suite group
				});

				it(title="Case 1: returnvariable attribute for cfschedule (should still be available)", body=function(){
					try{
						local.result= cfscheduleWithReturnVariable();
					}catch( any e ){
						hasError = true;
					}
					expect(hasError).toBeFalse();
					// But it set to false here, so we need to remove the attribute returnvariable
				}, labels="returnvariable attribute for cfschedule (should still be available)");

				it(title="Case 2: returnvariable attribute for script version of cfschedule (should still be available)", body=function(){
					try{
						local.result= cfscheduleWithReturnVariableScript();
					}catch( any e ){
						hasError = true;
					}
					expect(hasError).toBeFalse();
					// But it set to false here, so we need to remove the attribute returnvariable
				}, labels="returnvariable attribute for script version of cfschedule (should still be available)");



				it(title="Case 3: result attribute for cfschedule(should be available)", body=function(){
					try{
						local.result = cfscheduleWithResult();
					}catch( any e ){
						hasError = true;
					}
					expect(hasError).toBeFalse();
					// But it set to true here, so we need to add the attribute result
				}, labels="result attribute for cfschedule(should be available)");



				it(title="Case 4: result attribute for script version of cfschedule(should be available)", body=function(){
					try{
						local.result = cfscheduleWithResultScript();
					}catch( any e ){
						hasError = true;
					}
					expect(hasError).toBeFalse();
					// But it set to true here, so we need to add the attribute result
				}, labels="result attribute for script version of cfschedule(should be available)");
			}

			, labels="Test suite for checking cfschedule result attribute in Lucee");
		}


		private function cfscheduleWithReturnVariableOldScript() {
			var attrStruct = {};
			attrStruct.action = "list";
			attrStruct.returnvariable = "local.result";
			schedule attributeCollection="#attrStruct#";

			return local.result;
		}

		private function cfscheduleWithReturnVariableScript() {
			schedule action = "list" returnvariable = "local.result";
			return local.result;
		}

		private function cfscheduleWithResultScript() {
			var attrStruct = {};
			attrStruct.action = "list";
			attrStruct.result = "local.result";
			schedule attributeCollection="#attrStruct#";

			return local.result;
		}
		
		

	</cfscript>

	<cffunction name="cfscheduleWithReturnVariableOld" access="private">
		<cfset attrStruct = {}>
		<cfset attrStruct.action = "list">
		<cfset attrStruct.returnvariable = "local.result">
		<cfschedule attributeCollection="#attrStruct#">

		<cfreturn local.result>
	</cffunction>

	<cffunction name="cfscheduleWithReturnVariable" access="private">
		<cfschedule action = "list" returnvariable = "local.result">
		<cfreturn local.result>
	</cffunction>

	<cffunction name="cfscheduleWithResult" access="private">
		<cfset attrStruct = {}>
		<cfset attrStruct.action = "list">
		<cfset attrStruct.result = "local.result">
		<cfschedule attributeCollection="#attrStruct#">

		<cfreturn local.result>
	</cffunction>
</cfcomponent>