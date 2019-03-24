package com.blacktaxandwhitebenefits.PageReadaHead

import com.blacktaxandwhitebenefits.ProjectData

object RetrofitReadaHeadClass {
    // All readAHeadStatus variables

    var readAHeadStatus = RetrofitReadAHead.NOTSTARTED
    /*  knownGoodLastPage: The last known good page in the API. Used by RetrofitFunction.READAHEAD.  This isn't
        necessarily the last page of the API...just the lastknowngoodpage.
     */
    var knownGoodLastPage: Int = ProjectData.knownGoodLastPage
}