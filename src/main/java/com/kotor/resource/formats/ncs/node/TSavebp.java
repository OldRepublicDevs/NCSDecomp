// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TSavebp extends Token {
   public TSavebp() {
      super.setText("SAVEBP");
   }

   public TSavebp(int line, int pos) {
      super.setText("SAVEBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TSavebp clone() {
      return new TSavebp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTSavebp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TSavebp text.");
   }
}

