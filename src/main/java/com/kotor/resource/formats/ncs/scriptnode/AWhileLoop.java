// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public class AWhileLoop extends AControlLoop {
   public AWhileLoop(int start, int end) {
      super(start, end);
   }

   @Override
   public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append(this.tabs + "while" + this.formattedCondition() + " {" + this.newline);

      for (int i = 0; i < this.children.size(); i++) {
         buff.append(this.children.get(i).toString());
      }

      buff.append(this.tabs + "}" + this.newline);
      return buff.toString();
   }
}

