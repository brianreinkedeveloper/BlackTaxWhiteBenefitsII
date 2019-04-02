package com.blacktaxandwhitebenefits

import com.blacktaxandwhitebenefits.ObjectEnumClasses.TextSizeIconEnum

object ProjectData {
    // TODO: Please use UPPER_CASE names for constants
    //https://kotlinlang.org/docs/reference/coding-conventions.html#property-names

    //
    // VARIABLES NEEDED FOR RETROFITREADAHEAD.
    //
    const val maxPagesAtCompile = 1              // Based of how many pages we know is valid.
//    var maxPages: Int = maxPagesAtCompile           //not used at the moment

    // CurrentPage is the current page of the API
    //
    /*  knownGoodLastPage: The last known good page in the API. Used by RetrofitFunction.READAHEAD.  This isn't
        necessarily the last late of the API...just the lastknowngoodpage.
    */
    var currentPage = 1

    // SharedPreferences variables
    var knownGoodLastPage: Int = 1                // Will eventually be loaded from sharedpreferences.



    // Variables for Privacy Policy
    var acceptPrivacyPolicy = false



    var onSavedState = false

    var butPrevPageState: Boolean? = false
    var butNextPageState: Boolean? = false

    var buttonClicked=""


    //
    // SHARED PREFERENCE TAGS
    //
    const val putExtra_BlogWebView = "EXTRA_BLOGWEBVIEW"
    const val SHAREDPREF_KNOWNLASTPAGE = "SHAREDPREF_KNOWNLASTPAGE"
    const val SHAREDPREF_PRIVACYPOLICY = "SHAREDPREF_PRIVACYPOLICY"
    const val SHAREDPREF_HTMLTEXTSIZE = "SHAREDPREF_HTMLTEXTSIZE"
    const val SHAREDPREF_HTMLTEXTSIZENUM = "SHAREDPREF_HTMLTEXTSIZENUM"


    /*  HTMLTextSizeIcon IN WebViewActivity.
    This is the Text-sizing icon in webview.
    */
    // Initially, default to small icon size.
    const val HTMLTEXTSIZEDEFAULT: Int = 18
    const val HTMLTEXTSIZEINCREASEAMOUNT: Int = 3
    var htmlTextSize = HTMLTEXTSIZEDEFAULT
    var texticonSizeEnum = TextSizeIconEnum.SMALL
}


