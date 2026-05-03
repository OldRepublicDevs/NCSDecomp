// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TNop extends Token {
   public TNop() {
      super.setText("NOP");
   }

   public TNop(int line, int pos) {
      super.setText("NOP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TNop clone() {
      return new TNop(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTNop(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TNop text.");
   }
}

