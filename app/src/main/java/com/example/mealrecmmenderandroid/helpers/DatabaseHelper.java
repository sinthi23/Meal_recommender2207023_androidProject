package com.example.mealrecmmenderandroid.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "MealRecommender.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_USER_NAME = "user_name";
    private static final String COLUMN_USER_EMAIL = "user_email";
    private static final String COLUMN_USER_PASSWORD = "user_password";
    private static final String COLUMN_USER_ROLE = "user_role";
    private static final String COLUMN_USER_PHONE = "user_phone";
    private static final String COLUMN_CREATED_AT = "created_at";

    private static final String CREATE_USER_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_USER_NAME + " TEXT,"
            + COLUMN_USER_EMAIL + " TEXT UNIQUE,"
            + COLUMN_USER_PASSWORD + " TEXT,"
            + COLUMN_USER_ROLE + " TEXT DEFAULT 'user',"
            + COLUMN_USER_PHONE + " TEXT,"
            + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String TABLE_RECIPES = "recipes";
    private static final String COLUMN_RECIPE_ID = "recipe_id";
    private static final String COLUMN_RECIPE_NAME = "recipe_name";
    private static final String COLUMN_RECIPE_DESCRIPTION = "recipe_description";
    private static final String COLUMN_RECIPE_INGREDIENTS = "recipe_ingredients";
    private static final String COLUMN_RECIPE_INSTRUCTIONS = "recipe_instructions";
    private static final String COLUMN_RECIPE_CALORIES = "recipe_calories";
    private static final String COLUMN_RECIPE_CATEGORY = "recipe_category";
    private static final String COLUMN_RECIPE_IMAGE = "recipe_image";

    private static final String CREATE_RECIPE_TABLE = "CREATE TABLE " + TABLE_RECIPES + "("
            + COLUMN_RECIPE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_RECIPE_NAME + " TEXT,"
            + COLUMN_RECIPE_DESCRIPTION + " TEXT,"
            + COLUMN_RECIPE_INGREDIENTS + " TEXT,"
            + COLUMN_RECIPE_INSTRUCTIONS + " TEXT,"
            + COLUMN_RECIPE_CALORIES + " INTEGER,"
            + COLUMN_RECIPE_CATEGORY + " TEXT,"
            + COLUMN_RECIPE_IMAGE + " TEXT"
            + ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_RECIPE_TABLE);

        insertDefaultAdmin(db);
        insertSampleRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);

        onCreate(db);
    }



    private void insertDefaultAdmin(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, "Admin");
        values.put(COLUMN_USER_EMAIL, "admin@meal.com");
        values.put(COLUMN_USER_PASSWORD, "admin123");
        values.put(COLUMN_USER_ROLE, "admin");
        values.put(COLUMN_USER_PHONE, "1234567890");

        db.insert(TABLE_USERS, null, values);
    }


    public long addUser(String name, String email, String password, String role, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, name);
        values.put(COLUMN_USER_EMAIL, email);
        values.put(COLUMN_USER_PASSWORD, password);
        values.put(COLUMN_USER_ROLE, role != null ? role : "user");
        values.put(COLUMN_USER_PHONE, phone);

        long id = db.insert(TABLE_USERS, null, values);
        db.close();

        return id;
    }


    public boolean checkUserCredentials(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ? AND " + COLUMN_USER_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }


    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        int count = cursor.getCount();
        cursor.close();
        db.close();

        return count > 0;
    }

    public int getUserId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_USER_ID};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
        }

        cursor.close();
        db.close();

        return userId;
    }

    public String getUserName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_USER_NAME};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        String userName = "";
        if (cursor.moveToFirst()) {
            userName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
        }

        cursor.close();
        db.close();

        return userName;
    }


    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {COLUMN_USER_ROLE};
        String selection = COLUMN_USER_EMAIL + " = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);

        String userRole = "user";
        if (cursor.moveToFirst()) {
            userRole = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE));
        }

        cursor.close();
        db.close();

        return userRole;
    }


    private void insertSampleRecipes(SQLiteDatabase db) {
        ContentValues recipe1 = new ContentValues();
        recipe1.put(COLUMN_RECIPE_NAME, "Healthy Chicken Salad");
        recipe1.put(COLUMN_RECIPE_DESCRIPTION, "A light and nutritious chicken salad");
        recipe1.put(COLUMN_RECIPE_INGREDIENTS, "Chicken breast, lettuce, tomatoes, cucumber, olive oil");
        recipe1.put(COLUMN_RECIPE_INSTRUCTIONS, "1. Grill chicken\n2. Chop vegetables\n3. Mix with olive oil");
        recipe1.put(COLUMN_RECIPE_CALORIES, 350);
        recipe1.put(COLUMN_RECIPE_CATEGORY, "Healthy");
        recipe1.put(COLUMN_RECIPE_IMAGE, "");
        db.insert(TABLE_RECIPES, null, recipe1);

        ContentValues recipe2 = new ContentValues();
        recipe2.put(COLUMN_RECIPE_NAME, "Vegetable Soup");
        recipe2.put(COLUMN_RECIPE_DESCRIPTION, "Warm and comforting vegetable soup");
        recipe2.put(COLUMN_RECIPE_INGREDIENTS, "Carrots, celery, onions, vegetable broth, herbs");
        recipe2.put(COLUMN_RECIPE_INSTRUCTIONS, "1. Chop vegetables\n2. Simmer in broth\n3. Season to taste");
        recipe2.put(COLUMN_RECIPE_CALORIES, 180);
        recipe2.put(COLUMN_RECIPE_CATEGORY, "Healthy");
        recipe2.put(COLUMN_RECIPE_IMAGE, "");
        db.insert(TABLE_RECIPES, null, recipe2);

        ContentValues recipe3 = new ContentValues();
        recipe3.put(COLUMN_RECIPE_NAME, "Grilled Salmon");
        recipe3.put(COLUMN_RECIPE_DESCRIPTION, "Omega-3 rich grilled salmon");
        recipe3.put(COLUMN_RECIPE_INGREDIENTS, "Salmon fillet, lemon, garlic, herbs");
        recipe3.put(COLUMN_RECIPE_INSTRUCTIONS, "1. Season salmon\n2. Grill for 6-8 minutes\n3. Serve with lemon");
        recipe3.put(COLUMN_RECIPE_CALORIES, 420);
        recipe3.put(COLUMN_RECIPE_CATEGORY, "Protein");
        recipe3.put(COLUMN_RECIPE_IMAGE, "");
        db.insert(TABLE_RECIPES, null, recipe3);
    }


    public Cursor getAllRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPES, null);
    }

    public Cursor getRecipesByCategory(String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPES +
                        " WHERE " + COLUMN_RECIPE_CATEGORY + " = ?",
                new String[]{category});
    }

    public Cursor searchRecipes(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_RECIPES +
                        " WHERE " + COLUMN_RECIPE_NAME + " LIKE ?",
                new String[]{"%" + query + "%"});
    }
}