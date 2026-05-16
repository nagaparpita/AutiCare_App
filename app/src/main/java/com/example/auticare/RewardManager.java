package com.example.auticare;

import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RewardManager {

    private static final String PREF_NAME = "AutiCareRewardData";
    
    // Constants for Game IDs
    public static final String GAME_LEARN_COMM = "LearnComm";
    public static final String GAME_EMOTIONS = "Emotions";
    public static final String GAME_ROUTINE = "Routine";
    public static final String GAME_FOCUS = "Focus";

    // Aliases for Game IDs
    public static final String KEY_EMOTION = GAME_EMOTIONS;
    public static final String KEY_ROUTINE = GAME_ROUTINE;
    public static final String KEY_FOCUS = GAME_FOCUS;

    // Keys
    private static final String KEY_STARS_PREFIX = "game_stars_";
    private static final String KEY_TOTAL_STARS = "total_stars_earned";
    private static final String KEY_STICKER_PREFIX = "sticker_unlocked_";
    private static final String KEY_BADGE_PREFIX = "badge_unlocked_";
    private static final String KEY_DOOR_ACTIVE = "door_reward_active";
    private static final String KEY_CHESTS_OPENED = "chests_opened_count";
    
    // Activity Counters for Badges
    public static final String COUNTER_EMOTION = "counter_emotion";
    public static final String COUNTER_ROUTINE = "counter_routine";
    public static final String COUNTER_FOCUS = "counter_focus";

    public static void addStar(Context context, String gameId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String starKey = KEY_STARS_PREFIX + gameId;
        int currentStars = prefs.getInt(starKey, 0);

        if (currentStars < 5) {
            int newStars = currentStars + 1;
            editor.putInt(starKey, newStars);
            
            // Increment total stars for the progress towards stickers
            // Check if game reached 5 stars -> Unlock Sticker
            if (newStars == 5) {
                unlockNextSticker(context, editor);
            }
        }
        editor.apply();
    }

    public static void addStars(Context context, int count) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        int totalStars = prefs.getInt(KEY_TOTAL_STARS, 0) + count;
        editor.putInt(KEY_TOTAL_STARS, totalStars);

        editor.apply();
    }

    public static void addGameStars(Context context, String gameId, int score) {
        // Add the score to total stars (used for chests)
        addStars(context, score);
        
        // Progress individual game stars (used to unlock stickers)
        // Each time they complete a game, we add one progress star (max 5 per game)
        if (score > 0) {
            addStar(context, gameId);
        }
        
        // Also increment activity count for the specific game to unlock badges
        if (GAME_EMOTIONS.equals(gameId)) {
            incrementActivityCount(context, COUNTER_EMOTION);
        } else if (GAME_ROUTINE.equals(gameId)) {
            incrementActivityCount(context, COUNTER_ROUTINE);
        } else if (GAME_FOCUS.equals(gameId)) {
            incrementActivityCount(context, COUNTER_FOCUS);
        }
    }

    private static void unlockNextSticker(Context context, SharedPreferences.Editor editor) {
        String[] stickers = {"Rainbow", "Dog", "Car", "Star", "Cat", "Apple"};
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        for (String sticker : stickers) {
            String key = KEY_STICKER_PREFIX + sticker;
            if (!prefs.getBoolean(key, false)) {
                editor.putBoolean(key, true);
                editor.putString(key + "_date", getCurrentDate());
                // Enable door reward when a NEW sticker is unlocked
                editor.putBoolean(KEY_DOOR_ACTIVE, true);
                break;
            }
        }
    }

    public static void incrementActivityCount(Context context, String counterKey) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(counterKey, 0) + 1;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(counterKey, count);
        
        // Check for Badges
        checkBadges(prefs, editor, counterKey, count);
        
        editor.apply();
    }

    private static void checkBadges(SharedPreferences prefs, SharedPreferences.Editor editor, String key, int count) {
        // First Step: First time any game activity is done
        if (!prefs.getBoolean(KEY_BADGE_PREFIX + "First Step", false)) {
            unlockBadge(editor, "First Step");
        }

        if (key.equals(COUNTER_EMOTION) && count >= 5) {
            unlockBadge(editor, "Emotion Master");
        } else if (key.equals(COUNTER_ROUTINE) && count >= 5) {
            unlockBadge(editor, "Good Habits Star");
        } else if (key.equals(COUNTER_FOCUS) && count >= 5) {
            unlockBadge(editor, "Focus Champion");
        }
    }

    private static void unlockBadge(SharedPreferences.Editor editor, String badgeName) {
        String key = KEY_BADGE_PREFIX + badgeName;
        editor.putBoolean(key, true);
        editor.putString(key + "_date", getCurrentDate());
    }

    public static boolean isDoorActive(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_DOOR_ACTIVE, false);
    }

    public static void consumeDoorReward(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().putBoolean(KEY_DOOR_ACTIVE, false).apply();
    }

    public static int getAvailableChests(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int totalStars = prefs.getInt(KEY_TOTAL_STARS, 0);
        int opened = prefs.getInt(KEY_CHESTS_OPENED, 0);
        return (totalStars / 3) - opened;
    }

    public static void openChest(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int opened = prefs.getInt(KEY_CHESTS_OPENED, 0);
        prefs.edit().putInt(KEY_CHESTS_OPENED, opened + 1).apply();
    }

    private static String getCurrentDate() {
        return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
    }

    // Getter methods for UI
    public static int getStarsForGame(Context context, String gameId) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getInt(KEY_STARS_PREFIX + gameId, 0);
    }
    
    public static boolean isStickerUnlocked(Context context, String stickerName) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_STICKER_PREFIX + stickerName, false);
    }
    
    public static boolean isBadgeUnlocked(Context context, String badgeName) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).getBoolean(KEY_BADGE_PREFIX + badgeName, false);
    }

    public static void resetAllProgress(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
