package com.github.xepozz.fs_info

import com.github.xepozz.fs_info.files.FileNodeDescriptor
import com.github.xepozz.fs_info.files.FileSystemStructureCollector
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.guessProjectDir
import com.intellij.openapi.vfs.VirtualFile

@Service(value = [Service.Level.PROJECT])
class FileSystemService(val project: Project) {
    private var collector = FileSystemStructureCollector()
    private val settings by lazy { project.getService(FsInfoSettings::class.java) }

    fun refresh() {
        if (!settings.enabled) return

        val projectDirectory = project.guessProjectDir() ?: return
        val projectPath = projectDirectory.toNioPath()

        collector.refresh(projectPath)
    }

    fun refresh(virtualFiles: Collection<VirtualFile>) {
        if (!settings.enabled) return

        virtualFiles.forEach { collector.removeNode(it.toNioPath()) }

        val projectDirectory = project.guessProjectDir() ?: return
        val projectPath = projectDirectory.toNioPath()

        collector.refresh(projectPath)
    }

    fun findDescriptor(virtualFile: VirtualFile): FileNodeDescriptor? {
        return collector.getNode(virtualFile.toNioPath())
    }
}