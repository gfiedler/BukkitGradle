package ru.endlesscode.bukkitgradle

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile

class BukkitGradlePlugin implements Plugin<Project> {
    final static String GROUP = 'Bukkit'

    Project project

    static boolean isTesting() {
        System.properties['test'] == 'true'
    }

    @Override
    void apply(Project project) {
        this.project = project
        configureProject()
    }

    /**
     * Configures project
     */
    void configureProject() {
        addPlugins()
        configureEncoding()
        addRepositories()
        addDependencies()
    }

    /**
     * Adds all needed plugins
     */
    void addPlugins() {
        project.with {
            plugins.with {
                apply('java')
                apply('eclipse')
                apply('idea')
                apply(PluginMetaPlugin)
                apply(DevServerPlugin)
            }

            convention.getPlugin(JavaPluginConvention).with {
                sourceCompatibility = targetCompatibility = JavaVersion.VERSION_1_8
            }
        }
    }

    /**
     * Sets force encoding on compile to UTF-8
     */
    void configureEncoding() {
        project.tasks.withType(JavaCompile) {
            options.encoding = 'UTF-8'
        }
    }

    /**
     * Adds needed repositories
     */
    void addRepositories() {
        project.with {
            repositories {
                mavenLocal()
                mavenCentral()

                maven {
                    name = 'sk89q'
                    url = 'http://maven.sk89q.com/repo/'
                }

                maven {
                    name = 'spigot'
                    url = 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/'
                }
            }
        }
    }

    /**
     * Adds needed dependencies
     */
    void addDependencies() {
        project.gradle.addListener(new DependencyResolutionListener() {
            @Override
            void beforeResolve(ResolvableDependencies resolvableDependencies) {
                addBukkitApi(project)
                project.gradle.removeListener(this)
            }

            @Override
            void afterResolve(ResolvableDependencies resolvableDependencies) {}
        })
    }

    /**
     * Adds Bukkit API to project dependencies
     * @param project The project
     */
    static void addBukkitApi(Project project) {
        project.with {
            def compileOnlyDeps = configurations.compileOnly.dependencies
            def testCompileDeps = configurations.testCompile.dependencies
            def bukkitDep = dependencies.create("org.bukkit:bukkit:$bukkit.dependencyVersion")

            compileOnlyDeps.add(bukkitDep)
            testCompileDeps.add(bukkitDep)
        }
    }
}
