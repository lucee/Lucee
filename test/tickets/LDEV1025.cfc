component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeALL(){
		dateandtime = createDateTime(2009,8,9,11,22,33);
		serverTZ = {
			TZ: dateTimeFormat(dateandtime, "z"),
			Offset: dateTimeFormat(dateandtime, "Z")
		};
	}

	private function getJavaVersion() {
        var raw=server.java.version;
        var arr=listToArray(raw,'.');
        if(arr[1]==1) // version 1-9
            return arr[2];
        return arr[1];
    }

	function run( testResults , testBox ) {
		setTimeZone("CEST");
		describe( "Test suite for LDEV-1025", function() {
			describe("checking 'DateFormat' function with all mask", function() {
				it("checking 'date' mask parsing", function() {
					ds_date = {
						df_d      : DateFormat(dateandtime, "d"),
						df_dd     : DateFormat(dateandtime, "dd"),
						df_ddd    : DateFormat(dateandtime, "ddd"),
						df_dddd   : DateFormat(dateandtime, "dddd"),
						dfu_D     : DateFormat(dateandtime, "D"),
						dfu_DD    : DateFormat(dateandtime, "DD"),
						dfu_DDD   : DateFormat(dateandtime, "DDD"),
						dfu_DDDD  : DateFormat(dateandtime, "DDDD")
					};
					expect(ds_date.df_d).toBe(9);
					expect(ds_date.df_dd).toBe(09);
					expect(ds_date.df_ddd).toBe('sun');
					expect(ds_date.df_dddd).toBe('sunday');
					expect(ds_date.dfu_D).toBe(9);
					expect(ds_date.dfu_DD).toBe(09);
					expect(ds_date.dfu_DDD).toBe('sun');
					expect(ds_date.dfu_DDDD).toBe('sunday');
				});

				it("checking 'month' mask parsing", function() {
					ds_month = {
						df_m      : DateFormat(dateandtime, "m"),
						df_mm     : DateFormat(dateandtime, "mm"),
						df_mmm    : DateFormat(dateandtime, "mmm"),
						df_mmmm   : DateFormat(dateandtime, "mmmm"),
						dfu_M     : DateFormat(dateandtime, "M"),
						dfu_MM    : DateFormat(dateandtime, "MM"),
						dfu_MMM   : DateFormat(dateandtime, "MMM"),
						dfu_MMMM  : DateFormat(dateandtime, "MMMM")
					};
					expect(ds_month.df_m).toBe(8);
					expect(ds_month.df_mm).toBe(08);
					expect(ds_month.df_mmm).toBe('Aug');
					expect(ds_month.df_mmmm).toBe('August');
					expect(ds_month.dfu_M).toBe(8);
					expect(ds_month.dfu_MM).toBe(08);
					expect(ds_month.dfu_MMM).toBe('Aug');
					expect(ds_month.dfu_MMMM).toBe('August');
				});

				it("checking 'year' mask parsing", function() {
					ds_year = {
						df_y     : DateFormat(dateandtime, "y"),
						df_yy    : DateFormat(dateandtime, "yy"),
						df_yyy   : DateFormat(dateandtime, "yyy"),
						df_yyyy  : DateFormat(dateandtime, "yyyy"),
						df_gg    : DateFormat(dateandtime, "gg"),
						dfu_Y    : DateFormat(dateandtime, "Y"),
						dfu_YY   : DateFormat(dateandtime, "YY"),
						dfu_YYY  : DateFormat(dateandtime, "YYY"),
						dfu_YYYY : DateFormat(dateandtime, "YYYY"),
						dfu_GG   : DateFormat(dateandtime, "GG")
					};
					expect(ds_year.df_y).toBe(2009);
					expect(ds_year.df_yy).toBe(09);
					expect(ds_year.df_yyy).toBe(2009);
					expect(ds_year.df_yyyy).toBe(2009);
					expect(ds_year.df_gg).toBe('AD');
					expect(ds_year.dfu_Y).toBe(2009);
					expect(ds_year.dfu_YY).toBe(09);
					expect(ds_year.dfu_YYY).toBe(2009);
					expect(ds_year.dfu_YYYY).toBe(2009);
					expect(ds_year.dfu_gg).toBe('AD');
				});

				it("checking predefined parsing", function() {
					ds_predefined = {
						df_short : DateFormat(dateandtime, "short"),
						df_medium : DateFormat(dateandtime, "medium"),
						df_long : DateFormat(dateandtime, "long"),
						df_full : DateFormat(dateandtime, "full"),
						dfu_SHORT : DateFormat(dateandtime, "SHORT"),
						dfu_MEDIUM : DateFormat(dateandtime, "MEDIUM"),
						dfu_LONG : DateFormat(dateandtime, "LONG"),
						dfu_FULL : DateFormat(dateandtime, "full")
					};
					expect(ds_predefined.df_short).toBe('8/9/09');
					expect(ds_predefined.df_medium).toBe('Aug 9, 2009');
					expect(ds_predefined.df_long).toBe('August 9, 2009');
					expect(ds_predefined.df_full).toBe('Sunday, August 9, 2009');
					expect(ds_predefined.dfu_SHORT).toBe('8/9/09');
					expect(ds_predefined.dfu_MEDIUM).toBe('Aug 9, 2009');
					expect(ds_predefined.dfu_LONG).toBe('August 9, 2009');
					expect(ds_predefined.dfu_FULL).toBe('Sunday, August 9, 2009');
				});
			});
			
			describe("checking 'DateTimeFormat' function with all mask", function() {
				it("checking 'date' mask parsing", function() {
					ds_date = {
						df_d      : DateTimeFormat(dateandtime, "d"),
						df_dd     : DateTimeFormat(dateandtime, "dd"),
						df_ddd    : DateTimeFormat(dateandtime, "ddd"),
						df_dddd   : DateTimeFormat(dateandtime, "dddd"),
						dfu_D     : DateTimeFormat(dateandtime, "D"),
						dfu_DD    : DateTimeFormat(dateandtime, "DD"),
						dfu_DDD   : DateTimeFormat(dateandtime, "DDD"),
						dfu_DDDD  : DateTimeFormat(dateandtime, "DDDD")
					};
					expect(ds_date.df_d).toBe(9);
					expect(ds_date.df_dd).toBe(09);
					expect(ds_date.dfu_D).toBe(221);
					expect(ds_date.dfu_DD).toBe(221);
					expect(ds_date.df_ddd).toBe('sun');
					expect(ds_date.dfu_DDD).toBe('sun');
					expect(ds_date.df_dddd).toBe('sunday');
					expect(ds_date.dfu_DDDD).toBe('sunday');
				});

				it("checking 'month' mask parsing", function() {
					ds_month = {
						df_m      : DateTimeFormat(dateandtime, "m"),
						df_mm     : DateTimeFormat(dateandtime, "mm"),
						df_mmm    : DateTimeFormat(dateandtime, "mmm"),
						df_mmmm   : DateTimeFormat(dateandtime, "mmmm"),
						dfu_M     : DateTimeFormat(dateandtime, "M"),
						dfu_MM    : DateTimeFormat(dateandtime, "MM"),
						dfu_MMM   : DateTimeFormat(dateandtime, "MMM"),
						dfu_MMMM  : DateTimeFormat(dateandtime, "MMMM")
					};
					expect(ds_month.df_m).toBe(8);
					expect(ds_month.df_mm).toBe(08);
					expect(ds_month.df_mmm).toBe('Aug');
					expect(ds_month.df_mmmm).toBe('August');
					expect(ds_month.dfu_M).toBe(8);
					expect(ds_month.dfu_MM).toBe(08);
					expect(ds_month.dfu_MMM).toBe('Aug');
					expect(ds_month.dfu_MMMM).toBe('August');
				});

				it("checking 'year' mask parsing", function() {
					ds_year = {
						df_y     : DateTimeFormat(dateandtime, "y"),
						df_yy    : DateTimeFormat(dateandtime, "yy"),
						df_yyy   : DateTimeFormat(dateandtime, "yyy"),
						df_yyyy  : DateTimeFormat(dateandtime, "yyyy"),
						df_gg    : dateTimeFormat(dateandtime, "gg"),
						dfu_Y    : DateTimeFormat(dateandtime, "Y"),
						dfu_YY   : DateTimeFormat(dateandtime, "YY"),
						dfu_YYY  : DateTimeFormat(dateandtime, "YYY"),
						dfu_YYYY : DateTimeFormat(dateandtime, "YYYY"),
						dfu_GG   : dateTimeFormat(dateandtime, "GG")
					};
					expect(ds_year.df_y).toBe(2009);
					expect(ds_year.df_yy).toBe(09);
					expect(ds_year.df_yyy).toBe(2009);
					expect(ds_year.df_yyyy).toBe(2009);
					expect(ds_year.df_gg).toBe('AD');
					expect(ds_year.dfu_Y).toBe(2009);
					expect(ds_year.dfu_YY).toBe(09);
					expect(ds_year.dfu_YYY).toBe(2009);
					expect(ds_year.dfu_YYYY).toBe(2009);
					expect(ds_year.dfu_GG ).toBe('AD');
				});
				
				it("checking 'Hour' mask parsing", function() {
					ds_hour = {
						dtf_h  : dateTimeFormat(dateandtime,"h"),
						dtf_hh  : dateTimeFormat(dateandtime,"hh"),
						dtfu_H    : dateTimeFormat(dateandtime,"H"),
						dtfu_HH   : dateTimeFormat(dateandtime,"HH")
					};
					expect(ds_hour.dtf_h).toBe(11);
					expect(ds_hour.dtf_hh).toBe(11);
					expect(ds_hour.dtfu_H).toBe(11);
					expect(ds_hour.dtfu_HH).toBe(11);
				});

				it("checking 'minutes' mask parsing", function() {
					ds_minutes = {
						dtf_n  : dateTimeFormat(dateandtime,"n"),
						dtf_nn  : dateTimeFormat(dateandtime,"nn"),
						dtfu_N : dateTimeFormat(dateandtime,"N"),
						dtfu_NN  : dateTimeFormat(dateandtime,"NN")
					};
					expect(ds_minutes.dtf_n).toBe(22);
					expect(ds_minutes.dtf_nn).toBe(22);
					expect(ds_minutes.dtfu_N).toBe(22);
					expect(ds_minutes.dtfu_NN).toBe(22);
				});

				it("checking 'seconds' mask parsing", function() {
					ds_seconds = {
						dtf_s  : dateTimeFormat(dateandtime,"s"),
						dtf_ss  : dateTimeFormat(dateandtime,"ss"),
						dtfu_S  : dateTimeFormat(dateandtime,"S"),
						dtfu_SS  : dateTimeFormat(dateandtime,"SS")
					};
					expect(ds_seconds.dtf_s).toBe(33);
					expect(ds_seconds.dtf_ss).toBe(33);
					expect(ds_seconds.dtfu_S).toBe(33);
					expect(ds_seconds.dtfu_SS).toBe(33);
				});

				it("checking 'milliseconds' mask parsing", function() {
					ds_milliseconds = {
						dtf_ms  : dateTimeFormat(dateandtime,"l"),
						dtfu_ms  : dateTimeFormat(dateandtime,"L")
					};
					expect(ds_milliseconds.dtf_ms).toBe(0);
					expect(ds_milliseconds.dtfu_ms).toBe(0);
				});

				it("checking 'AMorPM' mask parsing", function() {
					ds_AMorPM = {
						dtf_t : dateTimeFormat(dateandtime,"t"),
						dtf_tt : dateTimeFormat(dateandtime,"tt"),
						dtfu_T : dateTimeFormat(dateandtime,"T"),
						dtfu_TT : dateTimeFormat(dateandtime,"TT")
					};
					expect(ds_AMorPM.dtf_t).toBe('A');
					expect(ds_AMorPM.dtf_tt).toBe('AM');
					expect(ds_AMorPM.dtfu_T).toBe('A');
					expect(ds_AMorPM.dtfu_TT).toBe('AM');
				});

				it("checking 'timezone' mask parsing", function() {
					ds_timeZone = {
						dtf_z  : dateTimeFormat(dateandtime, "z"),
						dtfu_Z  : dateTimeFormat(dateandtime, "Z")
					};
					expect(ds_timeZone.dtf_z).toBe("#serverTZ.TZ#");
					expect(ds_timeZone.dtfu_Z).toBe("#serverTZ.Offset#");
				});

				it("checking predefined date parsing short", function() {
					ds_predefined = {
						df_short : DateTimeFormat(dateandtime, "short"),
						dfu_SHORT : DateTimeFormat(dateandtime, "SHORT")
					};
					
					if(getJavaVersion()>=9) {
						expect(ds_predefined.df_short) .toBe('8/9/09, 11:22 AM');
						expect(ds_predefined.dfu_SHORT).toBe('8/9/09, 11:22 AM');

					}
					else {	
						expect(ds_predefined.df_short) .toBe('8/9/09 11:22 AM');
						expect(ds_predefined.dfu_SHORT).toBe('8/9/09 11:22 AM');
					}
				});

				it("checking predefined date parsing medium", function() {
					ds_predefined = {
						df_medium : DateTimeFormat(dateandtime, "medium"),
						dfu_MEDIUM : DateTimeFormat(dateandtime, "MEDIUM")
					};
					
					if(getJavaVersion()>=9) {
						expect(ds_predefined.df_medium) .toBe('Aug 9, 2009, 11:22:33 AM');
						expect(ds_predefined.dfu_MEDIUM).toBe('Aug 9, 2009, 11:22:33 AM');	
					}
					else {	
						expect(ds_predefined.df_medium) .toBe('Aug 9, 2009 11:22:33 AM');
						expect(ds_predefined.dfu_MEDIUM).toBe('Aug 9, 2009 11:22:33 AM');
					}
				});
				
				it("checking predefined date parsing long", function() {
					ds_predefined = {
						df_long : DateTimeFormat(dateandtime, "long"),
						dfu_LONG : DateTimeFormat(dateandtime, "LONG")
					};
					
					if(getJavaVersion()>=9) {
						expect(ds_predefined.df_long) .toBe('August 9, 2009 at 11:22:33 AM #serverTZ.TZ#');
						expect(ds_predefined.dfu_LONG).toBe('August 9, 2009 at 11:22:33 AM #serverTZ.TZ#');
					}
					else {	
						expect(ds_predefined.df_long) .toBe('August 9, 2009 11:22:33 AM #serverTZ.TZ#');
						expect(ds_predefined.dfu_LONG).toBe('August 9, 2009 11:22:33 AM #serverTZ.TZ#');
					}
				});
				it("checking predefined date parsing", function() {
					ds_predefined = {
						df_full : DateTimeFormat(dateandtime, "full"),
						dfu_FULL : DateTimeFormat(dateandtime, "full")
					};
					var tzi=getTimeZoneInfo();
					if(getJavaVersion()>=9) {
						expect(ds_predefined.df_full) .toBe('Sunday, August 9, 2009 at 11:22:33 AM Central European Summer Time');
						expect(ds_predefined.dfu_FULL).toBe('Sunday, August 9, 2009 at 11:22:33 AM Central European Summer Time');
					}
					else {	
						expect(ds_predefined.df_full) .toBe('Sunday, August 9, 2009 11:22:33 AM CEST');
						expect(ds_predefined.dfu_FULL).toBe('Sunday, August 9, 2009 11:22:33 AM CEST');
					}
				});

			});
		});
	}
}