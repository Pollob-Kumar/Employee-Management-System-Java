package com.ems.ui.panels;

import com.ems.dao.UserProfileDetails;
import com.ems.model.Department;
import com.ems.model.UserRole;
import com.ems.service.DepartmentService;
import com.ems.service.UserManagementService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;

public class UsersListPanel extends JPanel {

    private final UserManagementService service = new UserManagementService();
    private final DepartmentService departmentService = new DepartmentService();

    private final JComboBox<String> roleFilterCombo = new JComboBox<>(new String[]{"ALL", "EMPLOYEE", "MANAGER"});
    private final JTextField searchField = new JTextField();
    private final JButton refreshBtn = new JButton("Refresh");
    private final JButton deleteBtn = new JButton("Delete Selected");
    private final JButton editBtn = new JButton("Edit Selected");

    private final DefaultTableModel model;
    private final JTable table;

    private final JSplitPane split;

    // Edit form
    private final JPanel editPanel = new JPanel(new GridBagLayout());
    private final JLabel editTitle = new JLabel("Edit User");
    private final JComboBox<Department> deptCombo = new JComboBox<>();
    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextArea addressArea = new JTextArea(3, 20);
    private final JTextField joinDateField = new JTextField();

    // Manager extras
    private final JPanel managerExtraPanel = new JPanel(new GridBagLayout());
    private final JTextField designationField = new JTextField();
    private final JTextField allowanceField = new JTextField("0.00");

    private final JButton saveBtn = new JButton("Save Changes");
    private final JButton cancelBtn = new JButton("Cancel");

    private Long selectedUserId = null;
    private UserRole selectedRole = null;

