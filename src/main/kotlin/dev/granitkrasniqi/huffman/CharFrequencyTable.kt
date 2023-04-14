package dev.granitkrasniqi.huffman


private const val EXTENDED_ASCII_SIZE = 256

class CharFrequencyTable(private val text: String) {

    fun compute(): IntArray {
        val frequencies = IntArray(EXTENDED_ASCII_SIZE)
        val textAsCharArray = text.toCharArray()
        for (value in textAsCharArray) {
            frequencies[value.code]++
        }
        return frequencies
    }

}