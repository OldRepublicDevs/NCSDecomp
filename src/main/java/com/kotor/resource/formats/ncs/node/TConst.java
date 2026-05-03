// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

/**
 * Terminal token for the CONST opcode emitted by the lexer.
 */
public final class TConst extends Token {
   public TConst() {
      super.setText("CONST");
   }

   public TConst(int line, int pos) {
      super.setText("CONST");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TConst clone() {
      return new TConst(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTConst(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TConst text.");
   }
}

