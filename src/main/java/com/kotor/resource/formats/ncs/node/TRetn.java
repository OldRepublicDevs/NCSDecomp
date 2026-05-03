// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TRetn extends Token {
   public TRetn() {
      super.setText("RETN");
   }

   public TRetn(int line, int pos) {
      super.setText("RETN");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TRetn clone() {
      return new TRetn(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTRetn(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TRetn text.");
   }
}

