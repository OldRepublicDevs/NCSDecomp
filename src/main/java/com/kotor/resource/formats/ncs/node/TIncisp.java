// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TIncisp extends Token {
   public TIncisp() {
      super.setText("INCISP");
   }

   public TIncisp(int line, int pos) {
      super.setText("INCISP");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TIncisp clone() {
      return new TIncisp(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTIncisp(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TIncisp text.");
   }
}

