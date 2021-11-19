/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.runtime.services;

import java.util.Map;

import coldfusion.sql.DataSourceDef;
import lucee.runtime.db.ClassDefinition;
import lucee.runtime.db.DataSource;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class DatSourceDefImpl implements DataSourceDef {

	private final DataSource ds;

	public DatSourceDefImpl(DataSource ds) {
		this.ds = ds;
	}

	@Override
	public Object get(Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Struct getAllowedSQL() {
		Struct allow = new StructImpl();
		allow.setEL(KeyConstants._alter, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_ALTER)));
		allow.setEL(KeyConstants._create, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_CREATE)));
		allow.setEL(KeyConstants._delete, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_DELETE)));
		allow.setEL(KeyConstants._drop, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_DROP)));
		allow.setEL(KeyConstants._grant, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_GRANT)));
		allow.setEL(KeyConstants._insert, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_INSERT)));
		allow.setEL(KeyConstants._revoke, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_REVOKE)));
		allow.setEL(KeyConstants._select, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_SELECT)));
		allow.setEL("storedproc", Caster.toBoolean(true));// TODO
		allow.setEL(KeyConstants._update, Caster.toBoolean(ds.hasAllow(DataSource.ALLOW_UPDATE)));
		return allow;
	}

	@Override
	public ClassDefinition getClassDefinition() {
		return ds.getClassDefinition();
	}

	@Override
	public String getDatabase() {
		return ds.getDatabase();
	}

	@Override
	public String getDesc() {
		return "";
	}

	@Override
	public String getDriver() {
		return "";
	}

	@Override
	public String getDsn() {
		return ds.getDsnTranslated();
	}

	@Override
	public Struct getExtraData() {
		Struct rtn = new StructImpl();
		Struct connprop = new StructImpl();
		String[] names = ds.getCustomNames();
		rtn.setEL("connectionprops", connprop);
		for (int i = 0; i < names.length; i++) {
			connprop.setEL(names[i], ds.getCustomValue(names[i]));
		}
		rtn.setEL("maxpooledstatements", new Double(1000));
		rtn.setEL("sid", "");
		rtn.setEL("timestampasstring", Boolean.FALSE);
		rtn.setEL("useTrustedConnection", Boolean.FALSE);
		rtn.setEL("datasource", ds.getName());
		rtn.setEL("_port", new Double(ds.getPort()));
		rtn.setEL("port", new Double(ds.getPort()));
		rtn.setEL("_logintimeout", new Double(30));
		rtn.setEL("args", "");
		rtn.setEL("databaseFile", "");
		rtn.setEL("defaultpassword", "");
		rtn.setEL("defaultusername", "");
		rtn.setEL("host", ds.getHost());
		rtn.setEL("maxBufferSize", new Double(0));
		rtn.setEL("pagetimeout", new Double(0));
		rtn.setEL("selectMethod", "direct");
		rtn.setEL("sendStringParamterAsUnicode", Boolean.TRUE);
		rtn.setEL("systemDatabaseFile", "");

		return rtn;
	}

	@Override
	public String getHost() {
		return ds.getHost();
	}

	@Override
	public String getIfxSrv() {
		return "";
	}

	@Override
	public int getInterval() {
		return 0;
	}

	@Override
	public String getJNDIName() {
		return "";
	}

	@Override
	public String getJndiName() {
		return getJNDIName();
	}

	@Override
	public Struct getJndienv() {
		return new StructImpl();
	}

	@Override
	public int getLoginTimeout() {
		return ds.getConnectionTimeout();
	}

	@Override
	public int getLogintimeout() {
		return getLoginTimeout();
	}

	@Override
	public int getMaxBlobSize() {
		return 64000;
	}

	@Override
	public int getMaxClobSize() {
		return 64000;
	}

	@Override
	public int getMaxConnections() {
		return ds.getConnectionLimit();
	}

	@Override
	public int getMaxPooledStatements() {
		return 0;
	}

	@Override
	public int getMaxconnections() {
		return getMaxConnections();
	}

	@Override
	public int getPort() {
		return ds.getPort();
	}

	@Override
	public String getSelectMethod() {
		return "";
	}

	@Override
	public String getSid() {
		return "";
	}

	@Override
	public boolean getStrPrmUni() {
		return false;
	}

	@Override
	public int getTimeout() {
		return ds.getConnectionTimeout();
	}

	@Override
	public int getType() {
		return 0;
	}

	@Override
	public String getUrl() {
		return ds.getDsnTranslated();
	}

	@Override
	public String getUsername() {
		return ds.getUsername();
	}

	@Override
	public String getVendor() {
		return "";
	}

	@Override
	public boolean isBlobEnabled() {
		return ds.isBlob();
	}

	@Override
	public boolean isClobEnabled() {
		return ds.isClob();
	}

	@Override
	public boolean isConnectionEnabled() {
		return true;
	}

	@Override
	public boolean isDynamic() {
		return false;
	}

	@Override
	public boolean isPooling() {
		return true;
	}

	@Override
	public boolean isRemoveOnPageEnd() {
		return false;
	}

	@Override
	public boolean isSQLRestricted() {
		return false;
	}

	@Override
	public void setAllowedSQL(Struct arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setBlobEnabled(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClobEnabled(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setConnectionEnabled(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDatabase(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDesc(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDriver(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDsn(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setDynamic(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setExtraData(Struct arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setHost(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIfxSrv(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setInterval(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJNDIName(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJndiName(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setJndienv(Struct arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLoginTimeout(Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLogintimeout(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMap(Map arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxBlobSize(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxClobSize(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxConnections(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxConnections(Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setMaxPooledStatements(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPassword(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPooling(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPort(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPort(Object arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRemoveOnPageEnd(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSelectMethod(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSid(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStrPrmUni(boolean arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setStrPrmUni(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTimeout(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setType(int arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUrl(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setUsername(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVendor(String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setClassDefinition(ClassDefinition cd) {
		// TODO Auto-generated method stub

	}

}