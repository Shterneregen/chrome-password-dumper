package random.chrome;

public record ChromeAccount(String usernameValue,
                            String usernameElement,
                            String displayName,
                            String password,
                            String actionUrl,
                            String originUrl,
                            String dateCreated,
                            String dateLastUsed,
                            String datePasswordModified,
                            Integer timesUsed
) {
    public static final String LOGIN_QUERY = """
            SELECT action_url, origin_url,
                username_element, username_value, display_name,
                password_value,
                datetime(date_created / 1000000 + (strftime('%s', '1601-01-01')), 'unixepoch', 'localtime') as date_created,
                datetime(date_last_used / 1000000 + (strftime('%s', '1601-01-01')), 'unixepoch', 'localtime') as date_last_used,
                datetime(date_password_modified / 1000000 + (strftime('%s', '1601-01-01')), 'unixepoch', 'localtime') as date_password_modified,
                times_used
            FROM logins
            ORDER BY date_last_used DESC
            """;
}
