package com.ruon2.android.activities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.core.app.ApplicationProvider;

import com.ruon2.app.R;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Checks the large-screen behavior of the layouts: content is capped and centered on wide
 * windows (Android 16 ignores the portrait lock on tablets and foldables), stays full width
 * on phones, and the login footer never overlaps the form in short windows.
 *
 * <p>Runs at mdpi, so 1dp == 1px.
 */
@RunWith(RobolectricTestRunner.class)
@Config(qualifiers = "mdpi")
public class AdaptiveLayoutRobolectricTest {

    private static final int TABLET_WIDTH = 1080;
    private static final int TABLET_HEIGHT = 800;
    private static final int PHONE_WIDTH = 360;
    private static final int PHONE_HEIGHT = 780;

    @Test
    public void login_formIsCappedAndCentered_onWideScreen() {
        View root = layout(R.layout.activity_login, TABLET_WIDTH, TABLET_HEIGHT);
        View form = root.findViewById(R.id.login_form);
        assertEquals(480, form.getWidth());
        assertCentered(form, TABLET_WIDTH);
    }

    @Test
    public void login_formIsFullWidth_onPhone() {
        View root = layout(R.layout.activity_login, PHONE_WIDTH, PHONE_HEIGHT);
        assertEquals(PHONE_WIDTH, root.findViewById(R.id.login_form).getWidth());
    }

    @Test
    public void login_footerStaysBelowForm_inShortWindow() {
        View root = layout(R.layout.activity_login, TABLET_WIDTH, 360);
        View form = root.findViewById(R.id.login_form);
        int formBottom = absoluteTop(form) + form.getHeight();
        assertTrue(absoluteTop(root.findViewById(R.id.version_name)) >= formBottom);
        assertTrue(absoluteTop(root.findViewById(R.id.copyright)) >= formBottom);
    }

    @Test
    public void login_footerIsAtBottom_inTallWindow() {
        View root = layout(R.layout.activity_login, PHONE_WIDTH, PHONE_HEIGHT);
        View version = root.findViewById(R.id.version_name);
        // 16dp bottom margin
        assertEquals(PHONE_HEIGHT - 16, absoluteTop(version) + version.getHeight());
    }

    @Test
    public void alarmList_isCappedAndCentered_onWideScreen() {
        View root = layout(R.layout.activity_main, TABLET_WIDTH, TABLET_HEIGHT);
        View list = root.findViewById(R.id.mList);
        assertEquals(840, list.getWidth());
        assertCentered(list, TABLET_WIDTH);
    }

    @Test
    public void alarmList_isFullWidth_onPhone() {
        View root = layout(R.layout.activity_main, PHONE_WIDTH, PHONE_HEIGHT);
        assertEquals(PHONE_WIDTH, root.findViewById(R.id.mList).getWidth());
    }

    @Test
    public void alarmDetails_areCappedAndCentered_onWideScreen() {
        View root = layout(R.layout.alarm_details_activity, TABLET_WIDTH, TABLET_HEIGHT);
        View content = root.findViewById(R.id.details_content);
        assertEquals(840, content.getWidth());
        assertCentered(content, TABLET_WIDTH);
    }

    @Test
    public void alarmDetails_areFullWidth_onPhone() {
        View root = layout(R.layout.alarm_details_activity, PHONE_WIDTH, PHONE_HEIGHT);
        assertEquals(PHONE_WIDTH, root.findViewById(R.id.details_content).getWidth());
    }

    private static View layout(int layoutRes, int width, int height) {
        Context context = new ContextThemeWrapper(
                ApplicationProvider.getApplicationContext(), R.style.AppTheme);
        View root = LayoutInflater.from(context).inflate(layoutRes, null, false);
        root.measure(
                View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
        root.layout(0, 0, width, height);
        return root;
    }

    private static void assertCentered(View view, int parentWidth) {
        int left = absoluteLeft(view);
        assertEquals(parentWidth - (left + view.getWidth()), left);
    }

    private static int absoluteLeft(View view) {
        int left = 0;
        for (View v = view; v != null; v = v.getParent() instanceof View ? (View) v.getParent() : null) {
            left += v.getLeft();
        }
        return left;
    }

    private static int absoluteTop(View view) {
        int top = 0;
        for (View v = view; v != null; v = v.getParent() instanceof View ? (View) v.getParent() : null) {
            top += v.getTop();
        }
        return top;
    }
}
