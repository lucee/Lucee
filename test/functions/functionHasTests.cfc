component extends="org.lucee.cfml.test.LuceeTestCase" skip="true" { 

	function run( testResults , testBox ) {
		describe( "Checking all functions have tests", function() {

			it(title = "Checking all BIF functions have tests", body = function( currentSpec ) {
				var fl = getFunctionList();//.toStruct(valueAsKey=true);
				var tests = directoryList(path=expandPath("."), listInfo="array")
				var stTests = {};

				loop array=tests  item="local.func" {
					stTests[ listFirst( listLast(func,"/\") ,".") ] = true;
				}

				var missing = [];
				loop collection=fl key="local.func" item="local.ignore"{
					if ( !structKeyExists( stTests, func ) )
						arrayAppend( missing, func );
				}
				expect( missing ).toBeEmpty( missing.toList(", ") );
			});
		});
	};

}