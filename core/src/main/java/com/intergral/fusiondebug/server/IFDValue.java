package com.intergral.fusiondebug.server;

import java.util.List;

public interface IFDValue {
   String toString();

   void set(String var1) throws FDMutabilityException, FDLanguageException;

   boolean isMutable();

   List getChildren();

   boolean hasChildren();
}
