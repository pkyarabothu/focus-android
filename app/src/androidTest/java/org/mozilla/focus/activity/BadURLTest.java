/* -*- Mode: Java; c-basic-offset: 4; tab-width: 20; indent-tabs-mode: nil; -*-
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.focus.activity;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.RequiresDevice;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mozilla.focus.R;
import org.mozilla.focus.helpers.TestHelper;
import org.mozilla.focus.utils.AppConstants;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static junit.framework.Assert.assertTrue;
import static org.mozilla.focus.fragment.FirstrunFragment.FIRSTRUN_PREF;
import static org.mozilla.focus.helpers.TestHelper.waitingTime;

// This test opens enters and invalid URL, and Focus should provide an appropriate error message
@RunWith(AndroidJUnit4.class)
@RequiresDevice
public class BadURLTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule
            = new ActivityTestRule<MainActivity>(MainActivity.class) {

        @Override
        protected void beforeActivityLaunched() {
            super.beforeActivityLaunched();

            Context appContext = InstrumentationRegistry.getInstrumentation()
                    .getTargetContext()
                    .getApplicationContext();

            // This test is for webview only. Debug is defaulted to Webview, and Klar is used for GV testing.
            org.junit.Assume.assumeTrue(!AppConstants.INSTANCE.isGeckoBuild() && !AppConstants.INSTANCE.isKlarBuild());

            PreferenceManager.getDefaultSharedPreferences(appContext)
                    .edit()
                    .putBoolean(FIRSTRUN_PREF, true)
                    .apply();
        }
    };

    @After
    public void tearDown() {
       mActivityTestRule.getActivity().finishAndRemoveTask();
    }

    @Test
    public void BadURLcheckTest() throws UiObjectNotFoundException {

        UiObject cancelOpenAppBtn = TestHelper.mDevice.findObject(new UiSelector()
                .resourceId("android:id/button2"));
        UiObject openAppalert = TestHelper.mDevice.findObject(new UiSelector()
        .text("Open link in another app"));

        // provide an invalid URL
        TestHelper.inlineAutocompleteEditText.waitForExists(waitingTime);
        TestHelper.inlineAutocompleteEditText.clearTextField();
        TestHelper.inlineAutocompleteEditText.setText("htps://www.mozilla.org");
        TestHelper.hint.waitForExists(waitingTime);
        TestHelper.pressEnterKey();

        // Check for error message
        onWebView(withText(R.string.error_malformedURI_title));
        onWebView(withText(R.string.error_malformedURI_message));
        onWebView(withText("Try Again"));

        TestHelper.floatingEraseButton.perform(click());

        /* provide market URL that is handled by Google Play app */
        TestHelper.inlineAutocompleteEditText.waitForExists(waitingTime);
        TestHelper.inlineAutocompleteEditText.clearTextField();
        TestHelper.inlineAutocompleteEditText.setText("market://details?id=org.mozilla.firefox&referrer=" +
                "utm_source%3Dmozilla%26utm_medium%3DReferral%26utm_campaign%3Dmozilla-org");
        TestHelper.pressEnterKey();

        // Wait for the dialog
        cancelOpenAppBtn.waitForExists(waitingTime);
        assertTrue(openAppalert.exists());
        assertTrue(cancelOpenAppBtn.exists());
        cancelOpenAppBtn.click();
        TestHelper.floatingEraseButton.perform(click());
        TestHelper.erasedMsg.waitForExists(waitingTime);
    }
}
