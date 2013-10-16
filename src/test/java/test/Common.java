package test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.Parameterized.Parameters;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Platform;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

public abstract class Common {

	@Parameters
	public static Collection<Object[]> data() throws IOException, FileNotFoundException {
		
		String browser = "";
		BufferedReader br;		
		List<Object[]> list = new ArrayList<Object[]>();
		
		br = new BufferedReader(new FileReader("src/test/resources/browsers.dat"));
		while ((browser = br.readLine()) != null) {

			if (!browser.startsWith("#")) {
				Object[] aBrowser = new String[] {browser};
				list.add(aBrowser);
			}
		}
		br.close();
		br = null;

		return list;
	}

	@Rule
	public TestWatcher watchman = new TestWatcher() {
		@Override
		protected void failed(Throwable e, Description d) {
			Log.error("Failed at " + d.getClassName() + " en " + browser);
			if (driver != null) {
				screenCap();
				driver.close();
				driver.quit();
			}
		}

		@Override
		protected void succeeded(Description d) {
			Log.info("Done with " + d.getClassName() + " en "
					+ browser);
			driver.close();
			driver.quit();
		}
	};

	public String browser;
	public Logger Log = Logger.getLogger(this.getClass());
	private String caps = ".//target//failsafe-reports//" + this.getClass().getName() + "//";
	private WebDriver driver;

	private final String qmurl = "http://129.181.202.30:8080/qm";
	private final String user = "bullqa";
	private final String pass = "bullqa";
	private final String SELENIUM_HUB = "http://129.181.202.121:4444/wd/hub";

	public void logout() throws Exception {
		try {
			driver.findElement(By.id("sign-out")).click();
			String serverMsg = "Está usted en la página de bienvenida";
			Thread.sleep(1000);
			String src = driver.getPageSource();
			String found = "";
			if (src.indexOf(serverMsg) != -1) {
				found = serverMsg;
			}
			assertThat("After-logout message not found",serverMsg, is(found));
			Log.info("Logged out");
		} catch (TimeoutException e) {
			fail("Timeout loading " + qmurl);
		}
	}
	public void login() throws Exception {
		try {
			Log.info("Login in");
			driver.get(qmurl);
			driver.findElement(By.id("_58_login")).sendKeys(user);
			driver.findElement(By.id("_58_password")).sendKeys(pass);
			driver.findElement(By.className("aui-button-input-submit")).click();
			Thread.sleep(1000);
			String serverMsg = "Valores";
			String src = driver.getPageSource();
			String found = "";
			if (src.indexOf(serverMsg) != -1) {
				found = serverMsg;
			}
			assertThat("After-login message not found", serverMsg, is(found));
			Log.info("Logged in");
		} catch (TimeoutException e) {
			fail("Timeout en la carga de " + qmurl);
		}
	}

	public void screenCap() {
		try {
			if (!browser.equals("htmlunit")	&& !browser
							.equals("org.apache.jmeter.protocol.java.sampler.JUnitSampler")) {
				File scrFile = ((TakesScreenshot) driver)
						.getScreenshotAs(OutputType.FILE);
				FileUtils.copyFile(scrFile,
						new File(caps + System.currentTimeMillis() / 1000
								+ browser + ".png"));
			}
		} catch (Exception e) {
			Log.error("Error en la captura de pantalla " + e.toString());
		}
	}

	@Before
	public void setUp() throws MalformedURLException {
		Log.debug("Setting up");
		DesiredCapabilities capabilities = null;

		if (browser.equalsIgnoreCase("Firefox")) {
			capabilities = DesiredCapabilities.firefox();
		} else if (browser.equalsIgnoreCase("IE8")) {
			capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setVersion("8");
		} else if (browser.equalsIgnoreCase("IE9")) {
			capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setVersion("9");
		} else if (browser.equalsIgnoreCase("Chrome")) {
			capabilities = DesiredCapabilities.chrome();
		} else if (browser.equalsIgnoreCase("htmlunit")
				|| browser.equals("org.apache.jmeter.protocol.java.sampler.JUnitSampler")) {
			capabilities = DesiredCapabilities.htmlUnit();
		} else if (browser.equalsIgnoreCase("phantomjs")) {
			capabilities = DesiredCapabilities.phantomjs();
			capabilities.setBrowserName("phantomjs");
			capabilities.setCapability("seleniumProtocol", "WebDriver");
		} else {
			fail("Unconfigured browser " + browser);
		}

		if (!browser.equals("phantomjs")) {
			capabilities.setPlatform(Platform.WINDOWS);
		}

		driver = new RemoteWebDriver(new URL(SELENIUM_HUB), capabilities);

		driver = new Augmenter().augment(driver);
		driver.manage().window().setSize(new Dimension(1024, 768));
		driver.manage().window().maximize();

		File directory = new File(caps);
		directory.mkdirs();
		driver.manage().window().maximize();
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
	}

	@After
	public void tearDown() throws Exception {
		if (browser
				.equals("org.apache.jmeter.protocol.java.sampler.JUnitSampler")) {
			Log.debug("Empty teardown");
			driver.close();
			driver.quit();
		}
	}
}