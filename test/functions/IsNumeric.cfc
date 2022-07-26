component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test case for isNumeric", function() {
			it(title = "isnumeric function", body = function( currentSpec ) {
				expect(isNumeric(6.62607004e-34)).toBe(true);
				expect(isNumeric(-77)).toBe(true);
				expect(isNumeric(0)).toBe(true);
				expect(isNumeric(-0)).toBe(true);
				expect(isNumeric(0.34e-22)).toBe(true);
				expect(isNumeric(5e2)).toBe(true);
				expect(isNumeric("lucee")).toBe(false);
				expect(isNumeric("6.62607004e-34")).toBe(true);

				expect(isNumeric('35e3f')).toBe(false);
				expect(isNumeric('6f')).toBe(false);
				expect(isNumeric('6.6f')).toBe(false);
				expect(isNumeric('2d')).toBe(false);
				expect(isNumeric('3.3d')).toBe(false);
				expect(isNumeric('6.62607004e-34')).toBe(true);
				expect(isNumeric('12E4d')).toBe(false);
				expect(isNumeric('123456789L')).toBe(false);
                expect(isNumeric('123456789l')).toBe(false);
                
                expect(isNumeric(1)).tobe(true);
                expect(isNumeric(1.3)).tobe(true);
                expect(isNumeric("1")).tobe(true);
                expect(isNumeric("susi")).tobe(false);
                expect(isNumeric(true)).tobe(false);
                expect(isNumeric("6/2017")).tobe(false);
                expect(isNumeric(arrayNew(1))).tobe(false);
                str = "Susi";
                expect(isNumeric(str.length())).tobe(true);
                expect(isNumeric(' 123 ')).tobe(true);

                expect(isNumeric(toNumeric("123.45"))).tobe(true);
                expect(isNumeric(toNumeric("0110","bin"))).tobe(true);
                expect(isNumeric(toNumeric("000C","hex"))).tobe(true);
                expect(isNumeric(toNumeric("24","oct"))).tobe(true);

				expect(isNumeric('+123')).toBe(true);
				expect(isNumeric('-123')).toBe(true);
				expect(isNumeric('+12+3')).toBe(false);
				expect(isNumeric('-12-3')).toBe(false);
				expect(isNumeric('12+3')).toBe(false);
				expect(isNumeric('12-3')).toBe(false);
				expect(isNumeric('+')).toBe(false);
				expect(isNumeric('-')).toBe(false);
			});
		});
	}
}