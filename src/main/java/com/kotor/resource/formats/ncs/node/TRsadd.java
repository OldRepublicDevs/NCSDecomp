// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TRsadd extends Token {
   public TRsadd() {
      super.setText("RSADD");
   }

   public TRsadd(int line, int pos) {
      super.setText("RSADD");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TRsadd clone() {
      return new TRsadd(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTRsadd(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TRsadd text.");
   }
}

