// Copyright 2021-2025 NCSDecomp
// Licensed under the Business Source License 1.1 (BSL 1.1).
// Visit https://bolabaden.org for more information and other ventures
// See LICENSE.txt file in the project root for full license information.

package com.kotor.resource.formats.ncs;

import com.kotor.resource.formats.ncs.stack.Variable;
import java.util.Hashtable;
import java.util.Vector;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

/**
 * Small helper for producing Swing {@link TreeModel} instances used in the GUI.
 */
public class TreeModelFactory extends JTree {
   private static final long serialVersionUID = 1L;
   protected static TreeModel emptyModel;

   public TreeModelFactory() {
      emptyModel = JTree.createTreeModel(new Hashtable<>());
   }

   /**
    * Builds a {@link TreeModel} from the provided Hashtable.
    * This is the preferred typed method for variable data.
    */
   public static TreeModel createTreeModel(Hashtable<String, Vector<Variable>> hashtable) {
      return JTree.createTreeModel(hashtable);
   }

   /**
    * Builds a {@link TreeModel} from the provided root object.
    * This method is deprecated because it accepts Object, which is not type-safe.
    * Use the typed {@link #createTreeModel(Hashtable<String, Vector<Variable>>)} createTreeModel method instead when possible.
    * 
    * @param object the root object (should be a Hashtable or compatible type)
    * @return a TreeModel instance
    * @deprecated Use createTreeModel(Hashtable) instead when possible.
    */
   @Deprecated
   @SuppressWarnings("unused")
   public static TreeModel createTreeModel(Object object) {
      if (object == null) {
         throw new IllegalArgumentException("Object cannot be null");
      }
      return JTree.createTreeModel(object);
   }

   /**
    * Returns an empty model that can be reused when no file is selected.
    */
   public static TreeModel getEmptyModel() {
      return emptyModel;
   }
}

