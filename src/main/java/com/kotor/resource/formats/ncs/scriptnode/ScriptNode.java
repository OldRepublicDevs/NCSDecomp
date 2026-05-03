// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public abstract class ScriptNode {
   private ScriptNode parent;
   protected String tabs;
   protected String newline = System.getProperty("line.separator");

   public ScriptNode parent() {
      return this.parent;
   }

   public void parent(ScriptNode parent) {
      this.parent = parent;
      if (parent != null) {
         this.tabs = parent.tabs + "\t";
      }
   }

   public void close() {
      this.parent = null;
   }
}

