package dev.granitkrasniqi.huffman

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CharFrequencyTableTest {

    @Test
    fun `should be able to count frequencies of each character in a string`() {
        // Arrange
        val text = "AaAbbBc1"

        // Act
        val frequencies = CharFrequencyTable(text).compute()

        // Assert
        assertEquals(2, frequencies['A'.code])
        assertEquals(1, frequencies['a'.code])
        assertEquals(2, frequencies['b'.code])
        assertEquals(1, frequencies['B'.code])
        assertEquals(1, frequencies['c'.code])
        assertEquals(1, frequencies['1'.code])
    }

}