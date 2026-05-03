// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TDot extends Token {
   public TDot() {
      super.setText(".");
   }

   public TDot(int line, int pos) {
      super.setText(".");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TDot clone() {
      return new TDot(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTDot(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TDot text.");
   }
}

