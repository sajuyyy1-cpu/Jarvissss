package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        MemoryEntity::class,
        TaskEntity::class,
        MudArtCustomerEntity::class,
        MudArtOrderEntity::class,
        MudArtNoteEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
    abstract fun taskDao(): TaskDao
    abstract fun mudArtDao(): MudArtDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_system_vault.db"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial Sajjad profile & protocols in background coroutine
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            seedInitialData(database)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: JarvisDatabase) {
            val memoryDao = db.memoryDao()
            val taskDao = db.taskDao()
            val mudArtDao = db.mudArtDao()
            val chatDao = db.chatDao()

            // Seed user profile identity memory - Sajjad (सज्जाद)
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "User Identity & Title",
                    content = "Name: Sajjad (सज्जाद). In Hindi address as 'सज्जाद जी'. In English address as 'Sajjad'. Primary Operator.",
                    category = "IDENTITY",
                    tags = "owner,user,name,sajjad"
                )
            )
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "Primary Language Protocol",
                    content = "Trilingual mode enabled: English, Hindi (हिंदी), and conversational Hinglish.",
                    category = "PREFERENCE",
                    tags = "language,trilingual"
                )
            )
            memoryDao.insertMemory(
                MemoryEntity(
                    title = "Majikhan Mud Art Business",
                    content = "Sajjad manages 'Majikhan Mud Art' - traditional Lippan and Mud Mirror Art craft business.",
                    category = "SYSTEM",
                    tags = "business,mudart,lippan"
                )
            )

            // Seed initial tasks
            taskDao.insertTask(
                TaskEntity(
                    title = "Calibrate Audio & Vocal Receptors",
                    description = "Verify English, Hindi & Hinglish Speech Recognition on Android.",
                    priority = "HIGH",
                    isCompleted = false,
                    dueDate = "Today"
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Review Majikhan Mud Art Catalog",
                    description = "Check pending Lippan art orders and customer requests.",
                    priority = "MEDIUM",
                    isCompleted = false,
                    dueDate = "Tomorrow"
                )
            )

            // Seed sample mud art order & customer
            mudArtDao.insertCustomer(
                MudArtCustomerEntity(
                    name = "Vikram Sharma",
                    phone = "+91 98765 43210",
                    email = "vikram.sharma@example.com",
                    city = "Jaipur, Rajasthan",
                    notes = "Prefers geometric Lippan art with circular mirrors."
                )
            )
            mudArtDao.insertOrder(
                MudArtOrderEntity(
                    customerName = "Vikram Sharma",
                    artPieceTitle = "Heritage Peacock Lippan Panel",
                    dimensions = "36 x 24 inches",
                    amount = 14500.0,
                    status = "IN_PROGRESS",
                    orderDate = "2026-10-10",
                    notes = "Clay cured, mirror embedding in progress."
                )
            )
            mudArtDao.insertNote(
                MudArtNoteEntity(
                    title = "Kutch White Clay Formula",
                    content = "Mix filtered river clay with gundar (natural gum) and fine chalk powder for smooth finish.",
                    category = "MATERIAL"
                )
            )

            // Initial greeting chat message
            chatDao.insertMessage(
                ChatMessageEntity(
                    sender = "JARVIS",
                    text = "Welcome, Sajjad. All systems are operational. Cores energized and audio receptors standing by. How may I assist you today, Sir?",
                    language = "EN"
                )
            )
        }
    }
}
