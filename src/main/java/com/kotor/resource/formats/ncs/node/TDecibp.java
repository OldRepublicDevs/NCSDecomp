// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TDecibp extends Token {
   public TDecibp() {
      super.setText("DECIBP");
   }

   public TDecibp(int line, int pos) {
      super.setText("DECIBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TDecibp clone() {
      return new TDecibp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTDecibp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TDecibp text.");
   }
}

