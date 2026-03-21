// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.parser;

import com.kotor.resource.formats.ncs.node.Token;

/**
 * Checked exception indicating parse failure; carries the offending token.
 */
public class ParserException extends Exception {
   private static final long serialVersionUID = 1L;
   private final transient Token token;

   public ParserException(Token token, String message) {
      super(message);
      this.token = token;
   }

   public Token getToken() {
      return this.token;
   }
}

