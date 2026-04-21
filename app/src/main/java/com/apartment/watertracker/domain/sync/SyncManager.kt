package com.apartment.watertracker.domain.sync

interface SyncManager {
    fun startPeriodicSync()
    fun stopPeriodicSync()
    fun triggerImmediateSync()
}
