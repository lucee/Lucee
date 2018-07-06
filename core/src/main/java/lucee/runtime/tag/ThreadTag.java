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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.RandomUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.Page;
import lucee.runtime.PageContext;
import lucee.runtime.PageContextImpl;
import lucee.runtime.config.ConfigImpl;
import lucee.runtime.exp.ApplicationException;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.SecurityException;
import lucee.runtime.ext.tag.BodyTagImpl;
import lucee.runtime.ext.tag.DynamicAttributes;
import lucee.runtime.op.Caster;
import lucee.runtime.spooler.ExecutionPlan;
import lucee.runtime.spooler.ExecutionPlanImpl;
import lucee.runtime.thread.ChildSpoolerTask;
import lucee.runtime.thread.ChildThread;
import lucee.runtime.thread.ChildThreadImpl;
import lucee.runtime.thread.ThreadUtil;
import lucee.runtime.thread.ThreadsImpl;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.scope.Threads;
import lucee.runtime.type.util.KeyConstants;
import lucee.runtime.type.util.ListUtil;

// MUST change behavior of multiple headers now is a array, it das so?

/**
* Lets you execute HTTP POST and GET operations on files. Using cfhttp, you can execute standard 
*   GET operations and create a query object from a text file. POST operations lets you upload MIME file 
*   types to a server, or post cookie, formfield, URL, file, or CGI variables directly to a specified server.
*
*
*
* 
**/
public final class ThreadTag extends BodyTagImpl implements DynamicAttributes {
	
	private static final int ACTION_JOIN = 0;
	private static final int ACTION_RUN = 1;
	private static final int ACTION_SLEEP = 2;
	private static final int ACTION_TERMINATE = 3;
	
	private static final int TYPE_DAEMON = 0;
	private static final int TYPE_TASK = 1;

	public static final int LEVEL_KIDS = 1;
	public static final int LEVEL_PARENTS = 2;
	public static final int LEVEL_CURRENT = 4;
	public static final int LEVEL_ALL = LEVEL_KIDS+LEVEL_PARENTS+LEVEL_CURRENT;
	
	
	
	
	
	private static final ExecutionPlan[] EXECUTION_PLAN = new ExecutionPlan[0];
	
	private int action=ACTION_RUN;
	private long duration=-1;
	private Collection.Key _name;
	private int priority=Thread.NORM_PRIORITY;
	private long timeout=0;
	private PageContext pc;
	private int type=TYPE_DAEMON;
	private ExecutionPlan[] plans=EXECUTION_PLAN;
	private Struct attrs;
	

	@Override
	public void release()	{
		super.release();
		action=ACTION_RUN;
		duration=-1;
		_name=null;
		priority=Thread.NORM_PRIORITY;
		type=TYPE_DAEMON;
		plans=EXECUTION_PLAN;
		timeout=0;
		attrs=null;
		pc=null;
	}
	
	/**
	 * @param action the action to set
	 */
	public void setAction(String strAction) throws ApplicationException {
		String lcAction = strAction.trim().toLowerCase();
		
		if("join".equals(lcAction)) 			this.action=ACTION_JOIN;
		else if("run".equals(lcAction)) 		this.action=ACTION_RUN;
		else if("sleep".equals(lcAction)) 	this.action=ACTION_SLEEP;
		else if("terminate".equals(lcAction)) this.action=ACTION_TERMINATE;
		else 
			throw new ApplicationException("invalid value ["+strAction+"] for attribute action","values for attribute action are:join,run,sleep,terminate");
	}


