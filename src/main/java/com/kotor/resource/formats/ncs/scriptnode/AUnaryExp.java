// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

import com.kotor.resource.formats.ncs.stack.StackEntry;

public class AUnaryExp extends ScriptNode implements AExpression {
   private AExpression exp;
   private String op;
   private StackEntry stackentry;

   public AUnaryExp(AExpression exp, String op) {
      this.exp(exp);
      this.op = op;
   }

   protected void exp(AExpression exp) {
      this.exp = exp;
      exp.parent(this);
   }

   public AExpression exp() {
      return this.exp;
   }

   public String op() {
      return this.op;
   }

   @Override
   public String toString() {
      return ExpressionFormatter.format(this);
   }

   @Override
   public StackEntry stackentry() {
      return this.stackentry;
   }

   @Override
   public void stackentry(StackEntry stackentry) {
      this.stackentry = stackentry;
   }

   @Override
   public void close() {
      super.close();
      if (this.exp != null) {
         ((ScriptNode)this.exp).close();
      }

      this.exp = null;
      if (this.stackentry != null) {
         this.stackentry.close();
      }

      this.stackentry = null;
   }
}

