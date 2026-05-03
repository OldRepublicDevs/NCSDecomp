// Copyright 2021-2025 DeNCS
// Licensed under the MIT License. See LICENSE in the project root for full license text.

package com.kotor.resource.formats.ncs.scriptnode;

import com.kotor.resource.formats.ncs.stack.StackEntry;

public interface AExpression {
   @Override
   String toString();

   ScriptNode parent();

   void parent(ScriptNode var1);

   StackEntry stackentry();

   void stackentry(StackEntry var1);
}

