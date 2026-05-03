// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TLt extends Token {
   public TLt() {
      super.setText("LT");
   }

   public TLt(int line, int pos) {
      super.setText("LT");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TLt clone() {
      return new TLt(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTLt(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TLt text.");
   }
}

