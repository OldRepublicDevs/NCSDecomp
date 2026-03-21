// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public class AReturnStatement extends ScriptNode {
   protected AExpression returnexp;

   public AReturnStatement() {
   }

   public AReturnStatement(AExpression returnexp) {
      this.returnexp(returnexp);
   }

   public void returnexp(AExpression returnexp) {
      returnexp.parent(this);
      this.returnexp = returnexp;
   }

   public AExpression exp() {
      return this.returnexp;
   }

   @Override
   public String toString() {
      return this.returnexp == null
         ? this.tabs + "return;" + this.newline
         : this.tabs + "return " + ExpressionFormatter.formatValue(this.returnexp) + ";" + this.newline;
   }

   @Override
   public void close() {
      super.close();
      if (this.returnexp != null) {
         ((ScriptNode)this.returnexp).close();
      }

      this.returnexp = null;
   }
}

