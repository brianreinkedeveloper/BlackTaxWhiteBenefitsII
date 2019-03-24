package com.blacktaxandwhitebenefits




import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.work.*
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitFunction
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitReadAHead
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitReadaHeadClass
import com.blacktaxandwhitebenefits.WorkManager.BackgroundTask
import com.blacktaxandwhitebenefits.WorkManager.NOTIFICATION_WORKREQUEST_TAG
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.BlogArticles
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.GetBlogService
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.RecycleDTO
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.RetrofitClientInstance
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.getAppSharedPreferencesInt
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.setAppSharedPreferencesAsync
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    // Retrofit service.
    private val service = RetrofitClientInstance.retrofitInstance?.create(GetBlogService::class.java)
    var myList = mutableListOf<RecycleDTO>()

    // Action bar icons
    var actionbarNavAfterActive: MenuItem? = null
    var actionbarNavAfterInActive: MenuItem? = null
    var actionbarNavBeforeActive: MenuItem? = null
    var actionbarNavBeforeInActive: MenuItem? = null

    //
    // MainActivity
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Must be here in onCreate() here otherwise things won't work correctly.

        initialize()

        //
        // RetrofitClientInstance
        //
        loadRetrofitPages(service, ProjectData.currentPage)
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        // For some reason, this doesn't work if outPersistentState exists!
        super.onSaveInstanceState(outState)
        ProjectData.onSavedState =true
    }


    //
    // Menu
    //
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_mainactactionbar, menu)

        this.actionbarNavAfterActive=menu?.findItem(R.id.navafteractive)
        this.actionbarNavAfterInActive=menu?.findItem(R.id.navafterinactive)

        this.actionbarNavBeforeActive=menu?.findItem(R.id.navbeforeactive)
        this.actionbarNavBeforeInActive=menu?.findItem(R.id.navbeforeinactive)

