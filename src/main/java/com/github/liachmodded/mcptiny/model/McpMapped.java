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
import net.fabricmc.mapping.tree.Mapped;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

abstract class McpMapped implements Mapped {

  private final String obf;
  private final String srg;
  private @MonotonicNonNull String intermediary;
  private @MonotonicNonNull String mcp;
  private @Nullable String comment;

  McpMapped(String obf, String srg) {
    this.obf = obf;
    this.srg = srg;
  }

  @Override
  public String getName(String s) {
    switch (s) {
      case "official":
        return obf;
      case "intermediary":
        return getIntermediaryChecked();
      case "searge":
        return srg;
      case "named":
        return getMcp();
    }
    throw new UnsupportedOperationException("Unknown namespace " + s);
  }

  protected String getIntermediaryChecked() {
    return Objects.requireNonNull(intermediary);
  }

  public String getObf() {
    return obf;
  }

  public String getSrg() {
    return srg;
  }

  public @Nullable String getIntermediary() {
    return intermediary;
  }

  public void setIntermediary(String intermediary) {
    this.intermediary = intermediary;
  }

  public String getMcp() {
    return mcp == null ? srg : mcp;
  }

  public void setMcp(String mcp) {
    this.mcp = mcp;
  }

  @Override
  public String getRawName(String s) {
    return getName(s);
  }

  @Override
  public @Nullable String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
