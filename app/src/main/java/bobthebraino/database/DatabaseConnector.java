package bobthebraino.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import bobthebraino.database.annotations.ColumnDefinition;
import bobthebraino.database.annotations.TableDefinition;
import bobthebraino.database.tables.LoginStatus;
import bobthebraino.database.tables.Timestamp;
import bobthebraino.database.tables._DatabaseTable;

/**
 * The database connector. Comporable to a manager class in it´s functionality.
 */
public class DatabaseConnector extends SQLiteOpenHelper {
    /**
     * The current database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * The database name
     */
    private static final String DATABASE_NAME = "wrknghrs";

    private Context Context;
    public Context getContext(){ return Context; }

    /**
     * Default constructor, establishes the DatabaseConnector helper class.
     * @param context
     */
    public DatabaseConnector(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        Context = context;
    }

    /**
     * OnCreate: Initialize all tables needed by the app
     * @param db
     */
    public void onCreate(SQLiteDatabase db) {
        InitializeTable(db, LoginStatus.class);
        InitializeTable(db, Timestamp.class);
    }

    /**
     * OnUpgrade: Call onCreate
     * Basically... not yet implemented
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }

    /**
     * OnDowngrade: Call onUpgrade
     * Basically... not yet implemented
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) { onUpgrade(db, oldVersion, newVersion); }

    /**
     * Initialize a database table.
     * Use on first time starting the app.
     * @param _class
     */
    private void InitializeTable(SQLiteDatabase db, Class<?> _class) {
        TableDefinition anno = _class.getAnnotation(TableDefinition.class);
        String TableName = anno.TableName();
        Boolean UseAutoID = anno.UseAutoID();

        String query = "CREATE TABLE IF NOT EXISTS " +  TableName + " (";
        if(UseAutoID)
            query += "_ID INTEGER PRIMARY KEY, ";
        Field[] fields = _class.getFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(ColumnDefinition.class)){
                Boolean checkForNullable = true;
                String columnName = field.getAnnotation(ColumnDefinition.class).ColumnName();
                query += columnName;
                switch(field.getAnnotation(ColumnDefinition.class).Type()){
                    case BOOLEAN:
                        query += " BOOLEAN NOT NULL CHECK(" + columnName + " IN (0,1))";
                        checkForNullable = false;
                        break;
                    case DOUBLE:
                        query += " DOUBLE ";
                        break;
                    case INTEGER:
                        query += " INTEGER ";
                        break;
                    case STRING:
                        query += " NVARCHAR(MAX) ";
                        break;
                    default:
                        try {
                            throw new Exception("Empty datatype not allowed!");
                        } catch(Exception _ex){ }
                }
                if(checkForNullable) {
                    Boolean pk = field.getAnnotation(ColumnDefinition.class).PrimaryKey();
                    Boolean nullable = pk ? false : field.getAnnotation(ColumnDefinition.class).Nullable();
                    if (pk)
                        query += "PRIMARY KEY";
                    if (!nullable && field.getAnnotation(ColumnDefinition.class).Type() != DataType.BOOLEAN)
                        query += "NOT NULL";
                    else
                        query += "NULL";
                }
                query += ", ";
            }
        }
        query = query.substring(0, query.length() - 2);
        query += ")";
        db.execSQL(query);
    }

    /**
     * Store an entity to the database
     * @param _entity
     * @param <T>
     */
    public <T extends _DatabaseTable> void Insert(T _entity) {
        SQLiteDatabase db = getWritableDatabase();

        String tableName = _entity.getClass().getAnnotation(TableDefinition.class).TableName();
        String columns = " (";
        String values = " VALUES(";

        Field[] fields = _entity.getClass().getFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(ColumnDefinition.class)){
                columns += field.getAnnotation(ColumnDefinition.class).ColumnName() + ", ";
                try {
                    Object value = field.get(_entity);
                    if(value instanceof Boolean)
                        values += (boolean)value == true ? 1 : 0;
                    else
                        values += value;

                    values +=  ", ";
                } catch(IllegalAccessException ex){ }
            }
        }
        columns = columns.substring(0, columns.length() - 2);
        columns += ")";
        values = values.substring(0, values.length() - 2);
        values += ")";

        String insertQuery = "INSERT INTO " + tableName + columns + values;

        db.execSQL(insertQuery);
    }

    /**
     * Update an entity in the database
     * @param _entity
     * @param <T>
     */
    public <T extends _DatabaseTable> void Update(T _entity) {
        SQLiteDatabase db = getWritableDatabase();

        String tableName = _entity.getClass().getAnnotation(TableDefinition.class).TableName();

        String updateValues = "";
        boolean hasPK = false;

        Field[] fields = _entity.getClass().getFields();
        for(Field field: fields){
            if(field.isAnnotationPresent(ColumnDefinition.class)){
                if (field.getAnnotation(ColumnDefinition.class).PrimaryKey())
                    hasPK = true;
                try {
                    Object value = field.get(_entity);
                    value = (boolean)value == true ? 1 : (boolean)value == false ? 0 : value;
                    updateValues += field.getName() + " = " + value + ", ";
                }
                catch(IllegalAccessException ex){

                }
            }
        }
        updateValues = updateValues.substring(0, updateValues.length() - 2);

        String updateQuery = "UPDATE " + tableName + " SET " + updateValues;

        if(hasPK)
            updateQuery += " WHERE " + " "; // TODO

        db.execSQL(updateQuery);
    }

    /**
     * Load a single entity from the database
     * @param _class
     * @param _pkValues
     * @param <T>
     * @return
     */
    public <T extends _DatabaseTable> T LoadSingle(Class<T> _class, String[] _pkValues){
        SQLiteDatabase db = getReadableDatabase();
        try {
            T entity = _class.getDeclaredConstructor(DatabaseConnector.class).newInstance(this);

            String tableName = _class.getAnnotation(TableDefinition.class).TableName();
            String query = "SELECT * FROM " + tableName + " WHERE ";

            Field[] fields = _class.getFields();
            List<String> pks = new ArrayList<String>();
            pks.add("_ID");
            for(Field field: fields){
                if(field.isAnnotationPresent(ColumnDefinition.class))
                    if (field.getAnnotation(ColumnDefinition.class).PrimaryKey())
                        pks.add(field.getAnnotation(ColumnDefinition.class).ColumnName());
            }

            for(int i = 0; i < pks.size(); i++){
                query += pks.get(i) + "=" + _pkValues[i] + " AND ";
            }
            query = query.substring(0, query.length() - 5);

            Cursor cursor = db.rawQuery(query, null);

           while(cursor.moveToNext()) {
               for(Field field: fields) {
                   if(field.isAnnotationPresent(ColumnDefinition.class)) {
                       switch (field.getAnnotation(ColumnDefinition.class).Type()) {
                           case BOOLEAN:
                               field.set(entity, cursor.getInt(cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName())) == 1);
                               break;
                           case DOUBLE:

                               break;
                           case INTEGER:
                               field.set(entity, cursor.getInt(cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName())));
                               break;
                           case STRING:
                               field.set(entity, cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName()));
                               break;
                       }
                   }
               }
            }
            cursor.close();
            return entity;
        }
        catch (NoSuchMethodException ex) {

        }
        catch(InvocationTargetException ex){

        }
        catch(IllegalAccessException ex){

        }
        catch(InstantiationException ex) {

        }
        return null;
    }

    /**
     * Load all entities of a type from the database
      * @param _class
     * @param <T>
     * @return
     */
    public <T extends _DatabaseTable> List<T> LoadAll(Class<T> _class){
        SQLiteDatabase db = getReadableDatabase();

        try {
            List<T> entities =  new ArrayList<T>();

            String tableName = _class.getAnnotation(TableDefinition.class).TableName();
            String query = "SELECT * FROM " + tableName;

            Field[] fields = _class.getFields();
            Cursor cursor = db.rawQuery(query, null);

            while(cursor.moveToNext()) {
                T entity = _class.getDeclaredConstructor(DatabaseConnector.class).newInstance(this);
                for(Field field: fields) {
                    if(field.isAnnotationPresent(ColumnDefinition.class)) {
                        switch (field.getAnnotation(ColumnDefinition.class).Type()) {
                            case BOOLEAN:
                                field.set(entity, cursor.getInt(cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName())) == 1);
                                break;
                            case DOUBLE:

                                break;
                            case INTEGER:
                                field.set(entity, cursor.getInt(cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName())));
                                break;
                            case STRING:
                                field.set(entity, cursor.getColumnIndexOrThrow(field.getAnnotation(ColumnDefinition.class).ColumnName()));
                                break;
                        }
                    }
                }
                entities.add(entity);
            }
            cursor.close();
            return entities;
        }
        catch(InvocationTargetException ex){

        }
        catch(NoSuchMethodException ex) {

        }
        catch(IllegalAccessException ex){

        }
        catch(InstantiationException ex) {

        }
        return null;
    }

    /**
     * Drop tbe database. Use with caution!!!
     */
    public void dropDatabase() {
        SQLiteDatabase db = getWritableDatabase();

        // query to obtain the names of all tables in your database
        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        List<String> tables = new ArrayList<>();

        // iterate over the result set, adding every table name to a list
        while (c.moveToNext()) {
            tables.add(c.getString(0));
        }

        // call DROP TABLE on every table name
        for (String table : tables) {
            String dropQuery = "DROP TABLE IF EXISTS " + table;
            db.execSQL(dropQuery);
        }
    }
}