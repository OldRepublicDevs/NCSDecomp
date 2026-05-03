// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TComp extends Token {
   public TComp() {
      super.setText("COMP");
   }

   public TComp(int line, int pos) {
      super.setText("COMP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TComp clone() {
      return new TComp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTComp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TComp text.");
   }
}

