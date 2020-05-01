omponent extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults, testBox ) {
		describe( title = "Testcase for LDEV-2337", body = function() {
			it( title = "Lucee does not support using Unicode currency symbols in variable names", body = function( CurrentSpec ) {
				VARIABLES._$ = 41.00;
				VARIABLES._€ = 42.00;
				VARIABLES._£ = 43.00;
				VARIABLES._₤ = 44.00;
				VARIABLES._₠ = 45.00;
				VARIABLES._₡ = 46.00;
				VARIABLES._₢ = 47.00;
				VARIABLES._₣ = 48.00;
				VARIABLES._₥ = 49.00;
				VARIABLES._₦ = 50.00;
				VARIABLES._₧ = 51.00;
				VARIABLES._₨ = 52.00;
				VARIABLES._₩ = 53.00;
				VARIABLES._₪ = 54.00;
				VARIABLES._₫ = 55.00;
				VARIABLES._₭ = 56.00;
				VARIABLES._₮ = 57.00;
				VARIABLES._₯ = 58.00;
				VARIABLES._₰ = 59.00;
				VARIABLES._₱ = 60.00;
				VARIABLES._₲ = 61.00;
				VARIABLES._₳ = 62.00;
				VARIABLES._₴ = 63.00;
				VARIABLES._₵ = 64.00;
				VARIABLES._₶ = 65.00;
				VARIABLES._₷ = 66.00;
				VARIABLES._₸ = 67.00;
				VARIABLES._₹ = 68.00;
				VARIABLES._₺ = 69.00;

				expect(_$).tobe(41.00); expect(_€).tobe(42.00); expect(_£).tobe(43.00); expect(_₤).tobe(44.00);
				expect(_₠).tobe(45.00); expect(_₡).tobe(46.00); expect(_₢).tobe(47.00); expect(_₣).tobe(48.00);
				expect(_₥).tobe(49.00); expect(_₦).tobe(50.00); expect(_₧).tobe(51.00); expect(_₨).tobe(52.00);
				expect(_₩).tobe(53.00); expect(_₪).tobe(54.00); expect(_₫).tobe(55.00); expect(_₭).tobe(56.00);
				expect(_₮).tobe(57.00); expect(_₯).tobe(58.00); expect(_₰).tobe(59.00); expect(_₱).tobe(60.00);
				expect(_₲).tobe(61.00); expect(_₳).tobe(62.00); expect(_₴).tobe(63.00); expect(_₵).tobe(64.00);
				expect(_₶).tobe(65.00); expect(_₷).tobe(66.00); expect(_₸).tobe(67.00); expect(_₹).tobe(68.00);
				expect(_₺).tobe(69.00);
			});
		});
	}
}