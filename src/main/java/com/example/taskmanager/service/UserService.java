package com.example.taskmanager.service;

import com.example.taskmanager.model.User;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {

    private static UserService instance;

    /** STACK - oturum açan kullanıcıları takip eder */
    private final java.util.Stack<User> sessionStack = new java.util.Stack<>();

    /** LINKED LIST - kullanıcılar bağlı listede tutulur */
    private final java.util.LinkedList<User> userList = new java.util.LinkedList<>();

    private EmployeeAVLTree employeeAVLTree = new EmployeeAVLTree();

    private UserService() {
        loadUsers();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    private void loadUsers() {
        try {
            InputStream is = getClass().getResourceAsStream("/com/example/taskmanager/users.json");
            if (is == null) {
                System.err.println("HATA: users.json bulunamadı!");
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line.trim());
            }
            reader.close();

            String json = sb.toString();
            List<User> parsed = parseUsersFromJson(json);
            userList.addAll(parsed);
            buildEmployeeAVLTree();

            System.out.println("✅ " + userList.size() + " kullanıcı yüklendi.");

        } catch (Exception e) {
            System.err.println("JSON okuma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void buildEmployeeAVLTree() {
        employeeAVLTree = new EmployeeAVLTree();

        for (User user : userList) {
            if (!user.isManager()) {
                employeeAVLTree.insert(user);
            }
        }
    }

    public java.util.LinkedList<User> searchEmployeesWithAVL(String keyword, String departmentFilter) {
        return employeeAVLTree.search(keyword, departmentFilter);
    }

    private List<User> parseUsersFromJson(String json) { //json'dan kullanıcıları alır
        List<User> result = new ArrayList<>();
        int arrayStart = json.indexOf('[');
        int arrayEnd = json.lastIndexOf(']');
        if (arrayStart == -1 || arrayEnd == -1) return result;

        String arrayContent = json.substring(arrayStart + 1, arrayEnd);
        List<String> objects = extractJsonObjects(arrayContent);

        for (String obj : objects) {
            User user = parseUserObject(obj);
            if (user != null) {
                result.add(user);
            }
        }
        return result;
    }

    private List<String> extractJsonObjects(String arrayContent) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;

        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    objects.add(arrayContent.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private User parseUserObject(String obj) {
        try {
            int id = Integer.parseInt(extractValue(obj, "id"));
            String username = extractValue(obj, "username");
            String password = extractValue(obj, "password");
            String roleStr = extractValue(obj, "role");
            String fullName = extractValue(obj, "fullName");
            String department = extractValue(obj, "department");

            String title = extractValue(obj, "title");

            User.Role role = "MANAGER".equalsIgnoreCase(roleStr)
                    ? User.Role.MANAGER
                    : User.Role.EMPLOYEE;

            //user nesnesini oluştururken title bilgisini ekliyoruz
            User user = new User(id, username, password, role, fullName, department);
            user.setTitle(title);

            return user;

        } catch (Exception e) {
            System.err.println("Kullanıcı parse hatası: " + e.getMessage());
            return null;
        }
    }

    private String extractValue(String obj, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = obj.indexOf(searchKey);
        if (keyIndex == -1) return "";

        int colonIndex = obj.indexOf(':', keyIndex + searchKey.length());
        if (colonIndex == -1) return "";

        int valueStart = colonIndex + 1;
        while (valueStart < obj.length() && (obj.charAt(valueStart) == ' ' || obj.charAt(valueStart) == '\t')) {
            valueStart++;
        }

        if (valueStart < obj.length() && obj.charAt(valueStart) == '"') {
            int end = obj.indexOf('"', valueStart + 1);
            if (end != -1) return obj.substring(valueStart + 1, end);
        } else {
            int end = valueStart;
            while (end < obj.length() && obj.charAt(end) != ',' && obj.charAt(end) != '}' && obj.charAt(end) != ']') {
                end++;
            }
            return obj.substring(valueStart, end).trim();
        }
        return "";
    }

    public Optional<User> login(String username, String password) {
        for (User user : userList) {
            if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                sessionStack.push(user);
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    public void logout() {
        if (!sessionStack.isEmpty()) {
            sessionStack.pop();
        }
    }

    public Optional<User> getCurrentUser() {
        if (!sessionStack.isEmpty()) {
            return Optional.of(sessionStack.peek());
        }
        return Optional.empty();
    }

    public java.util.LinkedList<User> getAllEmployees() {
        java.util.LinkedList<User> employees = new java.util.LinkedList<>();
        for (User u : userList) {
            if (u.getRole() == User.Role.EMPLOYEE) {
                employees.add(u);
            }
        }
        return employees;
    }

    public java.util.LinkedList<User> getEmployeesByDepartment(String department) {
        java.util.LinkedList<User> result = new java.util.LinkedList<>();
        for (User u : userList) {
            if (u.getRole() == User.Role.EMPLOYEE &&
                    u.getDepartment().equalsIgnoreCase(department)) {
                result.add(u);
            }
        }
        return result;
    }

    public java.util.LinkedList<User> getAllUsers() {
        return new java.util.LinkedList<>(userList);
    }
}