package lucee.runtime.engine;

import lucee.print;
import lucee.commons.io.SystemUtil;
import lucee.runtime.PageContext;

public class Request extends Thread {

	public static final short TYPE_CFML=1;
	public static final short TYPE_LUCEE=2;
	public static final short TYPE_REST=3;
	
	
	private PageContext pc;
	private Thread parent;
	private boolean done;
	private short type;
	
	public Request(PageContext pc, short type) {
		this.parent=Thread.currentThread();
		this.pc=pc;
		this.type=type;
			
	}
	
	public void run() {
    	ThreadQueue queue = null;
        
        try {
        	ThreadLocalPageContext.register(pc);
        	ThreadQueue tmp = pc.getConfig().getThreadQueue();
	        tmp.enter(pc);
	        queue=tmp;
        	if(type==TYPE_CFML)pc.executeCFML(pc.getHttpServletRequest().getServletPath(),false,true);
        	else if(type==TYPE_LUCEE) pc.execute(pc.getHttpServletRequest().getServletPath(),false,true);
        	else pc.executeRest(pc.getHttpServletRequest().getServletPath(),false);
        } 
        catch (Throwable _t) {}
        finally {
        	if(queue!=null)queue.exit(pc);
            ThreadLocalPageContext.release();
        }
        done=true;
        SystemUtil.notify(parent);
	}

	public boolean isDone() {
		return done;
	}

}
