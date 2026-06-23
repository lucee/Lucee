package com.intergral.fusiondebug.server;

import java.util.List;

public interface IFDController {
   List pause();

   String getEngineName();

   String getEngineVersion();

   void output(String var1);

   List getExceptionTypes();

   boolean getCaughtStatus(String var1, String var2, String var3, String var4, String var5, int var6);

   String getLicenseInformation(String var1);

   IFDThread getByNativeIdentifier(String var1);

   String getCompletionType();

   String getCompletionMethod();

   void release();
}
