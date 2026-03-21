// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.lexer;

/**
 * Checked exception indicating tokenization failure in the generated lexer.
 */
public class LexerException extends Exception {
   private static final long serialVersionUID = 1L;
   public LexerException(String message) {
      super(message);
   }
}

