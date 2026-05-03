// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TCpdownsp extends Token {
   public TCpdownsp() {
      super.setText("CPDOWNSP");
   }

   public TCpdownsp(int line, int pos) {
      super.setText("CPDOWNSP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TCpdownsp clone() {
      return new TCpdownsp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTCpdownsp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TCpdownsp text.");
   }
}

