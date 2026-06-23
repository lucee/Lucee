package com.intergral.fusiondebug.server;

import java.util.List;

public interface IFDThread {
   List getStack();

   IFDStackFrame getTopStackFrame();

   void stop();

   String getOutputBuffer();

   Thread getThread();

   String getName();

   int id();
}
