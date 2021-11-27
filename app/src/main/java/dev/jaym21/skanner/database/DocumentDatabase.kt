package dev.jaym21.skanner.database

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.jaym21.skanner.models.Document

@Database(entities = [Document::class], version = 1, exportSchema = false)
abstract class DocumentDatabase: RoomDatabase() {


}