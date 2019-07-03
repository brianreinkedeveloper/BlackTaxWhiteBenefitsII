package com.blacktaxandwhitebenefits

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.ViewModelStore
import androidx.room.PrimaryKey
import com.blacktaxandwhitebenefits.ObjectEnumClasses.TextSizeIconEnum
import com.blacktaxandwhitebenefits.ProjectData.HTMLTEXTSIZEINCREASEAMOUNT
import com.blacktaxandwhitebenefits.blacktaxandwhitebenefits.ObjectEnumClasses.AppSharedPreferences.setAppSharedPreferencesSync
import com.blacktaxandwhitebenefits.data.SavedBlog
import com.blacktaxandwhitebenefits.viewmodels.SavedBlogViewModel
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_webview.*



class WebViewActivity: AppCompatActivity() {
    private lateinit var titleData: String
    private lateinit var modPostedDate: String
    private lateinit var modDate: String
    private lateinit var urlLink: String
    private lateinit var id: String
    private lateinit var blogArticleData: ArrayList<String>

    // Additional value from SavedBlog.
    private var databaseId: Int = 0
    private var isBookMarkFilled: Boolean = false


    // Bookmark feature change---->
    /*
        blogArticleData[0] --> Article posted date.
        blogArticleData[1] --> Title
        blogArticleData[2] --> Image URL
        blogArticleData[3] --> HTML article
        blogArticleData[4] --> URL Link
        blogArticleData[5] --> ID; unique posted article ID.
     <-------------------| */


    // TextSizeIcon
    private var iconTextSizeSmall: MenuItem? = null
    private var iconTextSizeMedium: MenuItem? = null
    private var iconTextSizeLarge: MenuItem? = null

    // BookmarkIcon
    private var iconBookmarkUnused: MenuItem? = null
    private var iconBookmarkUsed: MenuItem? = null

    private lateinit var mViewModelWebView: SavedBlogViewModel


    /* *************************************************
    onCreate() must be completed first before anything else so it gives the menuOptions widgets
    time to initialize.
    *************************************************  */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        mViewModelWebView = ViewModelProviders.of(this).get(SavedBlogViewModel::class.java)
        mViewModelWebView.getAllSavedBlogs().observe(this, Observer<List<SavedBlog>> {
            checkBlogWasSaved(mViewModelWebView)
        })

        blogArticleData= intent.getStringArrayListExtra(ProjectData.putExtra_BlogWebView)

        imgWebView.setOnClickListener {
            loadArticleWebLink(blogArticleData[4])
        }

        initializeWebView()
        loadPageData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // TextIconSize
        menuInflater.inflate(R.menu.menu_webview, menu)

        this.iconTextSizeSmall = menu?.findItem(R.id.textsizeicon_small)
        this.iconTextSizeMedium = menu?.findItem(R.id.textsizeicon_med)
        this.iconTextSizeLarge = menu?.findItem(R.id.textsizeicon_large)

        this.iconBookmarkUnused = menu?.findItem(R.id.menuitem_bookmark_unused)
        this.iconBookmarkUsed = menu?.findItem(R.id.menuitem_bookmark_used)

        // set icon size
        clickedTextSizeIcon(ProjectData.texticonSizeEnum, "init")

