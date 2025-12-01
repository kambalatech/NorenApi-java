/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package com.kspl.norenexampleapp;

import com.noren.javaapi.NorenApiJava;
import com.noren.javaapi.OAuthHandler;
import com.noren.javaapi.NorenRoutes;
import com.kspl.norenexampleapp.ExampleCallback;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.Scanner;
import java.lang.ProcessBuilder;

import org.json.JSONArray;
import org.json.JSONObject;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.ZoneOffset;

public class NorenExamplewebsocket {

    public static void main(String[] args) {
        try {
            System.out.println("🚀 Starting Noren WebSocket Test...");

            // --- Step 1: Load credentials file ---
            String credPath = new File("cred.properties").getAbsolutePath();
            OAuthHandler oauth = new OAuthHandler(credPath);

            // --- Step 2: Generate OAuth URL ---
            String oauthUrl = oauth.getOAuthURL();
            System.out.println("\n🔗 Opening browser for OAuth login...");
            System.out.println("If it doesn’t open automatically, visit manually:\n" + oauthUrl);

            // ✅ Auto open browser silently (cross-platform)
            try {
                String os = System.getProperty("os.name").toLowerCase();
                ProcessBuilder pb;

                if (os.contains("win")) {
                    pb = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", oauthUrl);
                } else if (os.contains("mac")) {
                    pb = new ProcessBuilder("open", oauthUrl);
                } else {
                    pb = new ProcessBuilder("xdg-open", oauthUrl);
                }

                pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
                pb.redirectError(ProcessBuilder.Redirect.DISCARD);
                pb.start();

                // System.out.println("🌐 Browser opened!");
            } catch (Exception e) {
                System.err.println("⚠️ Could not auto-open browser. Please open manually: " + oauthUrl);
            }

            // --- Step 3: User inputs the authorization code ---
            Scanner scanner = new Scanner(System.in);
            System.out.print("\n Enter the code from the redirect URL: ");
            String code = scanner.nextLine();
            scanner.close();

            // --- Step 4: Generate access token dynamically ---
            NorenRoutes routes = new NorenRoutes();
            Map<String, String> tokenInfo = oauth.getAccessToken(code, oauth.getBaseUrl(), routes);

            if (tokenInfo == null || !tokenInfo.containsKey("access_token")) {
                System.err.println(" Failed to get access token.");
                return;
            }

            System.out.println("\n Access Token Generated:");
            System.out.println("Access Token: " + tokenInfo.get("access_token"));
            System.out.println("UID: " + tokenInfo.get("uid"));
            System.out.println("Account ID: " + tokenInfo.get("actid"));

            // --- Step 5: Initialize Noren API with OAuth token ---
            String baseApiUrl = oauth.getBaseUrl();
            String wsUrl = oauth.getWebsocketUrl();
            NorenApiJava api = new NorenApiJava(baseApiUrl, wsUrl, oauth);

            // --- Step 6: Start WebSocket and subscribe ---
            ExampleCallback appcallback = new ExampleCallback();
            api.startWebSocket(appcallback);

            String symbol = "NSE|22";
            api.subscribe(symbol);
            System.out.println("📡 Subscribed to: " + symbol);

            Thread.sleep(10000);  // Sleep 10 seconds to receive messages

            api.unsubscribe(symbol);
            System.out.println(" Unsubscribed from: " + symbol);

            // Keep the app alive to continue receiving WebSocket messages
            while (true) {
                Thread.sleep(2000);
            }

        } catch (Exception e) {
            System.err.println(" Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
