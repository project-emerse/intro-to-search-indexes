package org.emerse.gradle;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.internal.jvm.Jvm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public class AllJavadoc extends Exec
{

	private Path overviewHtml;
	private Path overviewMd;

	@Override
	protected void exec()
	{
		try
		{
			configureExec();
			renderOverviewMdAsHtml();
			super.exec();
			deleteOverviewHtml();
		}
		catch (Exception e)
		{
			if (e instanceof RuntimeException re)
			{
				throw re;
			}
			throw new RuntimeException(e);
		}
	}

	private void deleteOverviewHtml()
	{
		try
		{
			Files.deleteIfExists(overviewHtml);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void renderOverviewMdAsHtml()
	{
		var parser = Parser.builder().build();
		var renderer = HtmlRenderer.builder().build();
		try (
			var reader = Files.newBufferedReader(overviewMd);
			var writer = Files.newBufferedWriter(overviewHtml)
		)
		{
			var node = parser.parseReader(reader);
			writer.write("<html><body>\n");
			renderer.render(node, writer);
			writer.write("</body></html>");
			writer.flush();
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	private void configureExec() throws Exception
	{
		var projects = getProject();
		var projDir = getProject().getProjectDir().toPath();
		overviewMd = projDir.resolve("overview.md");
		overviewHtml = projDir.resolve("overview.html");

		var
			files =
			getProject()
				.getExtensions()
				.getByType(SourceSetContainer.class)
				.getAt("main")
				.getAllJava()
				.getAsFileTree()
				.getFiles()
				.stream()
				.map(File::getAbsolutePath)
				.toList();
		var optionsFile = getProject().getBuildDir().toPath().resolve("javadoc.opts").toFile();
		optionsFile.getParentFile().mkdirs();
		Files.writeString(
			optionsFile.toPath(),
			String.join("\n", files),
			StandardOpenOption.TRUNCATE_EXISTING,
			StandardOpenOption.WRITE,
			StandardOpenOption.CREATE
		);

		var args = Arrays.asList(
			"-d",
			getProject().getBuildDir().toPath().resolve("docs").resolve("javadoc").toString(),
			"-package",
			"-overview",
			overviewHtml.toString(),
			"-doctitle",
			"Index Example",
			"-quiet",
			"-Xdoclint:all,-missing",
			"--allow-script-in-comments",
			"@" + optionsFile
		);
		var javadoc = Jvm.current().getJavadocExecutable().toString();
		System.out.println(javadoc + " " + args);
		setArgs(args);
		setExecutable(javadoc);
	}
}
