component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run(){
		describe( title="Test cases for LDEV-1145", body=function(){
			it(title="switch case with duplicate case value", body=function(){
				var hasError = false;
				try {
					switch ("b") {
				        case "a": "case A";
				        case "a": "Case duplicate A";
				        case "b": "Case B";
				        break;
					};
				}
				catch ( any e ){
					hasError = true;
				}
				expect(hasError).toBeTrue();
			});
		});
	}
}