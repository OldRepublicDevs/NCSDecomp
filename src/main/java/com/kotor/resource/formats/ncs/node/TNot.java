// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TNot extends Token {
   public TNot() {
      super.setText("NOT");
   }

   public TNot(int line, int pos) {
      super.setText("NOT");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TNot clone() {
      return new TNot(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTNot(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TNot text.");
   }
}

