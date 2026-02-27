package com.ems.dao;

import com.ems.model.Department;
import com.ems.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    public List<Department> findAll() {
        String sql = "SELECT id, name FROM departments ORDER BY name";
        List<Department> list = new ArrayList<>();

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Department(rs.getLong("id"), rs.getString("name")));
            }
            return list;

        } catch (Exception e) {
            throw new RuntimeException("DB error in DepartmentDao.findAll", e);
        }
    }

    public long insert(String name) {
        String sql = "INSERT INTO departments(name) VALUES(?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getLong(1);
                throw new IllegalStateException("No generated key returned.");
            }
        } catch (Exception e) {
            throw new RuntimeException("DB error in DepartmentDao.insert", e);
        }
    }

    public void update(long id, String name) {
        String sql = "UPDATE departments SET name=? WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setLong(2, id);

            int updated = ps.executeUpdate();
            if (updated == 0) throw new IllegalStateException("Department not found: id=" + id);

        } catch (Exception e) {
            throw new RuntimeException("DB error in DepartmentDao.update", e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM departments WHERE id=?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setLong(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            // If FK blocks delete, MySQL will throw exception
            throw new RuntimeException("DB error in DepartmentDao.delete", e);
        }
    }
}