package lucee.runtime.instrumentation;

import java.lang.instrument.Instrumentation;


public class ExternalAgent {
	  private static Instrumentation instrumentation;
	  
	  public static void premain(String agentArgs, Instrumentation inst) {
		  setInstrumentation(inst);
	  }
	  
	  public static void agentmain(String agentArgs, Instrumentation inst) {
		  setInstrumentation(inst);
	  }
	  
	  private static void setInstrumentation(Instrumentation inst) {
			if(inst!=null) {
				try{
					/*
					System.out.println("start set instrumentation");
					System.out.println(Thread.currentThread().getContextClassLoader().getClass().getName());
					System.out.println(ClassLoader.getSystemClassLoader().getClass().getName());
					System.out.println(new ExternalAgent().getClass().getClassLoader().getClass().getName());
					*/
					instrumentation=inst;
				}
				catch(Throwable t){
					t.printStackTrace();
				}
			}
		}

	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}
}