	/**
	 * @param duration the duration to set
	 */
	public void setDuration(double duration) {
		this.duration = (long) duration;
	}


	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		if(StringUtil.isEmpty(name,true)) return;
		this._name=KeyImpl.init(name);
	}

	private Collection.Key name(boolean create) {
		if(_name==null && create) _name=KeyImpl.init("thread"+RandomUtil.createRandomStringLC(20));
		return _name;
	}
	private String nameAsString(boolean create) {
		name(create);
		return _name==null?null:_name.getString();
	}

	/**
	 * @param strPriority the priority to set
	 */
	public void setPriority(String strPriority) throws ApplicationException {
		int p = ThreadUtil.toIntPriority(strPriority);
		if(p==-1) {
			throw new ApplicationException("invalid value ["+strPriority+"] for attribute priority","values for attribute priority are:low,high,normal");
		}
		priority=p;
	}
	

	/**
	 * @param strType the type to set
	 * @throws ApplicationException 
	 * @throws SecurityException 
	 */
	public void setType(String strType) throws ApplicationException, SecurityException {
		strType=strType.trim().toLowerCase();

		if("task".equals(strType))	{
			// SNSN
			/*SerialNumber sn = pageContext.getConfig().getSerialNumber();
		    if(sn.getVersion()==SerialNumber.VERSION_COMMUNITY)
		         throw new SecurityException("no access to this functionality with the "+sn.getStringVersion()+" version of Lucee");
		    */
			
			
			//throw new ApplicationException("invalid value ["+strType+"] for attribute type","task is not supported at the moment");
			type=TYPE_TASK;
		}
		else if("daemon".equals(strType)) type=TYPE_DAEMON;
		else throw new ApplicationException("invalid value ["+strType+"] for attribute type","values for attribute type are:task,daemon (default)");
		
	}
	
	public void setRetryintervall(Object obj) throws PageException {
		setRetryinterval(obj);
	}
	
	public void setRetryinterval(Object obj) throws PageException {
		if(StringUtil.isEmpty(obj))return;
		Array arr = Caster.toArray(obj,null);
		if(arr==null){
			plans=new ExecutionPlan[]{toExecutionPlan(obj,1)};
		}
		else {
			Iterator<Object> it = arr.valueIterator();
			plans=new ExecutionPlan[arr.size()];
			int index=0;
			while(it.hasNext()) {
				plans[index++]=toExecutionPlan(it.next(),index==1?1:0);
			}
		}
		
	}
	
	
	


	private ExecutionPlan toExecutionPlan(Object obj,int plus) throws PageException {

		if(obj instanceof Struct){
			Struct sct=(Struct)obj;
			// GERT
			
			// tries
			Object oTries=sct.get(KeyConstants._tries,null);
			if(oTries==null)throw new ExpressionException("missing key tries inside struct");
			int tries=Caster.toIntValue(oTries);
			if(tries<0)throw new ExpressionException("tries must contain a none negative value");
			
			// interval
			Object oInterval=sct.get(KeyConstants._interval,null);
			if(oInterval==null)oInterval=sct.get(KeyConstants._intervall,null);
			
			if(oInterval==null)throw new ExpressionException("missing key interval inside struct");
			int interval=toSeconds(oInterval);
			if(interval<0)throw new ExpressionException("interval should contain a positive value or 0");
			
			
			return new ExecutionPlanImpl(tries+plus,interval);
		}
		return new ExecutionPlanImpl(1+plus,toSeconds(obj));
	}
	
	private int toSeconds(Object obj) throws PageException {
		return (int)Caster.toTimespan(obj).getSeconds();
	}
	/**
	 * @param timeout the timeout to set
	 */
	public void setTimeout(double timeout) {
		this.timeout = (long)timeout;
	}

	@Override
	public void setDynamicAttribute(String uri, String name, Object value) {
		if(attrs==null)attrs=new StructImpl();
		Key key = KeyImpl.getInstance(StringUtil.trim(name,""));
		attrs.setEL(key,value);
	}

	@Override
	public void setDynamicAttribute(String uri, Collection.Key name, Object value) {
		if(attrs==null)attrs=new StructImpl();
		Key key = KeyImpl.getInstance(StringUtil.trim(name.getString(),""));
		attrs.setEL(key,value);
	}

	@Override
	public int doStartTag() throws PageException	{
		pc=pageContext;
		switch(action) {
			case ACTION_JOIN:	
				doJoin();
			break;
			case ACTION_SLEEP:	
				required("thread", "sleep", "duration", duration,-1);	
				doSleep();
			break;
			case ACTION_TERMINATE:	
				required("thread", "terminate", "name", nameAsString(false));
				doTerminate();
			break;
			case ACTION_RUN:		
				//required("thread", "run", "name", name(true).getString());
				return EVAL_BODY_INCLUDE;
			
		}
		return SKIP_BODY;
	}

	@Override
	public int doEndTag() throws PageException {
		this.pc=pageContext;
		//if(ACTION_RUN==action) doRun();
		return EVAL_PAGE;
	}
	
	public void register(Page currentPage, int threadIndex) throws PageException	{
		if(ACTION_RUN!=action) return;

		Key name = name(true);
		try {
			Threads ts = ThreadTag.getThreadScope(pc,name,ThreadTag.LEVEL_ALL); // pc.getThreadScope(name);
			
			if(type==TYPE_DAEMON){
				if(ts!=null)
					throw new ApplicationException("could not create a thread with the name ["+name.getString()+"]. name must be unique within a request");
				ChildThreadImpl ct = new ChildThreadImpl((PageContextImpl) pc,currentPage,name.getString(),threadIndex,attrs,false);

				pc.setThreadScope(name,new ThreadsImpl(ct));
				ct.setPriority(priority);
				ct.setDaemon(false);
				ct.start();
			}
			else {
				ChildThreadImpl ct = new ChildThreadImpl((PageContextImpl) pc,currentPage,name.getString(),threadIndex,attrs,true);
				ct.setPriority(priority);
				((ConfigImpl)pc.getConfig()).getSpoolerEngine().add(new ChildSpoolerTask(ct,plans));
			}
			
		} 
		catch(Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
		finally {
			((PageContextImpl)pc).reuse(this);// this method is not called from template when type is run, a call from template is to early,
		}
	}
	

	public static java.util.Collection<String> getThreadScopeNames(PageContext pc, boolean recurive) {
		return getThreadScopeNames(pc, 
				recurive?
						LEVEL_CURRENT+LEVEL_KIDS:
						LEVEL_CURRENT);
	}

	public static java.util.Collection<String> getThreadScopeNames(PageContext pc, int level) {
		String[] names=null;
		java.util.Collection<String> result=new HashSet<String>();
		
		// current
		if((level&LEVEL_CURRENT)>0) {
			names=pc.getThreadScopeNames();
			if(names!=null)for(int i=0;i<names.length;i++) {
				result.add(names[i]);
			}
		}
		// parent
		if((level&LEVEL_PARENTS)>0) {
			PageContext parent=pc.getParentPageContext();
			while(parent!=null) {
				names=parent.getThreadScopeNames();
				if(names!=null)for(int i=0;i<names.length;i++) {
					result.add(names[i]);
				}
				parent=parent.getParentPageContext();
			}
		}
		// children
		if((level&LEVEL_KIDS)>0 && pc.hasFamily()) {
			getKidsThreadScopeNames(((PageContextImpl)pc).getChildPageContexts(),result,0);
		}
		return result;
	}
	
	private static void getKidsThreadScopeNames(List<PageContext> pageContexts,java.util.Collection<String> result, int level) {
		if(pageContexts==null || pageContexts.isEmpty()) return;
		
		String[] names=null;
		Iterator<PageContext> it = pageContexts.iterator();
		PageContext pc;
		while(it.hasNext()) {
			pc=it.next();
			names=pc.getThreadScopeNames();

			if(names!=null)for(int i=0;i<names.length;i++) {
				result.add(names[i]);
			}
			
			getKidsThreadScopeNames(((PageContextImpl)pc).getChildPageContexts(),result,level+1);
		}
	}

	public static Threads getThreadScope(PageContext pc, Key name, int level) {
		Threads t=null;
		// current
		if((level&LEVEL_CURRENT)>0) {
			t=pc.getThreadScope(name);
			if(t!=null) return t;
		}
		// parent
		if((level&LEVEL_PARENTS)>0) {
			PageContext parent=pc.getParentPageContext();
			while(parent!=null) {
				t=parent.getThreadScope(name);
				if(t!=null) return t;
				parent=parent.getParentPageContext();
			}
		}
		// children
		if((level&LEVEL_KIDS)>0 && pc.hasFamily()) {
			t=getKidsThreadScope(((PageContextImpl)pc).getChildPageContexts(),name);
			if(t!=null) return t;
		}
		return t;
	}
	
	private static Threads getKidsThreadScope(List<PageContext> pageContexts, Key name) {
		if(pageContexts==null || pageContexts.isEmpty()) return null;
		
		Threads t;
		Iterator<PageContext> it = pageContexts.iterator();
		PageContext pc;
		while(it.hasNext()) {
			pc=it.next();
			t=pc.getThreadScope(name);
			if(t!=null) return t;
			t=getKidsThreadScope(((PageContextImpl)pc).getChildPageContexts(),name);
			return t;
		}
		return null;
	}

	private void doSleep() throws ExpressionException {
		if(duration>=0) {
			SystemUtil.sleep(duration);
		}
		else throw new ExpressionException("The attribute duration must be greater or equal than 0, now ["+duration+"]");
		
	}

    private void doJoin() throws ApplicationException {
    	//PageContextImpl mpc=(PageContextImpl)getMainPageContext(pc);
		
    	String[] names,all=null;
    	Key name = name(false);
    	if(name==null) {
    		all=names=ListUtil.toStringArray(ThreadTag.getThreadScopeNames(pc, ThreadTag.LEVEL_CURRENT+ThreadTag.LEVEL_KIDS)); // mpc.getThreadScopeNames();
    	}
    	else names=ListUtil.listToStringArray(name.getLowerString(), ',');
    	
    	ChildThread ct;
    	Threads ts;
    	long start=System.currentTimeMillis(),_timeout=timeout>0?timeout:-1;
    	
    	for(int i=0;i<names.length;i++) {
    		if(StringUtil.isEmpty(names[i],true))continue;
    		//PageContextImpl mpc=(PageContextImpl)getMainPageContext(pc);
    		ts = ThreadTag.getThreadScope(pc, KeyImpl.init(names[i]), ThreadTag.LEVEL_CURRENT+ThreadTag.LEVEL_KIDS);//mpc.getThreadScope(names[i]);
    		if(ts==null) {
    			if(all==null)all=ListUtil.toStringArray(ThreadTag.getThreadScopeNames(pc, ThreadTag.LEVEL_CURRENT+ThreadTag.LEVEL_KIDS)); // mpc.getThreadScopeNames();
    			
    			throw new ApplicationException("there is no thread running with the name ["+
    											names[i]+"], only the following threads existing ["+
    											ListUtil.arrayToList(all,", ")+"]");
    		}
    		ct=ts.getChildThread();
    		
    		if(ct.isAlive()) {
    			try {
					if(_timeout!=-1)ct.join(_timeout);
					else ct.join();
				} 
    			catch (InterruptedException e) {}
    		}
    		if(_timeout!=-1){
    			_timeout=_timeout-(System.currentTimeMillis()-start);
    			if(_timeout<1) break;
    		}
    	}
    }
    
	private void doTerminate() throws ApplicationException {
		//PageContextImpl mpc=(PageContextImpl)getMainPageContext(pc);
		
		Threads ts = ThreadTag.getThreadScope(pc, KeyImpl.init(nameAsString(false)), ThreadTag.LEVEL_CURRENT+ThreadTag.LEVEL_KIDS); //mpc.getThreadScope(nameAsString(false));
		
		if(ts==null)
			throw new ApplicationException("there is no thread running with the name ["+nameAsString(false)+"]");
		ChildThread ct = ts.getChildThread();
		
		if(ct.isAlive()){
			ct.terminated();
			SystemUtil.stop(ct);
		}
		
	}

	/*private PageContext getMainPageContext(PageContext pc) {
		if(pc==null)pc=pageContext;
		if(pc.getParentPageContext()==null) return pc;
		return pc.getParentPageContext();
	}*/

	@Override
	public void doInitBody()	{
		
	}

	@Override
	public int doAfterBody()	{
		return SKIP_BODY;
	}

	/**
	 * sets if has body or not
	 * @param hasBody
	 */
	public void hasBody(boolean hasBody) {
	    
	}
}