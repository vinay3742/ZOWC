package com.vnay.zowc.domain

import kotlinx.coroutines.flow.Flow

interface ChatService {
    fun sendMessage(text: String): Flow<String>
    suspend fun initialize()
    fun close()
}
