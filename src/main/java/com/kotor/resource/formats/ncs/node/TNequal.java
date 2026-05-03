// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TNequal extends Token {
   public TNequal() {
      super.setText("NEQUAL");
   }

   public TNequal(int line, int pos) {
      super.setText("NEQUAL");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TNequal clone() {
      return new TNequal(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTNequal(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TNequal text.");
   }
}

