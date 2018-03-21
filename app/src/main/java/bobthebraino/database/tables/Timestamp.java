package bobthebraino.database.tables;

import java.util.Calendar;
import java.util.Date;

import bobthebraino.database.annotations.ColumnDefinition;
import bobthebraino.database.DataType;
import bobthebraino.database.DatabaseConnector;
import bobthebraino.database.annotations.TableDefinition;

@TableDefinition(TableName = "Timestamp", UseAutoID = true)
public class Timestamp extends _DatabaseTable {

    @ColumnDefinition(ColumnName = "Year", Nullable =  false, Type = DataType.INTEGER)
    public int Year;

    @ColumnDefinition(ColumnName = "Month", Nullable =  false, Type = DataType.INTEGER)
    public int Month;

    @ColumnDefinition(ColumnName = "Day", Nullable =  false, Type = DataType.INTEGER)
    public int Day;

    @ColumnDefinition(ColumnName = "Hour", Nullable = false, Type = DataType.INTEGER)
    public int Hour;

    @ColumnDefinition(ColumnName = "Minute", Nullable = false, Type = DataType.INTEGER)
    public int Minute;

    @ColumnDefinition(ColumnName = "Login", Nullable = false, Type = DataType.BOOLEAN)
    public boolean Login;

    @ColumnDefinition(ColumnName = "Millisecondstotal", Nullable = false, Type = DataType.INTEGER)
    public long Millisecondstotal;

    public Timestamp(DatabaseConnector _context){ super(_context); }

    public Timestamp(DatabaseConnector _context, boolean _login) {
        this(_context);
        Year = Calendar.getInstance().get(Calendar.YEAR);
        Month = Calendar.getInstance().get(Calendar.MONTH);
        Day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        Minute = Calendar.getInstance().get(Calendar.MINUTE);
        Login = _login;
        Millisecondstotal = new Date().getTime();
    }
}
