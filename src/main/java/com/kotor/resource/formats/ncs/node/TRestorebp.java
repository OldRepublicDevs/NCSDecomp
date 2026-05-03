// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TRestorebp extends Token {
   public TRestorebp() {
      super.setText("RESTOREBP");
   }

   public TRestorebp(int line, int pos) {
      super.setText("RESTOREBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TRestorebp clone() {
      return new TRestorebp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTRestorebp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TRestorebp text.");
   }
}

