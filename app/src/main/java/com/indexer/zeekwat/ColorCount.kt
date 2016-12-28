package com.indexer.zeekwat

import java.util.*


class ColorCount<K> : HashMap<K, Int>() {

    fun getCount(value: K): Int {
        if (get(value) == null) {
            return 0
        } else {
            return get(value) as Int
        }
    }

    fun add(value: K) {
        if (get(value) == null) {
            put(value, 1)
        } else {
            put(value, get(value)?.plus(1) as Int)
        }
    }

    operator fun iterator(): Iterator<K> {
        return keys.iterator()
    }
}
