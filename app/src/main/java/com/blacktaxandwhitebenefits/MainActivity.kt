package com.blacktaxandwhitebenefits



import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.*
import com.blacktaxandwhitebenefits.ObjectEnumClasses.TextSizeIconEnum
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitFunction
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitReadAHead
import com.blacktaxandwhitebenefits.PageReadaHead.RetrofitReadaHeadClass
import com.blacktaxandwhitebenefits.WorkManager.BackgroundTask
import com.blacktaxandwhitebenefits.WorkManager.NOTIFICATION_WORKREQUEST_TAG
import com.blacktaxandwhitebenefits.adapters.BlogAdapter
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.BlogArticles
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.GetBlogService
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.Blog
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.Networking.RetrofitClientInstance
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.getAppSharedPreferences
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.getAppSharedPreferencesInt
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.setAppSharedPreferencesAsync
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.setAppSharedPreferencesSync
import com.blacktaxandwhitebenefits.data.SavedBlog
import com.blacktaxandwhitebenefits.viewmodels.SavedBlogViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.TimeUnit






class MainActivity : AppCompatActivity() {

    // Firebase Analytics
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    // Retrofit service.
    private val service = RetrofitClientInstance.retrofitInstance?.create(GetBlogService::class.java)
    var myList = mutableListOf<Blog>()

    // Action bar icons
    var actionbarNavAfterActive: MenuItem? = null
    var actionbarNavAfterInActive: MenuItem? = null
    var actionbarNavBeforeActive: MenuItem? = null
    var actionbarNavBeforeInActive: MenuItem? = null

    // ViewModel for saved blogs.
    private lateinit var savedBlogViewModel: SavedBlogViewModel


