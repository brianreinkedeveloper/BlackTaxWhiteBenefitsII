package com.blacktaxandwhitebenefits.PageReadaHead

enum class RetrofitErrors (val code: Int) {
    SUCCESS(0),
    ERROR_NONETWORK(-1),
    ERROR_OTHER(-2)
}


// Tells us what asynchronous call Retrofit is using.
enum class RetrofitFunction {
    NORMALQUERY,        // Normal API read into DTO.
    READAHEAD,          // Reads the next page to see if the query if an API page exists.
    OTHER
}

// Tells us the READAHEAD retrofit load status.
enum class RetrofitReadAHead  {
    FINISHED,
    NOTSTARTED,
    NOTFINISHED,
    NOTNEEDED,
    ERROR
}