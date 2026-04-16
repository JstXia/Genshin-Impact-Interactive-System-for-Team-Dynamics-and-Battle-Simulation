package ui;

import db.DBManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MainUI extends JFrame {
    
    private static final String DUELIST = "DUELIST";
    private static final String MAGE = "MAGE";
    private static final String BOSS_MAGE = "BOSS_MAGE";
    private static final String TANK_BOSS = "TANK_BOSS";
    private JTabbedPane tabs = new JTabbedPane();

    public MainUI() {
        super("ProtoArk Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Build tabs
        tabs.addTab("Characters", buildCharacterPanel());
        tabs.addTab("Weapons", buildWeaponPanel());
        tabs.addTab("Artifacts", buildArtifactPanel());
        tabs.addTab("Teams", buildTeamPanel());
        tabs.addTab("Mobs", buildMobPanel());
        tabs.addTab("Domains", buildDomainPanel());
        tabs.addTab("Simulator", buildSimulatorPanel());

        add(tabs);
    }   

    // ---------------- CHARACTER TAB ----------------
    private JPanel buildCharacterPanel() {
        JPanel p = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refresh = new JButton("Refresh");
        top.add(refresh);
        p.add(top, BorderLayout.NORTH);

        // Table
        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        JScrollPane sp = new JScrollPane(table);
        p.add(sp, BorderLayout.CENTER);

        // Form panel
        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField();
        JTextField tfElement = new JTextField();
        JTextField tfRegion = new JTextField();
        JTextField tfLevel = new JTextField();
        JTextField tfWeapon = new JTextField();
        JTextField tfA1 = new JTextField();
        JTextField tfA2 = new JTextField();
        JTextField tfA3 = new JTextField();
        JTextField tfA4 = new JTextField();
        JTextField tfAsc = new JTextField();

        form.add(new JLabel("CharacterID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Element")); form.add(tfElement);
        form.add(new JLabel("Region")); form.add(tfRegion);
        form.add(new JLabel("Level")); form.add(tfLevel);
        form.add(new JLabel("WeaponID")); form.add(tfWeapon);
        form.add(new JLabel("Artifact1 ID")); form.add(tfA1);
        form.add(new JLabel("Artifact2 ID")); form.add(tfA2);
        form.add(new JLabel("Artifact3 ID")); form.add(tfA3);
        form.add(new JLabel("Artifact4 ID")); form.add(tfA4);
        form.add(new JLabel("Ascension Material")); form.add(tfAsc);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("Add");
        JButton btnUpdate = new JButton("Update");
        JButton btnDelete = new JButton("Delete");
        south.add(btnAdd); south.add(btnUpdate); south.add(btnDelete);

        JPanel right = new JPanel(new BorderLayout());
        right.add(form, BorderLayout.CENTER);
        right.add(south, BorderLayout.SOUTH);

        p.add(right, BorderLayout.EAST);

        // Load function
        Runnable reload = () -> {
            try {
                tm.setRowCount(0);
                tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM charactertable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) {
                    tm.addRow(r.values().toArray());
                }
            } catch (SQLException ex) {
                showError(ex);
            }
        };

        refresh.addActionListener(e -> reload.run());
        reload.run();

        // Add handler
        btnAdd.addActionListener(e -> {
            try {
                String sql = "INSERT INTO charactertable (Name, Element, Region, Level, WeaponID, Artifact1, Artifact2, Artifact3, Artifact4, AscensionMaterial) VALUES (?,?,?,?,?,?,?,?,?,?)";
                DBManager.execute(sql,
                        nonEmpty(tfName.getText()), nonEmpty(tfElement.getText()), nonEmpty(tfRegion.getText()),
                        parseInt(tfLevel.getText()), emptyToNullInt(tfWeapon.getText()),
                        emptyToNullInt(tfA1.getText()), emptyToNullInt(tfA2.getText()), emptyToNullInt(tfA3.getText()), emptyToNullInt(tfA4.getText()),
                        nonEmpty(tfAsc.getText())
                );
                clearFields(tfName, tfElement, tfRegion, tfLevel, tfWeapon, tfA1, tfA2, tfA3, tfA4, tfAsc);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        // Update: uses CharacterID from selected row in table
        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a row to update."); return; }
                Object id = tm.getValueAt(sel, 0);
                String sql = "UPDATE charactertable SET Name=?, Element=?, Region=?, Level=?, WeaponID=?, Artifact1=?, Artifact2=?, Artifact3=?, Artifact4=?, AscensionMaterial=? WHERE CharacterID=?";
                DBManager.execute(sql,
                        nonEmpty(tfName.getText()), nonEmpty(tfElement.getText()), nonEmpty(tfRegion.getText()),
                        parseInt(tfLevel.getText()), emptyToNullInt(tfWeapon.getText()),
                        emptyToNullInt(tfA1.getText()), emptyToNullInt(tfA2.getText()), emptyToNullInt(tfA3.getText()), emptyToNullInt(tfA4.getText()),
                        nonEmpty(tfAsc.getText()), id
                );
                clearFields(tfID, tfName, tfElement, tfRegion, tfLevel, tfWeapon, tfA1, tfA2, tfA3, tfA4, tfAsc);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow();
                if (sel < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete."); return; }
                Object id = tm.getValueAt(sel, 0);
                DBManager.execute("DELETE FROM charactertable WHERE CharacterID=?", id);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        // When row selected, fill form
        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow();
            if (sel < 0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1)));
            tfElement.setText(str(tm.getValueAt(sel,2)));
            tfRegion.setText(str(tm.getValueAt(sel,3)));
            tfLevel.setText(str(tm.getValueAt(sel,4)));
            tfWeapon.setText(str(tm.getValueAt(sel,5)));
            tfA1.setText(str(tm.getValueAt(sel,6)));
            tfA2.setText(str(tm.getValueAt(sel,7)));
            tfA3.setText(str(tm.getValueAt(sel,8)));
            tfA4.setText(str(tm.getValueAt(sel,9)));
            tfAsc.setText(str(tm.getValueAt(sel,10)));
        });

        return p;
    }   

    // ---------------- WEAPONS TAB ----------------
    private JPanel buildWeaponPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField();
        JTextField tfType = new JTextField();
        JTextField tfAttack = new JTextField();
        JTextField tfSub = new JTextField();

        form.add(new JLabel("WeaponID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Type")); form.add(tfType);
        form.add(new JLabel("Attack")); form.add(tfAttack);
        form.add(new JLabel("SubStat")); form.add(tfSub);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add"), btnUpdate = new JButton("Update"), btnDelete = new JButton("Delete");
        JButton btnRefresh = new JButton("Refresh");
        controls.add(btnRefresh); controls.add(btnAdd); controls.add(btnUpdate); controls.add(btnDelete);

        p.add(form, BorderLayout.EAST);
        p.add(controls, BorderLayout.SOUTH);

        Runnable reload = () -> {
            try {
                tm.setRowCount(0); tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM weapontable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) tm.addRow(r.values().toArray());
            } catch (SQLException ex) { showError(ex); }
        };
        btnRefresh.addActionListener(e -> reload.run());
        reload.run();

        btnAdd.addActionListener(e -> {
            try {
                DBManager.execute("INSERT INTO weapontable (Name,Type,Attack,SubStat) VALUES (?,?,?,?)",
                        nonEmpty(tfName.getText()), nonEmpty(tfType.getText()), parseInt(tfAttack.getText()), nonEmpty(tfSub.getText()));
                clearFields(tfName, tfType, tfAttack, tfSub);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow(); if (sel<0) { JOptionPane.showMessageDialog(this,"Select a row."); return;}
                Object id = tm.getValueAt(sel,0);
                DBManager.execute("UPDATE weapontable SET Name=?,Type=?,Attack=?,SubStat=? WHERE WeaponID=?",
                        nonEmpty(tfName.getText()), nonEmpty(tfType.getText()), parseInt(tfAttack.getText()), nonEmpty(tfSub.getText()), id);
                clearFields(tfID, tfName, tfType, tfAttack, tfSub);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try { int sel = table.getSelectedRow(); if (sel<0) return; Object id = tm.getValueAt(sel,0);
                DBManager.execute("DELETE FROM weapontable WHERE WeaponID=?", id); reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow(); if (sel<0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1)));
            tfType.setText(str(tm.getValueAt(sel,2)));
            tfAttack.setText(str(tm.getValueAt(sel,3)));
            tfSub.setText(str(tm.getValueAt(sel,4)));
        });

        return p;
    }

    // ---------------- ARTIFACT TAB ----------------
    private JPanel buildArtifactPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField();
        JTextField tfType = new JTextField();
        JTextField tfMain = new JTextField();
        JTextField tfS1 = new JTextField(), tfS2 = new JTextField(), tfS3 = new JTextField(), tfS4 = new JTextField();

        form.add(new JLabel("ArtifactID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Type")); form.add(tfType);
        form.add(new JLabel("MainStat")); form.add(tfMain);
        form.add(new JLabel("SubStat1")); form.add(tfS1);
        form.add(new JLabel("SubStat2")); form.add(tfS2);
        form.add(new JLabel("SubStat3")); form.add(tfS3);
        form.add(new JLabel("SubStat4")); form.add(tfS4);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add"), btnUpdate = new JButton("Update"), btnDelete = new JButton("Delete"), btnRefresh = new JButton("Refresh");
        controls.add(btnRefresh); controls.add(btnAdd); controls.add(btnUpdate); controls.add(btnDelete);

        p.add(form, BorderLayout.EAST);
        p.add(controls, BorderLayout.SOUTH);

        Runnable reload = () -> {
            try {
                tm.setRowCount(0); tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM artifacttable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) tm.addRow(r.values().toArray());
            } catch (SQLException ex) { showError(ex); }
        };
        btnRefresh.addActionListener(e -> reload.run());
        reload.run();

        btnAdd.addActionListener(e -> {
            try {
                DBManager.execute("INSERT INTO artifacttable (Name,Type,MainStat,SubStat1,SubStat2,SubStat3,SubStat4) VALUES (?,?,?,?,?,?,?)",
                        nonEmpty(tfName.getText()), nonEmpty(tfType.getText()), nonEmpty(tfMain.getText()),
                        nonEmpty(tfS1.getText()), nonEmpty(tfS2.getText()), nonEmpty(tfS3.getText()), nonEmpty(tfS4.getText()));
                clearFields(tfName, tfType, tfMain, tfS1, tfS2, tfS3, tfS4);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow(); if (sel<0) { JOptionPane.showMessageDialog(this,"Select a row."); return;}
                Object id = tm.getValueAt(sel,0);
                DBManager.execute("UPDATE artifacttable SET Name=?,Type=?,MainStat=?,SubStat1=?,SubStat2=?,SubStat3=?,SubStat4=? WHERE ArtifactID=?",
                        nonEmpty(tfName.getText()), nonEmpty(tfType.getText()), nonEmpty(tfMain.getText()),
                        nonEmpty(tfS1.getText()), nonEmpty(tfS2.getText()), nonEmpty(tfS3.getText()), nonEmpty(tfS4.getText()), id);
                clearFields(tfID, tfName, tfType, tfMain, tfS1, tfS2, tfS3, tfS4);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try { int sel = table.getSelectedRow(); if (sel<0) return; Object id = tm.getValueAt(sel,0);
                DBManager.execute("DELETE FROM artifacttable WHERE ArtifactID=?", id); reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow(); if (sel<0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1)));
            tfType.setText(str(tm.getValueAt(sel,2)));
            tfMain.setText(str(tm.getValueAt(sel,3)));
            tfS1.setText(str(tm.getValueAt(sel,4)));
            tfS2.setText(str(tm.getValueAt(sel,5)));
            tfS3.setText(str(tm.getValueAt(sel,6)));
            tfS4.setText(str(tm.getValueAt(sel,7)));
        });

        return p;
    }

    // ---------------- TEAM TAB ----------------
    private JPanel buildTeamPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField();
        JTextField tfC1 = new JTextField(), tfC2 = new JTextField(), tfC3 = new JTextField(), tfC4 = new JTextField();

        form.add(new JLabel("TeamID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Char1 ID")); form.add(tfC1);
        form.add(new JLabel("Char2 ID")); form.add(tfC2);
        form.add(new JLabel("Char3 ID")); form.add(tfC3);
        form.add(new JLabel("Char4 ID")); form.add(tfC4);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add"), btnUpdate = new JButton("Update"), btnDelete = new JButton("Delete"), btnRefresh = new JButton("Refresh");
        controls.add(btnRefresh); controls.add(btnAdd); controls.add(btnUpdate); controls.add(btnDelete);

        p.add(form, BorderLayout.EAST);
        p.add(controls, BorderLayout.SOUTH);

        Runnable reload = () -> {
            try {
                tm.setRowCount(0); tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM teamtable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) tm.addRow(r.values().toArray());
            } catch (SQLException ex) { showError(ex); }
        };
        btnRefresh.addActionListener(e -> reload.run());
        reload.run();

        btnAdd.addActionListener(e -> {
            try {
                // Validate exactly 4 chars present and not empty
                if (tfC1.getText().trim().isEmpty() || tfC2.getText().trim().isEmpty() || tfC3.getText().trim().isEmpty() || tfC4.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "A team must contain 4 character IDs (no empty fields).");
                    return;
                }
                DBManager.execute("INSERT INTO teamtable (Name,Char1,Char2,Char3,Char4) VALUES (?,?,?,?,?)",
                        nonEmpty(tfName.getText()), parseInt(tfC1.getText()), parseInt(tfC2.getText()), parseInt(tfC3.getText()), parseInt(tfC4.getText()));
                clearFields(tfName, tfC1, tfC2, tfC3, tfC4);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow(); if (sel<0) { JOptionPane.showMessageDialog(this, "Select row."); return;}
                Object id = tm.getValueAt(sel,0);
                if (tfC1.getText().trim().isEmpty() || tfC2.getText().trim().isEmpty() || tfC3.getText().trim().isEmpty() || tfC4.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "A team must contain 4 character IDs (no empty fields).");
                    return;
                }
                DBManager.execute("UPDATE teamtable SET Name=?,Char1=?,Char2=?,Char3=?,Char4=? WHERE TeamID=?",
                        nonEmpty(tfName.getText()), parseInt(tfC1.getText()), parseInt(tfC2.getText()), parseInt(tfC3.getText()), parseInt(tfC4.getText()), id);
                clearFields(tfID, tfName, tfC1, tfC2, tfC3, tfC4);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try { int sel = table.getSelectedRow(); if (sel<0) return; Object id = tm.getValueAt(sel,0);
                DBManager.execute("DELETE FROM teamtable WHERE TeamID=?", id); reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow(); if (sel<0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1)));
            tfC1.setText(str(tm.getValueAt(sel,2)));
            tfC2.setText(str(tm.getValueAt(sel,3)));
            tfC3.setText(str(tm.getValueAt(sel,4)));
            tfC4.setText(str(tm.getValueAt(sel,5)));
        });

        return p;
    }

    // ---------------- MOBS TAB ----------------
