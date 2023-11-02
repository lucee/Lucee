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

import java.security.Permission;
import java.util.HashMap;
import java.util.Map;

//import coldfusion.security.BasicPolicy;

public interface SecurityService extends Service {

	public abstract Map getContexts();

	// public abstract BasicPolicy getBasicPolicy();

	public abstract HashMap getCompiledCrossSiteScriptPatterns();

	public abstract String crossSiteProtectString(String arg0);

	public abstract boolean isJvmSecurityEnabled();

	public abstract boolean isSandboxSecurityEnabled();

	public abstract void setSandboxSecurityEnabled(boolean arg0);

	public abstract void checkPermission(Permission arg0);

	public abstract void setJvmSecurityEnabled(boolean arg0);

	public abstract void authenticateAdmin();

	public abstract void setAdminPassword(String arg0);

	public abstract boolean isAdminSecurityEnabled();

	public abstract void setAdminSecurityEnabled(boolean arg0);

	public abstract boolean checkAdminPassword(String arg0, String arg1);

	public abstract boolean checkAdminPassword(String arg0);

	public abstract String getAdminHash(Object arg0);

	public abstract void setRdsPassword(String arg0);

	public abstract boolean checkRdsPassword(String arg0);

	public abstract boolean isRdsSecurityEnabled();

	public abstract void setRdsSecurityEnabled(boolean arg0);

	public abstract Map getSettings();

	public abstract void setSettings(Map arg0) throws ServiceException;

	public abstract void registerWithWatchService();

	public abstract void setEnableWatch(boolean arg0);

}