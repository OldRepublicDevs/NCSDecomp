// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TLeq extends Token {
   public TLeq() {
      super.setText("LEQ");
   }

   public TLeq(int line, int pos) {
      super.setText("LEQ");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TLeq clone() {
      return new TLeq(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTLeq(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TLeq text.");
   }
}

