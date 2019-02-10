package com.sppaeknierrnairb.blacktaxandwhitebenefits

object ProjectData {
    // TODO: Please use UPPER_CASE names for constants
    //https://kotlinlang.org/docs/reference/coding-conventions.html#property-names

    const val maxPagesAtCompile = 5               // Base of how many pages we know is valid.
    var maxPages: Int = maxPagesAtCompile

    const val putExtra_BlogWebView = "EXTRA_BLOGWEBVIEW"
    var currentPage = 1

    var onSavedState = false
    private const val htmlTextSizeDefault = 22
    // TODO: should be HTML_TEXT_SIZE_DEFAULT = 22
    var htmlTextSize = htmlTextSizeDefault

    var butPrevPageState = false
    var butNextPageState = false

    var buttonClicked=""
}


