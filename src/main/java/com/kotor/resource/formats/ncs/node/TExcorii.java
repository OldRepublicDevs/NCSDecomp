// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TExcorii extends Token {
   public TExcorii() {
      super.setText("EXCORII");
   }

   public TExcorii(int line, int pos) {
      super.setText("EXCORII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TExcorii clone() {
      return new TExcorii(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTExcorii(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TExcorii text.");
   }
}