    //
    // MainActivity
    //
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // We must setup out code here in onCreate() here otherwise things won't work correctly.

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)


        // PrivacyPolicy
        val privacyPolicyAccept = initialize()
        if (privacyPolicyAccept) {
            //
            // RetrofitClientInstance
            //
            loadRetrofitPages(service, ProjectData.currentPage)
        }
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
            R.id.menu_showbookmarks -> {
                // bookmark collection
                showBookMarkList()
                return true
            }
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



    private fun initialize(): Boolean {
//        butPagePrev.text="<"
//        butPageNext.text=">"

        // Gets Privacy Policy from SharedPref
        getsPreferenceData()

        if (!ProjectData.acceptPrivacyPolicy) {
            displayPrivacyPolicy()
            listenPrivacyAcceptance()
        }

        if (ProjectData.acceptPrivacyPolicy) {
            // Initially, we don't this button active as there is no page 0.
            if (!ProjectData.onSavedState) {
                setPrevPageInactive()
            }
            Log.i("!!!", "current page" + ProjectData.currentPage.toString())
            initBackgroundTask()
        }

        return ProjectData.acceptPrivacyPolicy
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
                        rl_maincontent.visibility=View.GONE
                        ll_badresponsecode.visibility=View.VISIBLE
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
                            runEnqueue(service, currentPage, RetrofitFunction.NORMALQUERY)
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
                Blog(title, urlLink, date, id, modifiedDate, htmlArticle, imageBlogURL)
            )
        }

        Log.i("!!!", "RetrofitFunction.NORMALQUERY: "+ currentPage)
        Log.i("!!!", "RetrofitFunction.NORMALQUERY: "+ RetrofitReadaHeadClass.knownGoodLastPage)
        displayData(this@MainActivity.myList)
        Log.i("!!!", "retrofitCall: " + retrofitCall.toString())
        pageButtonsRestoreState(retrofitCall, currentPage)

        stopProgressBar()
    }


    private fun stopProgressBar() {
        progressBar1.visibility=View.INVISIBLE
        recyclerView.visibility=View.VISIBLE
    }


    private fun pageButtonsRestoreState(retrofitCall: RetrofitFunction, currentPage: Int) = GlobalScope.launch {
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

        // Only run for Readahead work!
        if (retrofitCall == RetrofitFunction.READAHEAD) {
            // Do until RetrofitReadAHead is finished.
            if (RetrofitReadaHeadClass.readAHeadStatus != RetrofitReadAHead.NOTNEEDED) {
                do {
                    delay(250)
                } while (RetrofitReadaHeadClass.readAHeadStatus != RetrofitReadAHead.FINISHED)
            }
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
//        stopProgressBar()
//        recyclerView.visibility=View.VISIBLE
////        butPagePrev.isEnabled=false
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


    fun displayData(list : MutableList<Blog>) {
        //
        // Call RecyclerView BlogAdapter
        //
        recyclerView.apply {
            val llm = LinearLayoutManager(this@MainActivity)
            val adapter = BlogAdapter(list)
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




    private fun displayPrivacyPolicy() {
        var privacyPolicyStr = "<p><b>Black Tax and White Benefits Privacy Policy</b></p>\n" +
                "<p>Neil Jay Warner and associates built the BlackTax and White Benefits app as an Open Source app. This SERVICE is provided by Neil Jay Warner and associates at no cost and is\n" +
                "intended for use as is.</p>\n" +
                "<p>This page is used to inform visitors regarding my policies with the collection, use, and disclosure of Personal Information if anyone decided to use my Service.</p>\n" +
                "<p>If you choose to use my Service, then you agree to the collection and use of information in relation to this policy. The Personal Information that I collect is used for providing\n" +
                "and improving the Service. I will not use or share your information with anyone except as described in this Privacy Policy.</p>\n" +
                "<p>The terms used in this Privacy Policy have the same meanings as in our Terms and Conditions, which is accessible at BlackTax and White Benefits unless otherwise defined in this\n" +
                "Privacy Policy.<br/></p>\n" +
                "<p><b>Information Collection and Use</b></p>\n" +
                "<p>For a better experience, while using our Service, I may require you to provide us with certain personally identifiable information. The information that I request will be\n" +
                "retained on your device and is not collected by me in any way.</p>\n" +
                "<p>The app does use third party services that may collect information used to identify you.</p>\n" +
                "<p>Link to privacy policy of third party service providers that may be used by the app:<br/>* Google Play Services<br/>* Firebase Analytics<br/>* Crashlytics</p>\n" +
                "<p><b>Log Data</b></p>\n" +
                "<p>I want to inform you that whenever you use my Service, in a case of an error in the app I collect data and information (through third party products) on your phone called Log\n" +
                "Data. This Log Data may include information such as your device Internet Protocol (\"IP\") address, device name, operating system version, the configuration of the app when\n" +
                "utilizing my Service, the time and date of your use of the Service, and other statistics.</p>\n" +
                "<p><b>Cookies</b></p>\n" +
                "<p>Cookies are files with a small amount of data that are commonly used as anonymous unique identifiers. These are sent to your browser from the websites that you visit and are\n" +
                "stored on your device's internal memory. This Service does not use these \"cookies\" explicitly. However, the app may use third party code and libraries that use 'cookies'; to collect information and improve their\n" +
                "services. You have the option to either accept or refuse these cookies and know when a cookie is being sent to your device. If you choose to refuse our cookies, you may not be \n" +
                "able to use some portions of this Service.</p>\n" +
                "<p><b>Service Providers</b></p>\n" +
                "<p>I may employ third-party companies and individuals due to the following reasons:<br/>* To facilitate our Service;<br/>* To provide the Service on our behalf;<br/>* To perform Service-related services; or<br/>* To assist us in analyzing how our Service is used.</p>\n" +
                "<p>I want to inform users of this Service that these third parties have access to your Personal Information. The reason is to perform the tasks assigned to them on our behalf.\n" +
                "However, they are obligated not to disclose or use the information for any other purpose.</p>\n" +
                "<p><b>Security</b></p>\n" +
                "<p>I value your trust in providing us your Personal Information, thus we are striving to use commercially acceptable means of protecting it. But remember that no method of\n" +
                "transmission over the internet, or method of electronic storage is 100% secure and reliable, and I cannot guarantee its absolute security.</p>\n" +
                "<p><b>Links to Other Sites</b></p>\n" +
                "<p>This Service may contain links to other sites. If you click on a third-party link, you will be directed to that site. Note that these external sites are not operated by me.\n" +
                "Therefore, I strongly advise you to review the Privacy Policy of these websites. I have no control over and assume no responsibility for the content, privacy policies, or\n" +
                "practices of any third-party sites or services.</p>\n" +
                "<p><b>Children's Privacy</b></p>\n" +
                "<p>These Services do not address anyone under the age of 13. I do not knowingly collect personally identifiable information from children under 13. In the case I discover that a\n" +
                "child under 13 has provided me with personal information, I immediately delete this from our servers. If you are a parent or guardian and you are aware that your child has\n" +
                "provided us with personal information, please contact me so that I will be able to do necessary actions.</p>\n" +
                "<p><b>Changes to This Privacy Policy</b></p>\n" +
                "<p>I may update our Privacy Policy from time to time. Thus, you are advised to review this page periodically for any changes. I will notify you of any changes by posting the new\n" +
                "Privacy Policy on this page. These changes are effective immediately after they are posted on this page.<p>\n" +
                "<p><b>Contact Us</b></p>\n" +
                "<p>If you have any questions or suggestions about my Privacy Policy, do not hesitate to contact me. This privacy policy page was created at privacypolicytemplate.net and modified/generated by \n" +
                "<a href=\"https://app-privacy-policy-generator.firebaseapp.com/\">App Privacy Policy Generator</a></p><p><b>" +
                "<p> </p>"


        txt_privacypolicy.loadData(privacyPolicyStr,"text/html", "utf-8")
        rl_maincontent.visibility = View.GONE
        rl_privacypolicy.visibility = View.VISIBLE
    }


    private fun listenPrivacyAcceptance() {
        // Listen for Privacy Acceptance
        but_privacy_reject.setOnClickListener {
            finish()
        }

        but_privacy_accept.setOnClickListener {
            ProjectData.acceptPrivacyPolicy=true
            setAppSharedPreferencesSync(this@MainActivity, ProjectData.SHAREDPREF_PRIVACYPOLICY, ProjectData.acceptPrivacyPolicy.toString())
            rl_privacypolicy.visibility=View.GONE

            // restart MainActivity.
            val intent = intent
            finish()
            startActivity(intent)
        }
    }


    private fun getsPreferenceData() {
        // Sets last known good page from sharedpref
        ProjectData.knownGoodLastPage = getAppSharedPreferencesInt(this@MainActivity, ProjectData.SHAREDPREF_KNOWNLASTPAGE)
        if (ProjectData.knownGoodLastPage > RetrofitReadaHeadClass.knownGoodLastPage) {
            RetrofitReadaHeadClass.knownGoodLastPage = ProjectData.knownGoodLastPage
        }

        // Gets information regarding Privacy Policy
        var privacyAcceptanceStringValue = getAppSharedPreferences(this@MainActivity, ProjectData.SHAREDPREF_PRIVACYPOLICY)!!
        if (privacyAcceptanceStringValue == "") {
            ProjectData.acceptPrivacyPolicy = false
        } else {
            ProjectData.acceptPrivacyPolicy = privacyAcceptanceStringValue.toBoolean()
        }

        //
        // Gets information regarding HTML Text Sizes
        //
        val getHtmlTextSize = getAppSharedPreferences(this@MainActivity, ProjectData.SHAREDPREF_HTMLTEXTSIZE)
        if (getHtmlTextSize != "") {
            // assign our new value.
            // Note: ProjectData.htmlTextSize already comes with a default value!
            ProjectData.htmlTextSize = getHtmlTextSize!!.toInt()
        }

        val getHtmlTextSizeEnum = getAppSharedPreferences(this@MainActivity, ProjectData.SHAREDPREF_HTMLTEXTSIZENUM)
        if (getHtmlTextSizeEnum != "") {
            // assign our new value.
            // Note: ProjectData.texticonSizeEnum already comes with a default value!
            when (getHtmlTextSizeEnum) {
                TextSizeIconEnum.SMALL.name -> {
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.SMALL
                    ProjectData.htmlTextSize = ProjectData.HTMLTEXTSIZEDEFAULT
                }
                TextSizeIconEnum.MEDIUM.name -> {
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.MEDIUM
                    ProjectData.htmlTextSize = ProjectData.HTMLTEXTSIZEDEFAULT + ProjectData.HTMLTEXTSIZEINCREASEAMOUNT
                }
                TextSizeIconEnum.LARGE.name -> {
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.LARGE
                    ProjectData.htmlTextSize = ProjectData.HTMLTEXTSIZEDEFAULT + ProjectData.HTMLTEXTSIZEINCREASEAMOUNT + ProjectData.HTMLTEXTSIZEINCREASEAMOUNT
                }
            }
        }
    }


    /*  Loads SavedBookMarksActivity.  */
    private fun showBookMarkList() {
        val intent = Intent(baseContext, SavedBookMarksActivity::class.java)
        startActivity(intent)
    }




}
