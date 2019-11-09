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

import com.github.liachmodded.mcptiny.model.McpClass;
import com.github.liachmodded.mcptiny.model.McpField;
import com.github.liachmodded.mcptiny.model.McpTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import net.fabricmc.mapping.tree.ClassDef;
import net.fabricmc.mapping.tree.FieldDef;
import net.fabricmc.mapping.tree.TinyMappingFactory;
import net.fabricmc.mapping.tree.TinyTree;

public final class FieldDescFixer {

  public static void fixFieldDesc(McpTree mcpTree, File intV2File) {
    try (BufferedReader reader = Files.newBufferedReader(intV2File.toPath())) {
      TinyTree tree = TinyMappingFactory.load(reader);
      Map<String, ClassDef> defaultDefs = tree.getDefaultNamespaceClassMap();

      for (Entry<String, McpClass> entry : mcpTree.getObfMap().entrySet()) {
        String key = entry.getKey();
        McpClass mcpClass = entry.getValue();

        ClassDef classDef = defaultDefs.get(key);
        if (classDef == null) {
          continue;
        }

        for (McpField mcpField : mcpClass.getMcpFields()) {
          String obf = mcpField.getObf();
          boolean descUpdated = false;
          for (FieldDef fieldDef : classDef.getFields()) {
            if (Objects.equals(fieldDef.getName("official"), obf)) {
              mcpField.setObfDesc(fieldDef.getDescriptor("official"));
              descUpdated = true;
              break;
            }
          }

          if (!descUpdated) {
            throw new RuntimeException("Failed to update desc for field " + mcpField.getSrg());
          }
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private FieldDescFixer() {}
}
