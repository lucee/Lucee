package lucee.runtime.schedule;

import lucee.runtime.schedule.ScheduleTask;

// FUTURE add to ScheduleTask and delete
public interface ScheduleTaskPro extends ScheduleTask {	
	/**
	 * @return Returns the userAgent.
	 */	
	public String getUserAgent();

}