private JPanel buildMobPanel() {
    JPanel p = new JPanel(new BorderLayout());

    DefaultTableModel tm = new DefaultTableModel();
    JTable table = new JTable(tm);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    // ===== FORM =====
    JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));

    JTextField tfID = new JTextField();
    tfID.setEditable(false);

    JTextField tfName = new JTextField();
    JTextField tfRegion = new JTextField();
    JTextField tfLevel = new JTextField();

    JComboBox<String> cbType = new JComboBox<>(
            new String[]{"Duelist", "Mage", "Boss Mage", "Tank Boss"}
    );

    JTextField tfElement = new JTextField();
    JTextField tfHP = new JTextField();
    JTextField tfATK = new JTextField();
    JTextField tfDEF = new JTextField();
    JTextField tfCritRate = new JTextField();
    JTextField tfCritDmg = new JTextField();

    form.add(new JLabel("MobID")); form.add(tfID);
    form.add(new JLabel("Name")); form.add(tfName);
    form.add(new JLabel("Region")); form.add(tfRegion);
    form.add(new JLabel("Level")); form.add(tfLevel);
    form.add(new JLabel("Type")); form.add(cbType);
    form.add(new JLabel("Element")); form.add(tfElement);
    form.add(new JLabel("HP")); form.add(tfHP);
    form.add(new JLabel("ATK")); form.add(tfATK);
    form.add(new JLabel("DEF")); form.add(tfDEF);
    form.add(new JLabel("Crit Rate")); form.add(tfCritRate);
    form.add(new JLabel("Crit Dmg")); form.add(tfCritDmg);

    // ===== CONTROLS =====
    JPanel controls = new JPanel();
    JButton btnAdd = new JButton("Add");
    JButton btnUpdate = new JButton("Update");
    JButton btnDelete = new JButton("Delete");
    JButton btnRefresh = new JButton("Refresh");

    controls.add(btnRefresh);
    controls.add(btnAdd);
    controls.add(btnUpdate);
    controls.add(btnDelete);

    p.add(form, BorderLayout.EAST);
    p.add(controls, BorderLayout.SOUTH);

    // ===== LOAD DATA =====
    Runnable reload = () -> {
        try {
            tm.setRowCount(0);
            tm.setColumnCount(0);

            List<Map<String, Object>> rows =
                    DBManager.fetchAll("SELECT * FROM mobstable");

            if (!rows.isEmpty()) {
                rows.get(0).keySet().forEach(tm::addColumn);
                for (Map<String, Object> r : rows) {
                    tm.addRow(r.values().toArray());
                }
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    };

    btnRefresh.addActionListener(e -> reload.run());
    reload.run();

    // ===== ADD MOB =====
    btnAdd.addActionListener(e -> {
        try {
            DBManager.execute(
                    "INSERT INTO mobstable (Name,Region,Level,MobType,Element,HP,ATK,DEF,CritRate,CritDmg) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    nonEmpty(tfName.getText()),
                    nonEmpty(tfRegion.getText()),
                    parseInt(tfLevel.getText()),
                    cbType.getSelectedItem().toString(),
                    nonEmpty(tfElement.getText()),
                    parseInt(tfHP.getText()),
                    parseInt(tfATK.getText()),
                    parseInt(tfDEF.getText()),
                    parseDouble(tfCritRate.getText()),
                    parseDouble(tfCritDmg.getText())
            );

            clearFields(tfName, tfRegion, tfLevel, tfElement, tfHP, tfATK, tfDEF, tfCritRate, tfCritDmg);
            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== UPDATE MOB =====
    btnUpdate.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a row.");
                return;
            }

            Object id = tm.getValueAt(sel, 0);

            DBManager.execute(
                    "UPDATE mobstable SET Name=?,Region=?,Level=?,MobType=?,Element=?,HP=?,ATK=?,DEF=?,CritRate=?,CritDmg=? WHERE MobID=?",
                    nonEmpty(tfName.getText()),
                    nonEmpty(tfRegion.getText()),
                    parseInt(tfLevel.getText()),
                    cbType.getSelectedItem().toString(),
                    nonEmpty(tfElement.getText()),
                    parseInt(tfHP.getText()),
                    parseInt(tfATK.getText()),
                    parseInt(tfDEF.getText()),
                    parseDouble(tfCritRate.getText()),
                    parseDouble(tfCritDmg.getText()),
                    id
            );

            clearFields(tfID, tfName, tfRegion, tfLevel, tfElement, tfHP, tfATK, tfDEF, tfCritRate, tfCritDmg);
            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== DELETE MOB =====
    btnDelete.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) return;

            Object id = tm.getValueAt(sel, 0);

            DBManager.execute("DELETE FROM mobstable WHERE MobID=?", id);
            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== SELECT ROW =====
    table.getSelectionModel().addListSelectionListener(e -> {
        int sel = table.getSelectedRow();
        if (sel < 0) return;

        tfID.setText(str(tm.getValueAt(sel, 0)));
        tfName.setText(str(tm.getValueAt(sel, 1)));
        tfRegion.setText(str(tm.getValueAt(sel, 2)));
        tfLevel.setText(str(tm.getValueAt(sel, 3)));
        cbType.setSelectedItem(str(tm.getValueAt(sel, 4)));
        tfElement.setText(str(tm.getValueAt(sel, 5)));
        tfHP.setText(str(tm.getValueAt(sel, 6)));
        tfATK.setText(str(tm.getValueAt(sel, 7)));
        tfDEF.setText(str(tm.getValueAt(sel, 8)));
        tfCritRate.setText(str(tm.getValueAt(sel, 9)));
        tfCritDmg.setText(str(tm.getValueAt(sel, 10)));
    });

    return p;
}
private JPanel buildDomainPanel() {
    JPanel p = new JPanel(new BorderLayout());

    DefaultTableModel tm = new DefaultTableModel();
    JTable table = new JTable(tm);
    p.add(new JScrollPane(table), BorderLayout.CENTER);

    // ===== FORM =====
    JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));

    JTextField tfID = new JTextField();
    tfID.setEditable(false);

    JTextField tfName = new JTextField();
    JTextField tfRegion = new JTextField();

    JComboBox<String> cbDifficulty =
            new JComboBox<>(new String[]{"Easy", "Medium", "Hard", "Extreme"});

    JComboBox<String> cbMob = new JComboBox<>();

    JButton btnAddMob = new JButton("Add Mob to Domain");
    JButton btnViewMobs = new JButton("View Domain Mobs");

    form.add(new JLabel("DomainID"));
    form.add(tfID);

    form.add(new JLabel("Name"));
    form.add(tfName);

    form.add(new JLabel("Region"));
    form.add(tfRegion);

    form.add(new JLabel("Difficulty"));
    form.add(cbDifficulty);

    form.add(new JLabel("Select Mob"));
    form.add(cbMob);

    form.add(btnAddMob);
    form.add(btnViewMobs);

    // ===== CONTROLS =====
    JPanel controls = new JPanel();
    JButton btnAdd = new JButton("Add");
    JButton btnUpdate = new JButton("Update");
    JButton btnDelete = new JButton("Delete");
    JButton btnRefresh = new JButton("Refresh");

    controls.add(btnRefresh);
    controls.add(btnAdd);
    controls.add(btnUpdate);
    controls.add(btnDelete);

    p.add(form, BorderLayout.EAST);
    p.add(controls, BorderLayout.SOUTH);

    // ===== LOAD DATA =====
    Runnable reload = () -> {
        try {
            tm.setRowCount(0);
            tm.setColumnCount(0);

            List<Map<String, Object>> rows =
                    DBManager.fetchAll("SELECT * FROM domaintable");

            if (!rows.isEmpty()) {
                rows.get(0).keySet().forEach(tm::addColumn);
                for (Map<String, Object> r : rows) {
                    tm.addRow(r.values().toArray());
                }
            }

            // reload mobs dropdown
            cbMob.removeAllItems();

            List<Map<String, Object>> mobs =
                    DBManager.fetchAll("SELECT * FROM mobstable");

            for (Map<String, Object> m : mobs) {
                cbMob.addItem(m.get("MobID") + " - " + m.get("Name"));
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    };

    btnRefresh.addActionListener(e -> reload.run());
    reload.run();

    // ===== ADD DOMAIN =====
    btnAdd.addActionListener(e -> {
        try {
            DBManager.execute(
                    "INSERT INTO domaintable (Name,Region,Difficulty) VALUES (?,?,?)",
                    nonEmpty(tfName.getText()),
                    nonEmpty(tfRegion.getText()),
                    cbDifficulty.getSelectedItem()
            );

            clearFields(tfName, tfRegion);
            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== UPDATE DOMAIN =====
    btnUpdate.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a domain row.");
                return;
            }

            Object id = tm.getValueAt(sel, 0);

            DBManager.execute(
                    "UPDATE domaintable SET Name=?,Region=?,Difficulty=? WHERE DomainID=?",
                    nonEmpty(tfName.getText()),
                    nonEmpty(tfRegion.getText()),
                    cbDifficulty.getSelectedItem(),
                    id
            );

            clearFields(tfID, tfName, tfRegion);
            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== DELETE DOMAIN =====
    btnDelete.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) return;

            Object id = tm.getValueAt(sel, 0);

            DBManager.execute("DELETE FROM domain_mobs WHERE DomainID=?", id);
            DBManager.execute("DELETE FROM domaintable WHERE DomainID=?", id);

            reload.run();

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== ADD MOB TO DOMAIN =====
    btnAddMob.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) {
                JOptionPane.showMessageDialog(this, "Select a domain first.");
                return;
            }

            Object domainId = tm.getValueAt(sel, 0);

            String selected = (String) cbMob.getSelectedItem();
            if (selected == null || !selected.contains(" - ")) return;

            int mobId = Integer.parseInt(selected.split(" - ")[0].trim());

            // prevent duplicate insert
            List<Map<String, Object>> check =
                    DBManager.fetchAll(
                            "SELECT * FROM domain_mobs WHERE DomainID=? AND MobID=?",
                            domainId, mobId
                    );

            if (!check.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mob already added to this domain.");
                return;
            }

            DBManager.execute(
                    "INSERT INTO domain_mobs (DomainID, MobID) VALUES (?, ?)",
                    domainId, mobId
            );

            JOptionPane.showMessageDialog(this, "Mob added to domain!");

        } catch (Exception ex) {
            showError(ex);
        }
    });

    // ===== VIEW DOMAIN MOBS =====
    btnViewMobs.addActionListener(e -> {
        try {
            int sel = table.getSelectedRow();
            if (sel < 0) return;

            Object domainId = tm.getValueAt(sel, 0);

            List<Map<String, Object>> mobs =
                    DBManager.fetchAll(
                            "SELECT m.* FROM mobstable m " +
                                    "JOIN domain_mobs dm ON m.MobID = dm.MobID " +
                                    "WHERE dm.DomainID=?",
                                    domainId
                    );

            if (mobs.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No mobs in this domain.");
            return;
            }

            StringBuilder sb = new StringBuilder("Domain Mobs:\n\n");

            for (Map<String, Object> m : mobs) {
                sb.append(m.get("MobID"))
                        .append(" - ")
                        .append(m.get("Name"))
                        .append(" (")
                        .append(m.get("MobType"))
                        .append(")\n");
            }

            JOptionPane.showMessageDialog(this, sb.toString());

        } catch (SQLException ex) {
            showError(ex);
        }
    });

    // ===== ROW SELECT =====
    table.getSelectionModel().addListSelectionListener(e -> {
        int sel = table.getSelectedRow();
        if (sel < 0) return;

        tfID.setText(str(tm.getValueAt(sel, 0)));
        tfName.setText(str(tm.getValueAt(sel, 1)));
        tfRegion.setText(str(tm.getValueAt(sel, 2)));
        cbDifficulty.setSelectedItem(str(tm.getValueAt(sel, 3)));
    });

    return p;
}
    // ---------------- SIMULATOR TAB ----------------
