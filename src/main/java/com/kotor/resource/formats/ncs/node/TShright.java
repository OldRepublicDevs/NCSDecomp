// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TShright extends Token {
   public TShright() {
      super.setText("SHRIGHTII");
   }

   public TShright(int line, int pos) {
      super.setText("SHRIGHTII");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TShright clone() {
      return new TShright(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTShright(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TShright text.");
   }
}

