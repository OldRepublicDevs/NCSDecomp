// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TJmp extends Token {
   public TJmp() {
      super.setText("JMP");
   }

   public TJmp(int line, int pos) {
      super.setText("JMP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TJmp clone() {
      return new TJmp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTJmp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TJmp text.");
   }
}

