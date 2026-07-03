component extends="org.lucee.cfml.test.LuceeTestCase"{
	function beforeAll(){}

	function afterAll(){}

	function run( testResults , testBox ) {

		var DateTimeUtil=createObject("java","lucee.commons.date.DateTimeUtil");
		var ScheduledTaskThread=createObject("java","lucee.runtime.schedule.ScheduledTaskThread");
		var ScheduleTaskImpl=createObject("java","lucee.runtime.schedule.ScheduleTaskImpl");
		var TimeZone=createObject("java","java.util.TimeZone");
		var Date=createObject("java","java.util.Date");
		var UNIX0=createDateTime(1970,1,1,0,0,0,0,"UTC");	


		describe( "Check the Scheduled Task Service", function() {
			beforeEach( function(){
				variables.startingTZ=getTimeZone();
				setTimeZone("UTC");
            });
			afterEach( function(){
                setTimeZone(variables.startingTZ?:"UTC");
            });

			it(title="check next calculation for daily", body=function(){
				var now=createDateTime(2021,1,1,11,1,1,0,"CET").getTime();
				var start=createDateTime(2020,11,1,9,1,1,0,"CET").getTime();
				
				var next=ScheduledTaskThread.calculateNextExecutionNotEvery(DateTimeUtil.getInstance(), now, true, TimeZone.getDefault(), start, ScheduleTaskImpl.INTERVAL_DAY);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,1,2,9,1,1,0,"CET"));
			});
			it(title="check next calculation for weekly", body=function(){
				var now=createDateTime(2021,1,1,11,1,1,0,"CET").getTime();
				var start=createDateTime(2020,11,1,9,1,1,0,"CET").getTime();
				
				var next=ScheduledTaskThread.calculateNextExecutionNotEvery(DateTimeUtil.getInstance(), now, true, TimeZone.getDefault(), start, ScheduleTaskImpl.INTERVAL_WEEK);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,1,3,9,1,1,0,"CET"));
			});
			it(title="check next calculation for monthly", body=function(){
				var now=createDateTime(2021,1,15,11,1,1,0,"CET").getTime();
				var start=createDateTime(2020,11,1,9,1,1,0,"CET").getTime();
				
				var next=ScheduledTaskThread.calculateNextExecutionNotEvery(DateTimeUtil.getInstance(), now, true, TimeZone.getDefault(), start, ScheduleTaskImpl.INTERVAL_MONTH);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,2,1,9,1,1,0,"CET"));
			});
			it(title="check next calculation for yearly", body=function(){
				var now=createDateTime(2021,1,15,11,1,1,0,"CET").getTime();
				var start=createDateTime(2020,11,1,9,1,1,0,"CET").getTime();
				
				var next=ScheduledTaskThread.calculateNextExecutionNotEvery(DateTimeUtil.getInstance(), now, true, TimeZone.getDefault(), start, ScheduleTaskImpl.INTERVAL_YEAR);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,11,1,9,1,1,0,"CET"));1
			});
			
			it(title="check next calculation for every", body=function(){
				var now=createDateTime(2021,1,15,11,1,1,0,"CET").getTime();
				var start=createDateTime(2020,11,1,9,1,1,0,"CET").getTime();
				var endTime=createDateTime(2120,11,1,9,1,1,0,"CET").getTime();
				
				var next=ScheduledTaskThread.calculateNextExecutionEvery(DateTimeUtil.getInstance(), now, true, TimeZone.getDefault(), start, endTime,30);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,1,15,11,1,31,0,"CET"));
			});
			// public static long calculateNextExecutionEvery(DateTimeUtil util, long now, boolean notNow, TimeZone timeZone, long start, long endTime, int amount) {

			it(title="LDEV-6426: an 'every N minutes' task with only an end time must stop firing after that time each day and resume the next day", body=function(){
				var util=DateTimeUtil.getInstance();
				var tz=TimeZone.getDefault();

				var start=createDateTime(2020,11,1,9,0,0,0,"CET").getTime();
				// endTime as stored on the real task is milliseconds-in-day, not an absolute timestamp (see constructor)
				var endTime=util.getMilliSecondsInDay(tz, createDateTime(2021,1,1,17,0,0,0,"CET").getTime());

				// already past today's 17:00 cutoff -> next run must be tomorrow's start time, not another run today
				var now=createDateTime(2021,1,15,17,1,0,0,"CET").getTime();

				var next=ScheduledTaskThread.calculateNextExecutionEvery(util, now, true, tz, start, endTime, 300);
				expect(dateAdd("l",next,UNIX0)).toBe(createDateTime(2021,1,16,9,0,0,0,"CET"));
			});

			it(title="LDEV-6426: task must stop after its end date even when no explicit end time was set", body=function(){
				var util=DateTimeUtil.getInstance();
				var tz=TimeZone.getDefault();
				var DAY=24*3600000;

				// end date is yesterday (midnight), no explicit end time -> defaults to a full day duration (see ScheduledTaskThread constructor)
				var endDate=createDateTime(2021,1,14,0,0,0,0,"CET").getTime();
				var endTime=DAY;

				// "now" has a small time-of-day component; the old buggy check (endDate < todayDate && endTime < todayTime)
				// never matched here because endTime (a full day) is never smaller than todayTime
				var now=createDateTime(2021,1,15,1,0,0,0,"CET").getTime();
				var todayTime=util.getMilliSecondsInDay(tz, now);
				var todayDate=now-todayTime;

				expect(ScheduledTaskThread.hasEndDatePassed(endDate, endTime, todayDate, todayTime)).toBeTrue();
			});

			it(title="LDEV-6426: task must keep running while still within its end date", body=function(){
				var util=DateTimeUtil.getInstance();
				var tz=TimeZone.getDefault();
				var DAY=24*3600000;

				var endDate=createDateTime(2021,1,20,0,0,0,0,"CET").getTime();
				var endTime=DAY;

				var now=createDateTime(2021,1,15,1,0,0,0,"CET").getTime();
				var todayTime=util.getMilliSecondsInDay(tz, now);
				var todayDate=now-todayTime;

				expect(ScheduledTaskThread.hasEndDatePassed(endDate, endTime, todayDate, todayTime)).toBeFalse();
			});

			it(title="LDEV-6426: task without any end date must never expire (no endDate, no endTime)", body=function(){
				var util=DateTimeUtil.getInstance();
				var tz=TimeZone.getDefault();
				var DAY=24*3600000;
				var MAX=createObject("java","java.lang.Long").MAX_VALUE;

				// mirrors the constructor defaults when neither endDate nor endTime is provided
				var endDate=MAX;
				var endTime=DAY;

				// far in the future, so a naive "endDate + endTime" overflow would wrongly report "passed"
				var now=createDateTime(2999,1,15,1,0,0,0,"CET").getTime();
				var todayTime=util.getMilliSecondsInDay(tz, now);
				var todayDate=now-todayTime;

				expect(ScheduledTaskThread.hasEndDatePassed(endDate, endTime, todayDate, todayTime)).toBeFalse();
			});

			it(title="LDEV-6426: task with only an end time (no endDate) must never expire", body=function(){
				var util=DateTimeUtil.getInstance();
				var tz=TimeZone.getDefault();
				var MAX=createObject("java","java.lang.Long").MAX_VALUE;

				var endDate=MAX;
				var endTime=util.getMilliSecondsInDay(tz, createDateTime(2021,1,1,18,0,0,0,"CET").getTime());

				var now=createDateTime(2999,1,15,1,0,0,0,"CET").getTime();
				var todayTime=util.getMilliSecondsInDay(tz, now);
				var todayDate=now-todayTime;

				expect(ScheduledTaskThread.hasEndDatePassed(endDate, endTime, todayDate, todayTime)).toBeFalse();
			});
		});
	}
}
