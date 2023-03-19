package random.chrome;

public record ChromeAccountEntry(String usernameValue,
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
                date_created, date_last_used, date_password_modified,
                times_used
            FROM logins
            """;
}
