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
package coldfusion.server;

import java.awt.Image;
import java.io.File;
import java.util.Map;
import java.util.Properties;

public interface DocumentService extends Service {

	public abstract boolean registerFontFile(String arg0);

	public abstract boolean registerFontDirectory(String arg0);

	public abstract void FontDiscovery();

	public abstract boolean isFontPathRegistered(String arg0);

	public abstract boolean isFontPathRegisteredAsUserFont(String arg0);

	public abstract Map getAvailableFontsForPDF();

	public abstract Map getAvailableFontsForJDK();

	public abstract Map getAvailableFontFamiles();

	public abstract Map getConfigMap();

	public abstract Map getUserConfigMap();

	public abstract Map getFontInfoFromFile(String arg0);

	public abstract boolean isCommonFont(String arg0);

	public abstract Properties getAwtFontMapper();

	public abstract Properties getAwtFontMapperBak();

	public abstract File getWmimagefile();

	public abstract Image getWmimage();

}