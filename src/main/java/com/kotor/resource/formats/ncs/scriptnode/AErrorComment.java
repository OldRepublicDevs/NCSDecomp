// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.
package com.kotor.resource.formats.ncs.scriptnode;

/**
 * Lightweight placeholder node that renders a block comment.
 */
public class AErrorComment extends ScriptNode {
   private final String message;

   public AErrorComment(String message) {
      this.message = message;
   }

   @Override
   public String toString() {
      return this.tabs + "/* " + this.message + " */" + this.newline;
   }
}
