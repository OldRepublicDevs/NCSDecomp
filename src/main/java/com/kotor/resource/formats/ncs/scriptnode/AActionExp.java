// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs.scriptnode;

import com.kotor.resource.formats.ncs.ActionsData;
import com.kotor.resource.formats.ncs.stack.StackEntry;
import java.util.ArrayList;
import java.util.List;

/**
 * Script expression node representing a function/action call in NSS output.
 * Captures the action name, arguments, inferred stack entry for type info, and
 * the original action ID.
 */
public class AActionExp extends ScriptNode implements AExpression {
   private List<AExpression> params;
   private String action;
   private StackEntry stackentry;
   private int id;
   private ActionsData actionsData;

   public AActionExp(String action, int id, List<AExpression> params) {
      this(action, id, params, null);
   }

   public AActionExp(String action, int id, List<AExpression> params, ActionsData actionsData) {
      this.action = action;
      this.params = new ArrayList<>();
      this.actionsData = actionsData;

      for (int i = 0; i < params.size(); i++) {
         this.addParam(params.get(i));
      }

      this.stackentry = null;
      this.id = id;
   }

   protected void addParam(AExpression param) {
      param.parent(this);
      this.params.add(param);
   }

   public AExpression getParam(int pos) {
      return this.params.get(pos);
   }

   public String action() {
      return this.action;
   }

   @Override
   public String toString() {
      StringBuffer buff = new StringBuffer();
      buff.append(this.action + "(");
      String prefix = "";

      // For round-trip testing, we need to output all parameters exactly as they were in the original
      // Disable parameter trimming to ensure bytecode matches
      int paramCount = this.params.size();
      // Parameter trimming disabled for round-trip accuracy
      // Original trimming logic commented out:
      /*
      if (this.actionsData != null) {
         try {
            List<String> defaults = this.actionsData.getDefaultValues(this.id);
            int requiredParams = this.actionsData.getRequiredParamCount(this.id);
            int optionalCount = Math.max(0, defaults.size() - requiredParams);

            // If any optional parameter differs from its default, keep the full argument list.
            boolean hasNonDefaultOptional = false;
            for (int i = requiredParams; i < Math.min(paramCount, defaults.size()); i++) {
               String defaultValue = defaults.get(i);
               if (defaultValue == null) {
                  continue;
               }
               String normalizedParam = normalizeValue(this.params.get(i).toString());
               String normalizedDefault = normalizeValue(defaultValue);
               if (!normalizedParam.equals(normalizedDefault)) {
                  hasNonDefaultOptional = true;
                  break;
               }
            }

            // Only trim when there are multiple optional parameters and all of them
            // match defaults that look like compiler-inserted sentinels.
            if (optionalCount > 1 && !hasNonDefaultOptional) {
               int lastNonDefault = paramCount;
               for (int i = paramCount - 1; i >= 0 && i < defaults.size(); i--) {
                  String defaultValue = defaults.get(i);
                  if (defaultValue == null) {
                     break;
                  }

                  if (!isLikelyCompilerInsertedDefault(defaultValue)) {
                     break;
                  }

                  String paramStr = this.params.get(i).toString();
                  String normalizedParam = normalizeValue(paramStr);
                  String normalizedDefault = normalizeValue(defaultValue);
                  if (normalizedParam.equals(normalizedDefault)) {
                     lastNonDefault = i;
                  } else {
                     break;
                  }
               }
               paramCount = lastNonDefault;
            } else {
               paramCount = this.params.size();
            }
         } catch (Exception e) {
            // If there's any error, output all parameters
            paramCount = this.params.size();
         }
      }
      */

      for (int i = 0; i < paramCount; i++) {
         buff.append(prefix + this.params.get(i).toString());
         prefix = ", ";
      }

      buff.append(")");
      return buff.toString();
   }

   private boolean isLikelyCompilerInsertedDefault(String defaultValue) {
      if (defaultValue == null) {
         return false;
      }
      String v = defaultValue.trim();
      return v.equals("-1") || v.equals("0xFFFFFFFF") || v.equals("0xFFFFFFFFFFFFFFFF");
   }

   /**
    * Heuristic to decide whether a trailing default argument was likely
    * inserted by the compiler rather than written explicitly in source.
    * <p>
    * Keep boolean defaults (e.g., FALSE/0) because many game scripts pass
    * them deliberately for clarity, and the compiled bytecode does not let
    * us distinguish an omitted optional bool from an explicit one. Only
    * trim sentinel values that are almost certainly compiler-generated.
    */

   private String normalizeValue(String value) {
      if (value == null) {
         return "";
      }
      value = value.trim();
      // Handle TRUE/FALSE constants
      if (value.equals("TRUE") || value.equals("1")) {
         return "1";
      }
      if (value.equals("FALSE") || value.equals("0")) {
         return "0";
      }
      // Normalize float literals (1.0f -> 1.0, 0.0f -> 0.0)
      if (value.endsWith("f") || value.endsWith("F")) {
         value = value.substring(0, value.length() - 1);
      }
      // Handle hex values (0xFFFFFFFF -> 4294967295, but we'll compare as hex)
      if (value.startsWith("0x") || value.startsWith("0X")) {
         try {
            long hexVal = Long.parseLong(value.substring(2), 16);
            return Long.toString(hexVal);
         } catch (NumberFormatException e) {
            // Not a valid hex number, return as-is
         }
      }
      return value;
   }

   @Override
   public StackEntry stackentry() {
      return this.stackentry;
   }

   @Override
   public void stackentry(StackEntry stackentry) {
      this.stackentry = stackentry;
   }

   public int getId() {
      return this.id;
   }

   @Override
   public void close() {
      super.close();
      if (this.params != null) {
         for (AExpression param : this.params) {
            if (param instanceof ScriptNode) {
               ((ScriptNode)param).close();
            }
         }

         this.params = null;
      }

      if (this.stackentry != null) {
         this.stackentry.close();
      }

      this.stackentry = null;
   }
}

