package lucee.runtime.engine;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import lucee.print;
import lucee.commons.io.IOUtil;
import lucee.commons.io.SystemUtil;
import lucee.commons.lang.ExceptionUtil;

public class LogST extends Thread {
	
	private static final char NL = '\n';
	private Thread thread;
	private long size=0;
	private long max=1024*1024*100; 
	
	public static void main(String[] args) throws InterruptedException {

    	print.e("----------- start ------------");
		LogST log = new LogST(Thread.currentThread());
		log.start();
		log.join();
    	print.e("----------- stop ------------");
	}
	
	public LogST(Thread thread) {
		this.thread=thread;
	}
	
	public void run() {
		PrintStream ps=null;
		try {
			ps=new PrintStream(createFile());
			while(true) {
				printStackTrace(ps,thread.getStackTrace());
				SystemUtil.sleep(10);
				if(size>max) {
					IOUtil.closeEL(ps);
					ps=new PrintStream(createFile());
					size=0;
				}
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			IOUtil.closeEL(ps);
		}
		print.e("----------- done ------------");
	}
	
	
    private File createFile() throws IOException {
    	File f;
    	int count=0;
    	while((f=new File("/Users/mic/stacktrace-"+(++count)+".log")).isFile()) {
    		
    	}
    	print.e(f.getCanonicalPath());
		return f;
	}

	private void printStackTrace(PrintStream ps, StackTraceElement[] trace) {
    	
        
        {
        	String line;
            // Print our stack trace
        	String head=System.currentTimeMillis()+"\n";
        	ps.print(head);
        	size += head.length();
        	for (StackTraceElement traceElement : trace) {
            	line="\tat " + traceElement+"\n";
                ps.print(line);
                size += line.length();
            }
            ps.print(NL);
            ps.flush();
            size+=1;
            
        }
    }

	public static void _do() {

    	print.e("----------- start ------------");
		LogST log = new LogST(Thread.currentThread());
		log.start();
		//log.join();
    	print.e("----------- stop ------------");
	}
}
