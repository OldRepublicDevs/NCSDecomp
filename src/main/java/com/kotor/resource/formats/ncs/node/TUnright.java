// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TUnright extends Token {
   public TUnright() {
      super.setText("USHRIGHTII");
   }

   public TUnright(int line, int pos) {
      super.setText("USHRIGHTII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TUnright clone() {
      return new TUnright(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTUnright(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TUnright text.");
   }
}

