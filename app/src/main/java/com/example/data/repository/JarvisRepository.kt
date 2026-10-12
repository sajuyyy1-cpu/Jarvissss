package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class JarvisRepository(private val database: JarvisDatabase) {
    private val memoryDao = database.memoryDao()
    private val taskDao = database.taskDao()
    private val mudArtDao = database.mudArtDao()
    private val chatDao = database.chatDao()

    // Memories
    fun getAllMemories(): Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    fun searchMemories(query: String): Flow<List<MemoryEntity>> = memoryDao.searchMemories(query)
    fun getMemoriesByCategory(category: String): Flow<List<MemoryEntity>> = memoryDao.getMemoriesByCategory(category)
    suspend fun getAllMemoriesList(): List<MemoryEntity> = memoryDao.getAllMemoriesList()
    suspend fun insertMemory(memory: MemoryEntity): Long = memoryDao.insertMemory(memory)
    suspend fun deleteMemory(memory: MemoryEntity) = memoryDao.deleteMemory(memory)
    suspend fun clearAllMemories() = memoryDao.clearAllMemories()

    // Tasks
    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) = taskDao.setTaskCompleted(id, completed)
    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)
    suspend fun clearAllTasks() = taskDao.clearAllTasks()

    // Mud Art Business
    fun getAllCustomers(): Flow<List<MudArtCustomerEntity>> = mudArtDao.getAllCustomers()
    fun searchCustomers(query: String): Flow<List<MudArtCustomerEntity>> = mudArtDao.searchCustomers(query)
    suspend fun insertCustomer(customer: MudArtCustomerEntity) = mudArtDao.insertCustomer(customer)
    suspend fun updateCustomer(customer: MudArtCustomerEntity) = mudArtDao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: MudArtCustomerEntity) = mudArtDao.deleteCustomer(customer)

    fun getAllOrders(): Flow<List<MudArtOrderEntity>> = mudArtDao.getAllOrders()
    fun getOrdersByStatus(status: String): Flow<List<MudArtOrderEntity>> = mudArtDao.getOrdersByStatus(status)
    suspend fun insertOrder(order: MudArtOrderEntity) = mudArtDao.insertOrder(order)
    suspend fun updateOrder(order: MudArtOrderEntity) = mudArtDao.updateOrder(order)
    suspend fun updateOrderStatus(id: Long, status: String) = mudArtDao.updateOrderStatus(id, status)
    suspend fun deleteOrder(order: MudArtOrderEntity) = mudArtDao.deleteOrder(order)

    fun getAllNotes(): Flow<List<MudArtNoteEntity>> = mudArtDao.getAllNotes()
    suspend fun insertNote(note: MudArtNoteEntity) = mudArtDao.insertNote(note)
    suspend fun deleteNote(note: MudArtNoteEntity) = mudArtDao.deleteNote(note)

    // Chat
    fun getAllMessages(): Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    suspend fun insertMessage(message: ChatMessageEntity) = chatDao.insertMessage(message)
    suspend fun clearChat() = chatDao.clearChat()
}
