// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TMovsp extends Token {
   public TMovsp() {
      super.setText("MOVSP");
   }

   public TMovsp(int line, int pos) {
      super.setText("MOVSP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TMovsp clone() {
      return new TMovsp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTMovsp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TMovsp text.");
   }
}

