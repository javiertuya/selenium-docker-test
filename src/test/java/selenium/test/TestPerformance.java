package selenium.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import giis.portable.util.JavaCs;

/**
 * Checks performance of different configurations.
 * The workflow spins-up the the selenium containers and the precise versions
 */
public class TestPerformance {

	private static final String DRIVER_URL = "http://127.0.0.1:4444";
	private static final String TEST_URL = "https://giis.uniovi.es";
	private static final int MAX_RUNS = 2;
	
	@Test
	public void testDynamicGrid() throws InterruptedException, MalformedURLException {
		for (int i = 0; i < MAX_RUNS; i++) {
			System.out.println("*** Dynamic Grid run: " + i);
			long timestamp = JavaCs.currentTimeMillis();
			RemoteWebDriver driver = getDriver(DRIVER_URL, "video"); // will create a folder per session
			log("Time to create driver", timestamp);

			runTest(driver);

			timestamp = JavaCs.currentTimeMillis();
			driver.quit();
			log("Time to close driver", timestamp);
		}
	}

	private RemoteWebDriver getDriver(String url, String videoName) throws MalformedURLException {
		ChromeOptions options = new ChromeOptions();
		options.setCapability("se:recordVideo", true);
		options.setCapability("se:name", videoName + ".mp4");
		return new RemoteWebDriver(new URL(url), options);
	}

	private void runTest(RemoteWebDriver driver) throws InterruptedException {
		log("Begin of test, session " + driver.getSessionId(), 0);
		driver.get(TEST_URL + "?lang=en");
		Thread.sleep(2000);
		driver.get(TEST_URL + "?lang=es");
		Thread.sleep(2000);
		log("End of test", 0);
	}

	private void log(String prompt, long start) {
		System.out.println(new Date().toString().substring(0, 20) + prompt 
				+ (start > 0 ? ": " + (JavaCs.currentTimeMillis() - start) + " ms." : ""));
	}

}
