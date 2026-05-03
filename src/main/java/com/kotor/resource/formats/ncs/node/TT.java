// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TT extends Token {
   public TT() {
      super.setText("T");
   }

   public TT(int line, int pos) {
      super.setText("T");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TT clone() {
      return new TT(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTT(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TT text.");
   }
}

