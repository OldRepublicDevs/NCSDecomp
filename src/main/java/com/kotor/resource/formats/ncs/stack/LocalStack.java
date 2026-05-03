// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.stack;

import java.util.LinkedList;

/**
 * Lightweight generic stack with clone support for analysis passes.
 */
public class LocalStack<T> implements Cloneable {
   protected LinkedList<T> stack = new LinkedList<>();

   public int size() {
      return this.stack.size();
   }

   @Override
   public LocalStack<T> clone() {
      LocalStack<T> newStack = new LocalStack<>();
      newStack.stack = new LinkedList<>(this.stack);
      return newStack;
   }

   public void close() {
      this.stack = null;
   }
}

