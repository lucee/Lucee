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
package lucee.runtime.functions.file;

import lucee.commons.io.res.ResourceProvider;
import lucee.runtime.PageContext;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class GetVFSMetaData {
	public static Struct call(PageContext pc, String scheme) {
		ResourceProvider[] providers = pc.getConfig().getResourceProviders();
		ResourceProvider provider;
		scheme = scheme.trim();
		Struct sct = new StructImpl();
		for (int i = 0; i < providers.length; i++) {
			provider = providers[i];
			if (provider.getScheme().equalsIgnoreCase(scheme)) {
				// MUST sct=provider.getMetaData();
				sct.setEL("Scheme", provider.getScheme());
				sct.setEL("Attributes", provider.isAttributesSupported());
				sct.setEL("CaseSensitive", provider.isCaseSensitive());
				sct.setEL("Mode", provider.isModeSupported());

				sct.setEL("Enabled", Boolean.TRUE);
				return sct;
			}
		}
		sct.setEL("Enabled", Boolean.FALSE);
		return sct;
	}

}