package dev.granitkrasniqi.huffman

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class HuffmanIntTest {

    @Test
    fun `should be able to compress and uncompress a text file`() {
        // Arrange
        val fileToBeCompressed = File("src/test/resources/test.txt")
        val compressedFile = File("src/test/resources/compressed_file.txt")
        val output = BinaryOut(compressedFile.outputStream())
        val textFileReader = TextFileReader()

        // Act
        val huffman = Huffman(textFileReader)
        huffman.compress(fileToBeCompressed.absolutePath, output)

        // Assert
        assertTrue(compressedFile.length() < fileToBeCompressed.length())
    }

    @Test
    fun `should be able to uncompress a text file`() {
        // Arrange
        val fileToBeCompressed = File("src/test/resources/test.txt")
        val compressedFile = File("src/test/resources/compressed_file.txt")
        val uncompressedFile = File("src/test/resources/uncompressed_file.txt")
        val output = BinaryOut(compressedFile.outputStream())
        val textFileReader = TextFileReader()

        // Act
        val huffman = Huffman(textFileReader)
        huffman.compress(fileToBeCompressed.absolutePath, output)
        huffman.uncompress(BinaryIn(compressedFile.inputStream()), BinaryOut(uncompressedFile.outputStream()))

        // Assert
        assertEquals(fileToBeCompressed.length(), uncompressedFile.length())
        assertEquals(BinaryIn(fileToBeCompressed.inputStream()).readString(), BinaryIn(uncompressedFile.inputStream()).readString())
    }

}