// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

/**
 * Terminal token for the ACTION opcode marker.
 */
public final class TAction extends Token {
   public TAction() {
      super.setText("ACTION");
   }

   public TAction(int line, int pos) {
      super.setText("ACTION");
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TAction clone() {
      return new TAction(this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTAction(this);
   }

   @Override
   public void setText(String text) {
      throw new RuntimeException("Cannot change TAction text.");
   }
}

