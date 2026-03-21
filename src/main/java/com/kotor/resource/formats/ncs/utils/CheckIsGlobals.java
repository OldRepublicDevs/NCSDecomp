// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.utils;

import com.kotor.resource.formats.ncs.analysis.PrunedReversedDepthFirstAdapter;
import com.kotor.resource.formats.ncs.node.ABpCommand;
import com.kotor.resource.formats.ncs.node.ACommandBlock;
import com.kotor.resource.formats.ncs.node.PCmd;

/**
 * Detects whether a subroutine represents the globals block (presence of BP ops).
 */
public class CheckIsGlobals extends PrunedReversedDepthFirstAdapter {
   private boolean isGlobals = false;

   @Override
   public void inABpCommand(ABpCommand node) {
      this.isGlobals = true;
   }

   @Override
   public void caseACommandBlock(ACommandBlock node) {
      this.inACommandBlock(node);
      PCmd[] temp = node.getCmd().toArray(new PCmd[0]);

      for (int i = temp.length - 1; i >= 0; i--) {
         temp[i].apply(this);
         if (this.isGlobals) {
            return;
         }
      }

      this.outACommandBlock(node);
   }

   public boolean getIsGlobals() {
      return this.isGlobals;
   }
}

