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
package lucee.runtime.spooler.remote;

import lucee.runtime.config.RemoteClient;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTaskWS;
import lucee.runtime.type.Collection;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.util.KeyConstants;

public class RemoteClientTask extends SpoolerTaskWS {

	public static final Collection.Key PASSWORD = KeyImpl.getInstance("password");
	public static final Collection.Key ATTRIBUTE_COLLECTION = KeyImpl.getInstance("attributeCollection");
	public static final Collection.Key CALLER_ID = KeyImpl.getInstance("callerId");
	private StructImpl args;
	private String action;
	private String type;

	public RemoteClientTask(ExecutionPlan[] plans, RemoteClient client, Struct attrColl, String callerId, String type) {
		super(plans, client);
		this.type = type;
		action = (String) attrColl.get(KeyConstants._action, null);
		args = new StructImpl();
		args.setEL(KeyConstants._type, client.getType());
		args.setEL(PASSWORD, client.getAdminPasswordEncrypted());
		args.setEL(ATTRIBUTE_COLLECTION, attrColl);
		args.setEL(CALLER_ID, callerId);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String subject() {
		return action + " (" + super.subject() + ")";
	}

	@Override
	public Struct detail() {
		Struct sct = super.detail();
		sct.setEL("action", action);
		return sct;
	}

	@Override
	protected String getMethodName() {
		return "invoke";
	}

	@Override
	protected Struct getArguments() {
		return args;
	}
}