package sat.imme_login_v2.senderTest;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import sat.imme_login_v2.usertoUserS;
import sat.imme_login_v2.PasswordReset;
import sat.imme_login_v2.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by tianlerk on 14/3/18.
 */

@RunWith(AndroidJUnit4.class)
@LargeTest
public class testNoPhoto {

    @Rule
    public IntentsTestRule<usertoUserS> mLogin = new IntentsTestRule(usertoUserS.class);


    @Test
    public void checkNophoto() {
        onView(withId(R.id.contact_NRIC))
                .perform(typeText("S0394674F"));
        onView(withId(R.id.contact_submit_button))
                .perform(click());

    }
}
