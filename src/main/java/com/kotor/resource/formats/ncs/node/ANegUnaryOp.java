// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

import com.kotor.resource.formats.ncs.analysis.Analysis;

public final class ANegUnaryOp extends PUnaryOp {
   private TNeg _neg_;

   public ANegUnaryOp() {
   }

   public ANegUnaryOp(TNeg _neg_) {
      this.setNeg(_neg_);
   }

   @Override
   public Node clone() {
      return new ANegUnaryOp((TNeg)this.cloneNode(this._neg_));
   }

   @Override
   public void apply(Switch sw) {
      ((Analysis)sw).caseANegUnaryOp(this);
   }

   public TNeg getNeg() {
      return this._neg_;
   }

   public void setNeg(TNeg node) {
      if (this._neg_ != null) {
         this._neg_.parent(null);
      }

      if (node != null) {
         if (node.parent() != null) {
            node.parent().removeChild(node);
         }

         node.parent(this);
      }

      this._neg_ = node;
   }

   @Override
   public String toString() {
      return this.toString(this._neg_);
   }

   @Override
   void removeChild(Node child) {
      if (this._neg_ == child) {
         this._neg_ = null;
      }
   }

   @Override
   void replaceChild(Node oldChild, Node newChild) {
      if (this._neg_ == oldChild) {
         this.setNeg((TNeg)newChild);
      }
   }
}

