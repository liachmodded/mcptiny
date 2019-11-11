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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import net.fabricmc.mapping.reader.v2.TinyMetadata;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.TinyTree;
import net.fabricmc.mapping.util.ClassMapper;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class McpTree implements TinyTree {

  private static final TinyMetadata METADATA = new Metadata();
  private final Map<String, McpClass> obfMap = new TreeMap<>();
  private final Map<String, McpClass> srgMap = new TreeMap<>();

  private final Map<String, McpField> fieldMap = new TreeMap<>(); // field_123456 -> xx; a -> RED
  private final Map<String, McpMethod> methodMap = new TreeMap<>(); // func_123456 -> xx; func_i2345 -> xx; equals -> equals

  private final Map<String, String> obfToInt = new HashMap<>();
  private final Map<String, String> srgToObf = new HashMap<>();
  private final ClassMapper intMapper = new ClassMapper(this.obfToInt);
  private final ClassMapper srgToObfMapper = new ClassMapper(this.srgToObf);

  public McpClass makeClass(String obf, String srg) {
    McpClass created = new McpClass(obf, srg, this.obfToInt);
    obfMap.put(obf, created);
    srgMap.put(srg, created);
    this.srgToObf.put(srg, obf);
    return created;
  }

  public McpField makeField(McpClass parent, String obf, String desc, String srg) {
    McpField created = new McpField(obf, srg, desc, intMapper);
    fieldMap.put(fixName(srg), created);
    parent.getMcpFields().add(created);
    return created;
  }

  public McpMethod makeOrGetMethod(McpClass parent, String obf, String desc, String srg) {
    McpMethod ret = methodMap.computeIfAbsent(fixName(srg), s -> new McpMethod(obf, srg, desc, intMapper));
    if (!parent.getMcpMethods().contains(ret)) {
      parent.getMcpMethods().add(ret);
    }
    return ret;
  }

  private McpClass getMcpClass(String notation) {
    McpClass parent = srgMap.get(notation);
    if (parent != null) {
      return parent;
    }

    @Nullable McpClass current = null;
    int t = notation.lastIndexOf('$');
    while (t != -1) {
      current = srgMap.get(notation.substring(0, t));
      if (current != null) {
        break;
      }
      t = notation.lastIndexOf('$', t - 1);
    }

    if (current == null) {
      throw new IllegalArgumentException("Nonexistent class " + notation);
    }

    do {
      int start = t;
      t = notation.indexOf('$', t + 1);
      if (t < 0) {
        t = notation.length();
      }
      McpClass created = makeClass(current.getObf() + "$" + notation.substring(start + 1, t),
          current.getSrg() + "$" + notation.substring(start + 1, t));
      current = created;
    } while (t != notation.length());

    return current;
  }

  public McpMethod makeConstructor(String index, String owner, String srgDesc) {
    McpClass parent = getMcpClass(owner);

    String desc = srgToObfMapper.mapDescriptor(srgDesc);

    @Nullable McpMethod target = null;
    for (McpMethod parentMethod : parent.getMcpMethods()) {
      if (Objects.equals(parentMethod.getSrg(), "<init>") && Objects.equals(desc, parentMethod.getDescriptor("official"))) {
        target = parentMethod;
        break;
      }
    }

    McpMethod result;
    if (target == null) {
      result = new McpMethod("<init>", "<init>", desc, intMapper);
      parent.getMcpMethods().add(result);
    } else {
      result = target;
    }
    methodMap.put("func_i" + index, result);
    return result;
  }

  public McpField findField(String srg) {
    McpField ret = fieldMap.get(fixName(srg));
    if (ret == null) {
      throw new IllegalArgumentException("field " + srg + " does not exist");
    }
    return ret;
  }

  public McpMethod findMethod(String srg) {
    McpMethod ret = methodMap.get(fixName(srg));
    if (ret == null) {
      throw new IllegalArgumentException("method " + srg + " does not exist");
    }
    return ret;
  }

  private McpMethod findMethodInternal(String index) {
    McpMethod ret = methodMap.get("func_" + index);
    if (ret == null) {
      throw new IllegalArgumentException("method with id " + index + " does not exist");
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

  private String fixName(String original) {
    String[] parts = original.split("_", 3);
    if (parts.length >= 2) {
      return parts[0] + "_" + parts[1];
    }
    return parts[0];
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

