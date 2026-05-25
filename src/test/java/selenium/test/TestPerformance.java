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
	private static final int MAX_RUNS = 5;
	
	@Test
	public void testDynamicGrid() throws InterruptedException, MalformedURLException {
		for (int i = 0; i < MAX_RUNS; i++) {
			System.out.println("*** Dynamic Grid run: " + i);
			long timestamp = JavaCs.currentTimeMillis();
			RemoteWebDriver driver = getDriver(DRIVER_URL, "video"); // will create a folder per session
			log("Time to create driver", timestamp);

			runTest(driver, i);

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

	private void runTest(RemoteWebDriver driver, int runNumber) throws InterruptedException {
		log("Begin of test, session " + driver.getSessionId(), 0);
		driver.get(TEST_URL + "?lang=en" + "&run=" + runNumber);
		Thread.sleep(2000);
		driver.get(TEST_URL + "?lang=es" + "&run=" + runNumber);
		Thread.sleep(2000);
		log("End of test", 0);
	}

	private void log(String prompt, long start) {
		System.out.println(new Date().toString().substring(0, 20) + prompt 
				+ (start > 0 ? ": " + (JavaCs.currentTimeMillis() - start) + " ms." : ""));
	}
	
	//////////////////////////////////////////////////////////////////
	/// To prepare next selema release to support event driven video
	//////////////////////////////////////////////////////////////////

	// Remove the video log check to determine if video is copied
	@Test
	public void testSelemaPreloaded20260202() throws InterruptedException, MalformedURLException {
		runSelemaPreloaded("20260202");
	}

	@Test
	public void testSelemaPreloaded20260505() throws InterruptedException, MalformedURLException {
		runSelemaPreloaded("20260505");
	}

	@Test
	public void testSelemaEventDriven20260404() throws InterruptedException, MalformedURLException {
		runSelemaPreloaded("20260404");
	}

	@Test
	public void testSelemaEventDriven20260505() throws InterruptedException, MalformedURLException {
		runSelemaPreloaded("20260505");
	}
	// @Test
	// public void testSelemaEventDrivenHubAndNode() throws InterruptedException, MalformedURLException {
	// runSelemaPreloaded("20260404");
	// }

	private void runSelemaPreloaded(String label) throws InterruptedException, MalformedURLException {
		for (int i = 0; i < MAX_RUNS; i++) {
			String videoContainer = "selenium-video-" + label;
			String videoPrefix = "target/preload-local/";
			String recordedVideo = videoPrefix + label + ".mp4";
			String savedVideo = videoPrefix + label + "-" + i + ".mp4";

			System.out.println("*** Preload run: " + i);
			long timestamp = JavaCs.currentTimeMillis();

			// Before all: check if running event driven mode, video related actions will depend on this mode
			// Note that this is not determined by the SE_VIDEO_EVENT_DRIVEN=true in the recorder,
			// but SE_VIDEO_FILE_NAME=auto
			String env = Docker.getContainerEnv(videoContainer);
			boolean eventDriven = env.contains("SE_VIDEO_FILE_NAME=auto");
			log("Event Driven Mode: " + eventDriven, 0);

			RemoteWebDriver driver = getDriver("http://localhost:4444", "video"); // will create a folder per session
			if (!eventDriven) // In event driven the driver should start the recording
				startRecording(videoContainer, recordedVideo);
			log("Time to start", timestamp);

			runTest(driver, i);

			timestamp = JavaCs.currentTimeMillis();
			if (eventDriven) { // Event driven must save the video after driver quit
				String sessionId = driver.getSessionId().toString();
				driver.quit();
				recordedVideo = videoPrefix + "videomp4_" + sessionId + ".mp4";
				saveRecording(videoContainer, recordedVideo, savedVideo);
			} else {
				stopRecording(videoContainer, recordedVideo, savedVideo);
				driver.quit();
			}
			log("Time to stop", timestamp);
		}
	}

	private void startRecording(String videoContainer, String recordedVideo) {
		// The recorder should be created and stopped in order to start and record video now.
		// If not, stop now
		if (!"exited".equals(Docker.getContainerStatus(videoContainer))) {
			log("Stopping video recorder", 0);
			Docker.runDocker("stop", videoContainer);
		}
		CommandLine.fileDelete(recordedVideo, false);

		log("Starting video recorder", 0);
		Docker.runDocker("start", videoContainer);
		Docker.waitDocker(videoContainer, "Display", "is open", 5);
	}

	private void stopRecording(String videoContainer, String recordedVideo, String savedVideo) {
		Docker.runDocker("stop", videoContainer);
		CommandLine.fileCopy(recordedVideo, savedVideo);
		CommandLine.fileDelete(recordedVideo, true);
	}

	private void saveRecording(String videoContainer, String recordedVideo, String savedVideo) {
		// in event driven the video is stopped on driver quit, just ensure video is stopped and copy video
		Docker.waitDocker(videoContainer, "Video recording stopped", "", 5);
		CommandLine.fileCopy(recordedVideo, savedVideo);
		CommandLine.fileDelete(recordedVideo, true);
	}

}
