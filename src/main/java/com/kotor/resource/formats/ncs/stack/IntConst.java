// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.stack;

import com.kotor.resource.formats.ncs.utils.Type;

/**
 * Constant representing an integer literal on the stack.
 */
public class IntConst extends Const {
   private Long value;

   public IntConst(Long value) {
      this.type = new Type((byte)3);
      this.value = value;
      this.size = 1;
   }

   public Long value() {
      return this.value;
   }

   @Override
   public String toString() {
      return this.value == Long.parseLong("FFFFFFFF", 16) ? "0xFFFFFFFF" : this.value.toString();
   }
}