//        return super.onCreateOptionsMenu(menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val menuID = item?.itemId
        when (menuID) {
            // we don't want to do anything if the menu is somehow inactive.
            R.id.navbeforeactive -> {
                // prev page
                pressPrevPage()
                return true}
            R.id.navafteractive -> {
                // next page
                pressNextPage()
                return true}
            else -> return super.onOptionsItemSelected(item)
        }
    }


    private fun pressPrevPage() {
        ProjectData.buttonClicked ="prev"

        // We have to make both buttons inactive while data loads otherwise strange things occur...wouldn't be an issue
        //   if we had DialogFragment.
        setPrevPageInactive()
        setNextPageInActive()

        // Hide RecyclerView when loading data...why? If visible, the user can scroll on the page and the app will crash!
        recyclerView.visibility=View.INVISIBLE

        ProjectData.currentPage--
        if (ProjectData.currentPage == 1) {
            // This sets prevpage to inactive icon
            setPrevPageInactive()
        }

        readAHeadNotNeededCheck()
        prepareNextPage(ProjectData.currentPage)
    }


    private fun pressNextPage() {
        // We turn it off until network load is finished.
        ProjectData.buttonClicked ="next"

        // We have to make both buttons inactive while data loads otherwise strange things occur...wouldn't be an issue
        //   if we had DialogFragment.
        setPrevPageInactive()
        setNextPageInActive()

        // Hide RecyclerView when loading data...why? If visible, the user can scroll on the page and the app will crash!
        recyclerView.visibility=View.INVISIBLE

        ProjectData.currentPage++
        if (ProjectData.currentPage == ProjectData.maxPagesAtCompile) {

            // disable next page as we're at the end.
            actionbarNavAfterActive?.setEnabled(false)
            actionbarNavAfterInActive?.setEnabled(true)
            ProjectData.butNextPageState = false
        }

        readAHeadNotNeededCheck()
        prepareNextPage(ProjectData.currentPage)
    }


    fun setNextPageActive() {
        actionbarNavAfterInActive?.setEnabled(false)
        actionbarNavAfterInActive?.setEnabled(false)

        actionbarNavAfterActive?.setVisible(true)
        actionbarNavAfterInActive?.setVisible(false)

        ProjectData.butNextPageState = actionbarNavAfterActive?.isEnabled
    }

    fun setNextPageInActive() {
        actionbarNavAfterInActive?.setEnabled(false)
        actionbarNavAfterActive?.setEnabled(true)

        actionbarNavAfterInActive?.setVisible(true)
        actionbarNavAfterActive?.setVisible(false)

        // not using this here.
//        ProjectData.butNextPageState = actionbarNavAfterInActive?.isEnabled
    }

    fun setPrevPageInactive() {
        actionbarNavBeforeInActive?.setEnabled(false)
        actionbarNavBeforeActive?.setEnabled(false)

        actionbarNavBeforeInActive?.setVisible(true)
        actionbarNavBeforeActive?.setVisible(false)

        ProjectData.butPrevPageState = false
    }

    fun setPrevPageActive() {
        actionbarNavBeforeActive?.setEnabled(true)
        actionbarNavBeforeInActive?.setEnabled(false)

        actionbarNavBeforeActive?.setVisible(true)
        actionbarNavBeforeInActive?.setVisible(false)

        ProjectData.butPrevPageState = actionbarNavBeforeActive?.isEnabled
    }



    private fun initialize() {
//        butPagePrev.text="<"
//        butPageNext.text=">"

        // Sets last known good page from sharedpref
        ProjectData.knownGoodLastPage = getAppSharedPreferencesInt(this@MainActivity, ProjectData.SHAREDPREF_KNOWNLASTPAGE)
        if (ProjectData.knownGoodLastPage > RetrofitReadaHeadClass.knownGoodLastPage) {
            RetrofitReadaHeadClass.knownGoodLastPage = ProjectData.knownGoodLastPage
        }

        // Initially, we don't this button active as there is no page 0.
        if  (!ProjectData.onSavedState) {
            setPrevPageInactive()
        }
        Log.i("!!!", "current page" + ProjectData.currentPage.toString())
        initBackgroundTask()
    }


    private fun initBackgroundTask() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequest.Builder(BackgroundTask::class.java, 12, TimeUnit.HOURS)
            .addTag(NOTIFICATION_WORKREQUEST_TAG)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance().enqueueUniquePeriodicWork(NOTIFICATION_WORKREQUEST_TAG, ExistingPeriodicWorkPolicy.REPLACE, periodicWork)
    }


    private fun runEnqueue(service: GetBlogService?, currentPage: Int = 1, retrofitCall: RetrofitFunction)  {
        runOnUiThread {
            progressBar1.visibility = View.VISIBLE
        }

        val call = service?.getAllArticles(currentPage.toString())

        call?.enqueue(object : Callback<List<BlogArticles>> {
            override fun onResponse(call: Call<List<BlogArticles>>, response: Response<List<BlogArticles>>) {
                // Retrofit succeeded to get networking and is hitting main url.
                // Retrofit only responds after it gets the data.
                if (response.isSuccessful) {
                    // Process if RetrofitFunction.READAHEAD.
                    if (retrofitCall==RetrofitFunction.READAHEAD) {
                        RetrofitReadaHeadClass.readAHeadStatus=RetrofitReadAHead.FINISHED
                        if (currentPage > RetrofitReadaHeadClass.knownGoodLastPage) {
                            RetrofitReadaHeadClass.knownGoodLastPage=currentPage
                            // Save to Shared Pref setting.
                            setAppSharedPreferencesAsync(this@MainActivity, ProjectData.SHAREDPREF_KNOWNLASTPAGE, RetrofitReadaHeadClass.knownGoodLastPage)
                            Log.i("!!!", "lasknownlastpage: " + RetrofitReadaHeadClass.knownGoodLastPage)
                        }
                    } else {
                        // RetrofitFunction.NORMALQUERY.
                        retrofitSuccess(response, retrofitCall, currentPage)
                    }
                } else {
                    // response.isSuccessful is not true here.
                    /* Could be several issues but all are issues with the query itself:
                        -- mal-formatted query.
                        --
                     */
                    if (retrofitCall==RetrofitFunction.READAHEAD) {
                        // We know there are no more pages to load.
                        RetrofitReadaHeadClass.readAHeadStatus = RetrofitReadAHead.FINISHED
                    } else {
                        RetrofitReadaHeadClass.readAHeadStatus = RetrofitReadAHead.ERROR
                        Log.i("!!!", "query is not found in retrofit!!")
                    }
                    if (retrofitCall == RetrofitFunction.NORMALQUERY) {
                        //                        pageButtonsRestoreState()
//                            stopProgressBar()
                    }
                }
            }
            override fun onFailure(call: Call<List<BlogArticles>>, t: Throwable) {
                // Almost always, no network or network connection issues, but we need to make sure.
                stopProgressBar()

                if (t is IOException) {
                    // We know we have an internet connection issue.
                    rl_nointernet.visibility=View.VISIBLE
                    rl_maincontent.visibility=View.GONE

                    but_refreshconnection.setOnClickListener {
                        val connectivity = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        val networkInfo=connectivity.activeNetworkInfo
                        if (networkInfo != null && networkInfo.isConnected) {
                            rl_maincontent.visibility=View.VISIBLE
                            rl_nointernet.visibility=View.GONE
                        }
                    }
                } else {
                    // Get the explicit error
                    var buildErrorString: String = ""
                    buildErrorString += "*** There is some kind of unknown (possibly internet connection) issue. ***"
                    buildErrorString += "Exact message error: '" + t.message + "'"
                    Log.e("!!!", buildErrorString)
                }
            }
        })
    }


    private fun retrofitSuccess (
        // For RetrofitFunction.NORMALQUERY only!!

        response: Response<List<BlogArticles>>,
        retrofitCall: RetrofitFunction,
        currentPage: Int
    ): Unit {

        // Check response code in case previous retrofit call was unsuccessful.
        // success codes: 200-299
        if (response.code() >= 200 && response.code()< 300) {
            // Verified good connection...remove 'no internet page' in case it was displaying.
            rl_maincontent.visibility=View.VISIBLE
            rl_nointernet.visibility=View.GONE
            but_refreshconnection.isEnabled=true
        }

        // Process recyclerView visibility.
        recyclerView.visibility=View.VISIBLE

        val body = response.body()  // The entire JSON body.
        val bodyLastIndex = body!!.lastIndex

        println("NORMALQUERY is successful!!")

        // We're converting the weird JSON output to a standard data class.
        for (i in 0..bodyLastIndex) {
            var title = body[i].title.titleRendered
            val urlLink = body[i].URLLink
            val date = body[i].date
            val id = body[i].id
            val modifiedDate = body[i].modifiedDate
            val htmlArticle = body[i].content.htmlRendered
            val imageBlogURL = body[i].imageBlogURL

            // Strips off some of the html codes that are not displaying correctly.
            title = parseTitle(title)

            // Adds to the recycler List DTO.
            this@MainActivity.myList.add(
                i,
                RecycleDTO(title, urlLink, date, id, modifiedDate, htmlArticle, imageBlogURL)
            )
        }

        Log.i("!!!", "RetrofitFunction.NORMALQUERY: "+ currentPage)
        Log.i("!!!", "RetrofitFunction.NORMALQUERY: "+ RetrofitReadaHeadClass.knownGoodLastPage)
        displayData(this@MainActivity.myList)
        pageButtonsRestoreState(currentPage)

        if (RetrofitReadaHeadClass.readAHeadStatus==RetrofitReadAHead.NOTNEEDED) {
            stopProgressBar()
        }
    }


    private fun stopProgressBar() {
        progressBar1.visibility=View.INVISIBLE
        recyclerView.visibility=View.VISIBLE
    }


    private fun pageButtonsRestoreState(currentPage: Int) = GlobalScope.launch {
        // ** ONLY for RetrofitFunction.NORMALQUERY process. **

        /* Two logical ways of doing this:
            1) Gets called by both RetrofitFunction.NORMALQUERY and RetrofitFunction.READAHEAD and changes nextPageButton status.\
                -- But is not ideal if the RetrofitFunction.READAHEAD takes too long to finish--or doesn't finish at all!
                -- Constant coloring changes on nextPageButton.
            2) If running RetrofitFunction.NORMALQUERY, wait for RetrofitFunction.READAHEAD to finish!
         */

        /* Enables the paging buttons...these are the reasons:
           1) If you kepageButtonsRestoreStateep clicking NextPage, it eventually "goes past" the last page and loads many more records in the List than it should.
           2) The same thing happens with PrevPage.
         */

        // Do until RetrofitReadAHead is finished.
        if (RetrofitReadaHeadClass.readAHeadStatus != RetrofitReadAHead.NOTNEEDED) {
            do {
                delay(250)
            } while (RetrofitReadaHeadClass.readAHeadStatus != RetrofitReadAHead.FINISHED)
        }

//        //
//        // RetrofitReadAHead process is finished...change butPageNext status.
//        Log.i("!!!", "************************************")
//        Log.i("!!!", "    finished with RetrofitReadAHead")
//        Log.i("!!!", "*   known max pages: " + RetrofitReadaHeadClass.knownGoodLastPage)
//        Log.i("!!!", "ProjectData.knownGoodLastPage: ${ProjectData.knownGoodLastPage}")
//        Log.i("!!!", "************************************")

        // reset readAHeadStatus.
        RetrofitReadaHeadClass.readAHeadStatus=RetrofitReadAHead.NOTSTARTED
        changePageButtonsState(currentPage)
    }


    private fun changePageButtonsState(currentPage: Int) = runOnUiThread {
        // Restore button states:
        stopProgressBar()
        recyclerView.visibility=View.VISIBLE
//        butPagePrev.isEnabled=false
        setPrevPageInactive()

        // Saves Shared Preference data.
          if (RetrofitReadaHeadClass.knownGoodLastPage > ProjectData.knownGoodLastPage) {
            ProjectData.knownGoodLastPage = RetrofitReadaHeadClass.knownGoodLastPage
        }

        if (currentPage == RetrofitReadaHeadClass.knownGoodLastPage) {
            Log.i("!!!", "??? RetrofitReadaHeadClass.readAHeadStatus is finished ?????")
            Log.i("!!!", "This is the last page")
            setPrevPageActive()
            setNextPageInActive()
        }
        if (currentPage < RetrofitReadaHeadClass.knownGoodLastPage) {
            // Next Page
            if (currentPage != 1) {
                setPrevPageActive()
            }
            setNextPageActive()
        }
    }


    fun displayData(list : MutableList<RecycleDTO>) {
        //
        // Call RecyclerView Adapter
        //
        recyclerView.apply {
            val llm = LinearLayoutManager(this@MainActivity)
            val adapter = Adapter(list)
            recyclerView.adapter=adapter
            recyclerView.layoutManager=llm
        }
    }

    // Loads two retrofitpages at same time.
    private fun loadRetrofitPages(service: GetBlogService?, currentPage: Int) = runBlocking(Dispatchers.IO) {
        /*
            Because we're using Retrofit, which itself is asynchronous, .await() doesn't work as expected because of
            asynchronous callbacks that retrofit uses...
            everything will have to be done from within Retrofit!  Also, deferred2.isCompleted shows right away!  It truly doesn't wait
            properly.
        */

        // Must run RetrofitFunction.READAHEAD first so it can finish first (hopefully).
        if (RetrofitReadaHeadClass.readAHeadStatus != RetrofitReadAHead.NOTNEEDED) {
            val deferred2 = async {
                runEnqueue(service, currentPage + 1, RetrofitFunction.READAHEAD)
            }
        }
        val deferred1 = async {
            runEnqueue(service, currentPage, RetrofitFunction.NORMALQUERY)}
    }

    private fun parseTitle(title: String): String {
        val titleMod: String

        if (Build.VERSION.SDK_INT >= 24) {
            titleMod= Html.fromHtml(title , Html.FROM_HTML_MODE_LEGACY).toString()
        } else {
            titleMod= Html.fromHtml(title).toString()
        }

        return titleMod
    }


    private fun readAHeadNotNeededCheck() {
        if (RetrofitReadaHeadClass.knownGoodLastPage > (ProjectData.currentPage + 1)) {
            RetrofitReadaHeadClass.readAHeadStatus = RetrofitReadAHead.NOTNEEDED
        }
    }


    // Gets called on next page / prev page, etc.
    private fun prepareNextPage(currentPage: Int) {
        recyclerView.visibility=View.INVISIBLE

        myList.clear()
//        runEnqueue(service, currentPage, RetrofitFunction.NORMALQUERY)
        loadRetrofitPages(service, currentPage)
    }



}
