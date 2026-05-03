// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TEqual extends Token {
   public TEqual() {
      super.setText("EQUAL");
   }

   public TEqual(int line, int pos) {
      super.setText("EQUAL");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TEqual clone() {
      return new TEqual(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTEqual(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TEqual text.");
   }
}

