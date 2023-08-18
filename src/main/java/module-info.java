module random {
    requires com.sun.jna.platform;
    requires java.desktop;
    requires java.datatransfer;
    requires java.sql;
    requires java.logging;
    requires org.json;
    requires org.xerial.sqlitejdbc;
    requires info.picocli;
    exports random;
    exports random.util;
    exports random.chrome;
    opens random;
    exports random.services;
}