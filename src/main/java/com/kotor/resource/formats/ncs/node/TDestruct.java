// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TDestruct extends Token {
   public TDestruct() {
      super.setText("DESTRUCT");
   }

   public TDestruct(int line, int pos) {
      super.setText("DESTRUCT");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TDestruct clone() {
      return new TDestruct(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTDestruct(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TDestruct text.");
   }
}