private JPanel buildSimulatorPanel() {
    JPanel p = new JPanel(new BorderLayout());

    JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JComboBox<model.DomainModel> cbDomain = new JComboBox<>();
    JComboBox<model.TeamModel> cbTeam = new JComboBox<>();
    JButton btnCalc = new JButton("Calculate");
    JButton btnRefresh = new JButton("Refresh");

    top.add(new JLabel("Domain:"));
    top.add(cbDomain);
    top.add(new JLabel("Team:"));
    top.add(cbTeam);
    top.add(btnCalc);
    top.add(btnRefresh);

    JTextArea out = new JTextArea(12, 80);
    out.setEditable(false);

    p.add(top, BorderLayout.NORTH);
    p.add(new JScrollPane(out), BorderLayout.CENTER);

    

    Runnable reloadBoxes = () -> {
        try {
            cbDomain.removeAllItems();
            cbTeam.removeAllItems();

            List<Map<String, Object>> drows =
                    DBManager.fetchAll("SELECT * FROM domaintable");

            for (Map<String, Object> r : drows) {
                model.DomainModel dm = new model.DomainModel();
                dm.domainID = toInt(r.get("DomainID"));
                dm.name = str(r.get("Name"));
                dm.region = str(r.get("Region"));
                dm.difficulty = str(r.get("Difficulty"));
                cbDomain.addItem(dm);
            }

            List<Map<String, Object>> trows =
                    DBManager.fetchAll("SELECT * FROM teamtable");

            for (Map<String, Object> r : trows) {
                model.TeamModel tm = new model.TeamModel();
                tm.teamID = toInt(r.get("TeamID"));
                tm.name = str(r.get("Name"));
                tm.char1 = toInt(r.get("Char1"));
                tm.char2 = toInt(r.get("Char2"));
                tm.char3 = toInt(r.get("Char3"));
                tm.char4 = toInt(r.get("Char4"));
                cbTeam.addItem(tm);
            }

        } catch (SQLException ex) {
            showError(ex);
        }
    };

    btnRefresh.addActionListener(e -> reloadBoxes.run());

    tabs.addChangeListener(e -> {
        if (tabs.getSelectedIndex() == 6) {
            reloadBoxes.run();
        }
    });

    reloadBoxes.run();

   btnCalc.addActionListener(e -> {
    try {
        DomainModel domain = (DomainModel) cbDomain.getSelectedItem();
        TeamModel team = (TeamModel) cbTeam.getSelectedItem();

        if (domain == null || team == null) {
            JOptionPane.showMessageDialog(this, "Select domain and team");
            return;
        }

        // =========================
        // GET DOMAIN MOBS
        // =========================
        List<Map<String, Object>> mobs = DBManager.fetchAll(
                "SELECT m.* FROM mobstable m " +
                        "JOIN domain_mobs dm ON m.MobID = dm.MobID " +
                        "WHERE dm.DomainID=?",
                domain.domainID
        );

        // =========================
        // VALIDATE COMPOSITION
        // =========================
        if (!validateDomainComposition(mobs, domain.difficulty)) {
            out.setText(
                    "❌ DOMAIN INVALID\n\n" +
                    "Composition does not match required difficulty rules:\n" +
                    domain.difficulty
            );
            return;
        }

        // =========================
        // GET CHARACTERS
        // =========================
        List<Integer> charIDs = Arrays.asList(
                team.char1, team.char2, team.char3, team.char4
        );

        List<Map<String, Object>> chars = DBManager.fetchAll(
                "SELECT * FROM charactertable WHERE CharacterID IN (?,?,?,?)",
                charIDs.get(0), charIDs.get(1), charIDs.get(2), charIDs.get(3)
        );

        if (chars.size() != 4) {
            JOptionPane.showMessageDialog(this, "One or more characters do not exist.");
            return;
        }

        // =========================
        // LEVEL CHECK
        // =========================
        double avgLevel = chars.stream()
                .mapToInt(m -> ((Number) m.get("Level")).intValue())
                .average()
                .orElse(0.0);

        int required = difficultyRequirement(domain.difficulty);

        if (avgLevel < required) {
            out.setText(String.format(
                    "❌ LEVEL TOO LOW\nAverage: %.2f\nRequired: %d\nDifficulty: %s",
                    avgLevel, required, domain.difficulty
            ));
            return;
        }

        // =========================
        // TEAM POWER CALCULATION
        // =========================
        double totalTeamPower = 0;
        StringBuilder sb = new StringBuilder();

        for (Map<String, Object> ch : chars) {

            int level = ((Number) ch.get("Level")).intValue();
            int wID = toInt(ch.get("WeaponID"));

            int weaponAtk = 0;
            if (wID > 0) {
                List<Map<String, Object>> w = DBManager.fetchAll(
                        "SELECT * FROM weapontable WHERE WeaponID=?",
                        wID
                );
                if (!w.isEmpty()) {
                    weaponAtk = toInt(w.get(0).get("Attack"));
                }
            }

            // =========================
            // ARTIFACT BONUS SYSTEM
            // =========================
            int a1 = toInt(ch.get("Artifact1"));
            int a2 = toInt(ch.get("Artifact2"));
            int a3 = toInt(ch.get("Artifact3"));
            int a4 = toInt(ch.get("Artifact4"));

            int artCount = 0;
            if (a1 > 0) artCount++;
            if (a2 > 0) artCount++;
            if (a3 > 0) artCount++;
            if (a4 > 0) artCount++;

            double artifactMultiplier = 2.1 + (0.5 * artCount);

            double charPower =
                    level * ((weaponAtk * 0.8) + 0.2) * artifactMultiplier;

            totalTeamPower += charPower;

            sb.append(String.format(
                    "Char %s (lvl %d) power %.1f\n",
                    ch.get("Name"), level, charPower
            ));
        }

        // =========================
        // DOMAIN HP CALCULATION
        // =========================
        double domainMultiplier;

        switch (domain.difficulty.toUpperCase()) {
            case "EASY": domainMultiplier = 0.8; break;
            case "MEDIUM": domainMultiplier = 1.0; break;
            case "HARD": domainMultiplier = 1.4; break;
            default: domainMultiplier = 2.0;
        }

        double baseHP = 0;

        for (Map<String, Object> m : mobs) {
            double hp = toDouble(m.get("HP"));
            String type = str(m.get("MobType")).toUpperCase();

            double typeMultiplier;

            switch (type) {
                case "DUELIST": typeMultiplier = 1.0; break;
                case "MAGE": typeMultiplier = 1.2; break;
                case "BOSS_MAGE": typeMultiplier = 1.6; break;
                case "TANK_BOSS": typeMultiplier = 2.2; break;
                default: typeMultiplier = 1.0;
            }

            baseHP += hp * typeMultiplier;
        }

        baseHP *= domainMultiplier;

        // =========================
        // SIMULATION
        // =========================
        double estimatedDPS = totalTeamPower / 10.0;
        double timeToKill = baseHP / Math.max(1, estimatedDPS);

        boolean canBeat = timeToKill < 120;

        sb.append("\n====================\n");
        sb.append("Total Team Power: ").append(String.format("%.1f", totalTeamPower)).append("\n");
        sb.append("Domain HP: ").append(String.format("%.0f", baseHP)).append("\n");
        sb.append("Estimated DPS: ").append(String.format("%.1f", estimatedDPS)).append("\n");
        sb.append("Time to Kill: ").append(String.format("%.1f sec", timeToKill)).append("\n");
        sb.append("Result: ").append(canBeat ? "WIN" : "LOSE");

        out.setText(sb.toString());

    } catch (SQLException ex) {
        showError(ex);
    }
});

    return p;
}

    // ---------------- Helpers ----------------
