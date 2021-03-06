/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jp.reports;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.jasperreports.engine.JRDataSource;

import com.jp.model.Contracts.TransactionEntry;
import com.jp.model.Contracts.AccountEntry;
import java.math.BigDecimal;

/**
 *
 * @author JurelP
 */
public class GenerateReports {
    
    private ResultSet resultSets;
    private JRDataSource dataSource;

    public GenerateReports() {
    }
    
    public GenerateReports(ResultSet resultSets) {
        this.resultSets = resultSets;
    }

    public void setResultSets(ResultSet resultSets) {
        this.resultSets = resultSets;
    }

    public JRDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(JRDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public void build() {
//        DRDataSource dataSource = new DRDataSource("name", "id", "reference_no", "date", "payee", 
//                "amount", "description", "type");

        DRDataSource dataSource = new DRDataSource("name", "id", "reference_no", "date", "payee", 
                "deposit", "payment", "is_clear");
        
        try {
            while (resultSets.next()) {
                
                dataSource.add(
                    resultSets.getString(AccountEntry.COL_CODE),
                    String.valueOf(resultSets.getInt(TransactionEntry.COL_ID)),
                    resultSets.getString(TransactionEntry.COL_REF_NO),
                    resultSets.getDate(TransactionEntry.COL_DATE),
                    resultSets.getString(TransactionEntry.COL_PAYEE),
                    new BigDecimal(resultSets.getDouble(TransactionEntry.COL_DEPOSIT)),
                    new BigDecimal(resultSets.getDouble(TransactionEntry.COL_PAYMENT)),
                    resultSets.getBoolean(TransactionEntry.COL_IS_CLEAR));
                
            }
        } catch (SQLException ex) {
            Logger.getLogger(GenerateReports.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.dataSource = dataSource;
    }

}
