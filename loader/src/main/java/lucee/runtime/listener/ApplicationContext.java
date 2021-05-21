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
package lucee.runtime.listener;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import lucee.commons.io.res.Resource;
import lucee.runtime.Mapping;
import lucee.runtime.PageContext;
import lucee.runtime.db.DataSource;
import lucee.runtime.exp.PageException;
import lucee.runtime.net.s3.Properties;
import lucee.runtime.orm.ORMConfiguration;
import lucee.runtime.rest.RestSettings;
import lucee.runtime.type.Collection;
import lucee.runtime.type.CustomType;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.TimeSpan;

/**
 * DTO Interface for Application Context data (defined by tag application)
 */
public interface ApplicationContext extends Serializable {

	public static final short WS_TYPE_AXIS1 = 1;
	public static final short WS_TYPE_AXIS2 = 2;
	public static final short WS_TYPE_JAX_WS = 4;
	public static final short WS_TYPE_CXF = 8;

	public static final int SCRIPT_PROTECT_NONE = 0;
	public static final int SCRIPT_PROTECT_FORM = 1;
	public static final int SCRIPT_PROTECT_URL = 2;
	public static final int SCRIPT_PROTECT_CGI = 4;
	public static final int SCRIPT_PROTECT_COOKIE = 8;
	public static final int SCRIPT_PROTECT_ALL = SCRIPT_PROTECT_CGI + SCRIPT_PROTECT_COOKIE + SCRIPT_PROTECT_FORM + SCRIPT_PROTECT_URL;

	/**
	 * @return Returns the applicationTimeout.
	 */
	public abstract TimeSpan getApplicationTimeout();

	/**
	 * @return Returns the loginStorage.
	 */
	public abstract int getLoginStorage();

	/**
	 * @return Returns the name.
	 */
	public abstract String getName();

	/**
	 * @return Returns the sessionTimeout.
	 */
	public abstract TimeSpan getSessionTimeout();

	/**
	 * @return Returns the setClientCookies.
	 */
	public abstract boolean isSetClientCookies();

	/**
	 * @return Returns the setClientManagement.
	 */
	public abstract boolean isSetClientManagement();

	/**
	 * @return Returns the setDomainCookies.
	 */
	public abstract boolean isSetDomainCookies();

	/**
	 * @return Returns the setSessionManagement.
	 */
	public abstract boolean isSetSessionManagement();

	/**
	 * @return Returns the clientstorage.
	 */
	public abstract String getClientstorage();

	/**
	 * @return if application context has a name
	 */
	public abstract boolean hasName();

	/**
	 * @return return script protect setting
	 */
	public int getScriptProtect();

	public Mapping[] getMappings();

	public Mapping[] getCustomTagMappings();

	public String getSecureJsonPrefix();

	public boolean getSecureJson();

	/**
	 * @return Default Datasource
	 * @deprecated use instead getDefDataSource()
	 */
	@Deprecated
	public String getDefaultDataSource();

	public boolean isORMEnabled();

	/**
	 * @return ORM Datasource
	 * @deprecated use instead getDefaultDataSource()
	 */
	@Deprecated
	public String getORMDatasource();

	public ORMConfiguration getORMConfiguration();

	public Properties getS3();

	public int getLocalMode();

	public String getSessionstorage();

	public TimeSpan getClientTimeout();

	public short getSessionType();

	public boolean getSessionCluster();

	public boolean getClientCluster();

	public Mapping[] getComponentMappings();

	public void setApplicationTimeout(TimeSpan applicationTimeout);

	public void setSessionTimeout(TimeSpan sessionTimeout);

	public void setClientTimeout(TimeSpan clientTimeout);

	public void setClientstorage(String clientstorage);

	public void setSessionstorage(String sessionstorage);

	public void setCustomTagMappings(Mapping[] customTagMappings);

	public void setComponentMappings(Mapping[] componentMappings);

	public void setMappings(Mapping[] mappings);

	public void setLoginStorage(int loginstorage);

	public void setDefaultDataSource(String datasource);

	public void setScriptProtect(int scriptrotect);

	public void setSecureJson(boolean secureJson);

	public void setSecureJsonPrefix(String secureJsonPrefix);

