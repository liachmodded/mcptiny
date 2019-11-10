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
import com.github.liachmodded.mcptiny.model.McpMethod;
import com.github.liachmodded.mcptiny.model.McpParam;
import com.github.liachmodded.mcptiny.model.McpTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Objects;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

public final class TsrgLoader {

  public static void loadTsrg(McpTree tree, File file) {
    @MonotonicNonNull McpClass last = null;
    try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
      String line;
      while ((line = reader.readLine()) != null) {
        try {
          if (line.startsWith("\t")) {
            if (last == null) {
              throw new IllegalStateException();
            }
            String[] parts = line.substring(1).split(" ");
            switch (parts.length) {
              case 2: // field
                if (!Objects.equals(parts[0], parts[1])) {
                  tree.makeField(last, parts[0], "", parts[1]); // remapped in field desc fixer
                }
                break;
              case 3: // method
                if (!Objects.equals(parts[0], parts[2])) {
                  tree.makeOrGetMethod(last, parts[0], parts[1], parts[2]);
                }
                break;
              default:
                throw new IllegalArgumentException("bad line \"" + line + "");
            }
          } else {
            String[] parts = line.split(" ");
            if (parts.length != 2) {
              throw new IllegalArgumentException("bad line \"" + line + "");
            }
            last = tree.makeClass(parts[0], parts[1]);
          }
        } catch (Throwable ex) {
          System.err.println("Error emerged on line \"" + line + "\"!");
          throw ex;
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void loadConstructorsSrg(McpTree tree, File ctorTxt) {
    try (BufferedReader reader = Files.newBufferedReader(ctorTxt.toPath())) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(" ");
        // index owner desc
        if (parts.length != 3) {
          throw new IllegalArgumentException("bad line \"" + line + "");
        }

        try {
          tree.makeConstructor(parts[0], parts[1], parts[2]);
        } catch (IllegalArgumentException ex) {
          System.err.println(ex.getMessage());
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void loadStaticMethods(McpTree tree, File staticMethodsTxt) {
    try (BufferedReader reader = Files.newBufferedReader(staticMethodsTxt.toPath())) {
      String line;
      while ((line = reader.readLine()) != null) {
        McpMethod method = tree.findMethod(line);
        method.setStaticModifier(true);
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void loadMethodsMcp(McpTree tree, File methodsCsv) {
    // 0 srg; 1 name; 3 desc;
    try (CSVParser parser = CSVParser.parse(methodsCsv, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
      boolean firstLine = true;
      for (CSVRecord line : parser) {
        if (firstLine) {
          firstLine = false;
          continue;
        }
        try {
          McpMethod method = tree.findMethod(line.get(0));
          method.setMcp(line.get(1));
          String comment = line.get(3);
          if (comment != null && !comment.isEmpty()) {
            method.setComment(line.get(3));
          }
        } catch (IllegalArgumentException ex) {
          System.err.println(ex.getMessage());
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void loadFieldsMcp(McpTree tree, File methodsCsv) {
    // 0 srg; 1 name; 3 desc;
    try (CSVParser parser = CSVParser.parse(methodsCsv, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
      boolean firstLine = true;
      for (CSVRecord line : parser) {
        if (firstLine) {
          firstLine = false;
          continue;
        }
        try {
          McpField field = tree.findField(line.get(0));
          field.setMcp(line.get(1));
          String comment = line.get(3);
          if (comment != null && !comment.isEmpty()) {
            field.setComment(line.get(3));
          }
        } catch (IllegalArgumentException ex) {
          System.err.println(ex.getMessage());
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public static void loadParamsMcp(McpTree tree, File methodsCsv) {
    // 0 srg; 1 name;
    try (CSVParser parser = CSVParser.parse(methodsCsv, StandardCharsets.UTF_8, CSVFormat.DEFAULT)) {
      boolean firstLine = true;
      for (CSVRecord line : parser) {
        if (firstLine) {
          firstLine = false;
          continue;
        }
        try {
          McpParam param = tree.makeParam(line.get(0));
          param.setMcp(line.get(1));
        } catch (IllegalArgumentException ex) {
          System.err.println(ex.getMessage());
        }
      }
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

}
