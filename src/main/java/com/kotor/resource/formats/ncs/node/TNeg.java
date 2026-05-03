// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TNeg extends Token {
   public TNeg() {
      super.setText("NEG");
   }

   public TNeg(int line, int pos) {
      super.setText("NEG");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TNeg clone() {
      return new TNeg(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTNeg(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TNeg text.");
   }
}

