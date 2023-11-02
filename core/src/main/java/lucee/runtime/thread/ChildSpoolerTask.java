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
package lucee.runtime.thread;

import lucee.runtime.config.Config;
import lucee.runtime.exp.PageException;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTaskSupport;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ChildSpoolerTask extends SpoolerTaskSupport {

	private ChildThreadImpl ct;

	public ChildSpoolerTask(ChildThreadImpl ct, ExecutionPlan[] plans) {
		super(plans);
		this.ct = ct;
	}

	@Override
	public Struct detail() {
		StructImpl detail = new StructImpl();
		detail.setEL("template", ct.getTemplate());
		return detail;
	}

	@Override
	public Object execute(Config config) throws PageException {
		PageException pe = ct.execute(config);
		if (pe != null) throw pe;
		return null;
	}

	@Override
	public String getType() {
		return "cfthread";
	}

	@Override
	public String subject() {
		return ct.getTagName();
	}

}