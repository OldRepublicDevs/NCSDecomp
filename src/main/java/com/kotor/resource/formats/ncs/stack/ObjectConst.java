// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.stack;

import com.kotor.resource.formats.ncs.utils.Type;

/**
 * Constant representing an object ID literal on the stack.
 */
public class ObjectConst extends Const {
   private Integer value;

   public ObjectConst(Integer value) {
      this.type = new Type((byte)6);
      this.value = value;
      this.size = 1;
   }

   public Integer value() {
      return this.value;
   }

   @Override
   public String toString() {
      if (this.value == 0) {
         return "OBJECT_SELF";
      } else {
         return this.value == 1 ? "OBJECT_INVALID" : this.value.toString();
      }
   }
}

