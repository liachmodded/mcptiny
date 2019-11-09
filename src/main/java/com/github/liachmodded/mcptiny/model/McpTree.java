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
import java.util.Map;
import java.util.TreeMap;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;

public final class McpTree implements TinyTree {

  private static final TinyMetadata METADATA = new Metadata();
  private final Map<String, McpClass> obfMap = new TreeMap<>();
  private final Map<String, McpClass> srgMap = new TreeMap<>();

  private final Map<String, McpField> fieldMap = new TreeMap<>(); // 123456 -> xx
  private final Map<String, McpMethod> methodMap = new TreeMap<>(); // 123456 -> xx; i2345 -> xx

  public McpClass makeClass(String obf, String srg) {
    McpClass created = new McpClass(obf, srg);
    obfMap.put(obf, created);
    srgMap.put(srg, created);
    return created;
  }

  public McpField makeField(McpClass parent, String obf, String srg, String desc) {
    McpField created = new McpField(obf, srg, desc);
    String[] parts = srg.split("_");
    fieldMap.put(parts[1], created);
    parent.getMcpFields().add(created);
    return created;
  }

  public McpMethod makeMethod(McpClass parent, String obf, String srg, String desc) {
    McpMethod created = new McpMethod(obf, srg, desc);
    String[] parts = srg.split("_");
    methodMap.put(parts[1], created);
    parent.getMcpMethods().add(created);
    return created;
  }
  
  public McpMethod makeConstructor(String index, String owner, String desc) {
    McpClass parent = srgMap.get(owner);
    if (parent == null)
      throw new IllegalArgumentException("Nonexistent class " + owner);
    
    McpMethod created = new McpMethod("<init>", "<init>", desc);
    methodMap.put("i" + index, created);
    parent.getMcpMethods().add(created);
    return created;
  }

  public McpField findField(String srg) {
    String[] parts = srg.split("_");
    McpField ret = fieldMap.get(parts[1]);
    if (ret == null) {
      throw new IllegalArgumentException("field " + srg + " does not exist");
    }
    return ret;
  }

  public McpMethod findMethod(String srg) {
    String[] parts = srg.split("_");
    McpMethod ret = methodMap.get(parts[1]);
    if (ret == null) {
      throw new IllegalArgumentException("method " + srg + " does not exist");
    }
    return ret;
  }

  private McpMethod findMethodInternal(String key) {
    McpMethod ret = methodMap.get(key);
    if (ret == null) {
      throw new IllegalArgumentException("method with id " + key + " does not exist");
    }
    return ret;
  }

  public McpParam makeParam(String srg) {
    String[] parts = srg.split("_");
    // 0: p; 1: method id; 2: index
    McpMethod method = findMethodInternal(parts[1]);
    int index = Integer.parseInt(parts[2]);

    McpParam created = new McpParam(index, srg);
    method.getMcpParams().put(index, created);
    return created;
  }

  public Map<String, McpClass> getObfMap() {
    return obfMap;
  }

  public Map<String, McpClass> getSrgMap() {
    return srgMap;
  }

  @Override
  public TinyMetadata getMetadata() {
    return METADATA;
  }

  @Override
  public Map<String, ClassDef> getDefaultNamespaceClassMap() {
    return (Map<String, ClassDef>) (Map<String, ?>) obfMap;
  }

  @Override
  public Collection<ClassDef> getClasses() {
    return (Collection<ClassDef>) (Collection<?>) obfMap.values();
  }
}

