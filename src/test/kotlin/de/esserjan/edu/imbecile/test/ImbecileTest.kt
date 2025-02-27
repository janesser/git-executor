package de.esserjan.edu.imbecile.test

import de.esserjan.edu.imbecile.Imbecile
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.Test

class ImbecileTest {

    private val underTest = Imbecile()

    @Test
    fun testImbecile() {
        assertThat(underTest.version().outputText, startsWith("git version 2"))
    }
}