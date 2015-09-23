package in.udacity.learning.shunshine.app;

import android.app.Application;
import android.test.AndroidTestCase;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testThatDemonstrateAssertion() throws Throwable{
        int a = 6;
        int b = 5;
        assertTrue("True", a > b);
        assertEquals("Equal",a,b+1);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}