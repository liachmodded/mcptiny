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
package com.github.liachmodded.mcptiny.serde;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.LocalVariableDef;
import net.fabricmc.mapping.tree.Mapped;
import net.fabricmc.mapping.tree.MethodDef;
import net.fabricmc.mapping.tree.ParameterDef;
import net.fabricmc.mapping.tree.TinyTree;
import org.checkerframework.checker.nullness.qual.Nullable;

public final class TinyPrinter {

  private TinyPrinter() {}

  public static void print(PrintWriter out, TinyTree tree) {
    final List<String> namespaces = tree.getMetadata().getNamespaces();
    print(out, tree, namespaces);
  }

  public static void print(PrintWriter out, TinyTree tree, List<String> namespaces) {
    final String defaultNamespace = namespaces.get(0);
    out.printf("tiny\t2\t0\t%s\n", String.join("\t", namespaces));
    for (ClassDef clazz : tree.getClasses()) {
      out.printf("c\t%s\n", collectNames(namespaces, clazz));
      printComment(out, clazz, 1);
      for (FieldDef field : clazz.getFields()) {
        out.printf("\tf\t%1$s\t%2$s\n", field.getDescriptor(defaultNamespace), collectNames(namespaces, field));
        printComment(out, field, 2);
      }
      for (MethodDef method : clazz.getMethods()) {
        out.printf("\tm\t%1$s\t%2$s\n", method.getDescriptor(defaultNamespace), collectNames(namespaces, method));
        printComment(out, method, 2);
        for (ParameterDef parameter : method.getParameters()) {
          out.printf("\t\tp\t%1$d\t%2$s\n", parameter.getLocalVariableIndex(), collectNames(namespaces, parameter));
          printComment(out, parameter, 3);
        }
        for (LocalVariableDef parameter : method.getLocalVariables()) {
          out.printf("\t\tv\t%1$d\t%2$d\t%3$d\t%4$s\n", parameter.getLocalVariableIndex(), parameter.getLocalVariableStartOffset(),
              parameter.getLocalVariableTableIndex(), collectNames(namespaces, parameter));
          printComment(out, parameter, 3);
        }
      }
    }
  }

  private static String collectNames(List<String> namespaces, Mapped mapped) {
    List<String> names = new ArrayList<>(namespaces.size());
    for (String namespace : namespaces) {
      names.add(mapped.getName(namespace));
    }
    return String.join("\t", names);
  }

  private static void printComment(PrintWriter stream, Mapped mapped, int indent) {
    @Nullable String comment = mapped.getComment();
    if (comment == null) {
      return;
    }
    StringBuilder sb = new StringBuilder(comment.length() + indent + 2);
    for (int i = 0; i < indent; i++) {
      sb.append('\t');
    }
    sb.append('c').append('\t');
    sb.append(escapeComment(comment));
    stream.println(sb);
  }

  private static String escapeComment(String old) {
    StringBuilder sb = new StringBuilder(old.length());
    for (int i = 0; i < old.length(); i++) {
      char c = old.charAt(i);
      int t = TO_ESCAPE.indexOf(c);
      if (t == -1) {
        sb.append(c);
      } else {
        sb.append('\\').append(ESCAPED.charAt(t));
      }
    }
    return sb.toString();
  }

  private static final String TO_ESCAPE = "\\\n\r\0\t";
  private static final String ESCAPED = "\\nr0t";

}