	public void setSetClientCookies(boolean setClientCookies);

	public void setSetClientManagement(boolean setClientManagement);

	public void setSetDomainCookies(boolean setDomainCookies);

	public void setSetSessionManagement(boolean setSessionManagement);

	public void setLocalMode(int localMode);

	public void setSessionType(short sessionType);

	public void setClientCluster(boolean clientCluster);

	public void setSessionCluster(boolean sessionCluster);

	public void setS3(Properties s3);

	public void setORMEnabled(boolean ormenabled);

	public void setORMConfiguration(ORMConfiguration ormConf);

	public void setORMDatasource(String string);

	public String getSecurityApplicationToken();

	public String getSecurityCookieDomain();

	public int getSecurityIdleTimeout();

	public void setSecuritySettings(String applicationtoken, String cookiedomain, int idletimeout);

	public void reinitORM(PageContext pc) throws PageException;

	public Resource getSource();

	public boolean getTriggerComponentDataMember();

	public void setTriggerComponentDataMember(boolean triggerComponentDataMember);

	/**
	 * return the default cache name for a certain type
	 * 
	 * @param type can be one of the following constants Config.CACHE_DEFAULT_OBJECT,
	 *            Config.CACHE_DEFAULT_TEMPLATE, Config.CACHE_DEFAULT_QUERY,
	 *            Config.CACHE_DEFAULT_RESOURCE, Config.CACHE_DEFAULT_FUNCTION
	 * @return name of the cache defined
	 */
	public String getDefaultCacheName(int type);

	public void setDefaultCacheName(int type, String cacheName);

	/**
	 * merge the fields with same name to array if true, otherwise to a comma separated string list
	 * 
	 * @param scope scope type, one of the following: Scope.SCOPE_FORM or Scope.SCOPE_URL
	 * @return Returns a list of fields.
	 */
	public boolean getSameFieldAsArray(int scope);

	public RestSettings getRestSettings();

	public JavaSettings getJavaSettings();

	public Resource[] getRestCFCLocations();

	public DataSource[] getDataSources();

	public DataSource getDataSource(String dataSourceName) throws PageException;

	public DataSource getDataSource(String dataSourceName, DataSource defaultValue);

	public void setDataSources(DataSource[] dataSources);

	/**
	 * default datasource name (String) or datasource (DataSource Object)
	 * 
	 * @return Returns the Default Datasource.
	 */
	public Object getDefDataSource();

	/**
	 * orm datasource name (String) or datasource (DataSource Object)
	 * 
	 * @return Returns the Default ORM Datasource.
	 */
	public Object getORMDataSource();

	public void setDefDataSource(Object datasource);

	public void setORMDataSource(Object string);

	public abstract boolean getBufferOutput();

	public abstract void setBufferOutput(boolean bufferOutput);

	public abstract Locale getLocale();

	public abstract void setLocale(Locale locale);

	public abstract void setTimeZone(TimeZone timeZone);

	public abstract TimeZone getTimeZone();

	public abstract Charset getResourceCharset();

	public abstract Charset getWebCharset();

	public abstract void setResourceCharset(Charset cs);

	public abstract void setWebCharset(Charset cs);

	public void setScopeCascading(short scopeCascading);

	public short getScopeCascading();

	public boolean getTypeChecking();

	public void setTypeChecking(boolean typeChecking);

	Map<Collection.Key, Map<Collection.Key, Object>> getTagAttributeDefaultValues(PageContext pc);

	public Map<Collection.Key, Object> getTagAttributeDefaultValues(PageContext pc, String fullName);

	public void setTagAttributeDefaultValues(PageContext pc, Struct sct);

	public TimeSpan getRequestTimeout();

	public void setRequestTimeout(TimeSpan timeout);

	public CustomType getCustomType(String strType);

	public boolean getAllowCompression();

	public void setAllowCompression(boolean allowCompression);

	public boolean getSuppressContent();

	public void setSuppressContent(boolean suppressContent);

	public short getWSType();

	public void setWSType(short wstype);

	public Object getCachedWithin(int type);

	public void setCachedWithin(int type, Object value);

	public abstract boolean getCGIScopeReadonly();

	public void setCGIScopeReadonly(boolean cgiScopeReadonly);

}