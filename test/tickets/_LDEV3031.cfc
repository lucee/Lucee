component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {
		describe( "test suite for LDEV3031", function() {
			it(title = "Querynew date column without parseDateTime", body = function( currentSpec ) {
				data = queryNew(
				      "id,label,modified"
				    , "integer,varchar,date"
				    , [
				          {id=1, label="Black", modified="2020-09-03 21:05:45.0"}
				        , {id=2, label="Gold", modified="2020-09-03 21:05:45.0"}
				    ]
				);
				sdf = createObject("java", "java.text.SimpleDateFormat").init("MMM d, yyyy h:mm:ss a");
				try {
					res = sdf.format(data.modified[1]);
				}
				catch(any e) {
					res = e.message;
				}
				expect("Sep 3, 2020 9:05:45 PM").toBe(res);
			});

			it(title = "Querynew date column with parseDateTime", body = function( currentSpec ) {
				data = queryNew(
				      "id,label,modified"
				    , "integer,varchar,date"
				    , [
				          {id=1, label="Black", modified="2020-09-03 21:05:45.0"}
				        , {id=2, label="Gold", modified="2020-09-03 21:05:45.0"}
				    ]
				);
				sdf = createObject("java", "java.text.SimpleDateFormat").init("MMM d, yyyy h:mm:ss a");
				try {
					res = sdf.format(parseDateTime(data.modified[1]));
				}
				catch(any e) {
					res = e.message
				}
				expect("Sep 3, 2020 9:05:45 PM").toBe(trim(res));
			});
		});
	}
}