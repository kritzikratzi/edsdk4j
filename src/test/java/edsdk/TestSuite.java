package edsdk;

import org.junit.runners.Suite;
import org.junit.runner.RunWith;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestJNA.class,TestCanonConstants.class, TestLibrary.class,TestEDSDK.class})
public class TestSuite {
	// nothing
}
