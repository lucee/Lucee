component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ){
		describe( title = "Testcase for LDEV-2353", body = function(){
			it( title = "IsNumeric(...) Not working for negative exponent notation numbers as string", body = function( currentSpec ){
				expect(IsNumeric(2E05)).tobe(true);
				expect(IsNumeric("2E05")).tobe(true);
				expect(IsNumeric("-143")).tobe(true);
				expect(IsNumeric(-407)).tobe(true);
				expect(IsNumeric(6E-05)).tobe(true);
				expect(IsNumeric("9E-05")).tobe(true);
				expect(IsNumeric(-0.000+00001)).tobe(true);
				expect(IsNumeric(+23-2E5)).tobe(true);
				expect(IsNumeric(+1E123)).tobe(true);
				expect(IsNumeric(2E+05)).tobe(true);
				expect(IsNumeric("5E+23")).tobe(true);
			});	

			it( title = "IsValid('numeric',...) Not working for negative exponent notation numbers as string", body = function( currentSpec ){
				expect(IsValid("numeric",1E08)).tobe(true);
				expect(IsValid("numeric","1E08")).tobe(true);
				expect(IsValid("numeric",2E-05)).tobe(true);
				expect(IsValid("numeric","-9E89")).tobe(true);
				expect(IsValid("numeric","-343E12")).tobe(true);
				expect(IsValid("numeric","2E-05")).tobe(true);
				expect(IsValid("numeric",-0.000+00001)).tobe(true);
				expect(IsValid("numeric",+23-2E5)).tobe(true);
				expect(IsValid("numeric","6E+12")).tobe(true);
				expect(IsValid("numeric",8E+40)).tobe(true);
			});
		});
	}
}