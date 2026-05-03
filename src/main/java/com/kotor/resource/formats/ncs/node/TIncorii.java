// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TIncorii extends Token {
   public TIncorii() {
      super.setText("INCORII");
   }

   public TIncorii(int line, int pos) {
      super.setText("INCORII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TIncorii clone() {
      return new TIncorii(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTIncorii(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TIncorii text.");
   }
}

