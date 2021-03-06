package ru.endlesscode.bukkitgradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import ru.endlesscode.bukkitgradle.meta.MetaFile

import java.nio.file.Files
import java.nio.file.Path

class TestBase {
    protected Project project

    @Before
    void setUp() throws Exception {
        System.properties.setProperty('test', 'true')

        this.project = ProjectBuilder.builder()
                .withName("TestProject")
                .withProjectDir(new File("build/testProject"))
                .build()
        this.project.with {
            apply plugin: BukkitGradlePlugin

            group = "com.example.plugin"
            description = "Test project description"
            version = "1.0"
            ext.url = "https://www.example.ru/"
        }
    }

    protected void initBukkitMeta() {
        this.project.bukkit.meta {
            name = "TestPlugin"
            description = "Test plugin description"
            version = "0.1"
            main = "com.example.plugin.Plugin"
            authors = ["OsipXD", "Contributors"]
            url = "http://www.example.com/"
        }
    }

    protected Path createDefaultMetaFile() {
        Path metaDir = this.project.buildDir.toPath().resolve("meta/")
        Files.createDirectories(metaDir)
        Path metaFile = metaDir.resolve(MetaFile.NAME)
        Files.deleteIfExists(metaFile)
        Files.createFile(metaFile)

        metaFile << '''name: TestPlugin
description: Test plugin description
version: 0.1

main: com.example.plugin.Plugin
author: OsipXD
website: www.example.com

depend: [Vault, ProtocolLib]
command:
  example'''
    }

    protected void executeTask(Task task) {
        task.taskDependencies.getDependencies(task).each {
            subTask -> executeTask(subTask)
        }

        task.execute()
    }

    protected void configureRun() {
        this.project.bukkit.run.with {
            eula = true
            onlineMode = true
            debug = false
            encoding = 'CP866'
            javaArgs = '-Xmx2G'
            bukkitArgs = '-s 2'
        }
    }
}
