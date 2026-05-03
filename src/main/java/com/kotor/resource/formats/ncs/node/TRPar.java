// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TRPar extends Token {
   public TRPar() {
      super.setText(")");
   }

   public TRPar(int line, int pos) {
      super.setText(")");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TRPar clone() {
      return new TRPar(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTRPar(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TRPar text.");
   }
}

