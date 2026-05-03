// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TSub extends Token {
   public TSub() {
      super.setText("SUB");
   }

   public TSub(int line, int pos) {
      super.setText("SUB");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TSub clone() {
      return new TSub(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTSub(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TSub text.");
   }
}

