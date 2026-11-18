package br.com.fiap.presente.chamada.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [TurmaEntity::class, AlunoEntity::class, RegistroPresencaEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class PresenteDatabase : RoomDatabase() {
    abstract fun dao(): PresenteDao

    companion object {
        fun criar(context: Context): PresenteDatabase =
            Room.databaseBuilder(context, PresenteDatabase::class.java, "presente-chamada.db").build()
    }
}
