package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        PostEntity::class,
        ReelEntity::class,
        StoryEntity::class,
        CommentEntity::class,
        DirectMessageEntity::class,
        NotificationEntity::class,
        AdCampaignEntity::class,
        PaymentTransactionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class InstaLinkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun reelDao(): ReelDao
    abstract fun storyDao(): StoryDao
    abstract fun commentDao(): CommentDao
    abstract fun messageDao(): MessageDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adCampaignDao(): AdCampaignDao
    abstract fun paymentTransactionDao(): PaymentTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: InstaLinkDatabase? = null

        fun getDatabase(context: Context): InstaLinkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    InstaLinkDatabase::class.java,
                    "instalink_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
