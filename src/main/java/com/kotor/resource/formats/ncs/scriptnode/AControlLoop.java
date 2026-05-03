// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public class AControlLoop extends ScriptRootNode {
   protected AExpression condition;

   public AControlLoop(int start, int end) {
      super(start, end);
   }

   public void end(int end) {
      this.end = end;
   }

   public void condition(AExpression condition) {
      condition.parent(this);
      this.condition = condition;
   }

   public AExpression condition() {
      return this.condition;
   }

   /**
    * Returns the condition wrapped in a single pair of parentheses, adding them only when needed.
    */
   protected String formattedCondition() {
      if (this.condition == null) {
         return " ()";
      }

      String cond = this.condition.toString().trim();
      // Check if the condition is TRULY wrapped in matching outer parentheses
      // Simply checking startsWith("(") && endsWith(")") is wrong because
      // "(A && B) && (C && D)" starts with ( and ends with ) but they don't match!
      boolean wrapped = isWrappedInParens(cond);
      String wrappedCond = wrapped ? cond : "(" + cond + ")";
      return " " + wrappedCond;
   }

   /**
    * Check if a string is wrapped in a MATCHING pair of outer parentheses.
    * "(A && B)" -> true
    * "(A && B) && (C && D)" -> false (the outer parens don't match)
    * "A && B" -> false
    */
   private static boolean isWrappedInParens(String s) {
      if (s == null || s.length() < 2) {
         return false;
      }
      if (!s.startsWith("(") || !s.endsWith(")")) {
         return false;
      }
      // Count parentheses depth - if we reach 0 before the end, they don't match
      int depth = 0;
      for (int i = 0; i < s.length(); i++) {
         char c = s.charAt(i);
         if (c == '(') {
            depth++;
         } else if (c == ')') {
            depth--;
            // If depth reaches 0 before the last character, the parens don't wrap the whole expression
            if (depth == 0 && i < s.length() - 1) {
               return false;
            }
         }
      }
      return depth == 0;
   }

   @Override
   public void close() {
      super.close();
      if (this.condition != null) {
         ((ScriptNode)this.condition).close();
         this.condition = null;
      }
   }
}

