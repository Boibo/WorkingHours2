package bobthebraino.database.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import bobthebraino.database.DataType;

/**
 * Used to configure a database column
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnDefinition {
    /**
     * Name of the column
     * @return
     */
    String ColumnName();

    /**
     * Is the column a primary key? Default = false
     * @return
     */
    boolean PrimaryKey() default false;

    /**
     * Is the column nullable? Default = true
     * @return
     */
    boolean Nullable() default true;

    /**
     * The datatype of the column
     * @return
     */
    DataType Type();

    /**
     * The default value of the column. Default = empty string
     * @return
     */
    String Default() default "";
}
