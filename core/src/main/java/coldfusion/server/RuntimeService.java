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

import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletContext;

import lucee.runtime.type.scope.Scope;

public interface RuntimeService extends Service {

	public abstract Boolean getWhitespace();

	public abstract Map getLocking();

	public abstract Map getCfxtags();

	public abstract Map getCustomtags();

	public abstract Map getCorba();

	public abstract Map getApplets();

	public abstract Map getVariables();

	public abstract Map getErrors();

	public abstract String getScriptProtect();

	public abstract void setScriptProtect(String arg0);

	public abstract Map getMappings();

	public abstract Map getApplications();

	public abstract String getRootDir();

	public abstract void setWhitespace(String arg0);

	public abstract File resolveTemplateName(String arg0, String arg1);

	public abstract String getFullTagName(ServletContext arg0, String arg1) throws IOException;

	public abstract File resolveTemplatePath(String arg0);

	public abstract String getRealPath(ServletContext arg0, String arg1);

	public abstract Scope getServerScope();

	public abstract String getRegistryDir();

	public abstract long getSlowRequestLimit();

	public abstract boolean logSlowRequests();

	public abstract long getRequestTimeoutLimit();

	public abstract boolean timeoutRequests();

	public abstract int getNumberSimultaneousRequests();

	public abstract int getNumberSimultaneousReports();

	public abstract void setNumberSimultaneousReports(int arg0);

	public abstract void setNumberSimultaneousRequests(int arg0);

	public abstract int getMaxQueued();

	public abstract void setMaxQueued(int arg0);

	public abstract int getMinRequests();

	public abstract void setMinRequests(int arg0);

	public abstract boolean isCachePaths();

	public abstract void setCachePaths(boolean arg0);

	public abstract boolean isTrustedCache();

	public abstract void setTrustedCache(boolean arg0);

	public abstract void setTemplateCacheSize(int arg0);

	public abstract int getTemplateCacheSize();

	public abstract long getApplicationTimeout();

	public abstract long getApplicationMaxTimeout();

	public abstract boolean isApplicationEnabled();

	public abstract long getSessionTimeout();

	public abstract long getSessionMaxTimeout();

	public abstract boolean isSessionEnabled();

	public abstract boolean useJ2eeSession();

	public abstract boolean isPureJavaKit();

	public abstract Map getRequestSettings();

	public abstract void setSaveClassFiles(boolean arg0);

	public abstract boolean getSaveClassFiles();

	public abstract Map getRequestThrottleSettings();

	public abstract Map getFileLockSettings();

	public abstract boolean isFileLockEnabled();

	public abstract float getPostSizeLimit();

	public abstract boolean isEnabledFlexDataServices();

	public abstract void setEnableFlexDataServices(boolean arg0) throws ServiceException;

	public abstract String getFlexAssemblerIPList();

	public abstract void setFlexAssemblerIPList(String arg0);

	public abstract boolean isEnabledFlashRemoting();

	public abstract void setEnableFlashRemoting(boolean arg0);

	public abstract boolean isEnabledRmiSSL();

	public abstract void setEnableRmiSSL(boolean arg0) throws ServiceException;

	public abstract void setKeystore(String arg0);

	public abstract String getKeystore();

	public abstract void setKeystorePassword(String arg0);

	public abstract String getKeystorePassword();

	public abstract void setDataServiceId(String arg0);

	public abstract String getDataServiceId();

}