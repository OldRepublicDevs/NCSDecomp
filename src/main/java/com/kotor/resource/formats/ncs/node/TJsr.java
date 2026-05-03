// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TJsr extends Token {
   public TJsr() {
      super.setText("JSR");
   }

   public TJsr(int line, int pos) {
      super.setText("JSR");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TJsr clone() {
      return new TJsr(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTJsr(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TJsr text.");
   }
}

