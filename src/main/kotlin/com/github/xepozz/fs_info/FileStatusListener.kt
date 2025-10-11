package com.github.xepozz.fs_info

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FileStatusListener(
    project: Project,
    private val coroutineScope: CoroutineScope,
) : BulkFileListener {
    val fileSystemService: FileSystemService = project.getService(FileSystemService::class.java)
    var job: Job? = null

    override fun after(events: List<VFileEvent>) {
        val files = events.mapNotNull { it.file }

        job?.cancel()
        job = coroutineScope.launch(Dispatchers.IO) {
            fileSystemService.refresh(files)
        }
    }
}