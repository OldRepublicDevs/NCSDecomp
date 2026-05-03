// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TCptopsp extends Token {
   public TCptopsp() {
      super.setText("CPTOPSP");
   }

   public TCptopsp(int line, int pos) {
      super.setText("CPTOPSP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TCptopsp clone() {
      return new TCptopsp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTCptopsp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TCptopsp text.");
   }
}

