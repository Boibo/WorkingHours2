package bobthebraino.database.tables;

import bobthebraino.database.annotations.ColumnDefinition;
import bobthebraino.database.DataType;
import bobthebraino.database.DatabaseConnector;
import bobthebraino.database.annotations.TableDefinition;

@TableDefinition(TableName = "LoginStatus", UseAutoID = false)
public class LoginStatus extends _DatabaseTable {

    @ColumnDefinition(ColumnName = "LoggedIn", Nullable = false, PrimaryKey = false, Type = DataType.BOOLEAN, Default = "false")
    public Boolean LoggedIn;

    public LoginStatus(DatabaseConnector _context) { super(_context); }
    public LoginStatus(DatabaseConnector _context, boolean _loggedIn) {
        this(_context);
        LoggedIn = _loggedIn;
    }
}