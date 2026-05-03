// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

/**
 * Terminal token for the BOOLANDII opcode (boolean AND on ints).
 */
public final class TBoolandii extends Token {
   public TBoolandii() {
      super.setText("BOOLANDII");
   }

   public TBoolandii(int line, int pos) {
      super.setText("BOOLANDII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TBoolandii clone() {
      return new TBoolandii(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTBoolandii(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TBoolandii text.");
   }
}

