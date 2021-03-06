package ru.endlesscode.bukkitgradle.extension

import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import ru.endlesscode.bukkitgradle.server.CoreType
import ru.endlesscode.bukkitgradle.server.ServerCore
import ru.endlesscode.bukkitgradle.task.PrepareServer

import java.nio.file.Files
import java.nio.file.Path

class RunConfiguration {
    private static
    final String DEBUG_ARGS = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

    private Project project

    boolean eula
    boolean onlineMode
    boolean debug
    String encoding
    String javaArgs
    String bukkitArgs

    private CoreType coreType

    RunConfiguration(Project project) {
        this.project = project

        this.coreType = CoreType.SPIGOT

        this.eula = false
        this.onlineMode = false
        this.debug = true
        this.encoding = 'UTF-8'

        this.javaArgs = '-Xmx1G'
        this.bukkitArgs = ''
    }

    void setCore(String core) {
        try {
            coreType = CoreType.valueOf(core.toUpperCase())
        } catch (IllegalArgumentException ignored) {
            project.logger.warn("Core type '$core' not found. May be it doesn't supported by BukkitGradle yet. " +
                    "You may write issue on GitHub to request supporting.\n" +
                    "Fallback core type is '${coreType.toString().toLowerCase()}'")
        }
    }

    def getCoreType() {
        return coreType
    }

    /**
     * Returns arguments for java
     *
     * @return Java arguments
     */
    String getJavaArgs() {
        return "${this.debug ? "$DEBUG_ARGS " : ''}-Dfile.encoding=$encoding ${this.javaArgs}"
    }

    /**
     * Returns arguments for bukkit
     *
     * @return Bukkit arguments
     */
    String getBukkitArgs() {
        return bukkitArgs ?: ''
    }

    /**
     * Builds and writes to file run configuration in IDEA .xml format
     *
     * @param configurationDir The configurations dir
     */
    void buildIdeaConfiguration(Path configurationDir) {
        if (Files.notExists(configurationDir)) {
            return
        }

        def taskName = 'Run Server'
        def serverDir = (project.tasks.prepareServer as PrepareServer).serverDir.toRealPath()
        def args = this.bukkitArgs

        def realDebug = this.debug
        this.debug = false
        def props = this.getJavaArgs()
        this.debug = realDebug

        def runConfiguration = configurationDir.resolve("${taskName.replace(' ', '_')}.xml")
        def xml = new MarkupBuilder(runConfiguration.newWriter())
        xml.component(name: 'ProjectRunConfigurationManager') {
            configuration(
                    default: 'false',
                    name: taskName,
                    type: 'JarApplication',
                    factoryName: 'JAR Application',
                    singleton: 'true'
            ) {
                option(name: 'JAR_PATH', value: "${serverDir.resolve(ServerCore.CORE_NAME)}")
                option(name: 'VM_PARAMETERS', value: props)
                option(name: 'PROGRAM_PARAMETERS', value: args)
                option(name: 'WORKING_DIRECTORY', value: serverDir)
                envs()
                method {
                    option(
                            name: 'Gradle.BeforeRunTask',
                            enabled: 'true',
                            tasks: 'prepareServer',
                            externalProjectPath: '$PROJECT_DIR$',
                            vmOptions: '',
                            scriptParameters: '')
                }
            }
        }
    }
}
