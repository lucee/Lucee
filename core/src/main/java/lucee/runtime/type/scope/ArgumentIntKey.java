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
package lucee.runtime.type.scope;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import lucee.runtime.op.Caster;
import lucee.runtime.type.KeyImpl;

public final class ArgumentIntKey extends KeyImpl {

	private static final ArgumentIntKey[] KEYS = new ArgumentIntKey[] { new ArgumentIntKey(0), new ArgumentIntKey(1), new ArgumentIntKey(2), new ArgumentIntKey(3),
			new ArgumentIntKey(4), new ArgumentIntKey(5), new ArgumentIntKey(6), new ArgumentIntKey(7), new ArgumentIntKey(8), new ArgumentIntKey(9), new ArgumentIntKey(10),
			new ArgumentIntKey(11), new ArgumentIntKey(12), new ArgumentIntKey(13), new ArgumentIntKey(14), new ArgumentIntKey(15), new ArgumentIntKey(16),
			new ArgumentIntKey(17), };

	private int intKey;

	/**
	 * Do NEVER use, only for Externilze
	 */
	public ArgumentIntKey() {
	}

	public ArgumentIntKey(int key) {
		super(Caster.toString(key));

		this.intKey = key;
	}

	public int getIntKey() {
		return intKey;
	}

	public static ArgumentIntKey init(int i) {
		if (i >= 0 && i < KEYS.length) return KEYS[i];
		return new ArgumentIntKey(i);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(intKey);
		super.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		intKey = in.readInt();
		super.readExternal(in);
	}
}