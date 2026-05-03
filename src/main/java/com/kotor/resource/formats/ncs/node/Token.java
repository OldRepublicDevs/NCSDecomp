// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

/**
 * Base class for all lexer tokens; stores text and source position.
 */
public abstract class Token extends Node {
   private String text;
   private int line;
   private int pos;

   public String getText() {
      return this.text;
   }

   public void setText(String text) {
      this.text = text;
   }

   public int getLine() {
      return this.line;
   }

   public void setLine(int line) {
      this.line = line;
   }

   public int getPos() {
      return this.pos;
   }

   public void setPos(int pos) {
      this.pos = pos;
   }

   @Override
   public String toString() {
      return this.text + " ";
   }

   @Override
   void removeChild(Node child) {
   }

   @Override
   void replaceChild(Node oldChild, Node newChild) {
   }

   /**
    * Creates and returns a copy of this token.
    * Subclasses should override this method to return their specific type
    * (covariant return type) instead of Node for better type safety.
    *
    * @return a clone of this token
    */
   @Override
   public abstract Token clone();
}

