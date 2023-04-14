package dev.granitkrasniqi.huffman

import java.util.PriorityQueue

private const val EXTENDED_ASCII_SIZE = 256

class Huffman(private val textFileReader: TextFileReader) {

    fun compress(filePath: String, output: BinaryOut) {
        val text = textFileReader.read(filePath)
        val frequenciesOfCharacters = CharFrequencyTable(text).compute()
        val root = buildTrie(frequenciesOfCharacters)
        val huffmanCode = arrayOfNulls<String>(EXTENDED_ASCII_SIZE)
        buildCode(huffmanCode, root, "")
        writeTrie(output, root)
        val encodedText = encodeTextUsingHuffmanCode(text, huffmanCode)
        writeEncodedTextToOutput(encodedText, text, output)
    }

    fun uncompress(input: BinaryIn, output: BinaryOut) {
        val root = readTrie(input)
        val length = input.readInt() // number of bytes to write
        decodeUsingHuffmanTrie(length, root, input, output)
        output.close()
    }

    private fun encodeTextUsingHuffmanCode(
        text: String,
        st: Array<String?>
    ): ArrayList<Boolean> {
        val encodedText = ArrayList<Boolean>()
        val textChars = text.toCharArray()
        for (i in textChars.indices) {
            val code = st[textChars[i].code]
            for (j in 0 until code!!.length) {
                if (code[j] == '0') {
                    encodedText.add(false)
                } else if (code[j] == '1') {
                    encodedText.add(true)
                } else throw IllegalStateException("Illegal state")
            }
        }
        return encodedText
    }

    private fun decodeUsingHuffmanTrie(
        length: Int,
        root: Node,
        input: BinaryIn,
        output: BinaryOut
    ) {
        for (i in 0 until length) {
            var x: Node? = root
            while (!x!!.isLeaf) {
                val bit = input.readBoolean()
                x = if (bit) x.right else x.left
            }
            output.write(x.ch, 8)
        }
    }

    private fun readTrie(input: BinaryIn): Node {
        val isLeaf = input.readBoolean()
        return if (isLeaf) {
            Node(input.readChar(), -1, null, null)
        } else {
            Node('\u0000', -1, readTrie(input), readTrie(input))
        }
    }

    private fun writeTrie(output: BinaryOut, x: Node?) {
        if (x!!.isLeaf) {
            output.write(true)
            output.write(x.ch, 8)
            return
        }
        output.write(false)
        writeTrie(output, x.left)
        writeTrie(output, x.right)
    }

    private fun buildCode(st: Array<String?>, x: Node?, s: String) {
        if (!x!!.isLeaf) {
            buildCode(st, x.left, s + '0')
            buildCode(st, x.right, s + '1')
        } else {
            st[x.ch.code] = s
        }
    }

    private fun buildTrie(freq: IntArray): Node {
        val pq: PriorityQueue<Node> = initPriorityQueueWithSingletonTrees(freq)
        mergeTwoSmallestTrees(pq)
        return pq.remove()
    }

    private fun initPriorityQueueWithSingletonTrees(freq: IntArray): PriorityQueue<Node> {
        val pq: PriorityQueue<Node> = PriorityQueue<Node>()
        for (c in 0 until EXTENDED_ASCII_SIZE) if (freq[c.toChar().code] > 0) pq.add(
            Node(
                c.toChar(),
                freq[c],
                null,
                null
            )
        )
        return pq
    }

    private fun mergeTwoSmallestTrees(pq: PriorityQueue<Node>) {
        while (pq.size > 1) {
            val left: Node = pq.remove()
            val right: Node = pq.remove()
            val parent = Node('\u0000', left.freq + right.freq, left, right)
            pq.add(parent)
        }
    }

    private fun writeEncodedTextToOutput(
        encodedText: ArrayList<Boolean>,
        text: String,
        output: BinaryOut
    ) {
        output.write(text.length)
        for (char in encodedText) {
            output.write(char)
        }
        output.flush()
        output.close()
    }

    private class Node(val ch: Char, val freq: Int, val left: Node?, val right: Node?) :
        Comparable<Node> {
        val isLeaf: Boolean
            // is the node a leaf node?
            get() {
                assert(left == null && right == null || left != null && right != null)
                return left == null && right == null
            }

        // compare, based on frequency
        override fun compareTo(other: Node): Int {
            return freq - other.freq
        }
    }

}