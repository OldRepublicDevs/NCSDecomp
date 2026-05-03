// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TGt extends Token {
   public TGt() {
      super.setText("GT");
   }

   public TGt(int line, int pos) {
      super.setText("GT");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TGt clone() {
      return new TGt(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTGt(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TGt text.");
   }
}

