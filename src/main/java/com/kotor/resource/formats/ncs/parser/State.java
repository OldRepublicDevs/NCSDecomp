// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.parser;

import com.kotor.resource.formats.ncs.node.Node;

/**
 * Lightweight stack frame used by the generated parser shift/reduce engine.
 */
final class State {
   int state;
   Node node;

   State(int state, Node node) {
      this.state = state;
      this.node = node;
   }
}

