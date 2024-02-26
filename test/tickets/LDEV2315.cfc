component extends="org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults , testBox ) {
        describe( "Test suite for LDEV-2315", function() {

            it( title='Checking getLocaleInfo() function', body=function( currentSpec ) {

                res = getLocaleInfo();

                expect(res).toHaveKey("country");
                expect(res).toHaveKey("currency");
                expect(res).toHaveKey("dateTimeFormat");
                expect(res).toHaveKey("display");
                expect(res).toHaveKey("iso");
                expect(res).toHaveKey("language");
                expect(res).toHaveKey("name");
                expect(res).toHaveKey("variant");
            });

            it( title='Checking getLocaleInfo() function with en_US timeZone', body=function( currentSpec ) {

                origLocale = getLocale();

                // en_US
                setLocale("english (united states)");
                res = getLocaleInfo();
                expect(res.country).toBe("US");
                expect(res.currency.code).toBe("USD");
                expect(res.currency.symbol).toBe("$"); // $
                // this differs in different java versions
                expect(res.dateTimeFormat.date).toBe(getJavaVersion()<9?"EEEE, MMMM d, yyyy":"EEEE, MMMM d, y");
                expect(res.dateTimeFormat.time).toBe("h:mm:ss a");

                setLocale(origLocale);
            });

            it( title='Checking getLocaleInfo() function with ja_JP_JP timeZone', body=function( currentSpec ) {

                origLocale = getLocale();

                // jpn
                setLocale("ja_JP_JP");
                res = getLocaleInfo();
                expect(res.country).toBe("JP");
                expect(res.currency.code).toBe("JPY");
                expect(Asc(res.currency.symbol)).toBe("65509"); // د.ج.
                expect(res.dateTimeFormat.time).toBe("H:mm:ss");

                setLocale(origLocale);
            });

            it( title='Checking getLocaleInfo() function with dutch (belgium) timeZone', body=function( currentSpec ) {

                origLocale = getLocale();

                // dutch (belgium)
                setLocale("dutch (belgium)");
                res = getLocaleInfo();
                expect(res.country).toBe("BE");
                expect(res.currency.code).toBe("EUR");
                expect(Asc(res.currency.symbol)).toBe("8364"); // €
                // this differs in different java versions
                expect(res.dateTimeFormat.date).toBe(getJavaVersion()<9?"EEEE d MMMM yyyy":"EEEE d MMMM y");
                expect(res.dateTimeFormat.time).toBe(getJavaVersion()<9?"H:mm:ss":"HH:mm:ss");

                setLocale(origLocale);
            });

            it( title='Checking getLocaleInfo() function with spanish (argentina) timeZone', body=function( currentSpec ) {

                origLocale = getLocale();

                // spanish (argentina)
                setLocale("spanish (argentina)");
                res = getLocaleInfo();
                expect(res.country).toBe("AR");
                expect(res.currency.code).toBe("ARS");
                expect(Asc(res.currency.symbol)).toBe("36"); // $
                // this differs in different java versions
                expect(res.dateTimeFormat.date).toBe(getJavaVersion()<9?"EEEE d' de 'MMMM' de 'yyyy":"EEEE, d 'de' MMMM 'de' y");
                expect(res.dateTimeFormat.time).toBe("HH:mm:ss");
                
                setLocale(origLocale);
            });

        });
    }

    private function getJavaVersion() {
		var raw=server.java.version;
		var arr=listToArray(raw,'.');
		if (arr[1]==1) // version 1-9
			return arr[2];
		return arr[1];
	}
}
