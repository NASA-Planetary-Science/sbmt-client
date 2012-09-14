/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * NISNewFunctionDialog.java
 *
 * Created on Sep 12, 2012, 5:22:17 PM
 */
package edu.jhuapl.near.gui.eros;

import edu.jhuapl.near.model.eros.NISSpectrum;


/**
 *
 * @author kahneg1
 */
public class NISNewFunctionDialog extends javax.swing.JDialog {

    private boolean success = false;

    /** Creates new form NISNewFunctionDialog */
    public NISNewFunctionDialog(java.awt.Frame parent, boolean modal, String function) {
        super(parent, modal);
        initComponents();

        setLocationRelativeTo(parent);

        if (function != null)
            functionTextField.setText(function);
    }

    /** Return non null string containing function if user
     *successfully entered a valid function. If user
     * canceled dialog, return null.
     */
    public String getFunction()
    {
        if (success)
            return functionTextField.getText();
        else
            return null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        functionTextField = new javax.swing.JTextField();
        cancelButton = new javax.swing.JButton();
        okayButton = new javax.swing.JButton();
        invalidFunctionLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okayButton.setText("OK");
        okayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okayButtonActionPerformed(evt);
            }
        });

        invalidFunctionLabel.setForeground(new java.awt.Color(255, 0, 0));
        invalidFunctionLabel.setText("  ");

        jLabel1.setText("<html>\nEnter the function below using standard infix notation. Use variables B01<br>\nthrough B64 to refer to specific bands. For example, '(B42 - B01) * 0.5'.");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(okayButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)
                        .addGap(20, 20, 20))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(invalidFunctionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE)
                            .addComponent(functionTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(functionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(invalidFunctionLabel)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(okayButton)
                    .addComponent(cancelButton))
                .addContainerGap(33, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okayButtonActionPerformed
        success = NISSpectrum.testUserDefinedDerivedParameter(functionTextField.getText());
        if (success)
        {
            setVisible(false);
        }
        else
        {
            invalidFunctionLabel.setText("The formula is invalid.");
        }
    }//GEN-LAST:event_okayButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
    }//GEN-LAST:event_cancelButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JTextField functionTextField;
    private javax.swing.JLabel invalidFunctionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton okayButton;
    // End of variables declaration//GEN-END:variables
}
