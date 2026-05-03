// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.stack;

import com.kotor.resource.formats.ncs.utils.Type;

/**
 * Constant representing a string literal on the stack.
 */
public class StringConst extends Const {
   private String value;

   public StringConst(String value) {
      this.type = new Type((byte)5);
      this.value = value;
      this.size = 1;
   }

   public String value() {
      return this.value;
   }

   @Override
   public String toString() {
      return this.value.toString();
   }
}