private static void clearFields(JTextField... fields) {
    for (JTextField f : fields) {
        if (f != null) f.setText("");
    }
}

private static String nonEmpty(String s) {
    return (s == null || s.trim().isEmpty()) ? null : s.trim();
}

private static Integer emptyToNullInt(String s) {
    try {
        if (s == null || s.trim().isEmpty()) return null;
        return Integer.valueOf(s.trim());
    } catch (NumberFormatException e) {
        return null;
    }
}

private static int parseInt(String s) {
    try {
        if (s == null || s.trim().isEmpty()) return 0;
        return Integer.parseInt(s.trim());
    } catch (NumberFormatException e) {
        return 0;
    }
}

private static double parseDouble(String s) {
    try {
        if (s == null || s.trim().isEmpty()) return 0.0;
        return Double.parseDouble(s.trim());
    } catch (NumberFormatException e) {
        return 0.0;
    }
}

private static String str(Object o) {
    return (o == null) ? "" : o.toString();
}

private static int toInt(Object o) {
    if (o == null) return 0;
    if (o instanceof Number) return ((Number) o).intValue();

    try {
        return Integer.parseInt(o.toString());
    } catch (NumberFormatException ex) {
        return 0;
    }
}

private static double toDouble(Object o) {
    if (o == null) return 0.0;
    if (o instanceof Number) return ((Number) o).doubleValue();

    try {
        return Double.parseDouble(o.toString());
    } catch (NumberFormatException ex) {
        return 0.0;
    }
}

