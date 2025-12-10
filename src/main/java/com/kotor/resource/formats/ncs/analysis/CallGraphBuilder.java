// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs.analysis;

import com.kotor.resource.formats.ncs.node.AJumpToSubroutine;
import com.kotor.resource.formats.ncs.node.ASubroutine;
import com.kotor.resource.formats.ncs.node.Node;
import com.kotor.resource.formats.ncs.utils.NodeAnalysisData;
import com.kotor.resource.formats.ncs.utils.SubroutineAnalysisData;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a call graph (JSR edges) between subroutines.
 * <p>
 * Relies on {@link NodeAnalysisData} destinations populated by {@code SetDestinations}.
 */
public class CallGraphBuilder extends PrunedDepthFirstAdapter {
   private final NodeAnalysisData nodedata;
   private final SubroutineAnalysisData subdata;
   private final Map<Integer, Set<Integer>> edges = new HashMap<>();
   private int current;

   public CallGraphBuilder(NodeAnalysisData nodedata, SubroutineAnalysisData subdata) {
      this.nodedata = nodedata;
      this.subdata = subdata;
   }

   public CallGraph build() {
      this.subdata.getSubroutines().forEachRemaining(sub -> sub.apply(this));
      return new CallGraph(this.edges);
   }

   @Override
   public void inASubroutine(ASubroutine node) {
      this.current = this.nodedata.getPos(node);
      this.edges.putIfAbsent(this.current, new HashSet<>());
   }

   @Override
   public void outAJumpToSubroutine(AJumpToSubroutine node) {
      Node dest = this.nodedata.getDestination(node);
      if (dest instanceof ASubroutine) {
         int dstPos = this.nodedata.getPos(dest);
         this.edges.computeIfAbsent(this.current, k -> new HashSet<>()).add(dstPos);
      }
   }

   public static class CallGraph {
      private final Map<Integer, Set<Integer>> forward;

      CallGraph(Map<Integer, Set<Integer>> forward) {
         this.forward = new HashMap<>();
         forward.forEach((k, v) -> this.forward.put(k, new HashSet<>(v)));
      }

      public Map<Integer, Set<Integer>> edges() {
         return Collections.unmodifiableMap(this.forward);
      }

      public Set<Integer> successors(int node) {
         return this.forward.getOrDefault(node, Collections.emptySet());
      }

      public Set<Integer> nodes() {
         return this.forward.keySet();
      }

      public Set<Integer> reachableFrom(int start) {
         Set<Integer> seen = new HashSet<>();
         this.dfs(start, seen);
         return seen;
      }

      private void dfs(int node, Set<Integer> seen) {
         if (!seen.add(node)) {
            return;
         }
         for (int succ : this.forward.getOrDefault(node, Collections.emptySet())) {
            this.dfs(succ, seen);
         }
      }
   }
}

