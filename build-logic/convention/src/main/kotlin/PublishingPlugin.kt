import com.teobaranga.kotlin.inject.viewmodel.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.findByType
import org.gradle.plugins.signing.SigningExtension

internal class PublishingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply(libs.plugins.maven.publish.get().pluginId)
            }

            extensions.findByType<SigningExtension>()?.let {
                it.useGpgCmd()
            }
        }
    }
}
