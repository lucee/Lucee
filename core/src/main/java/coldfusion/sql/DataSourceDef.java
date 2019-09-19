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
package coldfusion.sql;

import java.util.Map;

import lucee.runtime.db.ClassDefinition;
import lucee.runtime.type.Struct;

public interface DataSourceDef {
	public Object get(Object arg1);

	public int getType();

	public ClassDefinition getClassDefinition();

	public String getHost();

	public int getPort();

	public boolean isDynamic();

	public boolean isConnectionEnabled();

	public boolean isBlobEnabled();

	public boolean isClobEnabled();

	public String getDriver();

	public void setDriver(String arg1);

	public Struct getAllowedSQL();

	public void setAllowedSQL(Struct arg1);

	public boolean isSQLRestricted();

	public void setMap(Map arg1);

	public boolean isRemoveOnPageEnd();

	public void setRemoveOnPageEnd(boolean arg1);

	public void setDynamic(boolean arg1);

	public String getIfxSrv();

	public void setIfxSrv(String arg1);

	public boolean getStrPrmUni();

	public void setStrPrmUni(boolean arg1);

	public void setStrPrmUni(String arg1);

	public String getSelectMethod();

	public void setSelectMethod(String arg1);

	public String getSid();

	public void setSid(String arg1);

	public String getJndiName();

	public void setJndiName(String arg1);

	public int getMaxClobSize();

	public void setMaxClobSize(int arg1);

	public int getMaxBlobSize();

	public void setMaxBlobSize(int arg1);

	public void setClobEnabled(boolean arg1);

	public void setBlobEnabled(boolean arg1);

	public void setConnectionEnabled(boolean arg1);

	public int getLogintimeout();

	public void setLogintimeout(int arg1);

	public int getMaxconnections();

	public void setMaxConnections(int arg1);

	public void setMaxConnections(Object arg1);

	public void setDatabase(String arg1);

	public String getDatabase();

	public void setHost(String arg1);

	public void setVendor(String arg1);

	public String getVendor();

	public Struct getJndienv();

	public void setLoginTimeout(Object arg1);

	public int getLoginTimeout();

	public void setPort(int arg1);

	public void setPort(Object arg1);

	public int getMaxConnections();

	public void setJndienv(Struct arg1);

	public void setJNDIName(String arg1);

	public String getJNDIName();

	public void setType(String arg1);

	public void setType(int arg1);

	public String getDsn();

	public void setDsn(String arg1);

	public void setClassDefinition(ClassDefinition cd);

	public String getDesc();

	public void setDesc(String arg1);

	public String getUsername();

	public void setUsername(String arg1);

	public void setPassword(String arg1);

	public String getUrl();

	public void setUrl(String arg1);

	public boolean isPooling();

	public void setPooling(boolean arg1);

	public int getTimeout();

	public void setTimeout(int arg1);

	public int getInterval();

	public void setInterval(int arg1);

	public Struct getExtraData();

	public void setExtraData(Struct arg1);

	public void setMaxPooledStatements(int arg1);

	public int getMaxPooledStatements();

}