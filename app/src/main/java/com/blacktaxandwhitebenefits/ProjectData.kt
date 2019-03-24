package com.blacktaxandwhitebenefits

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





    var onSavedState = false
    private const val htmlTextSizeDefault = 22

    // TODO: should be HTML_TEXT_SIZE_DEFAULT = 22
    var htmlTextSize = htmlTextSizeDefault

    var butPrevPageState: Boolean? = false
    var butNextPageState: Boolean? = false

    var buttonClicked=""


    //
    // SHARED PREFERENCE TAGS
    //
    const val putExtra_BlogWebView = "EXTRA_BLOGWEBVIEW"
    const val SHAREDPREF_KNOWNLASTPAGE = "SHAREDPREF_KNOWNLASTPAGE"
}


