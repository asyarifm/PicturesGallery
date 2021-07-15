package com.asyarifm.picturesgallery.activity

import android.app.Activity
import android.content.Context
import android.view.KeyEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.asyarifm.picturesgallery.R
import com.asyarifm.picturesgallery.viewmodel.PicturesGalleryViewModel
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class MainActivityTest {

    @Rule
    @JvmField
    public var activity: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun recycleViewScrollandClickItemTest() {
        //scroll till end of page and click last item
        Espresso.onView(withId(R.id.pictureRecyclerView)).perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(11))
        Espresso.onView(withId(R.id.pictureRecyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(11, click()))

        Espresso.pressBack()
    }

    @Test
    fun switchIconClickTest() {
        //layout switch test
        Espresso.onView(withId(R.id.menu_switch_layout)).perform(click())
        Thread.sleep(1000)
        Espresso.onView(withId(R.id.menu_switch_layout)).perform(click())
        Thread.sleep(1000)
    }

    @Test
    fun searchImageTest() {
        //layout switch test
        Espresso.onView(withId(R.id.menu_search)).perform(click())
        Thread.sleep(1000)

        Espresso.onView(withId(androidx.appcompat.R.id.search_src_text)).perform(typeText("car"));
        Espresso.onView(withId(androidx.appcompat.R.id.search_src_text)).perform(pressKey(KeyEvent.KEYCODE_ENTER))
        Thread.sleep(1000)
    }
}