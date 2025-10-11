package com.github.xepozz.fs_info.actions

import com.github.xepozz.fs_info.FileSystemService
import com.intellij.openapi.actionSystem.ToggleOptionAction
import com.intellij.openapi.project.DumbAware

class ToggleEnabledAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::enabled
        override fun isEnabled() = true
        override fun setSelected(selected: Boolean) {
            if (selected) {
                super.setSelected(true)
                project
                    .getService(FileSystemService::class.java)
                    .refresh()
            }
        }
    }
})

class ToggleBytesAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showBytes
    }
})

class ToggleKBytesAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showKBytes
    }
})

class ToggleMBytesAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showMBytes
    }
})

class ToggleGBytesAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showGBytes
    }
})

class ToggleLinesAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showLines
    }
})

class ToggleDirectoryItemsAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showDirectoryItemsAmount
    }
})

class ToggleDirectorySizeAction : DumbAware, ToggleOptionAction({
    object : AbstractToggleAction(it) {
        override val option = settings::showDirectorySize
    }
})
