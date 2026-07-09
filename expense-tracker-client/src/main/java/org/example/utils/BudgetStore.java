
package org.example.utils;

import org.example.models.Budget;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Type;

public final class BudgetStore {
    private static final Map<Integer, List<Budget>> STORE = new ConcurrentHashMap<>();
    private static final Map<Integer, Long> SEQ = new ConcurrentHashMap<>();
    private static final String DATA_FILE = "budgets_data.json";

    static {
        loadData();
    }
    
    private static void loadData() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (Reader reader = new FileReader(file)) {
                com.google.gson.JsonObject root = com.google.gson.JsonParser.parseReader(reader).getAsJsonObject();
                for (String userIdStr : root.keySet()) {
                    int userId = Integer.parseInt(userIdStr);
                    List<Budget> list = new ArrayList<>();
                    com.google.gson.JsonArray arr = root.getAsJsonArray(userIdStr);
                    long maxId = 0;
                    for (com.google.gson.JsonElement e : arr) {
                        com.google.gson.JsonObject obj = e.getAsJsonObject();
                        Budget b = new Budget();
                        b.setId(obj.has("id") && !obj.get("id").isJsonNull() ? obj.get("id").getAsLong() : null);
                        b.setUserEmail(obj.has("userEmail") && !obj.get("userEmail").isJsonNull() ? obj.get("userEmail").getAsString() : null);
                        b.setCategory(obj.has("category") && !obj.get("category").isJsonNull() ? obj.get("category").getAsString() : null);
                        b.setLimitAmount(obj.has("limitAmount") && !obj.get("limitAmount").isJsonNull() ? new java.math.BigDecimal(obj.get("limitAmount").getAsString()) : java.math.BigDecimal.ZERO);
                        b.setSpentAmount(obj.has("spentAmount") && !obj.get("spentAmount").isJsonNull() ? new java.math.BigDecimal(obj.get("spentAmount").getAsString()) : java.math.BigDecimal.ZERO);
                        b.setYear(obj.has("year") && !obj.get("year").isJsonNull() ? obj.get("year").getAsInt() : 0);
                        b.setPeriodType(obj.has("periodType") && !obj.get("periodType").isJsonNull() ? Budget.PeriodType.valueOf(obj.get("periodType").getAsString()) : null);
                        b.setMonth(obj.has("month") && !obj.get("month").isJsonNull() ? obj.get("month").getAsInt() : null);
                        b.setQuarter(obj.has("quarter") && !obj.get("quarter").isJsonNull() ? obj.get("quarter").getAsInt() : null);
                        list.add(b);
                        if (b.getId() != null && b.getId() > maxId) maxId = b.getId();
                    }
                    STORE.put(userId, list);
                    SEQ.put(userId, maxId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    private static synchronized void saveData() {
        try {
            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            for (Map.Entry<Integer, List<Budget>> entry : STORE.entrySet()) {
                com.google.gson.JsonArray arr = new com.google.gson.JsonArray();
                for (Budget b : entry.getValue()) {
                    com.google.gson.JsonObject obj = new com.google.gson.JsonObject();
                    if (b.getId() != null) obj.addProperty("id", b.getId());
                    if (b.getUserEmail() != null) obj.addProperty("userEmail", b.getUserEmail());
                    if (b.getCategory() != null) obj.addProperty("category", b.getCategory());
                    if (b.getLimitAmount() != null) obj.addProperty("limitAmount", b.getLimitAmount().toString());
                    if (b.getSpentAmount() != null) obj.addProperty("spentAmount", b.getSpentAmount().toString());
                    obj.addProperty("year", b.getYear());
                    if (b.getPeriodType() != null) obj.addProperty("periodType", b.getPeriodType().name());
                    if (b.getMonth() != null) obj.addProperty("month", b.getMonth());
                    if (b.getQuarter() != null) obj.addProperty("quarter", b.getQuarter());
                    arr.add(obj);
                }
                root.add(String.valueOf(entry.getKey()), arr);
            }
            try (Writer writer = new FileWriter(DATA_FILE)) {
                new Gson().toJson(root, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private BudgetStore(){}

    private static synchronized long nextId(int userId){
        long v = SEQ.getOrDefault(userId, 0L) + 1;
        SEQ.put(userId, v);
        return v;
    }

    public static synchronized List<Budget> getBudgets(int userId){
        return new ArrayList<>(STORE.getOrDefault(userId, Collections.emptyList()));
    }

    public static synchronized Budget getById(int userId, Long id){
        if (id == null) return null;
        for (Budget b : STORE.getOrDefault(userId, Collections.emptyList())) {
            if (id.equals(b.getId())) return b;
        }
        return null;
    }

    public static synchronized void add(int userId, Budget b){
        if (b.getId() == null) b.setId(nextId(userId));
        List<Budget> list = STORE.computeIfAbsent(userId, k -> new ArrayList<>());
        int idx = -1;
        for (int i = 0; i < list.size(); i++) {
            Budget old = list.get(i);
            if (Objects.equals(old.getId(), b.getId()) || old.equals(b)) { idx = i; break; }
        }
        if (idx >= 0) list.set(idx, b); else list.add(b);
        saveData();
    }

    public static synchronized void update(int userId, Budget b){
        add(userId, b);
    }

    public static synchronized void removeById(int userId, Long id){
        if (id == null) return;
        List<Budget> list = STORE.getOrDefault(userId, Collections.emptyList());
        list.removeIf(b -> id.equals(b.getId()));
        saveData();
    }
}