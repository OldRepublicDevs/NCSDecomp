// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TGeq extends Token {
   public TGeq() {
      super.setText("GEQ");
   }

   public TGeq(int line, int pos) {
      super.setText("GEQ");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TGeq clone() {
      return new TGeq(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTGeq(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TGeq text.");
   }
}

