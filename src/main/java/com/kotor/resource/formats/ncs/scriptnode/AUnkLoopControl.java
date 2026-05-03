// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

public class AUnkLoopControl extends ScriptNode {
   protected int dest;

   public AUnkLoopControl(int dest) {
      this.dest = dest;
   }

   public int getDestination() {
      return this.dest;
   }

   @Override
   public String toString() {
      return "BREAK or CONTINUE undetermined";
   }
}

