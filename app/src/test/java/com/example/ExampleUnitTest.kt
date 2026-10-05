package com.example

import com.example.util.PhoneNumberNormalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPhoneNumberCleaning() {
        val raw = "+52 (993) 123-4567"
        val cleaned = PhoneNumberNormalizer.cleanNumber(raw)
        assertEquals("+529931234567", cleaned)

        val digits = PhoneNumberNormalizer.extractDigits(raw)
        assertEquals("529931234567", digits)
    }

    @Test
    fun testMexicanNumberEquivalence() {
        // Formato internacional vs formato nacional de 10 dígitos
        val num1 = "+52 993 123 4567"
        val num2 = "9931234567"
        val num3 = "+529931234567"
        val num4 = "(993) 123-4567"

        assertTrue(PhoneNumberNormalizer.areSameNumber(num1, num2))
        assertTrue(PhoneNumberNormalizer.areSameNumber(num2, num3))
        assertTrue(PhoneNumberNormalizer.areSameNumber(num1, num4))
    }

    @Test
    fun testDifferentNumbersDoNotMatch() {
        val num1 = "+52 993 123 4567"
        val num2 = "+52 993 123 4568"
        val num3 = "5512345678"

        assertFalse(PhoneNumberNormalizer.areSameNumber(num1, num2))
        assertFalse(PhoneNumberNormalizer.areSameNumber(num1, num3))
    }

    @Test
    fun testFormatForDisplay() {
        val formatted = PhoneNumberNormalizer.formatForDisplay("+529931234567")
        assertEquals("+52 993 123 4567", formatted)

        val formattedLocal = PhoneNumberNormalizer.formatForDisplay("9931234567")
        assertEquals("993 123 4567", formattedLocal)
    }
}
