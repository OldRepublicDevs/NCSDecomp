// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TMul extends Token {
   public TMul() {
      super.setText("MUL");
   }

   public TMul(int line, int pos) {
      super.setText("MUL");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TMul clone() {
      return new TMul(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTMul(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TMul text.");
   }
}

