component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		
		describe( title = "Test suite for QueryPrepend", body = function() {

			it( title = 'Checking with to queries with the same columns',body = function( currentSpec ) {
				
				var q1 = queryNew("id,name","Integer,Varchar", 
				[ 
				{id=4,name="Four"}, 
				{id=5,name="Five"}, 
				{id=6,name="Six"} 
				]); 
				var q2 = queryNew("id,name","Integer,Varchar", 
				[ 
				{id=1,name="One"}, 
				{id=2,name="Two"}, 
				{id=3,name="Three"} 
				]); 

				var qry=QueryPrepend(q1,q2);

				assertEquals("1,2,3,4,5,6",valueList(qry.id));
				assertEquals("1,2,3,4,5,6",valueList(q1.id));
				assertEquals("One,Two,Three,Four,Five,Six",valueList(q1.name));
			});
			it( title = 'Checking with to queries with the different columns',body = function( currentSpec ) {
				var q1 = queryNew("id,name","Integer,Varchar", 
				[ 
				{id=1,name="One"}, 
				{id=2,name="Two"}, 
				{id=3,name="Three"} 
				]); 
				var q2 = queryNew("id,label","Integer,Varchar",
				[ 
				{id=4,label="Four"}, 
				{id=5,label="Five"}, 
				{id=6,label="Six"} 
				]); 

				var error=false;
				try {
					var qry=QueryPrepend(q1,q2);
				}
				catch(e) {
					error=true;
				}
				assertTrue(error);
			});
		});

	}
}