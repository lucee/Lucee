package com.intergral.fusiondebug.server;

public class FDMutabilityException extends Exception {
   public FDMutabilityException() {
   }

   public FDMutabilityException(String message) {
      super(message);
   }

   public FDMutabilityException(Throwable cause) {
      super(cause);
   }

   public FDMutabilityException(String message, Throwable cause) {
      super(message, cause);
   }
}
