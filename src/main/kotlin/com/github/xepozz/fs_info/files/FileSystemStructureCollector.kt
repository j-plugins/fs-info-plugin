package com.github.xepozz.fs_info.files

import java.lang.ref.WeakReference
import java.nio.file.FileVisitResult
import java.nio.file.Path
import kotlin.io.path.fileVisitor
import kotlin.io.path.inputStream
import kotlin.io.path.name
import kotlin.io.path.visitFileTree


class FileSystemStructureCollector() {
    private val nodeMap = mutableMapOf<Path, FileNodeDescriptor>()

    fun removeNode(path: Path) {
        val descriptor = nodeMap.remove(path)
        descriptor?.children?.clear()
    }

    fun getNode(path: Path): FileNodeDescriptor? = nodeMap[path]

    fun refresh(projectPath: Path) {
        nodeMap[projectPath] = FileNodeDescriptor(
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
                isDirectory = true,
            )

            nodeMap[directory] = dirNode
            parentNode?.children?.put(directory.name, WeakReference(dirNode))

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
                size = fileSize,
                lines = lines,
                isDirectory = false,
            )

            nodeMap[file] = fileNode
            parentNode?.children?.put(file.name, WeakReference(fileNode))

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