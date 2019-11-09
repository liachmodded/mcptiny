/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org>
 */
package com.github.liachmodded.mcptiny.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import net.fabricmc.mapping.tree.LocalVariableDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.ParameterDef;

public final class McpMethod extends McpDescriptored implements MethodDef {

  private final NavigableMap<Integer, McpParam> mcpParams = new TreeMap<>();
  private boolean staticModifier = false;

  public McpMethod(String obf, String srg, String obfDesc) {
    super(obf, srg, obfDesc);
  }

  public NavigableMap<Integer, McpParam> getMcpParams() {
    return mcpParams;
  }

  public boolean hasStaticModifier() {
    return staticModifier;
  }

  public void setStaticModifier(boolean staticModifier) {
    this.staticModifier = staticModifier;
  }

  @Override
  public Collection<ParameterDef> getParameters() {
    return (Collection<ParameterDef>) (Collection<?>) mcpParams.values();
  }

  @Override
  public Collection<LocalVariableDef> getLocalVariables() {
    return Collections.emptyList();
  }
}
