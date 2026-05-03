// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

/**
 * Identity cast; used when no parenting adjustments are needed.
 */
public final class NoCast<T> implements Cast<T> {
   private static final long serialVersionUID = 1L;
   private static final NoCast<?> INSTANCE = new NoCast<>();

   private NoCast() {
   }

   @SuppressWarnings("unchecked")
   public static <T> NoCast<T> instance() {
      return (NoCast<T>)INSTANCE;
   }

   @SuppressWarnings("unchecked")
   @Override
   public T cast(Object o) {
      return (T)o;
   }
}

