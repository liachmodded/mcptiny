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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.util.ClassMapper;

public final class McpClass extends McpMapped implements ClassDef {

  private final Collection<McpField> mcpFields = new ArrayList<>();
  private final Collection<McpMethod> mcpMethods = new ArrayList<>();
  
  private final Map<String, String> obfToIntMap;

  public McpClass(String obf, String srg, Map<String, String> obfToIntMap) {
    super(obf, srg);
    this.obfToIntMap = obfToIntMap;
  }

  @Override
  public void setIntermediary(String intermediary) {
    super.setIntermediary(intermediary);
    this.obfToIntMap.put(getObf(), intermediary);
  }

  public Collection<McpField> getMcpFields() {
    return mcpFields;
  }

  public Collection<McpMethod> getMcpMethods() {
    return mcpMethods;
  }

  @Override
  public Collection<MethodDef> getMethods() {
    return (Collection<MethodDef>) (Collection<?>) mcpMethods;
  }

  @Override
  public Collection<FieldDef> getFields() {
    return (Collection<FieldDef>) (Collection<?>) mcpFields;
  }
}
