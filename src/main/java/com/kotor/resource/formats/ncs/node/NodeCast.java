// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.node;

/**
 * Cast implementation that preserves parented {@link Node} instances.
 */
public class NodeCast implements Cast<Node> {
   private static final long serialVersionUID = 1L;
   public static final NodeCast instance = new NodeCast();

   private NodeCast() {
   }

   @Override
   public Node cast(Object o) {
      if (!(o instanceof Node)) {
         throw new ClassCastException("Expected Node but got: " + (o != null ? o.getClass().getName() : "null"));
      }
      return (Node)o;
   }
}

