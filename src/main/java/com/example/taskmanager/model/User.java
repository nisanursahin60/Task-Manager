package com.example.taskmanager.model;

public class User {

    public enum Role {
        MANAGER, EMPLOYEE
    }

    private int id;
    private String username;
    private String password;
    private Role role;
    private String fullName;
    private String department;

    public User() {}

    public User(int id, String username, String password, Role role, String fullName, String department) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.fullName = fullName;
        this.department = department;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public boolean isManager() {
        return this.role == Role.MANAGER;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', role=" + role + ", fullName='" + fullName + "'}";
    }
}