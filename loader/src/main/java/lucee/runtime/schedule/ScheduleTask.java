/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.schedule;

import java.net.URL;

import lucee.commons.io.res.Resource;
import lucee.commons.security.Credentials;
import lucee.runtime.net.proxy.ProxyData;
import lucee.runtime.type.dt.Date;
import lucee.runtime.type.dt.Time;

/**
 * a single scheduler task
 */
public interface ScheduleTask {

	/**
	 * Field <code>OPERATION_HTTP_REQUEST</code>
	 */
	public static final short OPERATION_HTTP_REQUEST = 0;

	/**
	 * Field <code>INTERVAL_ONCE</code>
	 */
	public static final int INTERVAL_ONCE = 0;

	/**
	 * Field <code>INTERVAL_DAY</code>
	 */
	public static final int INTERVAL_DAY = 1;

	/**
	 * Field <code>INTERVAL_WEEK</code>
	 */
	public static final int INTERVAL_WEEK = 2;

	/**
	 * Field <code>INTERVAL_MONTH</code>
	 */
	public static final int INTERVAL_MONTH = 3;

	/**
	 * @return Returns the credentials.
	 */
	public abstract Credentials getCredentials();

	/**
	 * @return Returns has credentials.
	 */
	public abstract boolean hasCredentials();

	/**
	 * @return Returns the file.
	 */
	public abstract Resource getResource();

	/**
	 * @return Returns the interval.
	 */
	public abstract int getInterval();

	/**
	 * @return Returns the operation.
	 */
	public abstract short getOperation();

	/**
	 * @return Returns the proxyHost.
	 */
	public abstract ProxyData getProxyData();

	/**
	 * @return Returns the resolveURL.
	 */
	public abstract boolean isResolveURL();

	/**
	 * @return Returns the task name.
	 */
	public abstract String getTask();

	/**
	 * @return Returns the timeout.
	 */
	public abstract long getTimeout();

	/**
	 * @return Returns the url.
	 */
	public abstract URL getUrl();

	/**
	 * @param nextExecution Next Execution
	 */
	public abstract void setNextExecution(long nextExecution);

	/**
	 * @return Returns the nextExecution.
	 */
	public abstract long getNextExecution();

	/**
	 * @return Returns the endDate.
	 */
	public abstract Date getEndDate();

	/**
	 * @return Returns the startDate.
	 */
	public abstract Date getStartDate();

	/**
	 * @return Returns the endTime.
	 */
	public abstract Time getEndTime();

	/**
	 * @return Returns the startTime.
	 */
	public abstract Time getStartTime();

	/**
	 * @return returns interval definition as String
	 */
	public abstract String getIntervalAsString();

	/**
	 * @return Returns the strInterval.
	 */
	public abstract String getStringInterval();

	/**
	 * @return Returns the publish.
	 */
	public abstract boolean isPublish();

	/**
	 * @return Returns the valid.
	 */
	public abstract boolean isValid();

	/**
	 * @param valid The valid to set.
	 */
	public abstract void setValid(boolean valid);

	/**
	 * @return the hidden
	 */
	public boolean isHidden();

	/**
	 * @param hidden the hidden to set
	 */
	public void setHidden(boolean hidden);

	public boolean isPaused();
}