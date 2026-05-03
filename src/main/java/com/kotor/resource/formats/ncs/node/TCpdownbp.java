// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TCpdownbp extends Token {
   public TCpdownbp() {
      super.setText("CPDOWNBP");
   }

   public TCpdownbp(int line, int pos) {
      super.setText("CPDOWNBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TCpdownbp clone() {
      return new TCpdownbp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTCpdownbp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TCpdownbp text.");
   }
}

