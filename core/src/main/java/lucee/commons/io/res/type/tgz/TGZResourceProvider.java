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
package lucee.commons.io.res.type.tgz;

import java.io.IOException;

import lucee.commons.io.res.Resource;
import lucee.commons.io.res.type.compress.Compress;
import lucee.commons.io.res.type.compress.CompressResourceProvider;

public final class TGZResourceProvider extends CompressResourceProvider {

	public TGZResourceProvider() {
		scheme = "tgz";
	}

	@Override
	public Compress getCompress(Resource file) throws IOException {
		return Compress.getInstance(file, Compress.FORMAT_TGZ, caseSensitive);
	}

	@Override
	public boolean isAttributesSupported() {
		return false;
	}

	@Override
	public boolean isCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isModeSupported() {
		return true;
	}
}