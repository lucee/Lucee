package com.intergral.fusiondebug.server;

import java.util.List;

public interface IFDStackFrame {
   String getExecutionUnitPackage();

   String getExecutionUnitName();

   String getSourceFilePath();

   String getSourceFileName();

   int getLineNumber();

   List getScopeNames();

   List getVariables();

   List getVariables(String var1) throws FDLanguageException;

   IFDVariable evaluate(String var1) throws FDLanguageException;

   IFDThread getThread();

   String getFrameInformation();
}
