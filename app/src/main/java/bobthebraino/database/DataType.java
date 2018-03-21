package bobthebraino.database;

public enum DataType {
    BOOLEAN("BOOLEAN"),
    STRING("STRING"),
    INTEGER("INTEGER"),
    DOUBLE("DOUBLE");

    private final String keyword;

    DataType(String _keyword) {
        keyword = _keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}
