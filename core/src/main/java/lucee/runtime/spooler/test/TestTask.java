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
package lucee.runtime.spooler.test;

import lucee.runtime.config.Config;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.SpoolerTaskSupport;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class TestTask extends SpoolerTaskSupport {

	private int fail;
	private String label;

	public TestTask(ExecutionPlan[] plans, String label, int fail) {
		super(plans);
		this.label = label;
		this.fail = fail;
	}

	@Override
	public String getType() {
		return "test";
	}

	@Override
	public Struct detail() {
		return new StructImpl();
	}

	@Override
	public Object execute(Config config) throws PageException {
		// print.out("execute:"+label+":"+fail+":"+new Date());
		if (fail-- > 0) throw new ExpressionException("no idea");

		return null;
	}

	@Override
	public String subject() {
		return label;
	}

}