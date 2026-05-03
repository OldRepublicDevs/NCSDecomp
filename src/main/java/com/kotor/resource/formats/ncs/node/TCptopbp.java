// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TCptopbp extends Token {
   public TCptopbp() {
      super.setText("CPTOPBP");
   }

   public TCptopbp(int line, int pos) {
      super.setText("CPTOPBP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TCptopbp clone() {
      return new TCptopbp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTCptopbp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TCptopbp text.");
   }
}

