package com.ayberk.geminiapp.geminichatbot.ui

import android.graphics.Bitmap
import com.ayberk.geminiapp.geminichatbot.data.Chat

data class ChatState (
    val chatList : MutableList<Chat> = mutableListOf(),
    val prompt : String = "",
    val bitmap: Bitmap? = null
)