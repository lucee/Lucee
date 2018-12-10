component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for DateDiff", function() {
			it(title = "Checking with DateDiff", body = function( currentSpec ) {

				setTimeZone("CET");<!--- this timezone is used for DST tests --->
				d1=CreateDateTime(2001, 11, 1, 4, 10, 4);
				d2=CreateDateTime(2004, 03, 4, 6, 3, 1);

				assertEquals("73792377", "#DateDiff("s",d1, d2)#");
				assertEquals("1229872", "#DateDiff("n",d1, d2)#");
				assertEquals("20497", "#DateDiff("h",d1, d2)#");
				assertEquals("2", "#DateDiff("yyyy",d1, d2)#");

				assertEquals("#DateDiff("yyyy", 1, 2)#", "0" );


				assertEquals("2131", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#");


				assertEquals("2132", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#");

				<!--- switch to summer time, should be the same --->
				assertEquals("0", "#DateDiff('h', "{ts '2008-03-30 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#");

				assertEquals("127914", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#");
				assertEquals("127974", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#");
				assertEquals("127974", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 3:00:00'}")#");

				assertEquals("7674895", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 1:00:00'}")#");
				assertEquals("7678495", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 2:00:00'}")#");
				assertEquals("7678495", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-03-30 3:00:00'}")#");

				assertEquals("88", "#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 1:00:00'}")#");
				assertEquals("89", "#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 2:00:00'}")#");
				assertEquals("89", "#DateDiff('d', "{ts '2008-01-01 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#");

				assertEquals("1", "#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 1:00:00'}")#");
				assertEquals("2", "#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 2:00:00'}")#");
				assertEquals("2", "#DateDiff('m', "{ts '2008-01-30 2:00:00'}", "{ts '2008-03-30 3:00:00'}")#");




				<!--- year --->
				assertEquals("0", "#DateDiff('yyyy', CreateDate(1974, 6, 28), CreateDate(1975, 5, 28))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDate(1974, 6, 28), CreateDate(1975, 6, 27))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,2,3,3))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,2,3))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,2))#");
				assertEquals("1", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,3))#");
				assertEquals("1", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,4))#");
				assertEquals("1", "#DateDiff('yyyy', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 7, 28,3,3,4))#");
				assertEquals("100", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-100", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-1", "#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,2,9,1,1,30))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,4,9,1,1,30))#");
				assertEquals("-1", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,31))#");

				assertEquals("1", "#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:06'}")#");
				assertEquals("1", "#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("0", "#DateDiff('yyyy', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:04'}")#");

				assertEquals("2", "#DateDiff('yyyy', CreateDate(1975, 1, 9), "{ts '1978-01-07 10:21:34'}")#");
				assertEquals("3", "#DateDiff('yyyy', CreateDate(1974, 6, 28), "{ts '1978-01-07 10:21:34'}")#");
				assertEquals("-2", "#DateDiff('yyyy', "{ts '1978-01-07 10:21:34'}", CreateDate(1975, 1, 9))#");
				assertEquals("-3", "#DateDiff('yyyy', "{ts '1978-01-07 10:21:34'}", CreateDate(1974, 6, 28))#");

				<!--- month --->
				assertEquals("11", "#DateDiff('m', CreateDate(1974, 6, 28), CreateDate(1975, 6, 27))#");
				assertEquals("11", "#DateDiff('m', CreateDate(1974, 6, 28), CreateDate(1975, 5, 28))#");
				assertEquals("11", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,2,3,3))#");
				assertEquals("11", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,2,3))#");
				assertEquals("11", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,2))#");
				assertEquals("12", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,3))#");
				assertEquals("12", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 6, 28,3,3,4))#");
				assertEquals("13", "#DateDiff('m', CreateDateTime(1974, 6, 28,3,3,3), CreateDateTime(1975, 7, 28,3,3,4))#");

				assertEquals("12", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("5", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#");
				assertEquals("5", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#");
				assertEquals("6", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#");
				assertEquals("6", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#");
				assertEquals("6", "#DateDiff('m', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#");

				assertEquals("1200", "#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-1200", "#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-1", "#DateDiff('m', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("1", "#DateDiff('m', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('m', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");


				<!--- days --->
				assertEquals("36525", "#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-36524", "#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-28", "#DateDiff('d', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("31", "#DateDiff('d', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('d', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");

				assertEquals("366", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("181", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#");
				assertEquals("181", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#");

				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:04'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:05'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 6:05:06'}")#");


				assertEquals("8784", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("4366", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#");
				assertEquals("4366", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#");
				assertEquals("4367", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#");
				assertEquals("4367", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#");
				assertEquals("4367", "#DateDiff('h', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#");


				assertEquals("31622400", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("15721198", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#");
				assertEquals("15721199", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#");
				assertEquals("15721200", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#");
				assertEquals("15721201", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#");
				assertEquals("15721202", "#DateDiff('s', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#");

				assertEquals("527040", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2009-01-01 5:05:05'}")#");
				assertEquals("262019", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:03'}")#");
				assertEquals("262019", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:04'}")#");
				assertEquals("262020", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:05'}")#");
				assertEquals("262020", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:06'}")#");
				assertEquals("262020", "#DateDiff('n', "{ts '2008-01-01 5:05:05'}", "{ts '2008-07-01 5:05:07'}")#");




				assertEquals("366", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2009-01-01 5:00:00'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#");
				assertEquals("181", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 4:00:00'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 6:00:00'}")#");

				assertEquals("366", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2009-01-01 5:00:00'}")#");
				assertEquals("181", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 3:00:00'}")#");
				assertEquals("181", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 4:00:00'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#");
				assertEquals("182", "#DateDiff('d', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 6:00:00'}")#");


				assertEquals("4367", "#DateDiff('h', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#");
				assertEquals("262020", "#DateDiff('n', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#");
				assertEquals("15721200", "#DateDiff('s', "{ts '2008-01-01 5:00:00'}", "{ts '2008-07-01 5:00:00'}")#");


				assertEquals("744", "#DateDiff('h', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#");
				assertEquals("44640", "#DateDiff('n', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#");
				assertEquals("2678400", "#DateDiff('s', "{ts '2008-01-01 5:00:00'}", "{ts '2008-02-01 5:00:00'}")#");


				<!--- year --->
				assertEquals("100", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-100", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-1", "#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,2,9,1,1,30))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1974,4,9,1,1,30))#");
				assertEquals("-1", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('yyyy', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,31))#");

				<!--- quarter --->

				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,4,4,4,4,4))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,6,4,4,4,4))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,3))#");
				assertEquals("1", "#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,4))#");
				assertEquals("1", "#DateDiff('q', CreateDateTime(1975,4,4,4,4,4), CreateDateTime(1975,7,4,4,4,5))#");

				assertEquals("400", "#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-400", "#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('q', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");




				<!--- days (y) --->
				assertEquals("36525", "#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-36524", "#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-28", "#DateDiff('y', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("31", "#DateDiff('y', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('y', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");


				<!--- w (weekdays) --->
				assertEquals("5217", "#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-5217", "#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-4", "#DateDiff('w', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("4", "#DateDiff('w', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('w', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");

				<!--- ww (weeks) --->
				assertEquals("5217", "#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-5217", "#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-4", "#DateDiff('ww', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("4", "#DateDiff('ww', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('ww', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");

				<!--- hour --->
				assertEquals("876600", "#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-876576", "#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-672", "#DateDiff('h', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("744", "#DateDiff('h', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('h', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");

				<!--- minutes --->
				assertEquals("52596000", "#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(2075,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-52594560", "#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1875,1,9,1,1,30))#");

				assertEquals("-40320", "#DateDiff('n', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("44640", "#DateDiff('n', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("0", "#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("0", "#DateDiff('n', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");

				<!--- seconds --->
				assertEquals("31536000", "#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1976,1,9,1,1,30))#");
				assertEquals("0", "#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,30))#");
				assertEquals("-31536000", "#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1974,1,9,1,1,30))#");

				assertEquals("-2419200", "#DateDiff('s', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,2,9,1,1,30))#");
				assertEquals("2678400", "#DateDiff('s', CreateDateTime(1975,3,9,1,1,30), CreateDateTime(1975,4,9,1,1,30))#");
				assertEquals("-1", "#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,29))#");
				assertEquals("1", "#DateDiff('s', CreateDateTime(1975,1,9,1,1,30), CreateDateTime(1975,1,9,1,1,31))#");




				local.date1=CreateDate(2008,03,30);
				local.date2=createDate(2038,03,31);


				assertEquals("946767600", "#dateDiff("s",local.date1,local.date2)#");
				assertEquals("15779460", "#dateDiff("n",local.date1,local.date2)#");
				assertEquals("262991", "#dateDiff("h",local.date1,local.date2)#");
				assertEquals("10958", "#dateDiff("d",local.date1,local.date2)#");
				assertEquals("1565", "#dateDiff("w",local.date1,local.date2)#");
				assertEquals("1565", "#dateDiff("ww",local.date1,local.date2)#");

				assertEquals("120", "#dateDiff("q",local.date1,local.date2)#");
				assertEquals("360", "#dateDiff("m",local.date1,local.date2)#");
				assertEquals("30", "#dateDiff("yyyy",local.date1,local.date2)#");


				local.date1=CreateDate(2008,03,30);
				local.date2=createDate(2008,03,31);
				local.date3=createDate(2008,04,01);


				assertEquals("0", "#dateDiff("w",local.date1,local.date2)#");

				assertEquals("0", "#dateDiff("m",local.date1,local.date2)#");
				assertEquals("0", "#dateDiff("m",local.date1,local.date3)#");

				assertEquals("1", "#dateDiff("d",local.date1,local.date2)#");
				assertEquals("2", "#dateDiff("d",local.date1,local.date3)#");
				assertEquals("1", "#dateDiff("d",local.date2,local.date3)#");

				assertEquals("23", "#dateDiff("h",local.date1,local.date2)#");
				assertEquals("47", "#dateDiff("h",local.date1,local.date3)#");
				assertEquals("24", "#dateDiff("h",local.date2,local.date3)#");



				local.date1=createDate(2007,03,30);
				local.date2=createDate(2007,03,31);
				local.date3=createDate(2007,04,01);

				assertEquals("1", "#dateDiff("d",local.date1,local.date2)#");
				assertEquals("2", "#dateDiff("d",local.date1,local.date3)#");
				assertEquals("1", "#dateDiff("d",local.date2,local.date3)#");

				assertEquals("24", "#dateDiff("h",local.date1,local.date2)#");
				assertEquals("48", "#dateDiff("h",local.date1,local.date3)#");
				assertEquals("24", "#dateDiff("h",local.date2,local.date3)#");
			});
		});	
	}
}