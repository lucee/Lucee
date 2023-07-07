component extends="org.lucee.cfml.test.LuceeTestCase" skip=true {

	function run( testResults , testBox ) {

		describe( title='date parsing regression' , body=function(){

			it( title='parsing a date fails as string arg ' , body=function() {

				var srcDate = dateFormat(now(), "yyyy-mm-dd");
				var date1 = dateAdd("d", srcDate, 90);

				var date2 = dateAdd("d", "2023-07-05", 90); // boom
				
				expect( date1 ).toBeDate();
				expect( date2 ).toBeDate();
			});

		});

	}

} 