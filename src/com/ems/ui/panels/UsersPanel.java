package com.ems.ui.panels;

import com.ems.model.Department;
import com.ems.model.UserRole;
import com.ems.service.DepartmentService;
import com.ems.service.UserAdminService;
import com.ems.service.UserCreateRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class UsersPanel extends JPanel {

    private final DepartmentService departmentService = new DepartmentService();
    private final UserAdminService userAdminService = new UserAdminService();

    private final JComboBox<UserRole> roleCombo = new JComboBox<>(new UserRole[]{UserRole.EMPLOYEE, UserRole.MANAGER});
    private final JComboBox<Department> deptCombo = new JComboBox<>();

    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    private final JTextField fullNameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTextField phoneField = new JTextField();
    private final JTextArea addressArea = new JTextArea(3, 20);
    private final JTextField joinDateField = new JTextField(); // YYYY-MM-DD

    // manager-only
    private final JPanel managerExtraPanel = new JPanel(new GridBagLayout());
    private final JTextField designationField = new JTextField();
    private final JTextField allowanceField = new JTextField("0.00");

    private final JButton createBtn = new JButton("Create User");
    private final JButton clearBtn = new JButton("Clear");

    public UsersPanel() {
        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(12, 12, 12, 12));
        setBackground(new Color(245, 246, 250));

        JLabel title = new JLabel("Create Manager / Employee");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(14, 14, 14, 14));
        add(form, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 0;

        // Row helper
        addRow(form, gc, "Role", roleCombo);
        addRow(form, gc, "Department", deptCombo);
        addRow(form, gc, "Username", usernameField);
        addRow(form, gc, "Password", passwordField);
        addRow(form, gc, "Full Name", fullNameField);
        addRow(form, gc, "Email", emailField);
        addRow(form, gc, "Phone", phoneField);

        // Address (textarea)
        gc.gridy++;
        gc.gridx = 0; gc.weightx = 0;
        form.add(label("Address"), gc);
        gc.gridx = 1; gc.weightx = 1;
        JScrollPane addrScroll = new JScrollPane(addressArea);
        addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true);
        form.add(addrScroll, gc);

        addRow(form, gc, "Join Date (YYYY-MM-DD)", joinDateField);

        // Manager extra panel
        buildManagerExtraPanel();
        gc.gridy++;
        gc.gridx = 0; gc.weightx = 0;
        form.add(label("Manager Extra"), gc);
        gc.gridx = 1; gc.weightx = 1;
        form.add(managerExtraPanel, gc);

        // Buttons
        gc.gridy++;
        gc.gridx = 1;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.add(createBtn);
        btnRow.add(clearBtn);
        form.add(btnRow, gc);

        // Load departments
        reloadDepartments();

        // Default join date hint
        joinDateField.setText(java.time.LocalDate.now().toString());

        // Role toggle
        roleCombo.addActionListener(e -> updateRoleUI());
        updateRoleUI();

        // Actions
        createBtn.addActionListener(e -> onCreate());
        clearBtn.addActionListener(e -> clearForm());
    }

    private void reloadDepartments() {
        deptCombo.removeAllItems();
        List<Department> list = departmentService.list();
        for (Department d : list) deptCombo.addItem(d);
    }

    private void updateRoleUI() {
        UserRole role = (UserRole) roleCombo.getSelectedItem();
        boolean isManager = role == UserRole.MANAGER;
        managerExtraPanel.setVisible(isManager);
        revalidate();
        repaint();
    }

    private void onCreate() {
        try {
            Department dept = (Department) deptCombo.getSelectedItem();
            if (dept == null) throw new IllegalArgumentException("No department found. Create department first.");

            UserCreateRequest req = new UserCreateRequest();
            req.role = (UserRole) roleCombo.getSelectedItem();
            req.departmentId = dept.getId();

            req.username = usernameField.getText();
            req.password = passwordField.getPassword();

            req.fullName = fullNameField.getText();
            req.email = emailField.getText();
            req.phone = phoneField.getText();
            req.address = addressArea.getText();
            req.joinDate = joinDateField.getText();

            if (req.role == UserRole.MANAGER) {
                req.designation = designationField.getText();
                req.allowance = parseMoney(allowanceField.getText());
            }

            long newUserId = userAdminService.createManagerOrEmployee(req);

            JOptionPane.showMessageDialog(this, "User created successfully. ID = " + newUserId);
            clearForm();

        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.isBlank()) msg = ex.getClass().getSimpleName();
            JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
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

    private void clearForm() {
        usernameField.setText("");
        passwordField.setText("");
        fullNameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        addressArea.setText("");
        joinDateField.setText(java.time.LocalDate.now().toString());
        designationField.setText("");
        allowanceField.setText("0.00");
        usernameField.requestFocusInWindow();
    }

    private void buildManagerExtraPanel() {
        managerExtraPanel.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.weightx = 0;
        managerExtraPanel.add(label("Designation"), gc);
        gc.gridx = 1; gc.weightx = 1;
        managerExtraPanel.add(designationField, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        managerExtraPanel.add(label("Allowance"), gc);
        gc.gridx = 1; gc.weightx = 1;
        managerExtraPanel.add(allowanceField, gc);
    }

    private void addRow(JPanel form, GridBagConstraints gc, String label, JComponent field) {
        gc.gridy++;
        gc.gridx = 0; gc.weightx = 0;
        form.add(this.label(label), gc);

        gc.gridx = 1; gc.weightx = 1;
        form.add(field, gc);
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 13));
        return l;
    }
}