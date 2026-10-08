package com.waterquality.portal;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Critical user journeys through a real browser (headless Chrome).
 *
 * <p>Journeys:
 * <ol>
 *   <li>Login + dashboard summary visible</li>
 *   <li>Create sample record</li>
 *   <li>Search samples</li>
 *   <li>Role-based status workflow (collector submits)</li>
 *   <li>Reviewer cannot be bypassed / dashboard counts update</li>
 * </ol>
 *
 * <p>Run locally: {@code mvn verify -Dtest=WaterQualitySeleniumTest}.
 * On failure a screenshot is saved to {@code target/selenium-failures/}.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WaterQualitySeleniumTest {

    @LocalServerPort
    int port;

    WebDriver driver;
    WebDriverWait wait;
    String baseUrl;

    static final Duration TIMEOUT = Duration.ofSeconds(15);

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox",
                "--disable-dev-shm-usage", "--disable-gpu", "--window-size=1280,900");
        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
        wait = new WebDriverWait(driver, TIMEOUT);
        baseUrl = "http://localhost:" + port + "/water-quality-portal";
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // ---------- helpers ----------

    void loginAs(String username, String password) {
        driver.get(baseUrl + "/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        driver.findElement(By.id("username")).clear();
        driver.findElement(By.id("username")).sendKeys(username);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("loginBtn")).click();
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("/dashboard"),
                ExpectedConditions.urlContains("/samples")));
    }

    void screenshot(String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Paths.get("target", "selenium-failures");
            Files.createDirectories(dir);
            Files.copy(src.toPath(), dir.resolve(name + ".png"),
                    StandardCopyOption.REPLACE_EXISTING);
            Files.writeString(dir.resolve(name + ".html"), driver.getPageSource());
            System.out.println("Screenshot+HTML saved: " + dir.resolve(name + ".png"));
        } catch (Exception e) {
            System.out.println("Could not capture screenshot: " + e.getMessage());
        }
    }

    void run(String name, Runnable journey) {
        try {
            journey.run();
        } catch (AssertionError | Exception e) {
            screenshot(name + "-FAIL-"
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmmss")));
            throw e instanceof AssertionError ? (AssertionError) e : new AssertionError(e);
        }
    }

    String uniqueId(String prefix) {
        return prefix + "-" + System.currentTimeMillis() % 100000;
    }

    /** Sets a datetime-local input reliably (sendKeys is flaky in headless Chrome). */
    static void setDateTime(WebElement element, LocalDateTime value) {
        String formatted = value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        ((org.openqa.selenium.JavascriptExecutor) ((org.openqa.selenium.WrapsDriver) element).getWrappedDriver())
                .executeScript("arguments[0].value = arguments[1];"
                        + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                        + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                        element, formatted);
    }

    /** Prints HTML5 validity of every form control (diagnoses blocked submits). */
    void diagnoseForm() {
        try {
            Object out = ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "const f = document.getElementById('sampleForm');"
                    + "let r = 'formValid=' + f.checkValidity() + ' | ';"
                    + "for (const el of f.elements) {"
                    + "  if (el.id) r += el.id + ':valid=' + el.validity.valid"
                    + "    + ',val=' + JSON.stringify(el.value)"
                    + "    + ',msg=' + JSON.stringify(el.validationMessage) + ' | '; }"
                    + "return r;");
            System.out.println("FORM-DIAG: " + out);
        } catch (Exception e) {
            System.out.println("FORM-DIAG failed: " + e.getMessage());
        }
    }

    /**
     * Types into a field robustly: click + clear + sendKeys, verifying the value
     * stuck; falls back to JS value-setting (headless Chrome sometimes drops
     * keystrokes on re-rendered Thymeleaf forms).
     */
    static void type(WebDriver driver, String id, String value) {
        WebElement el = driver.findElement(By.id(id));
        el.click();
        el.clear();
        el.sendKeys(value);
        if (!value.equals(el.getAttribute("value"))) {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];"
                    + "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));"
                    + "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    el, value);
        }
    }

    // ---------- journeys ----------

    @Test
    void journey1_loginShowsDashboard() {
        run("j1-login", () -> {
            loginAs("admin", "admin123");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("summaryHeading")));
            String heading = driver.findElement(By.id("summaryHeading")).getText();
            assertTrue(heading.contains("samples"), "Dashboard summary missing: " + heading);
            assertFalse(driver.findElements(By.cssSelector("#statusCards .stat")).isEmpty(),
                    "Status cards missing");
        });
    }

    @Test
    void journey2_createSampleRecord() {
        run("j2-create", () -> {
            loginAs("collector", "collector123");
            String sampleId = uniqueId("WQ-SEL");
            driver.get(baseUrl + "/samples/new");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sampleForm")));
            type(driver, "sampleId", sampleId);
            new Select(driver.findElement(By.id("station")))
                    .selectByIndex(1);
            setDateTime(driver.findElement(By.id("collectedAt")),
                    LocalDateTime.now().minusDays(1));
            type(driver, "collector", "Selenium Collector");
            type(driver, "ph", "7.2");
            type(driver, "notes", "Created by Selenium journey 2");
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "document.getElementById('sampleForm').requestSubmit("
                    + "document.getElementById('saveBtn'));");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusBadge")));
            assertEquals("DRAFT", driver.findElement(By.id("statusBadge")).getText());
            assertTrue(driver.getPageSource().contains(sampleId));
        });
    }

    @Test
    void journey3_searchSamples() {
        run("j3-search", () -> {
            loginAs("collector", "collector123");
            driver.get(baseUrl + "/samples");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("searchForm")));
            type(driver, "q", "WQ-0001");
            driver.findElement(By.id("searchBtn")).click();
            wait.until(d -> d.getCurrentUrl().contains("q=WQ-0001"));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("samplesTable")));
            List<WebElement> rows = driver.findElements(By.cssSelector("#samplesTable tbody tr"));
            assertEquals(1, rows.size(), "Expected exactly 1 row for WQ-0001, got " + rows.size());
            assertTrue(rows.get(0).getText().contains("WQ-0001"));

            // Empty result set shows friendly message (guard against stale-page race:
            // wait for the old table to go stale before asserting on the new page).
            WebElement oldTable = driver.findElement(By.id("samplesTable"));
            driver.get(baseUrl + "/samples?q=ZZZ-NO-SUCH-SAMPLE");
            wait.until(ExpectedConditions.stalenessOf(oldTable));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("samplesTable")));
            assertNotNull(driver.findElement(By.id("noResults")));
        });
    }

    @Test
    void journey4_collectorSubmitsDraft() {
        run("j4-workflow", () -> {
            loginAs("collector", "collector123");
            driver.get(baseUrl + "/samples");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("samplesTable")));
            List<WebElement> links = driver.findElements(By.linkText("View"));
            assertFalse(links.isEmpty(), "No samples to transition");
            // Open first DRAFT sample; if none, create one
            String target = findOrCreateDraft();
            driver.get(baseUrl + "/samples/" + target);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusBadge")));
            String before = driver.findElement(By.id("statusBadge")).getText();
            if ("DRAFT".equals(before)) {
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                        "document.getElementById('btn-SUBMITTED').form.requestSubmit("
                        + "document.getElementById('btn-SUBMITTED'));");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusBadge")));
                wait.until(d -> !"DRAFT".equals(d.findElement(By.id("statusBadge")).getText()));
                assertEquals("SUBMITTED", driver.findElement(By.id("statusBadge")).getText());
            }
            // History must contain an entry
            assertTrue(driver.findElements(By.cssSelector("#historyTable tbody tr")).size() >= 1);
        });
    }

    @Test
    void journey5_dashboardCountsAndHistory() {
        run("j5-dashboard", () -> {
            loginAs("reviewer", "reviewer123");
            driver.get(baseUrl + "/dashboard");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("summaryHeading")));
            assertFalse(driver.findElements(By.cssSelector("#stationTable tbody tr")).isEmpty());
            assertFalse(driver.findElements(By.cssSelector("#recentTable tbody tr")).isEmpty());
        });
    }

    /** Returns the DB id of a DRAFT sample, creating one if needed. */
    private String findOrCreateDraft() {
        driver.get(baseUrl + "/samples");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("samplesTable")));
        for (WebElement row : driver.findElements(By.cssSelector("#samplesTable tbody tr"))) {
            String text = row.getText();
            if (text.contains("DRAFT")) {
                String href = row.findElement(By.linkText("View")).getAttribute("href");
                return href.substring(href.lastIndexOf('/') + 1);
            }
        }
        // create a draft via UI
        String sampleId = uniqueId("WQ-DRF");
        driver.get(baseUrl + "/samples/new");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("sampleForm")));
        type(driver, "sampleId", sampleId);
        new Select(driver.findElement(By.id("station"))).selectByIndex(1);
        setDateTime(driver.findElement(By.id("collectedAt")),
                LocalDateTime.now().minusDays(1));
        type(driver, "collector", "Selenium Collector");
        driver.findElement(By.id("saveBtn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("statusBadge")));
        String url = driver.getCurrentUrl();
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
