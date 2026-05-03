// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TStorestate extends Token {
   public TStorestate() {
      super.setText("STORE_STATE");
   }

   public TStorestate(int line, int pos) {
      super.setText("STORE_STATE");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TStorestate clone() {
      return new TStorestate(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTStorestate(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TStorestate text.");
   }
}

