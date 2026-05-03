// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class AReturnCmd extends PCmd {
   private PReturn _return_;

   public AReturnCmd() {
   }

   public AReturnCmd(PReturn _return_) {
      this.setReturn(_return_);
   }

   @Override
   public Node clone() {
      return new AReturnCmd((PReturn)this.cloneNode(this._return_));
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseAReturnCmd(this);
   }

   public PReturn getReturn() {
      return this._return_;
   }

   public void setReturn(PReturn node) {
      if (this._return_ != null) {
         this._return_.parent(null);
      }

      if (node != null) {
         if (node.parent() != null) {
            node.parent().removeChild(node);
         }

         node.parent(this);
      }

      this._return_ = node;
   }

   @Override
   public String toString() {
      return this.toString(this._return_);
   }

   @Override
   void removeChild(Node child) {
      if (this._return_ == child) {
         this._return_ = null;
      }
   }

   @Override
   void replaceChild(Node oldChild, Node newChild) {
      if (this._return_ == oldChild) {
         this.setReturn((PReturn)newChild);
      }
   }
}

