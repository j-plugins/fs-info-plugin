package com.github.xepozz.fs_info.files

import java.lang.ref.WeakReference

data class FileNodeDescriptor(
    var size: ULong = 0u,
    var lines: Int = 0,
    val isDirectory: Boolean,
    val children: MutableMap<String, WeakReference<FileNodeDescriptor>> = mutableMapOf(),
) {
    val childrenSize: Int
        get() = children
            .values
            .mapNotNull { it.get() }
            .sumOf { if (it.isDirectory) it.childrenSize else 1 }

    val fileCount: Int
        get() = if (isDirectory) {
            children
                .values
                .mapNotNull { it.get() }
                .sumOf { if (it.isDirectory) it.fileCount else 1 }
        } else 1

    val directorySize: ULong
        get() = if (isDirectory) {
            children
                .values
                .mapNotNull { it.get() }
                .sumOf { if (it.isDirectory) it.directorySize else it.size }
        } else 1.toULong()
}
