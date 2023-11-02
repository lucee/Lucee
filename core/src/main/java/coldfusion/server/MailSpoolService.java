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
package coldfusion.server;

import java.util.Map;

public interface MailSpoolService extends Service {

	// public abstract void storeMail(MailImpl arg0) throws MailSessionException,MailDeliveryException;

	// public abstract void validate(MailImpl arg0) throws ServiceException;

	public abstract Map getSettings();

	public abstract void setSettings(Map arg0);

	public abstract int getPort();

	public abstract long getSchedule();

	public abstract String getServer();

	public abstract String getSeverity();

	public abstract int getTimeout();

	public abstract boolean isMailSentLoggingEnable();

	public abstract void setMailSentLoggingEnable(boolean arg0);

	public abstract void setPort(int arg0);

	public abstract void setSchedule(int arg0);

	public abstract void setServer(String arg0);

	public abstract void setSeverity(String arg0);

	public abstract void setTimeout(int arg0);

	public abstract boolean verifyServer();

	public abstract void writeToLog(String arg0, boolean arg1);

}