// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TIncibp extends Token {
   public TIncibp() {
      super.setText("INCIBP");
   }

   public TIncibp(int line, int pos) {
      super.setText("INCIBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TIncibp clone() {
      return new TIncibp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTIncibp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TIncibp text.");
   }
}

