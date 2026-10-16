package com.intergral.fusiondebug.server;

import java.util.List;

public class FDSignalException extends Exception {
   private String runtimeExceptionType;
   private String runtimeExceptionExpression;
   private boolean runtimeExceptionCaughtStatus;
   private List exceptionStack;

   public String getRuntimeExceptionType() {
      return this.runtimeExceptionType;
   }

   public void setRuntimeExceptionType(String runtimeExceptionType) {
      this.runtimeExceptionType = runtimeExceptionType;
   }

   public String getRuntimeExceptionExpression() {
      return this.runtimeExceptionExpression;
   }

   public void setRuntimeExceptionExpression(String runtimeExceptionExpression) {
      this.runtimeExceptionExpression = runtimeExceptionExpression;
   }

   public boolean isRuntimeExceptionCaughtStatus() {
      return this.runtimeExceptionCaughtStatus;
   }

   public void setRuntimeExceptionCaughtStatus(boolean runtimeExceptionCaughtStatus) {
      this.runtimeExceptionCaughtStatus = runtimeExceptionCaughtStatus;
   }

   public List getExceptionStack() {
      return this.exceptionStack;
   }

   public void setExceptionStack(List exceptionStack) {
      this.exceptionStack = exceptionStack;
   }
}
