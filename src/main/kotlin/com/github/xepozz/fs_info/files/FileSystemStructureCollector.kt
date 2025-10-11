package com.github.xepozz.fs_info.files

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.fileVisitor
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.visitFileTree


class FileSystemStructureCollector() {
    private val nodeMap = mutableMapOf<Path, FileNodeDescriptor>()

    fun removeNode(path: Path) {
        nodeMap.remove(path)
    }

    fun removeParent(path: Path) {
        nodeMap.forEach { (key, value) ->
            if (value.parent?.path == path) {
                removeNode(key)
                removeParent(key)
            }
        }
    }

    fun getNode(path: Path): FileNodeDescriptor? = nodeMap[path]

    fun refresh(project: Project) {
        val projectDirectory = project.guessProjectDir() ?: return
        val projectPath = projectDirectory.toNioPath()

        nodeMap[projectPath] = FileNodeDescriptor(
            path = projectPath,
            isDirectory = true
        )
        projectPath.visitFileTree(structureVisitor)
    }

    val structureVisitor = fileVisitor {
        onPreVisitDirectory { directory, attributes ->
            if (nodeMap.containsKey(directory)) return@onPreVisitDirectory FileVisitResult.CONTINUE

            val parentPath = directory.parent
            val parentNode = nodeMap[parentPath]

            val dirNode = FileNodeDescriptor(
                path = directory,
                isDirectory = true,
                parent = parentNode,
            )

            nodeMap[directory] = dirNode
            parentNode?.children?.put(directory.name, dirNode)

            FileVisitResult.CONTINUE
        }

        onVisitFile { file, attributes ->
            if (nodeMap.containsKey(file)) return@onVisitFile FileVisitResult.CONTINUE

            val parentPath = file.parent
            val parentNode = nodeMap[parentPath]

            val fileSize = attributes.size().toULong()
            val lines = when {
                fileSize > FileSizeType.MEGABYTES.bytes * 10.toULong() -> 0
                !attributes.isRegularFile -> 0
                else -> countLines(file)
            }

            val fileNode = FileNodeDescriptor(
                path = file,
                size = fileSize,
                lines = lines,
                isDirectory = false,
                parent = parentNode
            )

            nodeMap[file] = fileNode
            parentNode?.children?.put(file.name, fileNode)

            FileVisitResult.CONTINUE
        }

        onVisitFileFailed { file, exception ->
            println("Failed to visit: $file, reason: $exception")
            FileVisitResult.CONTINUE
        }
    }

    fun countLines(path: Path): Int {
        var lines = 1

        val buffer = ByteArray(8 * FileSizeType.KILOBYTES.bytes.toInt())
        path.inputStream().apply {
            while ((read(buffer)) != -1) {
                lines += buffer.count { it == '\n'.code.toByte() }
            }

            close()
        }
        return lines
    }
}