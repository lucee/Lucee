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
package lucee.runtime.functions.system;

import lucee.runtime.CFMLFactory;
import lucee.runtime.CFMLFactoryImpl;
import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigWeb;
import lucee.runtime.config.ConfigWebImpl;
import lucee.runtime.db.DatasourceManagerImpl;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class GetSystemInfo implements Function {
    
    public static Struct call(PageContext pc) throws PageException {
    	Struct sct=new StructImpl();
    	ConfigWebImpl config = (ConfigWebImpl) pc.getConfig();
    	CFMLFactoryImpl factory = (CFMLFactoryImpl) config.getFactory();
    	
    	// threads/requests
    	sct.put("activeRequests", factory.getActiveRequests());
    	sct.put("activeThreads", factory.getActiveThreads());
    	sct.put("queueRequests", config.getThreadQueue().size());
    	
    	// Datasource connections
    	sct.put("activeDatasourceConnections", config.getDatasourceConnectionPool().openConnections().size());
    	
    	// tasks
    	sct.put("tasksOpen", config.getSpoolerEngine().getOpenTaskCount());
    	sct.put("tasksClosed", config.getSpoolerEngine().getClosedTaskCount());
    	
    	
        return sct;
    }
}