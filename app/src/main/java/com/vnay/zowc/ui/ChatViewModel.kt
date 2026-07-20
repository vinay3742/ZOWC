package com.vnay.zowc.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vnay.zowc.data.ChatMessage
import com.vnay.zowc.domain.ChatService
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatViewModel(private val chatService: ChatService) : ViewModel() {

    private val _messages = mutableStateListOf<ChatMessage>()
    val messages: List<ChatMessage> = _messages

    private val _inputText = mutableStateOf("")
    val inputText: State<String> = _inputText

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            _isLoading.value = true
            chatService.initialize()
            _isLoading.value = false
        }
    }

    fun onInputTextChange(text: String) {
        _inputText.value = text
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        if (text.isEmpty()) return

        _messages.add(ChatMessage(text = text, isUser = true))
        _inputText.value = ""

        viewModelScope.launch {
            val botMessage = ChatMessage(text = "", isUser = false)
            _messages.add(botMessage)
            val index = _messages.indexOf(botMessage)

            var fullText = ""
            chatService.sendMessage(text)
                .onStart { _isLoading.value = true }
                .onCompletion { _isLoading.value = false }
                .collect { chunk ->
                    // Assuming chunk is String for now, will fix if it's Message
                    fullText += chunk
                    _messages[index] = botMessage.copy(text = fullText)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatService.close()
    }
}
