package com.nova.assistant.data.repository

import com.nova.assistant.data.local.database.dao.ConversationDao
import com.nova.assistant.data.local.database.dao.MessageDao
import com.nova.assistant.data.local.database.entities.ConversationEntity
import com.nova.assistant.data.local.database.entities.MessageEntity
import com.nova.assistant.data.remote.ai.NovaAIManager
import com.nova.assistant.domain.model.*
import com.nova.assistant.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val aiManager: NovaAIManager
) : ChatRepository {

    override fun observeConversations(): Flow<List<Conversation>> =
        conversationDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observeMessages(conversationId: String): Flow<List<Message>> =
        messageDao.observeByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getOrCreateActiveConversation(): Conversation =
        withContext(Dispatchers.IO) {
            val existing = conversationDao.getMostRecent()
            if (existing != null) {
                existing.toDomain()
            } else {
                createConversation()
            }
        }

    override suspend fun createConversation(title: String): Conversation =
        withContext(Dispatchers.IO) {
            val entity = ConversationEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            conversationDao.insert(entity)
            entity.toDomain()
        }

    override suspend fun deleteConversation(conversationId: String) =
        withContext(Dispatchers.IO) { conversationDao.deleteById(conversationId) }

    override suspend fun clearAllConversations() =
        withContext(Dispatchers.IO) {
            messageDao.deleteAll()
            conversationDao.deleteAll()
        }

    override suspend fun saveMessage(message: Message) =
        withContext(Dispatchers.IO) {
            messageDao.insert(message.toEntity())
            conversationDao.touchConversation(message.conversationId, System.currentTimeMillis())
        }

    override suspend fun getRecentMessages(conversationId: String, limit: Int): List<Message> =
        withContext(Dispatchers.IO) {
            messageDao.getRecentMessages(conversationId, limit)
                .reversed()
                .map { it.toDomain() }
        }

    override suspend fun sendMessage(
        conversationId: String,
        userMessage: String,
        isVoice: Boolean
    ): AIResponse = withContext(Dispatchers.IO) {
        // 1. Save user message
        val userMsg = Message(
            conversationId = conversationId,
            role = MessageRole.USER,
            content = userMessage,
            metadata = MessageMetadata(isVoice = isVoice)
        )
        messageDao.insert(userMsg.toEntity())

        // 2. Get conversation history for context
        val history = getRecentMessages(conversationId, Constants.MAX_CONVERSATION_HISTORY)
            .filter { it.id != userMsg.id } // Exclude the message we just saved

        // 3. Call AI
        val startTime = System.currentTimeMillis()
        val response = aiManager.chat(history, userMessage)
        val processingTime = System.currentTimeMillis() - startTime

        // 4. Save AI response (or error placeholder)
        when (response) {
            is AIResponse.Success -> {
                val aiMsg = Message(
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = response.text,
                    metadata = MessageMetadata(processingTimeMs = processingTime)
                )
                messageDao.insert(aiMsg.toEntity())

                // Auto-title conversation after first exchange
                val count = messageDao.countByConversation(conversationId)
                if (count <= 3) {
                    val shortTitle = userMessage.take(40).let {
                        if (userMessage.length > 40) "$it…" else it
                    }
                    conversationDao.updateTitle(conversationId, shortTitle, System.currentTimeMillis())
                }
            }
            is AIResponse.Error -> {
                val errMsg = Message(
                    conversationId = conversationId,
                    role = MessageRole.ASSISTANT,
                    content = response.message,
                    isError = true
                )
                messageDao.insert(errMsg.toEntity())
            }
            else -> Unit
        }

        conversationDao.touchConversation(conversationId, System.currentTimeMillis())
        response
    }

    override suspend fun updateConversationTitle(conversationId: String, title: String) =
        withContext(Dispatchers.IO) {
            conversationDao.updateTitle(conversationId, title, System.currentTimeMillis())
        }

    // ── Mappers ────────────────────────────────────────────────────────────

    private fun ConversationEntity.toDomain() = Conversation(
        id = id, title = title, createdAt = createdAt, updatedAt = updatedAt, isPinned = isPinned
    )

    private fun MessageEntity.toDomain() = Message(
        id = id,
        conversationId = conversationId,
        role = when (role) {
            "user"      -> MessageRole.USER
            "assistant" -> MessageRole.ASSISTANT
            else        -> MessageRole.SYSTEM
        },
        content = content,
        timestamp = timestamp,
        isError = isError,
        metadata = if (isVoice || imageUri != null || processingTimeMs != null) {
            MessageMetadata(
                isVoice = isVoice,
                imageUri = imageUri,
                processingTimeMs = processingTimeMs
            )
        } else null
    )

    private fun Message.toEntity() = MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.apiValue,
        content = content,
        timestamp = timestamp,
        isError = isError,
        isVoice = metadata?.isVoice ?: false,
        imageUri = metadata?.imageUri,
        processingTimeMs = metadata?.processingTimeMs
    )
}

private val Message.Constants: com.nova.assistant.util.Constants
    get() = com.nova.assistant.util.Constants
