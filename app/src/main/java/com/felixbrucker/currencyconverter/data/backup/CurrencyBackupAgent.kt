package com.felixbrucker.currencyconverter.data.backup

import android.app.backup.BackupAgent
import android.app.backup.FullBackupDataOutput
import com.felixbrucker.currencyconverter.data.local.AppDatabase
import com.felixbrucker.currencyconverter.data.local.AppSettingEntity
import com.felixbrucker.currencyconverter.data.repository.CurrencyRepository
import kotlinx.coroutines.runBlocking
import kotlin.time.Clock.System.now

class CurrencyBackupAgent : BackupAgent() {
    
    // Key/Value backup not implemented, we rely on Auto Backup (Full Data Backup)
    override fun onBackup(
        oldState: android.os.ParcelFileDescriptor?,
        data: android.app.backup.BackupDataOutput?,
        newState: android.os.ParcelFileDescriptor?
    ) {
    }

    override fun onRestore(
        data: android.app.backup.BackupDataInput?,
        appVersionCode: Int,
        newState: android.os.ParcelFileDescriptor?
    ) {
    }

    override fun onFullBackup(data: FullBackupDataOutput?) {
        // First, record the backup time so it's ready for the NEXT backup 
        // or captured if the database file is copied after this call.
        val now = now().toEpochMilliseconds()
        runBlocking {
            val db = AppDatabase.getInstance(this@CurrencyBackupAgent)
            db.currencyDao().setSetting(
                AppSettingEntity(CurrencyRepository.KEY_LAST_BACKUP_TIME, now.toString())
            )
        }
        
        // Let the system perform the full backup of files defined in backup_rules.xml
        super.onFullBackup(data)
    }
}
