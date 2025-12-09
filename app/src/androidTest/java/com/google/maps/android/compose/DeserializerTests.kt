package com.google.maps.android.compose

import androidx.compose.ui.test.junit4.createComposeRule
import com.google.maps.android.compose.directions.JsonDeserializer
import com.google.maps.android.compose.utils.ClientEntry
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals

class DeserializerTests {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val deserializer = JsonDeserializer()
    private val clientsJsonString = """[
      {
        "color": 2162616058,
        "cost": "3",
        "name": "Elena",
        "starRating": 4.8
      },
      {
        "color": 2164251321,
        "cost": "4",
        "name": "Teo",
        "starRating": 5.0
      },
      {
        "color": 2163245184,
        "cost": "8",
        "name": "Doru Terminatoru",
        "starRating": 3.2
      }
    ]
    """
    private val expectedClients = listOf(
        ClientEntry(
            name = "Elena",
            cost = "3",
            starRating = 4.8f,
            color = 0x80E6E6FA
        ), ClientEntry(
            name = "Teo",
            cost = "4",
            starRating = 5f,
            color = 0x80FFDAB9
        ), ClientEntry(
            name = "Doru Terminatoru",
            cost = "8",
            starRating = 3.2f,
            color = 0x80F08080
        )
    )

    @Test
    fun testStartingCameraPosition() {
        val clientsDeserialized = deserializer.deserialize(clientsJsonString)
        assertEquals(clientsDeserialized, expectedClients)
    }
}