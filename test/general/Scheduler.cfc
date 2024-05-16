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
		});
	}
}
