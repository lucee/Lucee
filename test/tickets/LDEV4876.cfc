component extends = "org.lucee.cfml.test.LuceeTestCase" {

	function beforeAll(){

		variables.unordered = structnew();
		variables.ordered = structnew('ordered');

		loop times=5000 {
			var guid= createUUID();
			variables.unordered[ guid ] = true;
			variables.ordered[ guid ] = true;
		}
		systemOutput("", true);
	}

	function run ( testResults , testBox ) {
		describe("Testcase for LDEV-4876 - dump keys sorting",function(){
			
			it(title="test performance dump unordered struct",body =function( currentSpec ){
				var s = getTickCount();
				dump(var=variables.unordered, format="simple"); // keys are sorted before display
				systemOutput("dumping unordered struct took #getTickcount()-s# ms", true);
			});
			
			it(title="test performance dump ordered struct",body =function( currentSpec ){
				var s = getTickCount();
				dump(var=variables.ordered, format="simple");
				systemOutput("dumping ordered struct took #getTickcount()-s# ms", true);
			});

		});
	}

}