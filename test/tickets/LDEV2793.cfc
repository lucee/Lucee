component extends="org.lucee.cfml.test.LuceeTestCase" labels="java" {

	function run( testResults , testBox ) {
		describe( title='LDEV-2793' , body=function(){
			it( title='test parseDateTime ' , body=function() {
				var projects = [
					{
						id: 1,
						name: "Really old project",
						createdAt: createDate( 2015, 12, 15 ).getTime() // 1450155600000
					},
					{
						id: 500,
						name: "Recent project",
						createdAt: createDate( 2019, 10, 30 ).getTime() // 1572408000000
					},
					{
						id: 1000,
						name: "Current project",
						createdAt: createDate( 2020, 02, 26 ).getTime() // 1582693200000
					}
				];
				projects.sort(
					( a, b ) => {
						return( b.createdAt - a.createdAt );
					}
				);

			});
		});
	}

}