        // bookmark feature
        fillBookmark (this.isBookMarkFilled)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuitem_share -> {
                sendBlog(resources.getString(R.string.app_name), this.titleData, this.urlLink)
                return true }
            R.id.textsizeicon_small -> {
                clickedTextSizeIcon(TextSizeIconEnum.SMALL)
                return true}
            R.id.textsizeicon_med -> {
                clickedTextSizeIcon(TextSizeIconEnum.MEDIUM)
                return true}
            R.id.textsizeicon_large -> {
                clickedTextSizeIcon(TextSizeIconEnum.LARGE)
                return true}
            R.id.menuitem_bookmark_unused -> {
                setBookMarkIcon(false)
                return true }
            R.id.menuitem_bookmark_used -> {
                setBookMarkIcon(true)
                return true }
            else -> return super.onOptionsItemSelected(item)
        }

}


    private fun sendBlog(appTitle: String, blogTitle: String, blogURL: String) {
        val messageStr = "From: $appTitle:\n\n$blogTitle\n\n$blogURL"

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            // Adding Subject Intent in case user wants to send an email:
            putExtra(Intent.EXTRA_SUBJECT, blogTitle)
            putExtra(Intent.EXTRA_TEXT, messageStr)
        }

        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }



    private fun loadPageData() {
        //
        // Load the title.
        //
        this.titleData = blogArticleData[1]
        this.urlLink = blogArticleData[4]
        this.id = blogArticleData[5]

        val maxStringLength: Int = resources.getInteger(R.integer.title_maxlength)
        var currentPos=maxStringLength
        lateinit var tempTitle: String

        if (this.titleData.length > maxStringLength) {
            // Ensure the title has a full word instead of cutting off.
            var charStr = this.titleData.substring(maxStringLength - 1, maxStringLength)
            while (charStr != " ") {
                currentPos--
                charStr = this.titleData.substring(currentPos - 1, currentPos)
            }
            tempTitle = this.titleData.substring(0, currentPos) + "..."
        } else {
            tempTitle = this.titleData
        }

        this.titleData  = tempTitle

        // Sets activity title.
        title = tempTitle


        //
        // Blog posted date: This is the format of the blog: 2018-11-21T21:10:05      (YYYY/month//day/T/hour/min/sec
        //

        // |---Added for bookmark feature change ------->
        if (!(blogArticleData.lastIndex == 6 && blogArticleData[6] == resources.getString(R.string.function_bookmark))) {
            /*  We're running this only if it's not coming from the bookmark activity.  Why?
                 Because the dates from the bookmark activity are already formatted.
             */
            this.modPostedDate = blogDateConversion(blogArticleData[0])
        } else {
            // Coming from bookmark activity.
            this.modPostedDate = blogArticleData[0]
        }
        // <-----bookmark feature change---------|
        this.modDate="Posted Date: $modPostedDate"
        txtWebViewPostedDate.text = modDate


        //
        // Add the Article Web Link text to widget txtWebURLLink.
        //
        val linkText = "<a href='" + this.urlLink + "'>Article Web Link</a>"
        txtWebURLLink.apply {
            txtWebURLLink.text = Html.fromHtml(linkText)
            txtWebURLLink.setMovementMethod(LinkMovementMethod.getInstance())
        }



        //
        // Load the image
        //
        if (blogArticleData[2] != "") {
            Glide.with(this)
                .load(blogArticleData[2])
                .into(imgWebView)
        } else {
            // Load default image.
            Glide.with(this)
                .load(R.drawable.no_image)
                .into(imgWebView)
        }


        //
        // Lastly, load the webview.
        // Note: WebView just needs the html from JSON...it automatically enters in the HTML header info.
        //
        val htmlContext = blogArticleData[3]

        // Replaced webview.loadData with webview.loadDataWithBaseURL.  For some reason, this works better in converting HTML UTF chars.
        webview.loadDataWithBaseURL(null, htmlContext, "text/html", "utf-8", null)
    }



    private fun blogDateConversion(s: String): String {
        // This is the format of the blog: 2018-11-21T21:10:05      (YYYY/month//day/T/hour/min/sec

        // Remove the T section.
        val tPos=s.indexOf("T", ignoreCase = true)
        val modifiedDate=s.substring(0,tPos)

        val yearPos=modifiedDate.indexOf("-")
        val year=modifiedDate.substring(0,yearPos)

        val monthPos=modifiedDate.indexOf("-", yearPos+1)
        val month=modifiedDate.substring(yearPos+1,monthPos)

        var dayPos=modifiedDate.indexOf("-", monthPos+1)
        val day=modifiedDate.substring(monthPos+1,modifiedDate.length)

        return "$month/$day/$year"
    }



    private fun loadArticleWebLink(webURL: String) {
        // Loads the webpage in browser.

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.data = Uri.parse(webURL)
        startActivity(intent)
    }


    private fun initializeWebView() {
        /* Sets TextSizeIcon content */
        // read in value from shared pref.  If no shared pref, set to small.

//        iconTextSizeSmall?.setVisible(true)



        //
        // Changes Webview HTML text size!!
        //
        setWebViewSize(ProjectData.htmlTextSize)
    }


    private fun clickedTextSizeIcon(textSizeEnumVal: TextSizeIconEnum, initialTime: String = "notinit"): Unit {
        // This is the menu item we clicked on...so we go to the next higher one.

        when(textSizeEnumVal) {
            TextSizeIconEnum.SMALL -> {
                if (initialTime != "init") {
                    setSmallHtmlIconSize()
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.MEDIUM
                    ProjectData.htmlTextSize += HTMLTEXTSIZEINCREASEAMOUNT
                } else {
                    // init--sets small actually...assumes we're pushing htmltextsizeicon
                    setLargeHtmlIconSize()
                }
            }
            TextSizeIconEnum.MEDIUM -> {
                if (initialTime != "init") {
                    setMediumHtmlIconSize()
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.LARGE
                    ProjectData.htmlTextSize += HTMLTEXTSIZEINCREASEAMOUNT
                } else {
                    // init--sets medium actually...assumes we're pushing htmltextsizeicon
                    setSmallHtmlIconSize()
                }
            }
            TextSizeIconEnum.LARGE -> {
                if (initialTime != "init") {
                    setLargeHtmlIconSize()
                    ProjectData.texticonSizeEnum = TextSizeIconEnum.SMALL
                    ProjectData.htmlTextSize = ProjectData.HTMLTEXTSIZEDEFAULT
                } else {
                    // init--sets large actually...assumes we're pushing htmltextsizeicon
                    setMediumHtmlIconSize()
                }
            }
        }

        // Changes Webview HTML text size!!
        setWebViewSize(ProjectData.htmlTextSize)

        // Save new setting to sharedPref - async.
        setAppSharedPreferencesSync(this@WebViewActivity, ProjectData.SHAREDPREF_HTMLTEXTSIZE, ProjectData.htmlTextSize.toString())
        setAppSharedPreferencesSync(this@WebViewActivity, ProjectData.SHAREDPREF_HTMLTEXTSIZENUM, ProjectData.texticonSizeEnum.toString())
    }


    private fun setBookMarkIcon(isBookMarkFilledIcon: Boolean) {
        // Clicked on the bookmark save option.
        /* isBookMarkFilledIcon = true --> filled (or saved).  Need to remove from saved list.
           isBookMarkFilledIcon = false --> not filled/not saved.  We now want to save the bookmark.
         */

        // Get a SavedBlog object to insert or delete.
        if (!isBookMarkFilledIcon) {
            // Save bookmark.
            val newSavedBlog = SavedBlog(
                this.titleData,
                this.urlLink,
                this.modPostedDate,
                this.id,
                this.modDate,
                this.blogArticleData[3],          // html article
                this.blogArticleData[2]           // image URL
            )
            mViewModelWebView.insert(newSavedBlog)
        } else {
            // Delete bookmark.  To delete a bookmark, all you need is SavedBlog object, including
            //  the primary key of the database record...here, it is 'databaseId'.
            val newDeletedBlog = SavedBlog(
                this.titleData,
                this.urlLink,
                this.modPostedDate,
                this.id,
                this.modDate,
                this.blogArticleData[3],              // html article
                this.blogArticleData[2],             // image URL
                this.databaseId
            )

            // delete the bookmark.
            mViewModelWebView.delete(newDeletedBlog)
        }

        // Now change the icon.  But we are setting the opposite value.
        fillBookmark(!isBookMarkFilledIcon)
    }




    private fun setWebViewSize(newHtmlTextSize: Int) {
        webview.settings.apply {
            defaultFontSize = newHtmlTextSize
        }
    }


    private fun setSmallHtmlIconSize() {
        iconTextSizeLarge?.setVisible(false)
        iconTextSizeSmall?.setVisible(false)
        iconTextSizeMedium?.setVisible(true)
    }

    private fun setMediumHtmlIconSize() {
        iconTextSizeSmall?.setVisible(false)
        iconTextSizeMedium?.setVisible(false)
        iconTextSizeLarge?.setVisible(true)
    }

    private fun setLargeHtmlIconSize() {
        iconTextSizeLarge?.setVisible(false)
        iconTextSizeMedium?.setVisible(false)
        iconTextSizeSmall?.setVisible(true)
    }


    /* ***********************************************************************************
    -- Checks to see if the Blog was saved in the database...if so, fill in the bookmark icon.
    -- We're doing a simple loop because we're only going to have a limited # of saved bookmarks!
    -- How this works.  We'll loop thru the saved entries from the database and see if the Id matches
    to the current id loaded on page.
    -- If it needs to be saved, calls routine to fill in the bookmark.
  ********************************************************************************** */
    private fun checkBlogWasSaved(mViewModelWebView: SavedBlogViewModel) {
        // 7/1/19

        val blogs_contents = mViewModelWebView.getAllSavedBlogs()
        var counter: Int = -1
        for (i in blogs_contents.value!!) {
            counter++
            if (blogs_contents.value != null) {
                if (this.id == blogs_contents.value!![counter].id) {
                    // Fill in the bookmark.
//                    fillBookmark(true)
                    this.isBookMarkFilled = true        // almost should have a callback.
                    this.databaseId = blogs_contents.value!![counter].databaseId
                }
            }
        }
    }


    private fun fillBookmark(fillBookmarkIcon: Boolean) {
        this.isBookMarkFilled = fillBookmarkIcon

        if (fillBookmarkIcon) {
            // fill the bookmark
            iconBookmarkUnused?.setVisible(false)
            iconBookmarkUsed?.setVisible(true)
        } else {
            // bookmark should be removed.
            iconBookmarkUsed?.setVisible(false)
            iconBookmarkUnused?.setVisible(true)
        }
    }
}
