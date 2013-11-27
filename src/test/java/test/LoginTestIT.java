package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(value = Parameterized.class)
public class LoginTestIT extends Common {

	public LoginTestIT(String browser) {
		super.browser = browser;
		Log.info("Class constructor with " + browser);
	}

	@Test
	public void testLogin() throws Exception {
		Log.info("Starting test with " + super.browser);
		this.login();
		screenCap();
		Thread.sleep(2000);
		this.logout();
	}
}