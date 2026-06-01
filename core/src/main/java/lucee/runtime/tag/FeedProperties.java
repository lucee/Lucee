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
package lucee.runtime.tag;

import lucee.runtime.op.Caster;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.StructUtil;

public final class FeedProperties {
	public static Struct toProperties(Struct data) {
		data = (Struct) Duplicator.duplicate(data, true);

		Struct rdf = Caster.toStruct(data.removeEL(KeyConstants._RDF), null, false);
		if (rdf == null) rdf = Caster.toStruct(data.removeEL(KeyConstants._RSS), null, false);
		if (rdf != null) {
			rdf.removeEL(KeyConstants._ITEM);
			Struct channel = Caster.toStruct(rdf.get(KeyConstants._channel, null), null, false);
			if (channel != null) {
				channel.removeEL(KeyConstants._ITEMS);
				StructUtil.copy(channel, data, true);

			}
		}

		data.removeEL(KeyConstants._ITEM);
		data.removeEL(KeyConstants._ENTRY);

		return data;
	}
}