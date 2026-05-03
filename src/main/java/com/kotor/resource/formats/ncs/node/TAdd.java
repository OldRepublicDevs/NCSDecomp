// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

/**
 * Terminal token for the ADD opcode emitted by the lexer.
 */
public final class TAdd extends Token {
   public TAdd() {
      super.setText("ADD");
   }

   public TAdd(int line, int pos) {
      super.setText("ADD");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TAdd clone() {
      return new TAdd(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTAdd(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TAdd text.");
   }
}

