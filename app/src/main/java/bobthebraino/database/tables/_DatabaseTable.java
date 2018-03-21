package bobthebraino.database.tables;

import android.provider.BaseColumns;
import bobthebraino.database.DatabaseConnector;

/**
 * The base class for defining database tables
 */
public abstract class _DatabaseTable implements BaseColumns {
    /**
     * The database context
     */
    protected DatabaseConnector m_Context;

    /**
     * Base constructor, sets the context
     * @param _context
     */
    protected _DatabaseTable(DatabaseConnector _context){
        m_Context = _context;
    }
}
