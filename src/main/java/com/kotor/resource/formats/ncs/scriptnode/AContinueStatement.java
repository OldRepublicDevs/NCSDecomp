// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public class AContinueStatement extends ScriptNode {
   @Override
   public String toString() {
      return this.tabs + "continue;" + this.newline;
   }
}

