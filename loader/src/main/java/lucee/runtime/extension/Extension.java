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
package lucee.runtime.extension;

import lucee.runtime.PageContext;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;

public interface Extension {

	public String getAuthor();

	public String getCodename();

	public String getVideo();

	public String getSupport();

	public String getDocumentation();

	public String getForum();

	public String getMailinglist();

	public String getNetwork();

	public DateTime getCreated();

	public String getName();

	public String getLabel();

	public String getDescription();

	public String getCategory();

	public String getImage();

	public String getVersion();

	public String getProvider();

	public String getId();

	public Struct getConfig(PageContext pc);

	public String getStrConfig();

	public String getType();
}