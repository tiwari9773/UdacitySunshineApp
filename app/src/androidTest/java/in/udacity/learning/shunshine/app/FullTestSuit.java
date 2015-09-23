package in.udacity.learning.shunshine.app;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by Lokesh on 24-09-2015.
 */
public class FullTestSuit extends TestSuite {
    public static Test suite() {
        TestSuite builder = new TestSuiteBuilder(FullTestSuit.class).includeAllPackagesUnderHere().build();
        return builder;
    }

    public FullTestSuit() {
        super();
    }
}
