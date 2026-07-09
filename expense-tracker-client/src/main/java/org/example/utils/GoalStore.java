package org.example.utils;

import org.example.models.SavingsGoal;
import java.util.*;

public class GoalStore {
    public static List<SavingsGoal> getGoals(int userId) {
        return SqlUtil.getSavingsGoals(userId);
    }

    public static int nextId(int userId) {
        return 0; // Not used when DB auto-generates ID
    }

    public static void add(int userId, SavingsGoal goal) {
        SqlUtil.postSavingsGoal(userId, goal);
    }

    public static void update(int userId, SavingsGoal goal) {
        SqlUtil.putSavingsGoal(userId, goal);
    }

    public static void delete(int userId, int goalId) {
        SqlUtil.deleteSavingsGoal(goalId);
    }
}