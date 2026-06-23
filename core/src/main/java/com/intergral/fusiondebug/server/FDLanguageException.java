package com.intergral.fusiondebug.server;

public class FDLanguageException extends Exception {
   public FDLanguageException() {
   }

   public FDLanguageException(String message) {
      super(message);
   }

   public FDLanguageException(Throwable cause) {
      super(cause);
   }

   public FDLanguageException(String message, Throwable cause) {
      super(message, cause);
   }
}
