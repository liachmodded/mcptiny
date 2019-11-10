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

import com.github.liachmodded.mcptiny.model.McpTree;
import com.github.liachmodded.mcptiny.serde.IntermediaryWorker;
import com.github.liachmodded.mcptiny.serde.TinyPrinter;
import com.github.liachmodded.mcptiny.serde.TsrgLoader;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ModuleIdentifier;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.file.FileTree;
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.internal.component.external.model.DefaultModuleComponentIdentifier;

public class McpTiny implements Plugin<Project> {

  @Override
  public void apply(Project target) {
    target.getExtensions().create("mcptiny", McpTinyExtension.class, target, this);
  }

  Dependency makeDependency(Project project, String mcVersion, String mcpVersion) {
    RepositoryHandler repositories = project.getRepositories();
    repositories.maven(repo -> {
      repo.setName("forge");
      repo.setUrl(project.uri("https://files.minecraftforge.net/maven/"));
    });
    repositories.maven(repo -> {
      repo.setName("fabricintermediary");
      repo.setUrl(project.uri("https://maven.fabricmc.net"));
    });

    String srgNotation = String.format("de.oceanlabs.mcp:mcp_config:%s-+@zip", mcVersion);
    String mcpNotation = String.format("de.oceanlabs.mcp:mcp_snapshot:%s@zip", mcpVersion);
    String intNotation = String.format("net.fabricmc:intermediary:%s:v2", mcVersion);

    DependencyHandler dependencies = project.getDependencies();
    Dependency srgDep = dependencies.create(srgNotation);
    Dependency mcpDep = dependencies.create(mcpNotation);
    Dependency intDep = dependencies.create(intNotation);

    ConfigurationContainer configurations = project.getConfigurations();
    Configuration srgConfig = configurations.detachedConfiguration(srgDep);
    Configuration mcpConfig = configurations.detachedConfiguration(mcpDep);
    Configuration intConfig = configurations.detachedConfiguration(intDep);

    File srgZip = srgConfig.getSingleFile();
    File mcpZip = mcpConfig.getSingleFile();
    File intJar = intConfig.getSingleFile();

    // handle srg and mcp zips!
    McpTree tree = handleSrgZip(project, srgZip);
    applyIntermediary(project, tree, intJar);
    handleMcpZip(project, tree, mcpZip);

    File resultTinyJar = new File(project.getBuildDir(), "mcp-mappings-tiny-v2.jar");
    packTiny(project, tree, resultTinyJar);

    System.err.printf("Tiny jar built, ready at \"%s\".", resultTinyJar);

    ModuleIdentifier moduleIdentifier = DefaultModuleIdentifier.newId("de.oceanlabs.mcp", "mcp_snapshot");
    return new DefaultSelfResolvingDependency(new DefaultModuleComponentIdentifier(moduleIdentifier, mcpVersion),
        (FileCollectionInternal) project.files(resultTinyJar));
  }

  private McpTree handleSrgZip(Project project, File srgZip) {
    FileTree fileTree = project.zipTree(srgZip);

    McpTree mcpTree = new McpTree();

    File tsrg = fileTree.matching(patternFilterable -> {
      patternFilterable.include("config/joined.tsrg");
    }).getSingleFile();
    TsrgLoader.loadTsrg(mcpTree, tsrg);

    File constructorsTxt = fileTree.matching(patternFilterable -> {
      patternFilterable.include("config/constructors.txt");
    }).getSingleFile();
    TsrgLoader.loadConstructorsSrg(mcpTree, constructorsTxt);

    File staticMethods = fileTree.matching(patternFilterable -> {
      patternFilterable.include("config/static_methods.txt");
    }).getSingleFile();
    TsrgLoader.loadStaticMethods(mcpTree, staticMethods);

    return mcpTree;
  }

  private void applyIntermediary(Project project, McpTree tree, File intJar) {
    FileTree fileTree = project.zipTree(intJar);

    File mappingsTiny = fileTree.matching(patternFilterable -> {
      patternFilterable.include("mappings/mappings.tiny");
    }).getSingleFile();

    IntermediaryWorker.addIntermediaryAndFixFieldDesc(tree, mappingsTiny);
  }

  private void handleMcpZip(Project project, McpTree tree, File srgZip) {
    FileTree fileTree = project.zipTree(srgZip);

    File methodsCsv = fileTree.matching(patternFilterable -> {
      patternFilterable.include("methods.csv");
    }).getSingleFile();
    TsrgLoader.loadMethodsMcp(tree, methodsCsv);

    File fieldsCsv = fileTree.matching(patternFilterable -> {
      patternFilterable.include("fields.csv");
    }).getSingleFile();
    TsrgLoader.loadFieldsMcp(tree, fieldsCsv);

    File paramsCsv = fileTree.matching(patternFilterable -> {
      patternFilterable.include("params.csv");
    }).getSingleFile();
    TsrgLoader.loadParamsMcp(tree, paramsCsv);
  }

  private void packTiny(Project project, McpTree tree, File target) {
    File dir = new File(project.getBuildDir(), "tmp");
    dir.mkdirs();
    File tmpFile = new File(dir, "mcp-tmp.tiny");
    List<String> namespaces = Collections.unmodifiableList(Arrays.asList("official", "intermediary", "named"));
    try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(tmpFile.toPath()))) {
      TinyPrinter.print(writer, tree, namespaces);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }

    try (ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)))) {
      zipOutputStream.putNextEntry(new ZipEntry("mappings/mappings.tiny"));
      zipOutputStream.write(Files.readAllBytes(tmpFile.toPath()));
      zipOutputStream.closeEntry();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }
}
