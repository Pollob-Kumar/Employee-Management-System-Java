package com.ems.service;

import com.ems.dao.DepartmentDao;
import com.ems.model.Department;

import java.util.List;

public class DepartmentService {
    private final DepartmentDao dao = new DepartmentDao();

    public List<Department> list() {
        return dao.findAll();
    }

    public long create(String name) {
        name = normalize(name);
        validate(name);
        return dao.insert(name);
    }

    public void rename(long id, String name) {
        name = normalize(name);
        validate(name);
        dao.update(id, name);
    }

    public void remove(long id) {
        dao.delete(id);
    }

    private static String normalize(String name) {
        if (name == null) return "";
        return name.trim().replaceAll("\\s+", " ");
    }

    private static void validate(String name) {
        if (name.isEmpty()) throw new IllegalArgumentException("Department name required");
        if (name.length() < 2) throw new IllegalArgumentException("Department name too short");
        if (name.length() > 80) throw new IllegalArgumentException("Department name too long");
    }
}