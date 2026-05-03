// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TMod extends Token {
   public TMod() {
      super.setText("MOD");
   }

   public TMod(int line, int pos) {
      super.setText("MOD");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TMod clone() {
      return new TMod(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTMod(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TMod text.");
   }
}

