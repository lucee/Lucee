package com.intergral.fusiondebug.server;

public interface IFDVariable {
   String getName();

   IFDValue getValue();

   IFDStackFrame getStackFrame();
}
