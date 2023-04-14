package dev.granitkrasniqi.huffman

import java.io.File

class TextFileReader {

    fun read(filePath: String): String {
        return File(filePath).inputStream().readBytes().toString(Charsets.US_ASCII)
    }

}