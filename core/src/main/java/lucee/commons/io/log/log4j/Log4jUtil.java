/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.commons.io.log.log4j;

import org.apache.log4j.Appender;
import org.apache.log4j.HTMLLayout;
import org.apache.log4j.Layout;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.xml.XMLLayout;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.log4j.appender.ConsoleAppender;
import lucee.commons.io.log.log4j.appender.DatasourceAppender;
import lucee.commons.io.log.log4j.appender.RollingResourceAppender;
import lucee.commons.io.log.log4j.layout.ClassicLayout;
import lucee.commons.io.log.log4j.layout.DatasourceLayout;
import lucee.runtime.config.Config;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.db.ClassDefinition;
import lucee.transformer.library.ClassDefinitionImpl;

public class Log4jUtil {

    public static final long MAX_FILE_SIZE = 1024 * 1024 * 10;
    public static final int MAX_FILES = 10;

    public static final Log getLogger(Config config, Appender appender, String name, int level) {
	return new LogAdapter(_getLogger(config, appender, name, level));
    }

    static final Logger _getLogger(Config config, Appender appender, String name, int level) {
	// fullname
	String fullname = name;
	if (config instanceof ConfigWeb) {
	    ConfigWeb cw = (ConfigWeb) config;
	    fullname = "web." + cw.getLabel() + "." + name;
	}
	else fullname = "server." + name;

	Logger l = LogManager.exists(fullname);
	if (l != null) l.removeAllAppenders();
	else l = LogManager.getLogger(fullname);
	l.setAdditivity(false);
	l.addAppender(appender);
	l.setLevel(LogAdapter.toLevel(level));
	return l;
    }

    public static ClassDefinition<Appender> appenderClassDefintion(String className) {
	if ("console".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ConsoleAppender.class);
	if ("resource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(RollingResourceAppender.class);
	if ("datasource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(DatasourceAppender.class);

	return new ClassDefinitionImpl(className);
    }

    public static ClassDefinition<Layout> layoutClassDefintion(String className) {
	if ("classic".equalsIgnoreCase(className)) return new ClassDefinitionImpl(ClassicLayout.class);
	if ("datasource".equalsIgnoreCase(className)) return new ClassDefinitionImpl(DatasourceLayout.class);
	if ("html".equalsIgnoreCase(className)) return new ClassDefinitionImpl(HTMLLayout.class);
	if ("xml".equalsIgnoreCase(className)) return new ClassDefinitionImpl(XMLLayout.class);
	if ("pattern".equalsIgnoreCase(className)) return new ClassDefinitionImpl(PatternLayout.class);

	return new ClassDefinitionImpl(className);
    }

    // private static LoggerRepository repository=new Hierarchy(null);
}