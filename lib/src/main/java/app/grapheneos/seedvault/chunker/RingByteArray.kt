package app.grapheneos.seedvault.chunker

internal class RingByteArray(capacity: Int) {

    private val elements = ByteArray(capacity)

    /**
     * number of elements in array
     */
    var size = 0
        private set

    /**
     * index of first element of array
     */
    private var firstElementIndex = 0

    /**
     * index of next available slot
     */
    private var addElementIndex = 0

    init {
        check(capacity >= 0)
    }

    val isEmpty: Boolean get() = size == 0

    fun addAll(bytes: ByteArray) {
        for (aByte in bytes) {
            add(aByte)
        }
    }

    private fun add(aByte: Byte) {
        check(size != elements.size)
        elements[addElementIndex] = aByte
        // wrap-around
        addElementIndex = (addElementIndex + 1) % elements.size
        size++
    }

    fun resetPosition(index: Int) {
        check(index in 0..size)
        // wrap-around
        firstElementIndex = (firstElementIndex + index) % elements.size
        size -= index
    }

    operator fun get(index: Int): Byte {
        check(index in 0..<size)
        // wrap-around
        return elements[(firstElementIndex + index) % elements.size]
    }

    fun getRange(fromInclusive: Int, toExclusive: Int): ByteArray {
        check(fromInclusive in 0..toExclusive)
        check (fromInclusive < size && toExclusive <= size)

        if (fromInclusive == toExclusive) {
            return ByteArray(0)
        }
        // wrap-around
        val from = (firstElementIndex + fromInclusive) % elements.size
        val to = (firstElementIndex + toExclusive) % elements.size
        return if (from < to) {
            elements.copyOfRange(from, to)
        } else {
            // need to wrap array around, 'from' is larger than 'to'
            ByteArray(elements.size - from + to).apply {
                // copy 'from' -> end to beginning of new array
                elements.copyInto(this, 0, from, elements.size)
                // copy beginning -> 'to' to end of new array
                elements.copyInto(this, elements.size - from, 0, to)
            }
        }
    }
}
