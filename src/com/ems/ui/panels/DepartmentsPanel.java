package com.ems.ui.panels;

import com.ems.model.Department;
import com.ems.service.DepartmentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DepartmentsPanel extends JPanel {

    private final DepartmentService service = new DepartmentService();

    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField nameField = new JTextField();
    private final JButton addBtn = new JButton("Add");
    private final JButton updateBtn = new JButton("Update");
    private final JButton deleteBtn = new JButton("Delete");
    private final JButton clearBtn = new JButton("Clear");

    private Long selectedId = null;

    public DepartmentsPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 246, 250));

        JLabel title = new JLabel("Departments");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Department Name"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(28);

        // hide ID column visually (but keep in model)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        JScrollPane scroll = new JScrollPane(table);

        // Form panel (right side)
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(12, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;

        JLabel nameLbl = new JLabel("Department Name");
        nameLbl.setFont(new Font("SansSerif", Font.PLAIN, 13));
        form.add(nameLbl, gc);

        gc.gridy++;
        nameField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        form.add(nameField, gc);

        gc.gridy++;
        JPanel btnRow1 = new JPanel(new GridLayout(1, 2, 10, 10));
        btnRow1.setOpaque(false);
        btnRow1.add(addBtn);
        btnRow1.add(updateBtn);
        form.add(btnRow1, gc);

        gc.gridy++;
        JPanel btnRow2 = new JPanel(new GridLayout(1, 2, 10, 10));
        btnRow2.setOpaque(false);
        btnRow2.add(deleteBtn);
        btnRow2.add(clearBtn);
        form.add(btnRow2, gc);

        // Split view
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll, form);
        split.setResizeWeight(0.70);
        split.setDividerSize(8);
        add(split, BorderLayout.CENTER);

        // Actions
        addBtn.addActionListener(e -> onAdd());
        updateBtn.addActionListener(e -> onUpdate());
        deleteBtn.addActionListener(e -> onDelete());
        clearBtn.addActionListener(e -> clearForm());

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onRowSelect();
        });

        refreshTable();
        clearForm();
    }

    private void refreshTable() {
        List<Department> list = service.list();
        tableModel.setRowCount(0);
        for (Department d : list) {
            tableModel.addRow(new Object[]{d.getId(), d.getName()});
        }
    }

    private void onAdd() {
        try {
            String name = nameField.getText();
            service.create(name);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Department added.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onUpdate() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a department first.");
            return;
        }
        try {
            service.rename(selectedId, nameField.getText());
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Department updated.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onDelete() {
        if (selectedId == null) {
            JOptionPane.showMessageDialog(this, "Select a department first.");
            return;
        }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete selected department?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        try {
            service.remove(selectedId);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Department deleted.");
        } catch (Exception ex) {
            showError(ex);
        }
    }

    private void onRowSelect() {
        int row = table.getSelectedRow();
        if (row < 0) return;

        Object idObj = tableModel.getValueAt(row, 0);
        Object nameObj = tableModel.getValueAt(row, 1);

        selectedId = (idObj instanceof Number) ? ((Number) idObj).longValue() : Long.parseLong(idObj.toString());
        nameField.setText(nameObj == null ? "" : nameObj.toString());
    }

    private void clearForm() {
        selectedId = null;
        nameField.setText("");
        table.clearSelection();
        nameField.requestFocusInWindow();
    }

    private void showError(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) msg = ex.getClass().getSimpleName();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}