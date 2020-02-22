import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.exec
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2019.2"

project {
    buildType {
        name = "Build Backend"
        id = RelativeId("BuildBackend")

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            gradle {
                tasks = "build"
                workingDir = "backend"
            }
        }
    }

    buildType {
        name = "Build Frontend"
        id = RelativeId("BuildFrontend")

        vcs {
            root(DslContext.settingsRoot)
        }

        steps {
            script {
                workingDir = "frontend"
                scriptContent = "npm install run build"
                dockerImage = "node"
            }
        }
    }
}
























