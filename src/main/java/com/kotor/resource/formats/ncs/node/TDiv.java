// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TDiv extends Token {
   public TDiv() {
      super.setText("DIV");
   }

   public TDiv(int line, int pos) {
      super.setText("DIV");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TDiv clone() {
      return new TDiv(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTDiv(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TDiv text.");
   }
}

