package uni.project.a.b.domain;

public enum AppPermission {

    GLOBAL("global");

    private String permission;

    AppPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}

