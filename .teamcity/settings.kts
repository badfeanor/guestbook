import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.dockerSupport
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.*

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
    buildType(BuildBackend)

    buildType(BuildFrontend)

    buildType(DockerBackend)

    buildType(DockerFrontend)

    buildType(Test)
}

object BuildBackend : BuildType({
    name = "Build Backend"

    artifactRules = "backend/build/libs/*.jar"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "build"
            workingDir = "backend"
        }
    }
})

object DockerBackend : BuildType({
    name = "Docker Backend"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            commandType = build {
                commandArgs = "--build-arg JAR_FILE=backend/build/libs/*.jar"
                source = file {
                    path = "backend/Dockerfile"
                }
                namesAndTags = "734426463323.dkr.ecr.eu-west-1.amazonaws.com/guestbook:backend-%build.number%"
            }
        }
        dockerCommand {
            commandType = push {
                namesAndTags = "734426463323.dkr.ecr.eu-west-1.amazonaws.com/guestbook:backend-%build.number%"
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_174"
            }
        }
    }

    dependencies {
        dependency(RelativeId("BuildBackend")) {
            snapshot {}
            artifacts {
                artifactRules = "*.jar => backend/build/libs/"
            }
        }
    }
})

object BuildFrontend : BuildType({
    name = "Build Frontend"

    artifactRules = "frontend/docker/dist/ => dist.zip"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            workingDir = "frontend"
            scriptContent = """
npm install
npm run-script build
""".trimIndent()
            dockerImage = "node"
        }
    }
})

object DockerFrontend : BuildType({
    name = "Docker Frontend"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCommand {
            commandType = build {
                commandArgs = "--build-arg JAR_FILE=backend/build/libs/*.jar"
                source = file {
                    path = "frontend/docker/Dockerfile"
                }
                namesAndTags = "734426463323.dkr.ecr.eu-west-1.amazonaws.com/guestbook:frontend-%build.number%"
            }
        }
        dockerCommand {
            commandType = push {
                namesAndTags = "734426463323.dkr.ecr.eu-west-1.amazonaws.com/guestbook:frontend-%build.number%"
            }
        }
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_174"
            }
        }
    }

    dependencies {
        dependency(RelativeId("BuildFrontend")) {
            snapshot {}
            artifacts {
                artifactRules = "dist.zip!** => frontend/docker/dist/"
            }
        }
    }
})

object Test : BuildType({
    name = "Test"

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        dockerCompose {
            file = "systemTests/docker-compose.yml"
        }
        exec {
            path = "systemTests/systemTests.sh"
        }
    }

    params {
        param("env.REPOSITORY_URL", "734426463323.dkr.ecr.eu-west-1.amazonaws.com/")
        param("env.FRONTEND_IMAGE_VERSION", "guestbook:frontend-%dep.${RelativeId("DockerFrontend")}.build.number%")
        param("env.BACKEND_IMAGE_VERSION", "guestbook:backend-%dep.${RelativeId("DockerBackend")}.build.number%")
    }

    features {
        dockerSupport {
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_174"
            }
        }
    }

    dependencies {
        snapshot(RelativeId("DockerBackend")) {}
        snapshot(RelativeId("DockerFrontend")) {}
    }
})


















