package jp.kozu_osaka.android.kozuzen;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.gson.Gson;

import java.security.NoSuchAlgorithmException;

import jp.kozu_osaka.android.kozuzen.net.argument.post.ResetPasswordArguments;
import jp.kozu_osaka.android.kozuzen.security.HashedString;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        Gson gson = new Gson();
        try {
            ResetPasswordArguments a = new ResetPasswordArguments("mail", HashedString.encrypt("pass"));
        } catch (NoSuchAlgorithmException e) {
            KozuZen.createErrorReport(e);
        }
    }
}