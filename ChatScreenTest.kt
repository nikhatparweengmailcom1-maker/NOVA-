package com.nova.assistant

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI tests for ChatScreen.
 * Requires a device or emulator with Hilt support.
 */
@HiltAndroidTest
class ChatScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun novaTitle_isDisplayed() {
        composeRule.onNodeWithText("NOVA").assertIsDisplayed()
    }

    @Test
    fun inputField_isDisplayed() {
        composeRule.onNodeWithText("Message NOVA…").assertIsDisplayed()
    }

    @Test
    fun typeMessage_andSend_showsUserBubble() {
        composeRule.onNodeWithText("Message NOVA…")
            .performTextInput("Hello NOVA")
        composeRule.onNodeWithContentDescription("Send message")
            .performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Hello NOVA").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Hello NOVA").assertIsDisplayed()
    }

    @Test
    fun quickCommandChips_areDisplayed() {
        composeRule.onNodeWithText("Weather").assertIsDisplayed()
        composeRule.onNodeWithText("Joke").assertIsDisplayed()
    }

    @Test
    fun settingsButton_navigatesToSettings() {
        composeRule.onNodeWithContentDescription("Open settings").performClick()
        composeRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun remindersChip_navigatesToReminders() {
        composeRule.onNodeWithText("Reminders").performClick()
        composeRule.onNodeWithText("Reminders").assertIsDisplayed()
    }

    @Test
    fun todosChip_navigatesToTodos() {
        composeRule.onNodeWithText("Todos").performClick()
        composeRule.onNodeWithText("To-Do List").assertIsDisplayed()
    }
}
