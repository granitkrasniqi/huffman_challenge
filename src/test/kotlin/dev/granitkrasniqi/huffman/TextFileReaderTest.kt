package dev.granitkrasniqi.huffman

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

class TextFileReaderTest {

    @Test
    fun `should be able to read a file and turn it into a string`() {
        // Arrange
        val file = File("src/test/resources/test.txt")

        // Act
        val textContent = TextFileReader().read(file.absolutePath)

        // Assert
        assertEquals("AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1AaAbbBc1", textContent)
    }

}