package com.splitforge.app.util

import kotlin.random.Random

object IdGenerator {
    fun newId(): String {
        return Random.nextLong().toString(16).replace("-", "")
    }
}
