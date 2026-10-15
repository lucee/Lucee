component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run(testResults, textbox) {
        describe(title="LDEV-5890: Test suite for dateDiff with dateTimeFormat masks (long, short, medium) (ACF compact)", body=function(){
            xit(title="dateDiff() should work with dateTimeFormat() using long, short, and medium masks", body=function(currentSpec){
                var diffLong = dateDiff("s", dateTimeFormat(now(),"long"), dateTimeFormat(now(),"long"));
                var diffShort = dateDiff("s", dateTimeFormat(now(),"short"), dateTimeFormat(now(),"short"));
                var diffMedium = dateDiff("s", dateTimeFormat(now(),"medium"), dateTimeFormat(now(),"medium"));
                expect(diffLong).toBe(0);
                expect(diffShort).toBe(0);
                expect(diffMedium).toBe(0);
            });
            it(title="dateDiff() works with dateTimeFormat() using custom masks for long, short, and medium formats", body=function(currentSpec){
                var diffLong = dateDiff("s", dateTimeFormat(now(),"mmmm d, yyyy h:nn:ss tt zzz"), dateTimeFormat(now(),"mmmm d, yyyy h:nn:ss tt zzz"));
                var diffShort = dateDiff("s", dateTimeFormat(now(),"m/d/y h:nn tt"), dateTimeFormat(now(),"m/d/y h:nn tt"));
                var diffMedium = dateDiff("s", dateTimeFormat(now(),"mmm d, yyyy h:nn:ss tt"), dateTimeFormat(now(),"mmm d, yyyy h:nn:ss tt"));
                expect(diffLong).toBe(0);
                expect(diffShort).toBe(0);
                expect(diffMedium).toBe(0);
            });
		});

        describe(title="ACF-compact: dateDiff with LSDateTimeFormat masks (long, short, medium)", body=function(){
            xit(title="dateDiff() should works with LSDateTimeFormat() using long, short, and medium masks", body=function(currentSpec){
                var diffLong = dateDiff("s", LSDateTimeFormat(now(),"long"), LSDateTimeFormat(now(),"long"));
                var diffShort = dateDiff("s", LSDateTimeFormat(now(),"short"), LSDateTimeFormat(now(),"short"));
                var diffMedium = dateDiff("s", LSDateTimeFormat(now(),"medium"), LSDateTimeFormat(now(),"medium"));
                expect(diffLong).toBe(0);
                expect(diffShort).toBe(0);
                expect(diffMedium).toBe(0);
            });
            it(title="dateDiff() works with LSDateTimeFormat() using custom masks similar to long, medium, and short formats", body=function(currentSpec){
                // Custom masks as per Lucee documentation
                var diffLong = dateDiff("s", LSDateTimeFormat(now(),"mmmm d, yyyy h:nn:ss tt zzz"), LSDateTimeFormat(now(),"mmmm d, yyyy h:nn:ss tt zzz"));
                var diffShort = dateDiff("s", LSDateTimeFormat(now(),"m/d/y h:nn tt"), LSDateTimeFormat(now(),"m/d/y h:nn tt"));
                var diffMedium = dateDiff("s", LSDateTimeFormat(now(),"mmm d, yyyy h:nn:ss tt"), LSDateTimeFormat(now(),"mmm d, yyyy h:nn:ss tt"));
                expect(diffLong).toBe(0);
                expect(diffShort).toBe(0);
                expect(diffMedium).toBe(0);
            });
		});

	}
}
