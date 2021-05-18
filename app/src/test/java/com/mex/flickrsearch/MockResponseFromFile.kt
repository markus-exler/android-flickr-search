package com.mex.flickrsearch

import java.io.InputStreamReader

/**
 * Reads text from json file at the given path and sets [content] to the text
 */
class MockResponseFromFile(path: String) {

    /**
     * json file content as string
     */
    val content: String

    init {
        val reader = InputStreamReader(this.javaClass.classLoader?.getResourceAsStream(path))
        content = reader.readText()
        reader.close()
    }
}