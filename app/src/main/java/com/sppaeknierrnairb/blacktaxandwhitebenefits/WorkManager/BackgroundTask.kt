package com.sppaeknierrnairb.blacktaxandwhitebenefits.WorkManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.text.Html
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.sppaeknierrnairb.blacktaxandwhitebenefits.MainActivity
import com.sppaeknierrnairb.blacktaxandwhitebenefits.Networking.BlogArticles
import com.sppaeknierrnairb.blacktaxandwhitebenefits.Networking.GetBlogService
import com.sppaeknierrnairb.blacktaxandwhitebenefits.Networking.RetrofitClientInstance
import com.sppaeknierrnairb.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences
import com.sppaeknierrnairb.blacktaxandwhitebenefits.R
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


// notifications constants
const val CHANNEL_ID = 1
const val CHANNEL_ID_STR = "notificationbackgroundtask001"
const val CHANNEL_NAME = "notificationbackgroundtask"

// WorkManager constants
const val NOTIFICATION_WORKREQUEST_TAG = "NOTIFICATION_WORKREQUEST_TAG"

class BackgroundTask(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Worker gets called immediately upon WorkRequest setup.  Therefore, we may want to set an initial delay.

        checkForUpdatedBlogs()

        return Result.success()
    }


    private fun checkForUpdatedBlogs() {
        // Checks for updated blogs.

        // Make a Retrofit call to query the blogs.
        // We're only getting the first page's data...why?  Because any new blogs will be on the first page.
        val call = BackgroundTask.getArticleResponseCall(1)

        //
        // We're not interested in actually updating RecyclerView.  If we detect new articles, then just
        // send a notification to the user indicating that. Once the user clicks on the notification, then load the
        // page in the GUI.
        //
        call?.enqueue(object : Callback<List<BlogArticles>> {
            override fun onResponse(call: Call<List<BlogArticles>>, response: Response<List<BlogArticles>>) {
                // Retrofit succeeded to get networking and is hitting main url.
                // Retrofit only responds after it gets the data.
                if (response.isSuccessful) {
                    val body = response.body()  // The entire JSON body.
                    val bodyLastIndex = body!!.lastIndex

                    // We only want the first article to see if it's newer than what we have already.
                    for (i in 0..0) {
                        var title = body[i].title.titleRendered
                        val urlLink = body[i].URLLink
                        val date = body[i].date
                        val id = body[i].id
                        val modifiedDate = body[i].modifiedDate
                        val htmlArticle = body[i].content.htmlRendered
                        val imageBlogURL = body[i].imageBlogURL

                        // Strips off some of the html codes that are not displaying correctly.
                        title=convertUTFtoString(title)
                        Log.i("!!!SharedPref", "Title of blog 0 article: $title")
                        Log.i("!!!SharedPref", "Date of blog 0 article: $title")

                        // Saves title shared preference if title is newer.
                        detNewerSharedPreferences(title)
                    }
                } else {
                    // no data in query.
                    if (response.code() == 400) {
                        // Thankfully, the recyclerView doesn't fail here.
                        Log.i("!!!", "query is not found in retrofit!!")
                    }
                }
            }

            override fun onFailure(call: Call<List<BlogArticles>>, t: Throwable) {
                // No network or cannot get to URL.
                Log.i("!!!", "retrofit failed!")
            }
        })
    }


    private fun detNewerSharedPreferences(blogTitle: String) {
        // Saves title only if it's different...keep in mind that we're only comparing differences in the first record,
        //   which is the record being uploaded.
        val previousSharedPrefTitle = AppSharedPreferences.sharedPrefNotificationTitle

        if (AppSharedPreferences.sharedPrefNotificationTitle != blogTitle) {
            // Save the newest blog article to our SharedPref variable.
            AppSharedPreferences.setAppSharedPreferences(applicationContext, AppSharedPreferences.SHAREDPREF_BLOGTITLE,
                blogTitle
            )
            AppSharedPreferences.sharedPrefNotificationTitle = blogTitle

            if (previousSharedPrefTitle !="") {
                // We don't want the WorkManager notification to come up on a newly-installed app--only on subsequent new articles!
                // Because we have a newer title, make sure user can see it!
                val appName = applicationContext.resources.getString(R.string.app_name)
                sendNotification(appName, "A new article was found--$blogTitle")
            }
        }
    }


    /*
//TODO: sendNotification as it is has two issues:
    1) It sends an intent to simply open MainActivity. This causes MainActivity.onCreate() to get triggered which calls
       a variety of one-time processes!!
    2) When the user clicks on the notification, it should go directly to the article!
*/
   private fun sendNotification(title: String, message: String) {
       val appContext = applicationContext

       val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       //If on Oreo then notification required a notification channel.
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
           val channel = NotificationChannel(CHANNEL_ID_STR, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
           notificationManager.createNotificationChannel(channel)
       }

       val notification = NotificationCompat.Builder(appContext, CHANNEL_ID_STR).apply {
           val intent = Intent(appContext, MainActivity::class.java)
           val pendingIntent: PendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            setContentTitle(title)
            setContentText(message)
            setContentIntent(pendingIntent)
            setSmallIcon(R.mipmap.ic_launcher)
       }

       notificationManager.notify(CHANNEL_ID, notification.build())
    }



    companion object {
        // Returns static fields / methods.
        fun getArticleResponseCall(pageNum: Int): Call<List<BlogArticles>>? {
            val service = RetrofitClientInstance.retrofitInstance?.create(GetBlogService::class.java)

//        Log.d(TAG, "articleResponseCall url:" + articleResponseCall.request().url().toString())
            return service?.getAllArticles(pageNum.toString())
        }

        fun convertUTFtoString(title: String): String {
            // Converts HTML UTF codes into readable string.
            val convertedUTFString: String = if (Build.VERSION.SDK_INT >= 24) {
                Html.fromHtml(title , Html.FROM_HTML_MODE_LEGACY).toString()
            } else {
                Html.fromHtml(title).toString()
            }

            return convertedUTFString
        }
    }
}


