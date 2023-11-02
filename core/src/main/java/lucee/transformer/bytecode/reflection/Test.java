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
package lucee.transformer.bytecode.reflection;

public class Test {
	public void testVoid(int a, Integer b) {
		Class x = Integer.class;
		if (x == null) return;
	}

	public Class testClass(int a, Integer b) {
		return int[].class;
	}

	public void testVoid2(int[] c, Integer[] d) throws InterruptedException {
		wait();
	}

	public void testVoid23() {

	}

	public Object testInt() {
		return true;
	}
}