package com.blacktaxandwhitebenefits




import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.work.*
import com.blacktaxandwhitebenefits.WorkManager.BackgroundTask
import com.blacktaxandwhitebenefits.WorkManager.NOTIFICATION_WORKREQUEST_TAG
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.BlogArticles
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.RecycleDTO
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    // Retrofit service.
//    private val service = RetrofitClientInstance.retrofitInstance?.create(GetBlogService::class.java)
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
        setupListeners()

        //
        // RetrofitClientInstance
        //
        runEnqueue(ProjectData.currentPage)
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

//        // Turn "off" buttons until network load finishes
//        butPagePrev.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//        butPageNext.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))

        ProjectData.currentPage--
        if (ProjectData.currentPage == 1) {
//            butPagePrev.isEnabled = false

            // This sets prevpage to inactive icon
            setPrevPageInactive()
        } else {
//            butPagePrev.isEnabled = true
//            ProjectData.butNextPageState =butPageNext.isEnabled

            // This sets nextpage to active icon
//            setNextPageActive()
        }
        preparePage(ProjectData.currentPage)
    }


    private fun pressNextPage() {
        // We turn it off until network load is finished.
        ProjectData.buttonClicked ="next"
//        butPageNext.isEnabled=false

        // We have to make both buttons inactive while data loads otherwise strange things occur...wouldn't be an issue
        //   if we had DialogFragment.
        setPrevPageInactive()
        setNextPageInActive()

        // Hide RecyclerView when loading data...why? If visible, the user can scroll on the page and the app will crash!
        recyclerView.visibility=View.INVISIBLE

//        // Turn "off" buttons until network load finishes to give a 'disabled' look.
//        butPagePrev.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//        butPageNext.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))

        ProjectData.currentPage++
        if (ProjectData.currentPage == ProjectData.maxPagesAtCompile) {
//            butPageNext.isEnabled = false
//            ProjectData.butNextPageState =butPageNext.isEnabled

            // disable next page as we're at the end.
            actionbarNavAfterActive?.setEnabled(false)
            actionbarNavAfterInActive?.setEnabled(true)
            ProjectData.butNextPageState = false
        }

        preparePage(ProjectData.currentPage)
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

        // Initially, we don't this button active as there is no page 0.
        if  (!ProjectData.onSavedState) {
//            butPagePrev.isEnabled=false
            setPrevPageInactive()
        }
        Log.i("!!!", "current page" + ProjectData.currentPage.toString())
        pageButtonsSaveState(ProjectData.currentPage)
      
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


    private fun setupListeners() {
//        butPagePrev.setOnClickListener {
//            ProjectData.buttonClicked ="prev"
//
//            // Hide RecyclerView when loading data...why? If visible, the user can scroll on the page and the app will crash!
//            recyclerView.visibility=View.INVISIBLE
//
//            // Turn "off" buttons until network load finishes
//            butPagePrev.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//            butPageNext.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//
//            ProjectData.currentPage--
//            if (ProjectData.currentPage ==1) {
//                butPagePrev.isEnabled = false
//                ProjectData.butPrevPageState =false
//            } else{
//                butPagePrev.isEnabled = true
//                ProjectData.butNextPageState =butPageNext.isEnabled
//            }
//            preparePage(ProjectData.currentPage)
//        }

//        butPageNext.setOnClickListener {
//            // We turn it off until network load is finished.
//            ProjectData.buttonClicked ="next"
//            butPageNext.isEnabled=false
//
//            // Hide RecyclerView when loading data...why? If visible, the user can scroll on the page and the app will crash!
//            recyclerView.visibility=View.INVISIBLE
//
//            // Turn "off" buttons until network load finishes to give a 'disabled' look.
//            butPagePrev.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//            butPageNext.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
//
//            ProjectData.currentPage++
//            if (ProjectData.currentPage == ProjectData.maxPagesAtCompile) {
//                butPageNext.isEnabled = false
//                ProjectData.butNextPageState =butPageNext.isEnabled
//            }
//
//            preparePage(ProjectData.currentPage)
//        }
    }


    private fun preparePage(currentPage: Int) {
        myList.clear()
        runEnqueue(currentPage)
    }


    private fun runEnqueue(currentPage: Int = 1)  {
        progressBar1.visibility=View.VISIBLE

        val call = BackgroundTask.getArticleResponseCall(currentPage)

        call?.enqueue(object : Callback<List<BlogArticles>> {
            override fun onResponse(call: Call<List<BlogArticles>>, response: Response<List<BlogArticles>>) {
                // Retrofit succeeded to get networking and is hitting main url.
                // Retrofit only responds after it gets the data.
                if (response.isSuccessful) {
                    // Check response code in case previous retrofit call was unsuccessful.
                    // success codes: 200-299
                    if (response.code() >= 200 && response.code()< 300) {
                        // Verified good connection...remove 'no internet page' in case it was displaying.
                        rl_maincontent.visibility=View.VISIBLE
                        rl_nointernet.visibility=View.GONE
                        but_refreshconnection.isEnabled=true
                    }

                    val body = response.body()  // The entire JSON body.
                    val bodyLastIndex = body!!.lastIndex

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
                        title= BackgroundTask.convertUTFtoString(title)

                        // Adds to the recycler List DTO.
                        this@MainActivity.myList.add(
                            i,
                            RecycleDTO(
                                title,
                                urlLink,
                                date,
                                id,
                                modifiedDate,
                                htmlArticle,
                                imageBlogURL
                            )
                        )
                    }

                    // Make GUI changes after data is fully loaded.
                    displayData(this@MainActivity.myList)
                    pageButtonsRestoreState()
                    stopProgressBar()
                } else {
                    // response.isSuccessful is not true here.
                    /* Could be several issues but all are issues with the query itself:
                        -- mal-formatted query.
                        --
                     */
                    stopProgressBar()
//                    pageButtonsRestoreState()
                    ll_badresponsecode.visibility=View.VISIBLE

                    var buildErrorString: String = ""
                    buildErrorString += "*** There is a problem with the query. ***"
                    buildErrorString += "URL Error code: " + response.raw().code().toString() + "--"
                    buildErrorString += "Response message: " + response.message()
                    buildErrorString += "...no further details are available."

                    // There won't be any response body here since query was malformed.
                    Log.e("!!!", buildErrorString)
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
                                runEnqueue(ProjectData.currentPage)
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

    private fun stopProgressBar() {
        progressBar1.visibility=View.INVISIBLE
        recyclerView.visibility=View.VISIBLE
    }


    private fun pageButtonsSaveState(currentPage: Int) {
         /* Enables the paging buttons...these are the reasons:
            1) If you keep clicking NextPage, it eventually "goes past" page 5 and loads many more records in the List than it should.
            2) The same thing happens with PrevPage.
          */

        // save button sates
//        ProjectData.butPrevPageState = butPagePrev.isEnabled
//        ProjectData.butNextPageState = butPageNext.isEnabled



//        ProjectData.butPrevPageState = actionbarNavBeforeActive?.isEnabled
//        ProjectData.butNextPageState = actionbarNavAfterActive?.isEnabled

        if (currentPage == 1) {
            setPrevPageInactive()
        }
    }

    private fun pageButtonsRestoreState() {
        /* Enables the paging buttons...these are the reasons:
           1) If you keep clicking NextPage, it eventually "goes past" page 5 and loads many more records in the List than it should.
           2) The same thing happens with PrevPage.
         */

        // Restore button states
        if (ProjectData.currentPage > 1) {
//            butPagePrev.isEnabled=true
//            butPagePrev.setBackgroundColor(resources.getColor(R.color.colorSecondaryLight))
//            butPageNext.setBackgroundColor(resources.getColor(R.color.colorSecondaryLight))
            setPrevPageActive()
            setNextPageActive()

        }
        if (ProjectData.currentPage < ProjectData.maxPages) {
//            butPageNext.isEnabled=true
//            butPageNext.setBackgroundColor(resources.getColor(R.color.colorSecondaryLight))
            setNextPageActive()
        }
        if (ProjectData.currentPage == ProjectData.maxPages) {
//            butPageNext.setBackgroundColor(resources.getColor(R.color.colorWidgetLight))
            setNextPageInActive()
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
}
