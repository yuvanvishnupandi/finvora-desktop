
package org.example.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public final class CurrencyRates {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Map<String, Cache> CACHE = new HashMap<>();
    private static final long TTL_MS = 10 * 60 * 1000; 

    private static final Map<String, BigDecimal> USD_FALLBACK = Map.of(
            "INR", bd("83.0"), "EUR", bd("0.93"), "GBP", bd("0.79"), "JPY", bd("151"),
            "AUD", bd("1.50"), "CAD", bd("1.36"), "SGD", bd("1.35"), "CNY", bd("7.30"),
            "AED", bd("3.67")
    );

    private CurrencyRates() {}

    public static BigDecimal convert(String from, String to, BigDecimal amount) throws Exception {
        if (from.equalsIgnoreCase(to)) return amount;
        BigDecimal rate = getRate(from.toUpperCase(), to.toUpperCase());
        return amount.multiply(rate).setScale(4, java.math.RoundingMode.HALF_UP);
    }

    private static BigDecimal getRate(String from, String to) throws Exception {
        long now = System.currentTimeMillis();
        Cache c = CACHE.get(from);
        if (c == null || (now - c.at) > TTL_MS) {
            c = new Cache(fetchRates(from), now);
            CACHE.put(from, c);
        }
        BigDecimal r = c.rates.get(to);
        if (r == null) throw new IllegalStateException("No rate for " + from + " -> " + to);
        return r;
    }

    private static Map<String, BigDecimal> fetchRates(String base) throws Exception {
        try {
            String url = "https://api.exchangerate.host/latest?base=" + base;
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).GET().build();
            HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
            JsonObject root  = JsonParser.parseString(res.body()).getAsJsonObject();
            JsonObject rates = root.getAsJsonObject("rates");
            Map<String, BigDecimal> out = new HashMap<>();
            for (String k : rates.keySet()) out.put(k.toUpperCase(), rates.get(k).getAsBigDecimal());
            out.put(base.toUpperCase(), BigDecimal.ONE);
            return out;
        } catch (Exception ex) {
            
            Map<String, BigDecimal> out = new HashMap<>();
            if ("USD".equalsIgnoreCase(base)) {
                out.putAll(USD_FALLBACK);
                out.put("USD", BigDecimal.ONE);
                return out;
            }
            
            Map<String, BigDecimal> usd = new HashMap<>(USD_FALLBACK);
            usd.put("USD", BigDecimal.ONE);
            BigDecimal usdToBase = usd.getOrDefault(base.toUpperCase(), BigDecimal.ONE);
            for (var e : usd.entrySet()) {
                out.put(e.getKey(), e.getValue().divide(usdToBase, 8, java.math.RoundingMode.HALF_UP));
            }
            out.put(base.toUpperCase(), BigDecimal.ONE);
            return out;
        }
    }

    private static BigDecimal bd(String s) { return new BigDecimal(s); }

    private record Cache(Map<String, BigDecimal> rates, long at) {}
}