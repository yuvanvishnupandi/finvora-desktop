package org.example.utils;
import com.google.gson.JsonObject;
import com.google.gson.*;
import javafx.scene.control.Alert;
import org.example.models.Transaction;
import org.example.models.TransactionCategory;
import org.example.models.User;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SqlUtil {
    
    public static User getUserByEmail(String userEmail){
        
        HttpURLConnection conn = null;
        try{
            String encodedEmail = URLEncoder.encode(userEmail, StandardCharsets.UTF_8);
            conn = ApiUtil.fetchApi(
                    "/api/v1/user?email=" + encodedEmail,
                    ApiUtil.RequestMethod.GET, null
            );
            if(conn == null) return null;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(getUserByEmail): " +  conn.getResponseCode());
                return null;
            }

            String userDataJson = ApiUtil.readApiResponse(conn);

            JsonObject jsonObject = JsonParser.parseString(userDataJson).getAsJsonObject();

            int id = jsonObject.get("id").getAsInt();
            String name = jsonObject.get("name").getAsString();
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            LocalDateTime createdAt = new Gson().fromJson(jsonObject.get("created_at"), LocalDateTime.class);

            return new User(id, name, email, password, createdAt);
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
        }

        return null;
    }

    public static List<TransactionCategory> getAllTransactionCategoriesByUser(User user){
        List<TransactionCategory> categories = new ArrayList<>();
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/user/" + user.getId(),
                    ApiUtil.RequestMethod.GET, null
            );
            if(conn == null) return null;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(getAllTransactionCategoriesByUser): " + conn.getResponseCode());
            }

            String result = ApiUtil.readApiResponse(conn);
            JsonArray resultJsonArray = new JsonParser().parse(result).getAsJsonArray();

            for(JsonElement jsonElement : resultJsonArray){
                int categoryId = jsonElement.getAsJsonObject().get("id").getAsInt();
                String categoryName = jsonElement.getAsJsonObject().get("categoryName").getAsString();
                String categoryColor = jsonElement.getAsJsonObject().get("categoryColor").getAsString();

                categories.add(new TransactionCategory(categoryId, categoryName, categoryColor));
            }

            return categories;
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
        }

        return null;
    }

    public static List<Transaction> getRecentTransactionByUserId(int userId, int startPage, int endPage, int size){
        List<Transaction> recentTransactions = new ArrayList<>();

        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/recent/user/" + userId +
                            "?startPage=" + startPage + "&endPage=" + endPage + "&size=" + size,
                    ApiUtil.RequestMethod.GET,
                    null
            );
            if(conn == null) return null;

            if(conn.getResponseCode() != 200){
                return null;
            }

            String results = ApiUtil.readApiResponse(conn);
            JsonArray resultJsonArray = new JsonParser().parse(results).getAsJsonArray();
            for(int i = 0; i < resultJsonArray.size(); i++){
                JsonObject transactionJsonObj = resultJsonArray.get(i).getAsJsonObject();
                int transactionId = transactionJsonObj.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if(transactionJsonObj.has("transactionCategory")
                        && !transactionJsonObj.get("transactionCategory").isJsonNull()){
                    JsonObject transactionCategoryJsonObj = transactionJsonObj.get("transactionCategory").getAsJsonObject();
                    int transactionCategoryId = transactionCategoryJsonObj.get("id").getAsInt();
                    String transactionCategoryName = transactionCategoryJsonObj.get("categoryName").getAsString();
                    String transactionCategoryColor = transactionCategoryJsonObj.get("categoryColor").getAsString();

                    transactionCategory = new TransactionCategory(
                            transactionCategoryId,
                            transactionCategoryName,
                            transactionCategoryColor
                    );
                }

                String transactionName = transactionJsonObj.get("transactionName").getAsString();
                double transactionAmount = transactionJsonObj.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObj.get("transactionDate").getAsString());
                String transactionTime = null;
                if (transactionJsonObj.has("transactionTime") && !transactionJsonObj.get("transactionTime").isJsonNull()) {
                    transactionTime = transactionJsonObj.get("transactionTime").getAsString();
                }
                String transactionType = transactionJsonObj.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionTime,
                        transactionType
                );

                recentTransactions.add(transaction);
            }

            return recentTransactions;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<Transaction> getAllTransactionsByUserId(int userId, int year, Integer month){
        List<Transaction> transactions = new ArrayList<>();

        HttpURLConnection conn = null;
        String apiPath = "/api/v1/transaction/user/" + userId + "?year=" + year;
        if(month != null)
            apiPath += "&month=" + month;

        try{
            conn = ApiUtil.fetchApi(
                    apiPath,
                    ApiUtil.RequestMethod.GET,
                    null
            );
            if(conn == null) return null;

            if(conn.getResponseCode() != 200){
                return null;
            }

            String results = ApiUtil.readApiResponse(conn);
            JsonArray resultJson = new JsonParser().parse(results).getAsJsonArray();

            for(int i = 0; i < resultJson.size(); i++){
                JsonObject transactionJsonObj = resultJson.get(i).getAsJsonObject();
                int transactionId = transactionJsonObj.get("id").getAsInt();

                TransactionCategory transactionCategory = null;
                if(transactionJsonObj.has("transactionCategory")
                        && !transactionJsonObj.get("transactionCategory").isJsonNull()){
                    JsonObject transactionCategoryJsonObj = transactionJsonObj.get("transactionCategory").getAsJsonObject();
                    int transactionCategoryId = transactionCategoryJsonObj.get("id").getAsInt();
                    String transactionCategoryName = transactionCategoryJsonObj.get("categoryName").getAsString();
                    String transactionCategoryColor = transactionCategoryJsonObj.get("categoryColor").getAsString();

                    transactionCategory = new TransactionCategory(
                            transactionCategoryId,
                            transactionCategoryName,
                            transactionCategoryColor
                    );
                }

                String transactionName = transactionJsonObj.get("transactionName").getAsString();
                double transactionAmount = transactionJsonObj.get("transactionAmount").getAsDouble();
                LocalDate transactionDate = LocalDate.parse(transactionJsonObj.get("transactionDate").getAsString());
                String transactionTime = null;
                if (transactionJsonObj.has("transactionTime") && !transactionJsonObj.get("transactionTime").isJsonNull()) {
                    transactionTime = transactionJsonObj.get("transactionTime").getAsString();
                }
                String transactionType = transactionJsonObj.get("transactionType").getAsString();

                Transaction transaction = new Transaction(
                        transactionId,
                        transactionCategory,
                        transactionName,
                        transactionAmount,
                        transactionDate,
                        transactionTime,
                        transactionType
                );

                transactions.add(transaction);
            }

            return transactions;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return null;
    }

    public static List<Integer> getAllDistinctYears(int userId){
        List<Integer> distinctYears = new ArrayList<>();
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/years/" + userId,
                    ApiUtil.RequestMethod.GET, null
            );
            if(conn == null) return null;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(getAllDistinctYears): " + conn.getResponseCode());
            }

            String result = ApiUtil.readApiResponse(conn);
            JsonArray resultsArray = new JsonParser().parse(result).getAsJsonArray();

            for(int i = 0; i < resultsArray.size(); i++){
                int year = resultsArray.get(i).getAsInt();
                distinctYears.add(year);
            }

            return distinctYears;
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null)
                conn.disconnect();
        }

        return null;
    }

    public static boolean postLoginUser(String email, String password){
        
        HttpURLConnection conn = null;
        try{
            String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);
            String encodedPassword = URLEncoder.encode(password, StandardCharsets.UTF_8);
            conn = ApiUtil.fetchApi(
                    "/api/v1/user/login?email=" + encodedEmail + "&password=" + encodedPassword,
                    ApiUtil.RequestMethod.POST, null
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                return false;
            }

            return true;
        }catch(IOException e){
            e.printStackTrace();
            return false;
        }finally {
            if(conn != null)
                conn.disconnect();
        }
    }

    public static boolean postCreateUser(JsonObject userData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/user",
                    ApiUtil.RequestMethod.POST,
                    userData
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                return false; 
            }
            return true;
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }finally{
            if(conn != null)
                conn.disconnect();
        }
    }

    public static boolean postTransactionCategory(JsonObject transactionCategoryData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category",
                    ApiUtil.RequestMethod.POST,
                    transactionCategoryData
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean postTransaction(JsonObject transactionData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.POST,
                    transactionData
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean putTransaction(JsonObject newTransactionData){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction",
                    ApiUtil.RequestMethod.PUT,
                    newTransactionData
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(putTransaction): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }
    public static boolean putTransactionCategory(int categoryId, String newCategoryName, String newCategoryColor){
        HttpURLConnection conn = null;

        String encodedCategoryName = URLEncoder.encode(newCategoryName, StandardCharsets.UTF_8);
        String encodedCategoryColor = URLEncoder.encode(newCategoryColor, StandardCharsets.UTF_8);

        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId + "?newCategoryName=" + encodedCategoryName +
                            "&newCategoryColor=" + encodedCategoryColor,
                    ApiUtil.RequestMethod.PUT,
                    null
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(putTransactionCategory): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean deleteTransactionCategoryById(int categoryId){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction-category/" + categoryId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );
            if(conn == null) return false;

            int responseCode = conn.getResponseCode();
            
            if(responseCode != 200){
                
                System.out.println("Error(deleteTransactionCategoryById): " + responseCode);
                
                if (responseCode == 500 || responseCode == 409) {
                     new Alert(Alert.AlertType.ERROR, 
                               "Failed to delete category (Code " + responseCode + "). " +
                               "If the code is 500, please ensure all linked transactions are deleted or recategorized first.").showAndWait();
                }

                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static boolean deleteTransactionById(int transactionId){
        HttpURLConnection conn = null;
        try{
            conn = ApiUtil.fetchApi(
                    "/api/v1/transaction/" + transactionId,
                    ApiUtil.RequestMethod.DELETE,
                    null
            );
            if(conn == null) return false;

            if(conn.getResponseCode() != 200){
                System.out.println("Error(deleteTransactionById): " + conn.getResponseCode());
                return false;
            }

            return true;
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }

        return false;
    }

    public static java.util.List<org.example.models.SavingsGoal> getSavingsGoals(int userId) {
        java.util.List<org.example.models.SavingsGoal> goals = new java.util.ArrayList<>();
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/savings-goals/user/" + userId, ApiUtil.RequestMethod.GET, null);
            if (conn != null) {
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    com.google.gson.JsonArray array = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(conn.getInputStream())).getAsJsonArray();
                    for (com.google.gson.JsonElement el : array) {
                        com.google.gson.JsonObject obj = el.getAsJsonObject();
                        org.example.models.SavingsGoal goal = new org.example.models.SavingsGoal(
                            obj.get("name").getAsString(),
                            obj.get("targetAmount").getAsBigDecimal(),
                            obj.get("currentAmount").getAsBigDecimal(),
                            java.time.LocalDate.parse(obj.get("deadline").getAsString())
                        );
                        goal.setId(obj.get("id").getAsInt());
                        goals.add(goal);
                    }
                } else {
                    System.err.println("getSavingsGoals failed with code: " + code);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return goals;
    }

    public static org.example.models.SavingsGoal postSavingsGoal(int userId, org.example.models.SavingsGoal goal) {
        HttpURLConnection conn = null;
        try {
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("name", goal.getName());
            payload.addProperty("targetAmount", goal.getTargetAmount());
            payload.addProperty("currentAmount", goal.getCurrentAmount());
            payload.addProperty("deadline", goal.getDeadline().toString());
            
            com.google.gson.JsonObject userObj = new com.google.gson.JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);
            
            conn = ApiUtil.fetchApi("/api/v1/savings-goals", ApiUtil.RequestMethod.POST, payload);
            if (conn != null) {
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseReader(new java.io.InputStreamReader(conn.getInputStream())).getAsJsonObject();
                    goal.setId(obj.get("id").getAsInt());
                    return goal;
                } else {
                    System.err.println("postSavingsGoal failed with code: " + code);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return null;
    }

    public static boolean putSavingsGoal(int userId, org.example.models.SavingsGoal goal) {
        HttpURLConnection conn = null;
        try {
            com.google.gson.JsonObject payload = new com.google.gson.JsonObject();
            payload.addProperty("id", goal.getId());
            payload.addProperty("name", goal.getName());
            payload.addProperty("targetAmount", goal.getTargetAmount());
            payload.addProperty("currentAmount", goal.getCurrentAmount());
            payload.addProperty("deadline", goal.getDeadline().toString());
            
            com.google.gson.JsonObject userObj = new com.google.gson.JsonObject();
            userObj.addProperty("id", userId);
            payload.add("user", userObj);
            
            conn = ApiUtil.fetchApi("/api/v1/savings-goals", ApiUtil.RequestMethod.PUT, payload);
            if (conn != null) {
                int code = conn.getResponseCode();
                if (code >= 200 && code < 300) {
                    return true;
                } else {
                    System.err.println("putSavingsGoal failed with code: " + code);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }

    public static boolean deleteSavingsGoal(int goalId) {
        HttpURLConnection conn = null;
        try {
            conn = ApiUtil.fetchApi("/api/v1/savings-goals/" + goalId, ApiUtil.RequestMethod.DELETE, null);
            return conn != null && conn.getResponseCode() == 200;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.disconnect();
        }
        return false;
    }
}