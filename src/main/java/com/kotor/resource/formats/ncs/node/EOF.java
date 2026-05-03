// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class EOF extends Token {
   public EOF() {
      this.setText("");
   }

   public EOF(int line, int pos) {
      this.setText("");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public EOF clone() {
      return new EOF(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseEOF(this);
   }
}

