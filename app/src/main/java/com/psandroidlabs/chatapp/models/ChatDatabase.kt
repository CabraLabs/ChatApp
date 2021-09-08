package com.psandroidlabs.chatapp.models

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Message::class], version = 1)
abstract class ChatDatabase : RoomDatabase()