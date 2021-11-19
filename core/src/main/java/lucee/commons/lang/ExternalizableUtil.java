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
package lucee.commons.lang;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class ExternalizableUtil {

	public static String readString(ObjectInput in) throws ClassNotFoundException, IOException {
		return (String) in.readObject();
	}

	public static void writeString(ObjectOutput out, String str) throws IOException {
		// if(str==null) out.writeObject(""); string and null is not necessary the same
		out.writeObject(str);
	}

	public static Boolean readBoolean(ObjectInput in) throws IOException {
		int b = in.readInt();
		if (b == -1) return null;
		return b == 1 ? Boolean.TRUE : Boolean.FALSE;
	}

	public static void writeBoolean(ObjectOutput out, Boolean b) throws IOException {
		if (b == null) out.writeInt(-1);
		else out.writeInt(b.booleanValue() ? 1 : 0);
	}
}