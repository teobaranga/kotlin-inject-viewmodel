pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

plugins {
    id("org.jetbrains.kotlinx.kover.aggregation") version "0.9.4"
}

kover {
    enableCoverage()

    reports {
        excludedClasses.addAll(
            "amazon.lastmile.inject.*",
            "*ComponentMerged*",
            "*_Impl*",
            "*ViewModelComponent*",
            "*ViewModelFactory*",
            "*ComposableSingletons*",
        )
        excludedProjects.add(":app")
        excludesAnnotatedBy.addAll(
            "androidx.compose.ui.tooling.preview.Preview",
        )
    }
}

rootProject.name = "Kotlin Inject ViewModel"
include(":app")
include(":compiler")
include(":runtime", ":runtime-compose")
include(":androidApp")
