// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// Visit https://bolabaden.org for more information and other ventures
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs.stack;

import com.kotor.resource.formats.ncs.utils.Type;

/**
 * Base class for constant stack entries (int/float/string/object).
 */
public class Const extends StackEntry {
   public static Const newConst(Type type, Long intValue) {
      if (type.byteValue() != 3) {
         throw new RuntimeException("Invalid const type for int value: " + type);
      }
      return new IntConst(intValue);
   }

   public static Const newConst(Type type, Float floatValue) {
      if (type.byteValue() != 4) {
         throw new RuntimeException("Invalid const type for float value: " + type);
      }
      return new FloatConst(floatValue);
   }

   public static Const newConst(Type type, String stringValue) {
      if (type.byteValue() != 5) {
         throw new RuntimeException("Invalid const type for string value: " + type);
      }
      return new StringConst(stringValue);
   }

   public static Const newConst(Type type, Integer objectValue) {
      if (type.byteValue() != 6) {
         throw new RuntimeException("Invalid const type for object value: " + type);
      }
      return new ObjectConst(objectValue);
   }

   @Override
   public void removedFromStack(LocalStack<?> stack) {
   }

   @Override
   public void addedToStack(LocalStack<?> stack) {
   }

   @Override
   public void doneParse() {
   }

   @Override
   public void doneWithStack(LocalVarStack stack) {
   }

   @Override
   public String toString() {
      return "";
   }

   @Override
   public StackEntry getElement(int stackpos) {
      if (stackpos != 1) {
         throw new RuntimeException("Position > 1 for const, not struct");
      } else {
         return this;
      }
   }
}

