//    Chromis POS  - The New Face of Open Source POS
//    Copyright (c) 2015 
//    http://www.chromis.co.uk
//
//    This file is part of Chromis POS
//
//     Chromis POS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    Chromis POS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with Chromis POS.  If not, see <http://www.gnu.org/licenses/>.

package uk.chromis.pos.epm;

import java.awt.Component;
import java.util.List;
import java.util.UUID;
import uk.chromis.basic.BasicException;
import uk.chromis.data.loader.SentenceList;
import uk.chromis.data.user.DirtyManager;
import uk.chromis.data.user.EditorRecord;
import uk.chromis.pos.forms.AppLocal;
import uk.chromis.pos.forms.AppView;

/**
 *
 * @author Ali Safdar & Aneeqa Baber
 */
public final class BreaksView extends javax.swing.JPanel implements EditorRecord {

    private Object m_oId;
    private SentenceList m_sentcat;
    private DirtyManager m_Dirty;

    /** Creates new form BreaksView
     * @param app
     * @param dirty */
    public BreaksView(AppView app, DirtyManager dirty) {

        DataLogicPresenceManagement dlPresenceManagement = (DataLogicPresenceManagement) app.getBean("uk.chromis.pos.epm.DataLogicPresenceManagement");
        initComponents();
        m_sentcat = dlPresenceManagement.getBreaksList();
        m_Dirty = dirty;
        m_jBreakName.getDocument().addDocumentListener(dirty);
        m_jVisible.addActionListener(dirty);
        m_jBreakDescription.getDocument().addDocumentListener(dirty);
        writeValueEOF();
    }

    void activate() throws BasicException {
        List a = m_sentcat.list();
        a.add(0, null);
    }

    /**
     *
     */
    @Override
    public void writeValueEOF() {
        m_oId = null;
        m_jBreakName.setText(null);
        m_jBreakDescription.setText(null);
        m_jVisible.setSelected(false);
        m_jBreakName.setEditable(false);
        m_jBreakDescription.setEnabled(false);
        m_jVisible.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void writeValueInsert() {
        m_oId = null;
        m_jBreakName.setText(null);
        m_jBreakDescription.setText(null);
        m_jVisible.setSelected(true);
        m_jBreakName.setEditable(true);
        m_jBreakDescription.setEnabled(true);
        m_jVisible.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueEdit(Object value) {
        Object[] breaks = (Object[]) value;
        m_oId = breaks[0];
        m_jBreakName.setText((String) breaks[1]);
        m_jBreakDescription.setText((String) breaks[2]);
        m_jVisible.setSelected(((Boolean) breaks[3]).booleanValue());
        m_jBreakName.setEditable(true);
        m_jBreakDescription.setEnabled(true);
        m_jVisible.setEnabled(true);
    }

    /**
     *
     * @param value
     */
    @Override
    public void writeValueDelete(Object value) {
        Object[] breaks = (Object[]) value;
        m_oId = breaks[0];
        m_jBreakName.setText((String) breaks[1]);
        m_jBreakDescription.setText((String) breaks[2]);
        m_jVisible.setSelected(((Boolean) breaks[3]).booleanValue());
        m_jBreakName.setEditable(false);
        m_jBreakDescription.setEnabled(false);
        m_jVisible.setEnabled(false);
    }

    /**
     *
     */
    @Override
    public void refresh() {
    }

    /**
     *
     * @return
     */
    @Override
    public Component getComponent() {
        return this;
    }

    /**
     *
     * @return
     * @throws BasicException
     */
    @Override
    public Object createValue() throws BasicException {
        Object[] breaks = new Object[4];
        breaks[0] = m_oId == null ? UUID.randomUUID().toString() : m_oId;
        breaks[1] = m_jBreakName.getText();
        breaks[2] = m_jBreakDescription.getText();
        breaks[3] = Boolean.valueOf(m_jVisible.isSelected());

        return breaks;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        m_jBreakName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        m_jBreakDescription = new javax.swing.JTextArea();
        m_Ncx = new javax.swing.JLabel();
        m_Name3 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        m_jVisible = new eu.hansolo.custom.SteelCheckBox();

        m_jBreakName.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_jBreakDescription.setColumns(20);
        m_jBreakDescription.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jBreakDescription.setLineWrap(true);
        m_jBreakDescription.setRows(5);
        jScrollPane1.setViewportView(m_jBreakDescription);

        m_Ncx.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_Ncx.setText(AppLocal.getIntString("label.epm.visible")); // NOI18N

        m_Name3.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_Name3.setText(AppLocal.getIntString("label.epm.notes")); // NOI18N

        jLabel1.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("pos_messages"); // NOI18N
        jLabel1.setText(bundle.getString("label.epm.employee")); // NOI18N

        m_jVisible.setText(" ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .addComponent(m_Name3, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                    .addComponent(m_Ncx, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(m_jBreakName, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 223, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVisible, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(214, 214, 214))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_jBreakName, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(m_Ncx, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(m_jVisible, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(m_Name3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(31, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel m_Name3;
    private javax.swing.JLabel m_Ncx;
    private javax.swing.JTextArea m_jBreakDescription;
    private javax.swing.JTextField m_jBreakName;
    private eu.hansolo.custom.SteelCheckBox m_jVisible;
    // End of variables declaration//GEN-END:variables
}