    public UsersListPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 246, 250));

        JLabel title = new JLabel("Manage Users (Managers & Employees)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));

        JPanel top = buildTopBar();
        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(title, BorderLayout.NORTH);
        north.add(top, BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        // Table
        model = new DefaultTableModel(new Object[]{"User ID", "Username", "Role", "Full Name", "Department"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(28);
        JScrollPane tableScroll = new JScrollPane(table);

        // Edit panel UI
        buildEditPanel();
        editPanel.setVisible(false);

        split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, editPanel);
        split.setResizeWeight(0.80);
        split.setDividerSize(8);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        refreshDepartments();
        refreshTable();

        // actions
        refreshBtn.addActionListener(e -> refreshTable());
        roleFilterCombo.addActionListener(e -> refreshTable());
        searchField.addActionListener(e -> refreshTable());

        deleteBtn.addActionListener(e -> onDeleteSelected());
        editBtn.addActionListener(e -> onEditSelected());

        cancelBtn.addActionListener(e -> hideEdit());
        saveBtn.addActionListener(e -> onSave());
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        top.add(new JLabel("Role:"), gc);

        gc.gridx = 1; gc.weightx = 0;
        top.add(roleFilterCombo, gc);

        gc.gridx = 2; gc.weightx = 0;
        top.add(new JLabel("Search:"), gc);

        gc.gridx = 3; gc.weightx = 1;
        top.add(searchField, gc);

        gc.gridx = 4; gc.weightx = 0;
        top.add(refreshBtn, gc);

        gc.gridx = 5; gc.weightx = 0;
        top.add(editBtn, gc);

        gc.gridx = 6; gc.weightx = 0;
        top.add(deleteBtn, gc);

        return top;
    }

    private void refreshDepartments() {
        deptCombo.removeAllItems();
        for (Department d : departmentService.list()) deptCombo.addItem(d);
    }

    private void refreshTable() {
        try {
            var list = service.list((String) roleFilterCombo.getSelectedItem(), searchField.getText());
            model.setRowCount(0);
            for (var r : list) {
                model.addRow(new Object[]{r.getUserId(), r.getUsername(), r.getRole().name(), r.getFullName(), r.getDepartmentName()});
            }
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }
        long userId = ((Number) model.getValueAt(row, 0)).longValue();
        String username = String.valueOf(model.getValueAt(row, 1));
        String role = String.valueOf(model.getValueAt(row, 2));

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete user: " + username + " (" + role + ")?", "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            service.delete(userId);
            refreshTable();
            hideEdit();
            JOptionPane.showMessageDialog(this, "User deleted.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onEditSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a user first.");
            return;
        }

        selectedUserId = ((Number) model.getValueAt(row, 0)).longValue();
        selectedRole = UserRole.valueOf(String.valueOf(model.getValueAt(row, 2)));

        try {
            UserProfileDetails d = service.loadProfile(selectedUserId, selectedRole);
            prefill(d);
            showEditPanel();
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void prefill(UserProfileDetails d) {
        editTitle.setText("Edit " + d.role.name() + " (User ID: " + d.userId + ")");

        // set department selection
        Department selected = null;
        for (int i = 0; i < deptCombo.getItemCount(); i++) {
            Department dep = deptCombo.getItemAt(i);
            if (dep != null && dep.getId() == d.departmentId) {
                selected = dep;
                break;
            }
        }
        deptCombo.setSelectedItem(selected);

        fullNameField.setText(nullToEmpty(d.fullName));
        emailField.setText(nullToEmpty(d.email));
        phoneField.setText(nullToEmpty(d.phone));
        addressArea.setText(nullToEmpty(d.address));
        joinDateField.setText(nullToEmpty(d.joinDate));

        boolean isManager = d.role == UserRole.MANAGER;
        managerExtraPanel.setVisible(isManager);
        if (isManager) {
            designationField.setText(nullToEmpty(d.designation));
            allowanceField.setText(d.allowance == null ? "0.00" : d.allowance.toPlainString());
        } else {
            designationField.setText("");
            allowanceField.setText("0.00");
        }
    }

    private void showEditPanel() {
        editPanel.setVisible(true);

        // Expand split pane so edit panel is visible
        SwingUtilities.invokeLater(() -> {
            int h = split.getHeight();
            if (h <= 0) return;
            // give edit panel around 35% height
            split.setDividerLocation((int) (h * 0.60));
            editPanel.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
            editPanel.revalidate();
            editPanel.repaint();
        });

        revalidate();
        repaint();
    }

    private void onSave() {
        if (selectedUserId == null || selectedRole == null) return;

        Department dept = (Department) deptCombo.getSelectedItem();
        if (dept == null) {
            JOptionPane.showMessageDialog(this, "No department available.");
            return;
        }

        try {
            if (selectedRole == UserRole.EMPLOYEE) {
                service.updateEmployee(
                        selectedUserId,
                        dept.getId(),
                        fullNameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        addressArea.getText(),
                        joinDateField.getText()
                );
            } else {
                BigDecimal allowance = parseMoney(allowanceField.getText());
                service.updateManager(
                        selectedUserId,
                        dept.getId(),
                        fullNameField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        addressArea.getText(),
                        joinDateField.getText(),
                        designationField.getText(),
                        allowance
                );
            }

            JOptionPane.showMessageDialog(this, "Profile updated.");
            hideEdit();
            refreshTable();

        } catch (Exception ex) {
            showError(ex);
        }
    }

    private BigDecimal parseMoney(String text) {
        if (text == null) return BigDecimal.ZERO;
        text = text.trim();
        if (text.isEmpty()) return BigDecimal.ZERO;
        try {
            return new BigDecimal(text);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid allowance amount");
        }
    }

    private void hideEdit() {
        selectedUserId = null;
        selectedRole = null;
        editPanel.setVisible(false);

        fullNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        joinDateField.setText("");
        designationField.setText("");
        allowanceField.setText("0.00");

        revalidate();
        repaint();
    }

    private void buildEditPanel() {
        editPanel.setBackground(Color.WHITE);
        editPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; gc.weightx = 1;
        editTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        editPanel.add(editTitle, gc);

        gc.gridwidth = 1;

        addEditRow(gc, 1, "Department", deptCombo);
        addEditRow(gc, 2, "Full Name", fullNameField);
        addEditRow(gc, 3, "Email", emailField);
        addEditRow(gc, 4, "Phone", phoneField);

        // address
        gc.gridx = 0; gc.gridy = 5; gc.weightx = 0;
        editPanel.add(new JLabel("Address"), gc);
        gc.gridx = 1; gc.weightx = 1;
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        editPanel.add(addrScroll, gc);

        addEditRow(gc, 6, "Join Date (YYYY-MM-DD)", joinDateField);

        // Manager extras
        managerExtraPanel.setOpaque(false);
        managerExtraPanel.setLayout(new GridBagLayout());
        GridBagConstraints mgc = new GridBagConstraints();
        mgc.insets = new Insets(4, 4, 4, 4);
        mgc.fill = GridBagConstraints.HORIZONTAL;

        mgc.gridx = 0; mgc.gridy = 0; mgc.weightx = 0;
        managerExtraPanel.add(new JLabel("Designation"), mgc);
        mgc.gridx = 1; mgc.weightx = 1;
        managerExtraPanel.add(designationField, mgc);

        mgc.gridx = 0; mgc.gridy = 1; mgc.weightx = 0;
        managerExtraPanel.add(new JLabel("Allowance"), mgc);
        mgc.gridx = 1; mgc.weightx = 1;
        managerExtraPanel.add(allowanceField, mgc);

        gc.gridx = 0; gc.gridy = 7; gc.weightx = 0;
        editPanel.add(new JLabel("Manager Extra"), gc);
        gc.gridx = 1; gc.weightx = 1;
        editPanel.add(managerExtraPanel, gc);

        // buttons
        gc.gridx = 0; gc.gridy = 8; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(saveBtn);
        btnRow.add(cancelBtn);
        editPanel.add(btnRow, gc);
    }

    private void addEditRow(GridBagConstraints gc, int row, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = row; gc.weightx = 0;
        editPanel.add(new JLabel(label), gc);
        gc.gridx = 1; gc.weightx = 1;
        editPanel.add(comp, gc);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void showError(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.getClass().getSimpleName();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}