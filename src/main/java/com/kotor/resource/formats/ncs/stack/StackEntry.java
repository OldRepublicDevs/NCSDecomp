// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.stack;

import com.kotor.resource.formats.ncs.utils.Type;

/**
 * Base element stored on the decompiler's logical stacks (vars or types).
 */
public abstract class StackEntry {
   protected Type type;
   protected int size;

   public Type type() {
      return this.type;
   }

   public int size() {
      return this.size;
   }

   public abstract void removedFromStack(LocalStack<?> var1);

   public abstract void addedToStack(LocalStack<?> var1);

   @Override
   public abstract String toString();

   public abstract StackEntry getElement(int var1);

   public void close() {
      if (this.type != null) {
         this.type.close();
      }

      this.type = null;
   }

   public abstract void doneParse();

   public abstract void doneWithStack(LocalVarStack var1);
}

