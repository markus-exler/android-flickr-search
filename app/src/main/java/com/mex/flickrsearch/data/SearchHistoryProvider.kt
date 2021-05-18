package com.mex.flickrsearch.data

import android.content.SearchRecentSuggestionsProvider

class SearchHistoryProvider : SearchRecentSuggestionsProvider() {
    init {
        setupSuggestions(AUTHORITY, MODE)
    }

    companion object {
        const val AUTHORITY = "com.mex.flickrsearch.data.SearchHistoryProvider"
        const val MODE: Int = DATABASE_MODE_QUERIES
    }
}