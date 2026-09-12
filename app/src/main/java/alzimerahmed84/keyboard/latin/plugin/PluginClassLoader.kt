// SPDX-License-Identifier: GPL-3.0-only
package alzimerahmed84.keyboard.latin.plugin

import dalvik.system.DexClassLoader

/**
 * DexClassLoader shared by all plugin loaders. Classes whose package matches
 * [childFirstPrefixes] are resolved child-first so plugin-bundled dependencies
 * (ML Kit, Play services) never collide with host classes.
 */
open class PluginClassLoader(
    dexPath: String,
    optimizedDirectory: String?,
    private val librarySearchPath: String?,
    parent: ClassLoader,
    private val childFirstPrefixes: List<String> = emptyList()
) : DexClassLoader(dexPath, optimizedDirectory, librarySearchPath, parent) {

    override fun findLibrary(name: String): String? {
        if (librarySearchPath != null) {
            val filename = System.mapLibraryName(name)
            val file = java.io.File(librarySearchPath, filename)
            if (file.exists()) {
                return file.absolutePath
            }
        }
        return super.findLibrary(name)
    }

    @Suppress("ReturnCount")
    override fun loadClass(name: String, resolve: Boolean): Class<*> {
        if (childFirstPrefixes.any { name.startsWith(it) }) {
            val loaded = findLoadedClass(name)
            if (loaded != null) return loaded
            try {
                return findClass(name)
            } catch (_: ClassNotFoundException) {
                // fallback to parent
            }
        }
        return super.loadClass(name, resolve)
    }
}
