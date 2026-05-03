// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TDecisp extends Token {
   public TDecisp() {
      super.setText("DECISP");
   }

   public TDecisp(int line, int pos) {
      super.setText("DECISP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TDecisp clone() {
      return new TDecisp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTDecisp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TDecisp text.");
   }
}

