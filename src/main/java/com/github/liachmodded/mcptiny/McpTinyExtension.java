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
package com.github.liachmodded.mcptiny;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;

public class McpTinyExtension {

  private final Project project;
  private final McpTiny plugin;

  public McpTinyExtension(Project project, McpTiny plugin) {
    this.project = project;
    this.plugin = plugin;
  }

  /**
   * Creates a dependency on a specified MCP mapping.
   *
   * @param mcVersion the target Minecraft version
   * @param mcpVersion the target MCP snapshot version
   * @return the created dependency
   */
  public Dependency mcp(String mcVersion, String mcpVersion) {
    return plugin.makeMapping(project, mcVersion, mcpVersion);
  }
//    http://export.mcpbot.bspk.rs/mcp_snapshot_nodoc/20191108-1.14.3/mcp_snapshot_nodoc-20191108-1.14.3.zip
//    http://export.mcpbot.bspk.rs/mcp_snapshot/20191108-1.14.3/mcp_snapshot-20191108-1.14.3.zip
//
//    http://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_snapshot/20191108-1.14.3/mcp_snapshot-20191108-1.14.3.zip
//    https://files.minecraftforge.net/maven/de/oceanlabs/mcp/mcp_config/1.14.4-20190829.143755/mcp_config-1.14.4-20190829.143755.zip

}