private static int difficultyRequirement(String diff) {
    if ("Easy".equalsIgnoreCase(diff)) return 20;
    if ("Medium".equalsIgnoreCase(diff)) return 50;
    if ("Hard".equalsIgnoreCase(diff)) return 90;
    if ("Extreme".equalsIgnoreCase(diff)) return 100;
    return 0;
}

private void showError(Exception ex) {
    ex.printStackTrace();
    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
}

private boolean validateDomainComposition(List<Map<String, Object>> mobs, String difficulty) {

    Map<String, Integer> count = new HashMap<>();

    for (Map<String, Object> m : mobs) {
        String type = str(m.get("MobType")).toUpperCase();
        count.put(type, count.getOrDefault(type, 0) + 1);
    }

    switch (difficulty.toUpperCase()) {

        case "EASY":
            return count.getOrDefault(DUELIST, 0) >= 5;

        case "MEDIUM":
            return count.getOrDefault(DUELIST, 0) >= 5 &&
                   count.getOrDefault(MAGE, 0) >= 3;

        case "HARD":
            return count.getOrDefault(DUELIST, 0) >= 5 &&
                   count.getOrDefault(MAGE, 0) >= 5 &&
                   count.getOrDefault(BOSS_MAGE, 0) >= 5;

        case "EXTREME":
            return count.getOrDefault(DUELIST, 0) >= 10 &&
                   count.getOrDefault(MAGE, 0) >= 15 &&
                   count.getOrDefault(BOSS_MAGE, 0) >= 20 &&
                   count.getOrDefault(TANK_BOSS, 0) >= 5;
    }

    return false;
}
private double calculateDomainHP(int domainID, String difficulty) throws SQLException {

    List<Map<String, Object>> mobs = DBManager.fetchAll(
            "SELECT m.* FROM mobstable m " +
            "JOIN domain_mobs dm ON m.MobID = dm.MobID " +
            "WHERE dm.DomainID=?",
            domainID
    );

    double totalHP = 0;

    for (Map<String, Object> m : mobs) {
        double hp = toDouble(m.get("HP"));
        String type = str(m.get("MobType")).toUpperCase();

        double multiplier;

        switch (type) {
            case DUELIST: multiplier = 1.0; break;
            case MAGE: multiplier = 1.2; break;
            case BOSS_MAGE: multiplier = 1.6; break;
            case TANK_BOSS: multiplier = 2.2; break;
            default: multiplier = 1.0;
        }

        totalHP += hp * multiplier;
    }

    double difficultyMultiplier;

    switch (difficulty.toUpperCase()) {
        case "EASY": difficultyMultiplier = 1.0; break;
        case "MEDIUM": difficultyMultiplier = 1.3; break;
        case "HARD": difficultyMultiplier = 1.7; break;
        default: difficultyMultiplier = 2.2;
    }

    return totalHP * difficultyMultiplier;
}

    // main
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "MySQL JDBC driver not found. Add mysql-connector-java to classpath.");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainUI ui = new MainUI();
            ui.setVisible(true);
        });
    }
}
