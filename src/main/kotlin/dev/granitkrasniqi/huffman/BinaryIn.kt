package dev.granitkrasniqi.huffman

import java.io.*
import java.net.Socket
import java.net.URL

/**
 * *Binary input*. This class provides methods for reading
 * in bits from a binary input stream, either
 * one bit at a time (as a `boolean`),
 * 8 bits at a time (as a `byte` or `char`),
 * 16 bits at a time (as a `short`),
 * 32 bits at a time (as an `int` or `float`), or
 * 64 bits at a time (as a `double` or `long`).
 *
 *
 * The binary input stream can be from standard input, a filename,
 * a URL name, a Socket, or an InputStream.
 *
 *
 * All primitive types are assumed to be represented using their
 * standard Java representations, in big-endian (most significant
 * byte first) order.
 *
 *
 * The client should not intermix calls to `BinaryIn` with calls
 * to `In`; otherwise unexpected behavior will result.
 *
 */
class BinaryIn {
    private var `in`: BufferedInputStream? = null // the input stream
    private var buffer = 0 // one character buffer
    private var n = 0 // number of bits left in buffer

    /**
     * Initializes a binary input stream from standard input.
     */
    constructor() {
        `in` = BufferedInputStream(System.`in`)
        fillBuffer()
    }

    /**
     * Initializes a binary input stream from an `InputStream`.
     *
     * @param is the `InputStream` object
     */
    constructor(`is`: InputStream?) {
        `in` = BufferedInputStream(`is`)
        fillBuffer()
    }

    /**
     * Initializes a binary input stream from a socket.
     *
     * @param socket the socket
     */
    constructor(socket: Socket) {
        try {
            val `is` = socket.getInputStream()
            `in` = BufferedInputStream(`is`)
            fillBuffer()
        } catch (ioe: IOException) {
            System.err.println("Could not open $socket")
        }
    }

    /**
     * Initializes a binary input stream from a URL.
     *
     * @param url the URL
     */
    constructor(url: URL) {
        try {
            val site = url.openConnection()
            val `is` = site.getInputStream()
            `in` = BufferedInputStream(`is`)
            fillBuffer()
        } catch (ioe: IOException) {
            System.err.println("Could not open $url")
        }
    }

    /**
     * Initializes a binary input stream from a filename or URL name.
     *
     * @param name the name of the file or URL
     */
    constructor(name: String) {
        try {
            // first try to read file from local file system
            val file = File(name)
            if (file.exists()) {
                val fis = FileInputStream(file)
                `in` = BufferedInputStream(fis)
                fillBuffer()
                return
            }

            // next try for files included in jar
            var url = javaClass.getResource(name)

            // or URL from web
            if (url == null) {
                url = URL(name)
            }
            val site = url.openConnection()
            val `is` = site.getInputStream()
            `in` = BufferedInputStream(`is`)
            fillBuffer()
        } catch (ioe: IOException) {
            System.err.println("Could not open $name")
        }
    }

    private fun fillBuffer() {
        try {
            buffer = `in`!!.read()
            n = 8
        } catch (e: IOException) {
            System.err.println("EOF")
            buffer = EOF
            n = -1
        }
    }

    /**
     * Returns true if this binary input stream exists.
     *
     * @return `true` if this binary input stream exists;
     * `false` otherwise
     */
    fun exists(): Boolean {
        return `in` != null
    }

    val isEmpty: Boolean
        /**
         * Returns true if this binary input stream is empty.
         *
         * @return `true` if this binary input stream is empty;
         * `false` otherwise
         */
        get() = buffer == EOF

    /**
     * Reads the next bit of data from this binary input stream and return as a boolean.
     *
     * @return the next bit of data from this binary input stream as a `boolean`
     * @throws NoSuchElementException if this binary input stream is empty
     */
    fun readBoolean(): Boolean {
        if (isEmpty) throw NoSuchElementException("Reading from empty input stream")
        n--
        val bit = buffer shr n and 1 == 1
        if (n == 0) fillBuffer()
        return bit
    }

    /**
     * Reads the next 8 bits from this binary input stream and return as an 8-bit char.
     *
     * @return the next 8 bits of data from this binary input stream as a `char`
     * @throws NoSuchElementException if there are fewer than 8 bits available
     */
    fun readChar(): Char {
        if (isEmpty) throw NoSuchElementException("Reading from empty input stream")

        // special case when aligned byte
        if (n == 8) {
            val x = buffer
            fillBuffer()
            return (x and 0xff).toChar()
        }

        // combine last N bits of current buffer with first 8-N bits of new buffer
        var x = buffer
        x = x shl 8 - n
        val oldN = n
        fillBuffer()
        if (isEmpty) throw NoSuchElementException("Reading from empty input stream")
        n = oldN
        x = x or (buffer ushr n)
        return (x and 0xff).toChar()
        // the above code doesn't quite work for the last character if N = 8
        // because buffer will be -1
    }

