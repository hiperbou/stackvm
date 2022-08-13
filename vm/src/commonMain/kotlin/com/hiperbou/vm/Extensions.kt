package com.hiperbou.vm

fun <K,V>MutableMap<K,V>.putIfAbsent(key: K, value: V): V? {
    var v: V? = get(key)
    if (v == null) {
        v = put(key, value)
    }
    return v
}

inline fun assert(value: Boolean, lazyMessage: () -> Any) {
    //if (_Assertions.ENABLED) {
        if (!value) {
            val message = lazyMessage()
            throw AssertionError(message)
        }
    //}
}


