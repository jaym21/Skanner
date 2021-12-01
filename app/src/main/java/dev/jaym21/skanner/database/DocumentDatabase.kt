package dev.jaym21.skanner.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.jaym21.skanner.models.Document

@Database(entities = [Document::class], version = 1, exportSchema = false)
abstract class DocumentDatabase: RoomDatabase() {

    abstract fun getDocumentDAO(): DocumentDao

    companion object {

        @Volatile
        private var INSTANCE: DocumentDatabase? = null

        fun getDatabase(context: Context): DocumentDatabase {
            return INSTANCE ?: synchronized(this) {
                val databaseInstance = Room.databaseBuilder(
                    context.applicationContext,
                    DocumentDatabase::class.java,
                    "document_database"
                ).build()
                INSTANCE = databaseInstance
                return databaseInstance
            }
        }
    }
}