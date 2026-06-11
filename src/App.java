

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) throws Exception {
        // Start the server on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Context for serving the main UI
        server.createContext("/", new UIHandler());
        
        // Context for processing calculations
        server.createContext("/calculate", new CalcHandler());
        
        server.setExecutor(null); // creates a default executor
        System.out.println("Calculator Web App started at http://localhost:8080");
        server.start();
    }

    // Handler to serve the HTML/CSS/JS frontend
    static class UIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Java CalcWebApp</title>
                    <style>
                        body { font-family: Arial, sans-serif; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f4f4f9; margin: 0; }
                        .calc-box { background: white; padding: 25px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); text-align: center; width: 300px; }
                        input, select, button { width: 100%; padding: 12px; margin: 8px 0; box-sizing: border-box; border: 1px solid #ccc; border-radius: 5px; font-size: 16px; }
                        button { background-color: #007bff; color: white; border: none; cursor: pointer; font-weight: bold; }
                        button:hover { background-color: #0056b3; }
                        #result { margin-top: 15px; font-size: 20px; font-weight: bold; color: #333; }
                    </style>
                </head>
                <body>
                    <div class="calc-box">
                        <h2>Java Web Calculator</h2>
                        <input type="number" id="num1" placeholder="First Number">
                        <select id="operator">
                            <option value="add">+</option>
                            <option value="sub">-</option>
                            <option value="mul">*</option>
                            <option value="div">/</option>
                        </select>
                        <input type="number" id="num2" placeholder="Second Number">
                        <button onclick="sendCalculation()">Calculate</button>
                        <div id="result">Result: -</div>
                    </div>

                    <script>
                        async function sendCalculation() {
                            const num1 = document.getElementById('num1').value;
                            const num2 = document.getElementById('num2').value;
                            const op = document.getElementById('operator').value;
                            
                            if(!num1 || !num2) { alert('Please enter both numbers'); return; }
                            
                            const response = await fetch(`/calculate?num1=${num1}&num2=${num2}&op=${op}`);
                            const text = await response.text();
                            document.getElementById('result').innerText = "Result: " + text;
                        }
                    </script>
                </body>
                </html>
                """;
            
            byte[] responseBytes = html.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }

    // Handler to process the math logic from query parameters
    static class CalcHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery();
            
            double num1 = 0, num2 = 0;
            String op = "";
            String resultStr = "";

            // Crude query parser for query parameters (num1, num2, op)
            try {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] kv = pair.split("=");
                    if (kv[0].equals("num1")) num1 = Double.parseDouble(kv[1]);
                    if (kv[0].equals("num2")) num2 = Double.parseDouble(kv[1]);
                    if (kv[0].equals("op")) op = kv[1];
                }

                // Core Calculator Logic
                switch (op) {
                    case "add" -> resultStr = String.valueOf(num1 + num2);
                    case "sub" -> resultStr = String.valueOf(num1 - num2);
                    case "mul" -> resultStr = String.valueOf(num1 * num2);
                    case "div" -> {
                        if (num2 == 0) resultStr = "Error (Div by 0)";
                        else resultStr = String.valueOf(num1 / num2);
                    }
                    default -> resultStr = "Invalid Operation";
                }
            } catch (Exception e) {
                resultStr = "Error parsing inputs";
            }

            byte[] responseBytes = resultStr.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(200, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        }
    }
}