    /**
     * Reads the next *r* bits from this binary input stream and return
     * as an *r*-bit character.
     *
     * @param  r number of bits to read
     * @return the next `r` bits of data from this binary input stream as a `char`
     * @throws NoSuchElementException if there are fewer than `r` bits available
     * @throws IllegalArgumentException unless `1 <= r <= 16`
     */
    fun readChar(r: Int): Char {
        require(!(r < 1 || r > 16)) { "Illegal value of r = $r" }

        // optimize r = 8 case
        if (r == 8) return readChar()
        var x = 0.toChar()
        for (i in 0 until r) {
            x = (x.code shl 1).toChar()
            val bit = readBoolean()
            if (bit) x = (x.code or 1).toChar()
        }
        return x
    }

    /**
     * Reads the remaining bytes of data from this binary input stream and return as a string.
     *
     * @return the remaining bytes of data from this binary input stream as a `String`
     * @throws NoSuchElementException if this binary input stream is empty or if the number of bits
     * available is not a multiple of 8 (byte-aligned)
     */
    fun readString(): String {
        if (isEmpty) throw NoSuchElementException("Reading from empty input stream")
        val sb = StringBuilder()
        while (!isEmpty) {
            val c = readChar()
            sb.append(c)
        }
        return sb.toString()
    }

    /**
     * Reads the next 16 bits from this binary input stream and return as a 16-bit short.
     *
     * @return the next 16 bits of data from this binary input stream as a `short`
     * @throws NoSuchElementException if there are fewer than 16 bits available
     */
    fun readShort(): Short {
        var x: Short = 0
        for (i in 0..1) {
            val c = readChar()
            x = (x.toInt() shl 8).toShort()
            x = (x.toInt() or c.code).toShort()
        }
        return x
    }

    /**
     * Reads the next 32 bits from this binary input stream and return as a 32-bit int.
     *
     * @return the next 32 bits of data from this binary input stream as a `int`
     * @throws NoSuchElementException if there are fewer than 32 bits available
     */
    fun readInt(): Int {
        var x = 0
        for (i in 0..3) {
            val c = readChar()
            x = x shl 8
            x = x or c.code
        }
        return x
    }

    /**
     * Reads the next *r* bits from this binary input stream return
     * as an *r*-bit int.
     *
     * @param  r number of bits to read
     * @return the next `r` bits of data from this binary input stream as a `int`
     * @throws NoSuchElementException if there are fewer than r bits available
     * @throws IllegalArgumentException unless `1 <= r <= 32`
     */
    fun readInt(r: Int): Int {
        require(!(r < 1 || r > 32)) { "Illegal value of r = $r" }

        // optimize r = 32 case
        if (r == 32) return readInt()
        var x = 0
        for (i in 0 until r) {
            x = x shl 1
            val bit = readBoolean()
            if (bit) x = x or 1
        }
        return x
    }

    /**
     * Reads the next 64 bits from this binary input stream and return as a 64-bit long.
     *
     * @return the next 64 bits of data from this binary input stream as a `long`
     * @throws NoSuchElementException if there are fewer than 64 bits available
     */
    fun readLong(): Long {
        var x: Long = 0
        for (i in 0..7) {
            val c = readChar()
            x = x shl 8
            x = x or c.code.toLong()
        }
        return x
    }

    /**
     * Reads the next 64 bits from this binary input stream and return as a 64-bit double.
     *
     * @return the next 64 bits of data from this binary input stream as a `double`
     * @throws NoSuchElementException if there are fewer than 64 bits available
     */
    fun readDouble(): Double {
        return java.lang.Double.longBitsToDouble(readLong())
    }

    /**
     * Reads the next 32 bits from this binary input stream and return as a 32-bit float.
     *
     * @return the next 32 bits of data from this binary input stream as a `float`
     * @throws NoSuchElementException if there are fewer than 32 bits available
     */
    fun readFloat(): Float {
        return java.lang.Float.intBitsToFloat(readInt())
    }

    /**
     * Reads the next 8 bits from this binary input stream and return as an 8-bit byte.
     *
     * @return the next 8 bits of data from this binary input stream as a `byte`
     * @throws NoSuchElementException if there are fewer than 8 bits available
     */
    fun readByte(): Byte {
        val c = readChar()
        return (c.code and 0xff).toByte()
    }

    companion object {
        private const val EOF = -1 // end of file

        /**
         * Unit tests the `BinaryIn` data type.
         * Reads the name of a file or URL (first command-line argument)
         * and writes it to a file (second command-line argument).
         *
         * @param args the command-line arguments
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val `in` = BinaryIn(args[0])
            val out = BinaryOut(args[1])

            // read one 8-bit char at a time
            while (!`in`.isEmpty) {
                val c = `in`.readChar()
                out.write(c)
            }
            out.flush()
        }
    }
}