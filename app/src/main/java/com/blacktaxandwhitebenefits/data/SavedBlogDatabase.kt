package com.blacktaxandwhitebenefits.data

import android.content.Context
import android.os.AsyncTask
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

/*
    Name of the table: "blogs_database"
*/


@Database(entities = [SavedBlog::class], version = 1)
abstract class SavedBlogDatabase : RoomDatabase() {

    abstract fun savedBlogDao(): SavedBlogDao

    companion object {
        private var instance: SavedBlogDatabase? = null

        fun getInstance(context: Context): SavedBlogDatabase? {
            if (instance == null) {
                synchronized(SavedBlogDatabase::class) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        SavedBlogDatabase::class.java, "blogs_database"
                    )
                        .fallbackToDestructiveMigration() // when version increments, it migrates (deletes db and creates new) - else it crashes
                        // We're not using the callback here.  Keeping it here for example purposes.
                        // This callback runs at table creation...in this case loads some sample data.
                        //  *** DO NOT USE ----------->
                        //  .addCallback(roomCallback)
                        // <-----DO NOT USE ----------|
                        .build()
                }
            }
            return instance
        }

        fun destroyInstance() {
            instance = null
        }

        private val roomCallback = object : RoomDatabase.Callback() {
            /* onCreate() is adding initial data to Room (runs only once per app install).
                This is an easy way to determine if Room is working to save data.
             */
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                PopulateDbAsyncTask(instance)
                    .execute()
            }
        }
    }

    class PopulateDbAsyncTask(db: SavedBlogDatabase?) : AsyncTask<Unit, Unit, Unit>() {
        private val noteDao = db?.savedBlogDao()

        override fun doInBackground(vararg p0: Unit?) {
            noteDao?.insert(SavedBlog("sample article1", "", "6/20/19", "something", "", "", "", 3))
            noteDao?.insert(SavedBlog("sample article2", "", "6/11/19", "something", "", "", "", 4))
            noteDao?.insert(SavedBlog("sample article3", "", "2/20/19", "something", "", "", "", 5))
        }
    }
}