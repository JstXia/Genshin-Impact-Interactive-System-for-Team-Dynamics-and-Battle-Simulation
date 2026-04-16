package ui;

import db.DBManager;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class MainUI extends JFrame {
    // Top-level
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

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField(), tfRegion = new JTextField(), tfLevel = new JTextField(), tfType = new JTextField(),
                tfElement = new JTextField(), tfHP = new JTextField(), tfATK = new JTextField(), tfDEF = new JTextField(),
                tfCritRate = new JTextField(), tfCritDmg = new JTextField();

        form.add(new JLabel("MobID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Region")); form.add(tfRegion);
        form.add(new JLabel("Level")); form.add(tfLevel);
        form.add(new JLabel("Type")); form.add(tfType);
        form.add(new JLabel("Element")); form.add(tfElement);
        form.add(new JLabel("HP")); form.add(tfHP);
        form.add(new JLabel("ATK")); form.add(tfATK);
        form.add(new JLabel("DEF")); form.add(tfDEF);
        form.add(new JLabel("CritRate (%)")); form.add(tfCritRate);
        form.add(new JLabel("CritDmg (%)")); form.add(tfCritDmg);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add"), btnUpdate = new JButton("Update"), btnDelete = new JButton("Delete"), btnRefresh = new JButton("Refresh");
        controls.add(btnRefresh); controls.add(btnAdd); controls.add(btnUpdate); controls.add(btnDelete);

        p.add(form, BorderLayout.EAST);
        p.add(controls, BorderLayout.SOUTH);

        Runnable reload = () -> {
            try {
                tm.setRowCount(0); tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM mobstable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) tm.addRow(r.values().toArray());
            } catch (SQLException ex) { showError(ex); }
        };
        btnRefresh.addActionListener(e -> reload.run());
        reload.run();

        btnAdd.addActionListener(e -> {
            try {
                DBManager.execute("INSERT INTO mobstable (Name,Region,Level,Type,Element,HP,ATK,DEF,CritRate,CritDmg) VALUES (?,?,?,?,?,?,?,?,?,?)",
                        nonEmpty(tfName.getText()), nonEmpty(tfRegion.getText()), parseInt(tfLevel.getText()), nonEmpty(tfType.getText()),
                        nonEmpty(tfElement.getText()), parseInt(tfHP.getText()), parseInt(tfATK.getText()), parseInt(tfDEF.getText()),
                        parseDouble(tfCritRate.getText()), parseDouble(tfCritDmg.getText()));
                clearFields(tfName, tfRegion, tfLevel, tfType, tfElement, tfHP, tfATK, tfDEF, tfCritRate, tfCritDmg);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow(); if (sel<0) { JOptionPane.showMessageDialog(this,"Select a row."); return;}
                Object id = tm.getValueAt(sel,0);
                DBManager.execute("UPDATE mobstable SET Name=?,Region=?,Level=?,Type=?,Element=?,HP=?,ATK=?,DEF=?,CritRate=?,CritDmg=? WHERE MobID=?",
                        nonEmpty(tfName.getText()), nonEmpty(tfRegion.getText()), parseInt(tfLevel.getText()), nonEmpty(tfType.getText()),
                        nonEmpty(tfElement.getText()), parseInt(tfHP.getText()), parseInt(tfATK.getText()), parseInt(tfDEF.getText()),
                        parseDouble(tfCritRate.getText()), parseDouble(tfCritDmg.getText()), id);
                clearFields(tfID, tfName, tfRegion, tfLevel, tfType, tfElement, tfHP, tfATK, tfDEF, tfCritRate, tfCritDmg);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try { int sel = table.getSelectedRow(); if (sel<0) return; Object id = tm.getValueAt(sel,0);
                DBManager.execute("DELETE FROM mobstable WHERE MobID=?", id); reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow(); if (sel<0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1))); tfRegion.setText(str(tm.getValueAt(sel,2)));
            tfLevel.setText(str(tm.getValueAt(sel,3))); tfType.setText(str(tm.getValueAt(sel,4)));
            tfElement.setText(str(tm.getValueAt(sel,5))); tfHP.setText(str(tm.getValueAt(sel,6)));
            tfATK.setText(str(tm.getValueAt(sel,7))); tfDEF.setText(str(tm.getValueAt(sel,8)));
            tfCritRate.setText(str(tm.getValueAt(sel,9))); tfCritDmg.setText(str(tm.getValueAt(sel,10)));
        });

        return p;
    }

    // ---------------- DOMAIN TAB ----------------
    private JPanel buildDomainPanel() {
        JPanel p = new JPanel(new BorderLayout());
        DefaultTableModel tm = new DefaultTableModel();
        JTable table = new JTable(tm);
        p.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridLayout(0,2,6,6));
        JTextField tfID = new JTextField(); tfID.setEditable(false);
        JTextField tfName = new JTextField(), tfRegion = new JTextField();
        JComboBox<String> cbDifficulty = new JComboBox<>(new String[]{"Easy","Medium","Hard","Extreme"});

        form.add(new JLabel("DomainID")); form.add(tfID);
        form.add(new JLabel("Name")); form.add(tfName);
        form.add(new JLabel("Region")); form.add(tfRegion);
        form.add(new JLabel("Difficulty")); form.add(cbDifficulty);

        JPanel controls = new JPanel();
        JButton btnAdd = new JButton("Add"), btnUpdate = new JButton("Update"), btnDelete = new JButton("Delete"), btnRefresh = new JButton("Refresh");
        controls.add(btnRefresh); controls.add(btnAdd); controls.add(btnUpdate); controls.add(btnDelete);

        p.add(form, BorderLayout.EAST);
        p.add(controls, BorderLayout.SOUTH);

        Runnable reload = () -> {
            try {
                tm.setRowCount(0); tm.setColumnCount(0);
                List<Map<String,Object>> rows = DBManager.fetchAll("SELECT * FROM domaintable");
                if (rows.isEmpty()) return;
                rows.get(0).keySet().forEach(k -> tm.addColumn(k));
                for (Map<String,Object> r : rows) tm.addRow(r.values().toArray());
            } catch (SQLException ex) { showError(ex); }
        };
        btnRefresh.addActionListener(e -> reload.run());
        reload.run();

        btnAdd.addActionListener(e -> {
            try {
                DBManager.execute("INSERT INTO domaintable (Name,Region,Difficulty) VALUES (?,?,?)",
                        nonEmpty(tfName.getText()), nonEmpty(tfRegion.getText()), (String) cbDifficulty.getSelectedItem());
                clearFields(tfName, tfRegion);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnUpdate.addActionListener(e -> {
            try {
                int sel = table.getSelectedRow(); if (sel<0) { JOptionPane.showMessageDialog(this,"Select a row."); return;}
                Object id = tm.getValueAt(sel,0);
                DBManager.execute("UPDATE domaintable SET Name=?,Region=?,Difficulty=? WHERE DomainID=?",
                        nonEmpty(tfName.getText()), nonEmpty(tfRegion.getText()), (String) cbDifficulty.getSelectedItem(), id);
                clearFields(tfID, tfName, tfRegion);
                reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        btnDelete.addActionListener(e -> {
            try { int sel = table.getSelectedRow(); if (sel<0) return; Object id = tm.getValueAt(sel,0);
                DBManager.execute("DELETE FROM domaintable WHERE DomainID=?", id); reload.run();
            } catch (SQLException ex) { showError(ex); }
        });

        table.getSelectionModel().addListSelectionListener(e -> {
            int sel = table.getSelectedRow(); if (sel<0) return;
            tfID.setText(String.valueOf(tm.getValueAt(sel,0)));
            tfName.setText(str(tm.getValueAt(sel,1))); tfRegion.setText(str(tm.getValueAt(sel,2)));
            cbDifficulty.setSelectedItem(str(tm.getValueAt(sel,3)));
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
        top.add(new JLabel("Domain:")); top.add(cbDomain);
        top.add(new JLabel("Team:")); top.add(cbTeam);
        top.add(btnCalc);

        // Info output
        JTextArea out = new JTextArea(12,80);
        out.setEditable(false);

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(out), BorderLayout.CENTER);

        Runnable reloadBoxes = () -> {
            try {
                cbDomain.removeAllItems();
                cbTeam.removeAllItems();
                
                List<Map<String,Object>> drows = DBManager.fetchAll("SELECT * FROM domaintable");
                for (Map<String,Object> r : drows) {
                    model.DomainModel dm = new model.DomainModel();
                    dm.domainID = toInt(r.get("DomainID"));
                    dm.name = (r.get("Name") == null) ? "" : r.get("Name").toString();
                    dm.region = (r.get("Region") == null) ? "" : r.get("Region").toString();
                    dm.difficulty = (r.get("Difficulty") == null) ? "" : r.get("Difficulty").toString();
                    cbDomain.addItem(dm);
                }
                List<Map<String,Object>> trows = DBManager.fetchAll("SELECT * FROM teamtable");
                for (Map<String,Object> r : trows) {
                    model.TeamModel tm = new model.TeamModel();
                    tm.teamID = toInt(r.get("TeamID"));
                    tm.name = (r.get("Name") == null) ? "" : r.get("Name").toString();
                    tm.char1 = toInt(r.get("Char1")); tm.char2 = toInt(r.get("Char2"));
                    tm.char3 = toInt(r.get("Char3")); tm.char4 = toInt(r.get("Char4"));
                    cbTeam.addItem(tm);
                }
            } catch (SQLException ex) { showError(ex); }
        };

        reloadBoxes.run();

        btnCalc.addActionListener(e -> {
            try {
                DomainModel domain = (DomainModel) cbDomain.getSelectedItem();
                TeamModel team = (TeamModel) cbTeam.getSelectedItem();
                if (domain == null || team == null) { JOptionPane.showMessageDialog(this, "Select domain and team"); return; }

                // Validate average level vs difficulty
                List<Integer> charIDs = Arrays.asList(team.char1, team.char2, team.char3, team.char4);
                List<Map<String,Object>> chars = DBManager.fetchAll("SELECT * FROM charactertable WHERE CharacterID IN (?,?,?,?)",
                        charIDs.get(0), charIDs.get(1), charIDs.get(2), charIDs.get(3));
                if (chars.size() != 4) {
                    JOptionPane.showMessageDialog(this, "One or more characters in the team ID list do not exist.");
                    return;
                }
                double avgLevel = chars.stream().mapToInt(m -> ((Number)m.get("Level")).intValue()).average().orElse(0.0);
                int required = difficultyRequirement(domain.difficulty);
                if (avgLevel < required) {
                    out.setText(String.format("Team average level is %.2f but domain difficulty %s requires average level %d. Team cannot enter.\n", avgLevel, domain.difficulty, required));
                    return;
                }

                // Build a simplified "power" metric for each character
                double totalTeamPower = 0;
                StringBuilder sb = new StringBuilder();
                for (Map<String,Object> ch : chars) {
                    int level = ((Number)ch.get("Level")).intValue();
                    int wID = toInt(ch.get("WeaponID"));
                    int weaponAtk = 0;
                    if (wID > 0) {
                        List<Map<String,Object>> w = DBManager.fetchAll("SELECT * FROM weapontable WHERE WeaponID=?", wID);
                        if (!w.isEmpty()) weaponAtk = toInt(w.get(0).get("Attack"));
                    }
                    // approximate artifact main stat adds bonus: each artifact adds ~5% effective power
                    int a1 = toInt(ch.get("Artifact1")), a2 = toInt(ch.get("Artifact2")), a3 = toInt(ch.get("Artifact3")), a4 = toInt(ch.get("Artifact4"));
                    int artCount = 0; if (a1>0) artCount++; if (a2>0) artCount++; if (a3>0) artCount++; if (a4>0) artCount++;
                    double artifactMultiplier = 1.0 + 0.05 * artCount;

                    // character base power: level * (weaponAtk*0.6 + 10)
                    double charPower = level * ((weaponAtk * 0.6) + 10) * artifactMultiplier;
                    totalTeamPower += charPower;
                    sb.append(String.format("Char %s (lvl %d) base power %.1f\n", ch.get("Name"), level, charPower));
                }

                // Domain "difficulty multiplier" and target HP derived from difficulty
                double domainMultiplier;
                String diff = domain.difficulty;
                if ("Easy".equalsIgnoreCase(diff)) domainMultiplier = 0.8;
                else if ("Medium".equalsIgnoreCase(diff)) domainMultiplier = 1.0;
                else if ("Hard".equalsIgnoreCase(diff)) domainMultiplier = 1.4;
                else domainMultiplier = 2.0;

                // Domain HP baseline
                double baseHP = 5000.0 * domainMultiplier * (1 + (difficultyRequirement(domain.difficulty) / 100.0));
               
                double estimatedDPS = totalTeamPower / 10.0; 
                double timeToKill = baseHP / Math.max(1, estimatedDPS); 

                boolean canBeat = timeToKill < 120;

                sb.append("\nTotal team power: ").append(String.format("%.1f", totalTeamPower));
                sb.append("\nDomain base HP approximated: ").append(String.format("%.0f", baseHP));
                sb.append("\nEstimated team DPS: ").append(String.format("%.1f", estimatedDPS));
                sb.append("\nEstimated time to kill: ").append(String.format("%.1f seconds", timeToKill));
                sb.append("\nResult: ").append(canBeat ? "Team CAN beat the domain" : "Team CANNOT reliably beat the domain");

                out.setText(sb.toString());

            } catch (SQLException ex) { showError(ex); }
        });

        return p;
    }

    // ---------------- Helpers ----------------
    private static void clearFields(JTextField... fields) {
        for (JTextField f : fields) f.setText("");
    }
    private static String nonEmpty(String s) { return s == null || s.trim().isEmpty() ? null : s.trim(); }
    private static Integer emptyToNullInt(String s) { if (s==null || s.trim().isEmpty()) return null; return Integer.valueOf(s.trim()); }
    private static int parseInt(String s) { if (s==null || s.trim().isEmpty()) return 0; return Integer.parseInt(s.trim()); }
    private static double parseDouble(String s) { if (s==null || s.trim().isEmpty()) return 0.0; return Double.parseDouble(s.trim()); }
    private static String str(Object o) { return o == null ? "" : o.toString(); }
    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException ex) {
            return 0;
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
