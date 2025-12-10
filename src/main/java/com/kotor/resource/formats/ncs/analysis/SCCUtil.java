// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs.analysis;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Strongly-connected component helper for call-graph condensation.
 */
public final class SCCUtil {
   private SCCUtil() {
   }

   public static List<Set<Integer>> compute(Map<Integer, Set<Integer>> graph) {
      Tarjan tarjan = new Tarjan(graph);
      List<Set<Integer>> sccs = tarjan.run();
      return topologicalOrder(graph, sccs, tarjan.componentIndex);
   }

   private static List<Set<Integer>> topologicalOrder(Map<Integer, Set<Integer>> graph, List<Set<Integer>> sccs, Map<Integer, Integer> compIndex) {
      Map<Integer, Set<Integer>> condensed = new HashMap<>();
      int[] indegree = new int[sccs.size()];

      for (Map.Entry<Integer, Set<Integer>> entry : graph.entrySet()) {
         int fromComp = compIndex.get(entry.getKey());
         for (int succ : entry.getValue()) {
            int toComp = compIndex.get(succ);
            if (fromComp != toComp && condensed.computeIfAbsent(fromComp, k -> new HashSet<>()).add(toComp)) {
               indegree[toComp]++;
            }
         }
      }

      ArrayDeque<Integer> queue = new ArrayDeque<>();
      for (int i = 0; i < indegree.length; i++) {
         if (indegree[i] == 0) {
            queue.add(i);
         }
      }

      List<Set<Integer>> ordered = new ArrayList<>();
      while (!queue.isEmpty()) {
         int comp = queue.remove();
         ordered.add(sccs.get(comp));
         for (int succ : condensed.getOrDefault(comp, new HashSet<>())) {
            if (--indegree[succ] == 0) {
               queue.add(succ);
            }
         }
      }

      return ordered;
   }

   private static class Tarjan {
      private final Map<Integer, Set<Integer>> graph;
      private final Map<Integer, Integer> index = new HashMap<>();
      private final Map<Integer, Integer> lowlink = new HashMap<>();
      private final Deque<Integer> stack = new ArrayDeque<>();
      private final Set<Integer> onStack = new HashSet<>();
      private final List<Set<Integer>> components = new ArrayList<>();
      private int idx = 0;
      private final Map<Integer, Integer> componentIndex = new HashMap<>();

      Tarjan(Map<Integer, Set<Integer>> graph) {
         this.graph = graph;
      }

      List<Set<Integer>> run() {
         for (int node : this.graph.keySet()) {
            if (!this.index.containsKey(node)) {
               this.strongConnect(node);
            }
         }
         return this.components;
      }

      private void strongConnect(int v) {
         this.index.put(v, this.idx);
         this.lowlink.put(v, this.idx);
         this.idx++;
         this.stack.push(v);
         this.onStack.add(v);

         for (int w : this.graph.getOrDefault(v, new HashSet<>())) {
            if (!this.index.containsKey(w)) {
               this.strongConnect(w);
               this.lowlink.put(v, Math.min(this.lowlink.get(v), this.lowlink.get(w)));
            } else if (this.onStack.contains(w)) {
               this.lowlink.put(v, Math.min(this.lowlink.get(v), this.index.get(w)));
            }
         }

         if (this.lowlink.get(v).equals(this.index.get(v))) {
            Set<Integer> component = new HashSet<>();
            int w;
            do {
               w = this.stack.pop();
               this.onStack.remove(w);
               component.add(w);
               this.componentIndex.put(w, this.components.size());
            } while (w != v);
            this.components.add(component);
         }
      }
   }
}

