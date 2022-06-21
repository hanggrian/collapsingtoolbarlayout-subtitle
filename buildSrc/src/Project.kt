import org.gradle.api.JavaVersion
import org.gradle.api.Task
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.kotlin.dsl.findByType

fun ModuleDependency.features(vararg capabilityModules: Any) =
    capabilities { capabilityModules.forEach { requireCapability("$group:$it") } }

inline fun <reified T : Any> ExtensionContainer.find() = findByType<T>()
inline fun <reified T : Any> ExtensionContainer.find(action: T.() -> Unit) = find<T>()?.apply(action)
inline fun <reified T : Any> ExtensionContainer.find(name: String) = findByName(name) as T?
inline fun <reified T : Any> ExtensionContainer.find(name: String, action: T.() -> Unit) = find<T>(name)?.apply(action)

inline fun <T : Any> TaskContainer.find(name: String) = findByName(name) as T?
inline fun <T : Task> TaskContainer.find(name: String, action: T.() -> Unit) = find<T>(name)?.apply(action)

inline fun Provider<String>.getInt() = get().toInt()
inline fun Provider<String>.getJavaVersion() = JavaVersion.toVersion(getInt())
