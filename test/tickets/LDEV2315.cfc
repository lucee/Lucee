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

            it( title='Checking getLocaleInfo() function with different timeZone', body=function( currentSpec ) {

                origLocale = getLocale();

                // en_US
                setLocale("english (united states)");
                res = getLocaleInfo();
                expect(res.country).toBe("US");
                expect(res.currency.code).toBe("USD");
                expect(res.currency.symbol).toBe("$"); // $
                expect(res.dateTimeFormat.date).toBe("EEEE, MMMM d, yyyy");
                expect(res.dateTimeFormat.time).toBe("h:mm:ss a");

                // jpn
                setLocale("ja_JP_JP");
                res = getLocaleInfo();
                expect(res.country).toBe("JP");
                expect(res.currency.code).toBe("JPY");
                expect(Asc(res.currency.symbol)).toBe("65509"); // د.ج.
                expect(res.dateTimeFormat.time).toBe("H:mm:ss");

                // dutch (belgium)
                setLocale("dutch (belgium)");
                res = getLocaleInfo();
                expect(res.country).toBe("BE");
                expect(res.currency.code).toBe("EUR");
                expect(Asc(res.currency.symbol)).toBe("8364"); // €
                expect(res.dateTimeFormat.date).toBe("EEEE d MMMM yyyy");
                expect(res.dateTimeFormat.time).toBe("HH:mm:ss");

                // spanish (argentina)
                setLocale("spanish (argentina)");
                res = getLocaleInfo();
                expect(res.country).toBe("AR");
                expect(res.currency.code).toBe("ARS");
                expect(Asc(res.currency.symbol)).toBe("36"); // $
                expect(res.dateTimeFormat.date).toBe("EEEE, d 'de' MMMM 'de' yyyy");
                expect(res.dateTimeFormat.time).toBe("HH:mm:ss");

                setLocale(origLocale);
            });
        });
    }

}
