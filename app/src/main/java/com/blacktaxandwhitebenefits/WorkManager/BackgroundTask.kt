package com.blacktaxandwhitebenefits.WorkManager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import android.text.Html
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blacktaxandwhitebenefits.ProjectData
import com.blacktaxandwhitebenefits.R
import com.blacktaxandwhitebenefits.WebViewActivity
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.BlogArticles
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.GetBlogService
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.RetrofitClientInstance
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.URL
import java.util.*


// notifications constants
const val CHANNEL_ID = 1
const val CHANNEL_ID_STR = "notificationbackgroundtask001"
const val CHANNEL_NAME = "notificationbackgroundtask"

// WorkManager constants
const val NOTIFICATION_WORKREQUEST_TAG = "NOTIFICATION_WORKREQUEST_TAG"

private var backgroundTaskDataArray = ArrayList<String>(4)


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
        val call =
            getArticleResponseCall(
                1
            )

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
                        var blogTitle = body[i].title.titleRendered
                        val blogUrlLink = body[i].URLLink
                        val blogDate = body[i].date
                        val blogHtmlArticle = body[i].content.htmlRendered
                        val blogImageBlogURL = body[i].imageBlogURL
                        val blogID = body[i].id
                        val blogModifiedDate = body[i].modifiedDate

                        // Strips off some of the html codes that are not displaying correctly.
                        blogTitle=
                            convertUTFtoString(
                                blogTitle
                            )

                        backgroundTaskDataArray = ArrayList<String>(4)
                        backgroundTaskDataArray.add(0, blogDate)
                        backgroundTaskDataArray.add(1, blogTitle)
                        backgroundTaskDataArray.add(2, blogImageBlogURL)
                        backgroundTaskDataArray.add(3, blogHtmlArticle)
                        backgroundTaskDataArray.add(4, blogUrlLink)

                        /*  detNewerSharedPreferences Does two things:
                            1) If blogtitle is different than the saved title in sharedpref, then save the newer value to SharedPrefs right away!
                            2) Returns the newest blog title assigned to SharedPref.
                         */
                        val possibleNewSharedPrefTitle = detNewerSharedPreferences(blogTitle)
                        if (possibleNewSharedPrefTitle != "") {
                            /*
                               We don't want the WorkManager notification to come up:
                                  * On a newly-installed app--work manager runs as soon as the app is installed.
                                  * When articles haven't changed.
                            // Because we have a newer title, make sure user can see it!
                            */
                            val appName = applicationContext.resources.getString(R.string.app_name)
                            sendNotification(appName, "A new article was found--$blogTitle", blogImageBlogURL)
                        }
                    }
                } else {
                    // Do nothing because this is just a periodic background method....not critical.
                    Log.e("!!!", "Background Task: Retrofit response was unsuccessful!")
                }
            }

            override fun onFailure(call: Call<List<BlogArticles>>, t: Throwable) {
                // No network or cannot get to URL.
                // Do nothing because this is just a periodic background method....not critical.
                Log.e("!!!", "Background Task: Hit onFailure() method. Bad URL or network connection!")
            }
        })
    }


    private fun detNewerSharedPreferences(blogTitle: String): String {
        // Saves title only if it's different...keep in mind that we're only comparing differences in the first record,
        //   which is the record being uploaded.

        // blogTitle --> The most recent article downloaded via WorkManager from the endpoint.

        // To make this determination, we need to know what the old SharedPref blogtitle is.
        var currentSharedPrefTitle: String =
            AppSharedPreferences.getAppSharedPreferences(applicationContext, AppSharedPreferences.SHAREDPREF_BLOGTITLE).toString()
        var newerSharedPrefTitle = ""

        // Looks to compare to see if we have a newer blogtitle or not.
        if (currentSharedPrefTitle != "") {
            // currentSharedPrefTitle == "" the first time the app runs.
            if (blogTitle != "" && currentSharedPrefTitle != blogTitle) {
                // Save the newest blog article to our SharedPref variable.
                AppSharedPreferences.setAppSharedPreferencesSync(
                    applicationContext, AppSharedPreferences.SHAREDPREF_BLOGTITLE,
                    blogTitle )
                newerSharedPrefTitle = blogTitle
            }
        } else {
            // Runs the first time the app is installed.
            AppSharedPreferences.setAppSharedPreferencesSync(
                applicationContext, AppSharedPreferences.SHAREDPREF_BLOGTITLE,
                blogTitle )
        }
        return newerSharedPrefTitle
}


    /*
//TODO: sendNotification as it is has two issues:
    1) It sends an intent to simply open MainActivity. This causes MainActivity.onCreate() to get triggered which calls
       a variety of one-time processes!! --> Fixed.
    2) When the user clicks on the notification, it should go directly to the article! --> Done.
*/
   private fun sendNotification(title: String, message: String, urlImagePath: String) {
       val appContext = applicationContext

       val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

       //If on Oreo then notification required a notification channel.
       if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
           val channel = NotificationChannel(
               CHANNEL_ID_STR,
               CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
           notificationManager.createNotificationChannel(channel)
       }

       // Get's image drawable for large icon.
       val bitmap: Bitmap? = getBitmapFromURL(urlImagePath)

        if (bitmap != null) {
            // Show largeIcon
            val notification = NotificationCompat.Builder(appContext,
                CHANNEL_ID_STR
            ).apply {
                // Sets up our intent to open to the article page directly.
                val intent = Intent(appContext, WebViewActivity::class.java)

                // pass data into our intent.
                intent.putStringArrayListExtra(
                    ProjectData.putExtra_BlogWebView,
                    backgroundTaskDataArray
                )

                // Intent in case the user clicks on the article.
                val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                setContentTitle(title)
                setContentText(message)
                setContentIntent(pendingIntent)
                setLargeIcon(bitmap)

                // On API >= 21, image must be PNG and transparent.  It helps to be a clear solid image or outlined image.
                setSmallIcon(R.drawable.ic_stat_handshake)
                setAutoCancel(true)
            }
            notificationManager.notify(CHANNEL_ID, notification.build())
        } else {
            // No bitmap for setlargeIcon
            val notification = NotificationCompat.Builder(appContext,
                CHANNEL_ID_STR
            ).apply {
                // Sets up our intent to open to the article page directly.
                val intent = Intent(appContext, WebViewActivity::class.java)

                // Intent in case the user clicks on the article.
                val pendingIntent = PendingIntent.getActivity(appContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                setContentTitle(title)
                setContentText(message)
                setContentIntent(pendingIntent)

                // On API >= 21, image must be PNG and transparent.  It helps to be a clear solid image or outlined image.
                setSmallIcon(R.drawable.ic_stat_handshake)
                setAutoCancel(true)
            }
            notificationManager.notify(CHANNEL_ID, notification.build())
        }
    }


    fun getBitmapFromURL(imageURL: String): Bitmap? {
        // Goes until its done loading (synchronous)
        val bitmap = runBlocking (Dispatchers.IO) {
            var bmp: Bitmap? = null
            try {
                bmp = BitmapFactory.decodeStream(URL(imageURL).openStream())
            } catch (e: Exception) {
                Log.e("!!!", "getBitmapFromURL: " + e.message.toString())
            }

            bmp
        }
        return bitmap
    }



    companion object {
        // Returns static fields / methods.
        fun getArticleResponseCall(pageNum: Int): Call<List<BlogArticles>>? {
            val service = RetrofitClientInstance.retrofitInstance?.create(
                GetBlogService::class.java)

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


