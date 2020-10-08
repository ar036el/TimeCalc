package com.arealapps.timecalc.junitTests

import org.junit.After
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    var tester: Unit? = null

    @Before
    fun setUp() {
        tester = Unit
    }

    @After
    fun tearDown() {
        tester = null
    }


    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
