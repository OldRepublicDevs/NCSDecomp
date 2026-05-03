// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class TStringLiteral extends Token {
   public TStringLiteral(String text) {
      this.setText(text);
   }

   public TStringLiteral(String text, int line, int pos) {
      this.setText(text);
      this.setLine(line);
      this.setPos(pos);
   }

   @Override
   public TStringLiteral clone() {
      return new TStringLiteral(this.getText(), this.getLine(), this.getPos());
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseTStringLiteral(this);
   }
}

