package com.example.taskmanager.service;

import com.example.taskmanager.model.User;

import java.text.Collator;
import java.util.LinkedList;
import java.util.Locale;

public class EmployeeAVLTree {

    private static class Node {
        String key;
        User user;
        Node left;
        Node right;
        int height;

        Node(String key, User user) {
            this.key = key;
            this.user = user;
            this.height = 1;
        }
    }

    private Node root;

    private final Collator collator;

    public EmployeeAVLTree() {
        collator = Collator.getInstance(new Locale("tr", "TR"));
        collator.setStrength(Collator.PRIMARY);
    }

    private String normalize(String text) {
        if (text == null) return "";
        return text.trim().toLowerCase(new Locale("tr", "TR"));
    }

    private String createKey(User user) {
        String fullName = normalize(user.getFullName());
        String username = normalize(user.getUsername());

        // Aynı isimli iki kişi varsa username ile ayırt edilsin diye key'e ekliyoruz
        return fullName + " | " + username;
    }

    private int compare(String a, String b) {
        return collator.compare(a, b);
    }

    private int height(Node node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(Node node) {
        if (node == null) return 0;
        return height(node.left) - height(node.right);
    }

    private Node rightRotate(Node y) {
        Node x = y.left;
        Node temp = x.right;

        x.right = y;
        y.left = temp;

        y.height = Math.max(height(y.left), height(y.right)) + 1;
        x.height = Math.max(height(x.left), height(x.right)) + 1;

        return x;
    }

    private Node leftRotate(Node x) {
        Node y = x.right;
        Node temp = y.left;

        y.left = x;
        x.right = temp;

        x.height = Math.max(height(x.left), height(x.right)) + 1;
        y.height = Math.max(height(y.left), height(y.right)) + 1;

        return y;
    }

    public void insert(User user) {
        if (user == null) return;
        root = insert(root, createKey(user), user);
    }

    private Node insert(Node node, String key, User user) {
        if (node == null) {
            return new Node(key, user);
        }

        int cmp = compare(key, node.key);

        if (cmp < 0) {
            node.left = insert(node.left, key, user);
        } else if (cmp > 0) {
            node.right = insert(node.right, key, user);
        } else {
            node.user = user;
            return node;
        }

        node.height = Math.max(height(node.left), height(node.right)) + 1;

        int balance = getBalance(node);

        // Left Left
        if (balance > 1 && compare(key, node.left.key) < 0) {
            return rightRotate(node);
        }

        // Right Right
        if (balance < -1 && compare(key, node.right.key) > 0) {
            return leftRotate(node);
        }

        // Left Right
        if (balance > 1 && compare(key, node.left.key) > 0) {
            node.left = leftRotate(node.left);
            return rightRotate(node);
        }

        // Right Left
        if (balance < -1 && compare(key, node.right.key) < 0) {
            node.right = rightRotate(node.right);
            return leftRotate(node);
        }

        return node;
    }

    public LinkedList<User> search(String keyword, String departmentFilter) {
        LinkedList<User> result = new LinkedList<>();

        String aranan = normalize(keyword);

        if (aranan.isEmpty()) {
            inOrder(root, result, departmentFilter);
        } else {
            searchByTraversal(root, aranan, result, departmentFilter);
        }

        return result;
    }

    private void searchByTraversal(Node node, String keyword, LinkedList<User> result, String departmentFilter) {
        if (node == null) return;

        searchByTraversal(node.left, keyword, result, departmentFilter);

        User user = node.user;

        String fullName = normalize(user.getFullName());
        String username = normalize(user.getUsername());
        String department = normalize(user.getDepartment());
        String title = normalize(user.getTitle());

        boolean matchesSearch =
                fullName.contains(keyword)
                        || username.contains(keyword)
                        || department.contains(keyword)
                        || title.contains(keyword);

        if (matchesSearch && matchesDepartment(user, departmentFilter)) {
            result.add(user);
        }

        searchByTraversal(node.right, keyword, result, departmentFilter);
    }

    private void inOrder(Node node, LinkedList<User> result, String departmentFilter) {
        if (node == null) return;

        inOrder(node.left, result, departmentFilter);

        if (matchesDepartment(node.user, departmentFilter)) {
            result.add(node.user);
        }

        inOrder(node.right, result, departmentFilter);
    }

    private boolean matchesDepartment(User user, String departmentFilter) {
        if (departmentFilter == null || departmentFilter.trim().isEmpty()) {
            return true;
        }

        if (user.getDepartment() == null) {
            return false;
        }

        return normalize(user.getDepartment()).equals(normalize(departmentFilter));
    }

    public int getHeight() {
        return height(root);
    }
}