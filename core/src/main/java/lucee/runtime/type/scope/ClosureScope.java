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

import java.util.Iterator;

import lucee.runtime.PageContext;
import lucee.runtime.config.NullSupportHelper;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Duplicator;
import lucee.runtime.type.Collection;
import lucee.runtime.type.StructImpl;

public class ClosureScope extends ScopeSupport implements Variables {
	
	private static final Object NULL = new Object();
	private Argument arg;
	private Local local;
	private Variables var;
	
	public Argument getArgument() {
		return arg;
	}

	public Variables getVariables() {
		return var;
	}

	public Undefined getUndefined() {
		return und;
	}

	private boolean debug;
	private Undefined und; 

	public ClosureScope(PageContext pc,Argument arg, Local local,Variables var ){
		super("variables",SCOPE_VARIABLES,StructImpl.TYPE_UNDEFINED);
		arg.setBind(true);
		local.setBind(true);
		var.setBind(true);
		und = pc.undefinedScope();
		this.arg=arg;
		this.local=local;
		this.var=var;
		this.debug=pc.getConfig().debug();
	}

	@Override
	public boolean isInitalized() {
		return true;
	}

	@Override
	public void initialize(PageContext pc) {	
	}

	@Override
	public void release(PageContext pc) {
	}

	@Override
	public int getType() {
		return SCOPE_VARIABLES;
	}

	@Override
	public String getTypeAsString() {
		return "variables";
	}

	@Override
	public int size() {
		return var.size();
	}

	@Override
	public Key[] keys() {
		return var.keys();
	}

	@Override
	public Object remove(Key key) throws PageException {
		if(local.containsKey(key))
			return local.remove(key);
		return var.remove(key);
	}

	@Override
	public Object removeEL(Key key) {
		if(local.containsKey(key))
			return local.removeEL(key);
		return var.removeEL(key);
	}

	@Override
	public void clear() {
		var.clear();
	}

	@Override
	public Object get(Key key) throws PageException {
		Object value = local.get(key,NullSupportHelper.NULL());
		if(value!=NullSupportHelper.NULL()) return value;
		value=arg.get(key,NullSupportHelper.NULL());
		if(value!=NullSupportHelper.NULL()) {
			if(debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),arg.getTypeAsString(), key);
			return value;
		}
		
		value= var.get(key);
		if(debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),var.getTypeAsString(), key);
		return value;
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		Object value = local.get(key,NullSupportHelper.NULL());
		if(value!=NullSupportHelper.NULL()) return value;
		value=arg.get(key,NullSupportHelper.NULL());
		if(value!=NullSupportHelper.NULL()) {
			if(debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),arg.getTypeAsString(), key);
			return value;
		}
		value= var.get(key,NullSupportHelper.NULL());
		if(value!=NullSupportHelper.NULL()){
			if(debug) UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),var.getTypeAsString(), key);
			return value;
		}
		return defaultValue;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		if(und.getLocalAlways() || local.containsKey(key))     return local.set(key,value);
	    if(arg.containsKey(key))  {
	    	if(debug)UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),arg.getTypeAsString(), key);
	    	return arg.set(key,value);
	    }
	    if(debug)UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),var.getTypeAsString(), key);
		return var.set(key, value);
	}

	@Override
	public Object setEL(Key key, Object value) {
	    if(und.getLocalAlways() || local.containsKey(key))     return local.setEL(key,value);
        if(arg.containsKey(key))  {
        	if(debug)UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),arg.getTypeAsString(), key);
        	return arg.setEL(key,value);
        }
	    	
		if(debug)UndefinedImpl.debugCascadedAccess(ThreadLocalPageContext.get(),var.getTypeAsString(), key);
		return var.setEL(key,value);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return new ClosureScope(ThreadLocalPageContext.get(),(Argument)Duplicator.duplicate(arg,deepCopy), (Local)Duplicator.duplicate(local,deepCopy), (Variables)Duplicator.duplicate(var,deepCopy));
	}

	@Override
	public boolean containsKey(Key key) {
		return get(key,NULL)!=NULL;
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		return var.keyIterator();
	}
    
    @Override
	public Iterator<String> keysAsStringIterator() {
    	return var.keysAsStringIterator();
    }
	
	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		return var.entryIterator();
	}
	
	@Override
	public Iterator<Object> valueIterator() {
		return var.valueIterator();
	}
	
	@Override
	public void setBind(boolean bind) {}

	@Override
	public boolean isBind() {
		return true;
	}
	
	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel,
			DumpProperties properties) {
		
		DumpTable dt= (DumpTable) super.toDumpData(pageContext, maxlevel, properties);
		dt.setTitle("Closure Variable Scope");
		return dt;
	}


}