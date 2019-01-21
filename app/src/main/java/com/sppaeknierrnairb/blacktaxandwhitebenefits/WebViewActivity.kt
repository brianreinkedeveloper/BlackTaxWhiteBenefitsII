package com.sppaeknierrnairb.blacktaxandwhitebenefits

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.webkit.WebSettings
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_webview.*

class WebViewActivity: AppCompatActivity() {
    private lateinit var titleData: String
    private lateinit var modPostedDate: String
    private lateinit var modDate: String
    private lateinit var urlLink: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        loadPageData()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_share, menu)
        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val menuID = item.itemId
        return when (menuID) {
            R.id.menuitem_share -> {
                sendBlog(resources.getString(R.string.app_name), this.titleData, this.urlLink)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendBlog(appTitle: String, blogTitle: String, blogURL: String) {
        val messageStr = "From: $appTitle:\n\n$blogTitle\n\n$blogURL"

        // TODO: Please take away the subject - you don't want to 'force' the user to share as email
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
        val blogArticleData = intent.getStringArrayListExtra(ProjectData.putExtra_BlogWebView)

        //
        // Load the title.
        //
        this.titleData = blogArticleData[1]
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
        this.modPostedDate = blogDateConversion(blogArticleData[0])

        this.modDate="Posted Date: $modPostedDate"
        txtWebViewPostedDate.text = modDate

        //
        // Load the image
        //
        if (blogArticleData[2] != "") {
            Picasso.get().load(blogArticleData[2]).into(imgWebView)
        } else {
            // Load default image.
            Picasso.get().load(R.drawable.no_image).into(imgWebView)
        }

        // Load the URL Link
        this.urlLink=blogArticleData[4]


        // Lastly, load the webview.
        // Note: WebView just needs the html from JSON...it automatically enters in the HTML header info.
        val htmlContext = blogArticleData[3]
        webview.loadData(htmlContext, "text/html; charset=UTF-8", null)


        //
        // Changes Webview HTML text size!!
        //
        val websettings : WebSettings = webview.settings
        websettings.defaultFontSize = ProjectData.htmlTextSize
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


}
