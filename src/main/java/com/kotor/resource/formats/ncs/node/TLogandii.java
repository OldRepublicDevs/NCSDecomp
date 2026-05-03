// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TLogandii extends Token {
   public TLogandii() {
      super.setText("LOGANDII");
   }

   public TLogandii(int line, int pos) {
      super.setText("LOGANDII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TLogandii clone() {
      return new TLogandii(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTLogandii(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TLogandii text.");
   }
}

