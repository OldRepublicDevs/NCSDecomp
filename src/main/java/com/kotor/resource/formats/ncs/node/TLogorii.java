// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TLogorii extends Token {
   public TLogorii() {
      super.setText("LOGORII");
   }

   public TLogorii(int line, int pos) {
      super.setText("LOGORII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TLogorii clone() {
      return new TLogorii(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTLogorii(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TLogorii text.");
   }
}

