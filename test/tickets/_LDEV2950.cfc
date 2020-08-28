<cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<cfscript>
		function run(){
			describe( title = "Test suite for LDEV-2950", body=function(){
				it(title = "checking refindnocase with subexpression", body=function(){
					expect('S21').toBe(refindnocase( "^S(\d\d)$", "S21", 1, true ).match[1]);
					expect('1').toBe(refindnocase( "^S(\d\d)$", "S21", 1, false ));
					expect('21').toBe(refindnocase( "^S(\d\d)$", "S21", 1, true ).match[2]);
				});

				it(title = "checking refind with subexpression", body=function(){
					expect('S21').toBe(reFind( "^S(\d\d)$", "S21", 1, true ).match[1]);
					expect('1').toBe(reFind( "^S(\d\d)$", "S21", 1, false ));
					expect('21').toBe(reFind( "^S(\d\d)$", "S21", 1, true ).match[2]);
				});
			});
		}
	</cfscript>
</cfcomponent>