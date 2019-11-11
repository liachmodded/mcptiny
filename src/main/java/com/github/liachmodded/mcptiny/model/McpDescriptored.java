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

import java.util.Objects;
import net.fabricmc.mapping.tree.Descriptored;
import net.fabricmc.mapping.util.ClassMapper;

abstract class McpDescriptored extends McpMapped implements Descriptored {

  private String obfDesc;
  private final ClassMapper intMapper;

  McpDescriptored(String obf, String srg, String obfDesc, ClassMapper intMapper) {
    super(obf, srg);
    this.obfDesc = obfDesc;
    this.intMapper = intMapper;
  }

  public void setObfDesc(String obfDesc) {
    this.obfDesc = obfDesc;
  }

  @Override
  public String getDescriptor(String s) {
    if (Objects.equals("official", s)) {
      return obfDesc;
    }
    if (Objects.equals("intermediary", s)) {
      return intMapper.mapDescriptor(obfDesc);
    }
    throw new UnsupportedOperationException("Unsupported namespace \"" + s + "\" for descriptor");
  }
}
