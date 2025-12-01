package com.kspl.norenexampleapp;

import com.kspl.norenexampleapp.ExampleCallback;
import com.noren.javaapi.NorenApiJava;
import com.noren.javaapi.OAuthHandler;
import com.noren.javaapi.NorenRoutes;
import com.noren.javaapi.BasketItem;
import com.noren.javaapi.MainData;

import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.time.ZoneOffset;

public class NorenExampleApp {

    // Utility methods for safe printing
    private static void printJson(String label, JSONObject json) {
        if (json == null)
            System.out.println("️" + label + " API returned null or failed.");
        else
            System.out.println("\n" + label + ":\n" + json.toString(2));
    }

    private static void printArray(String label, JSONArray arr) {
        if (arr == null)
            System.out.println(" " + label + " API returned null or failed.");
        else
            System.out.println("\n" + label + ":\n" + arr.toString(2));
    }

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Noren API Test with OAuth...");

            // --- Step 1: Load credentials ---
            String credPath = new File("cred.properties").getAbsolutePath();
            OAuthHandler oauth = new OAuthHandler(credPath);

            // --- Step 2: Generate OAuth URL ---
            String oauthUrl = oauth.getOAuthURL();
            System.out.println("\n🔗 Opening browser for OAuth login...");
            System.out.println("If it doesn’t open automatically, visit manually:\n" + oauthUrl);

            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("win")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", oauthUrl);
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", oauthUrl);
                } else {
                    // Linux / Unix
                    pb = new ProcessBuilder("xdg-open", oauthUrl);
                }

                // 🚫 Suppress all output (stdout & stderr)
                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                pb.start();

                //System.out.println("🌐 Browser opened");
            } catch (Exception e) {
                System.err.println("⚠️ Could not auto-open browser. Please open manually: " + oauthUrl);
            }


            // --- Step 3: Get authorization code from user ---
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n Enter the code from the redirect URL: ");
            String code = scanner.nextLine();
            scanner.close();

            // --- Step 4: Exchange code for access + refresh token ---
            NorenRoutes routes = new NorenRoutes();
            Map<String, String> tokenInfo = oauth.getAccessToken(code, oauth.getBaseUrl(), routes);

            if (tokenInfo == null || !tokenInfo.containsKey("access_token")) {
                System.err.println(" Failed to fetch OAuth tokens. Exiting...");
                return;
            }

            System.out.println("\n OAuth Tokens Generated:");
            System.out.println("Access Token: " + tokenInfo.get("access_token"));
            System.out.println("Refresh Token: " + tokenInfo.get("refresh_token"));
            System.out.println("UID: " + tokenInfo.get("uid"));
            System.out.println("Account ID: " + tokenInfo.get("actid"));

            // --- Step 5: Initialize API ---
            String baseApiUrl = oauth.getBaseUrl();
            String websocketUrl = oauth.getWebsocketUrl();
            System.out.println("🌐 Base API URL: " + baseApiUrl);
            System.out.println("🔌 WebSocket URL: " + websocketUrl);

            NorenApiJava api = new NorenApiJava(baseApiUrl, websocketUrl, oauth);

            // --- Step 6: API Tests ---

            // 6a. Search
            printJson("📈 Search Response", api.search("NSE", "INFY"));

            // 6b. Forgot password OTP
            printJson("🔑 Forgot Password OTP Response", api.forgotpassword_OTP("NANDAN", "ABCDE1234N"));

            // 6c. Get Quotes
            printJson("📊 Quotes Response", api.get_quotes("NSE", "22"));

            // 6d. Get Limits
            printJson("💰 Limits Response", api.get_limits());

            // 6e. Place Order
            JSONObject orderReply = api.place_order(
                    "B", "I", "NSE", "CANBK-EQ", 1, 0, "LMT",
                    220.0, "Test Order", null, "DAY", "NO",
                    null, null, null
            );
            printJson("🧾 Place Order Response", orderReply);

            // 6f. Basket Items
            BasketItem item1 = new BasketItem("NSE", "TATATECH-EQ", 1, 1053.7, "C", "B", "LMT");
            BasketItem item2 = new BasketItem("NSE", "YESBANK-EQ", 1, 24.9795, "C", "B", "LMT");

            MainData basket = new MainData();
            basket.exch = "NSE";
            basket.tsym = "ACC-EQ";
            basket.qty = 1;
            basket.prc = 2720.445;
            basket.prd = "C";
            basket.trantype = "B";
            basket.prctyp = "LMT";
            basket.basketlists = Arrays.asList(item1, item2);

            printJson("📦 Basket Margin Response", api.get_Basket_Margin(basket));

            // 6g. Order Book
            printArray("📘 Order Book", api.get_order_book());

            // 6h. Trade Book
            printArray("📗 Trade Book", api.get_trade_book());

            // 6i. Position Book
            printArray("📙 Position Book", api.get_position_book());

            // 6j. Time-Price Series
            LocalDateTime lastBusDay = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
            int day = lastBusDay.getDayOfWeek().getValue();
            if (day == 6) lastBusDay = lastBusDay.minusDays(1);
            else if (day == 7) lastBusDay = lastBusDay.minusDays(2);

            System.out.println("📅 Fetching Time-Price Series for: " +
                    lastBusDay.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")));

            String startEpoch = "1761729005";
            String endEpoch = "1761729905";

            long startTime = System.currentTimeMillis();
            JSONArray timePriceSeries = api.get_time_price_series("NSE", "1594", startEpoch, endEpoch, null);

            if (timePriceSeries != null) {
                System.out.println("\n⏱ Time-Price Series (Duration: " +
                        (System.currentTimeMillis() - startTime) + " ms):\n" +
                        timePriceSeries.toString(2));
                System.out.println("\n✅ Time-Price Series fetched successfully!");
            } else {
                System.out.println("❌ Time-Price Series returned null or failed.");
            }

            // 6k. WebSocket example
            ExampleCallback appCallback = new ExampleCallback();
            api.startWebSocket(appCallback);
            api.subscribe("NSE|22");
            Thread.sleep(10000);
            api.unsubscribe("NSE|22");

            System.out.println("\n✅ All API tests completed successfully!");

            while (true) {
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
