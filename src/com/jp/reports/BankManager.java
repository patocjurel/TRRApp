/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jp.reports;

import com.jp.model.AccountSummary;
import com.jp.model.AdditionalFund;
import com.jp.model.OnDateFund;
import com.jp.model.Other;
import com.jp.utils.Utils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.dynamicreports.examples.Templates;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression;

import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static net.sf.dynamicreports.report.builder.DynamicReports.sbt;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import static net.sf.dynamicreports.report.builder.DynamicReports.exp;
import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport;
import static net.sf.dynamicreports.report.builder.DynamicReports.margin;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.variable;
import net.sf.dynamicreports.report.builder.VariableBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.component.SubreportBuilder;
import net.sf.dynamicreports.report.builder.style.FontBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.builder.subtotal.AggregationSubtotalBuilder;
import net.sf.dynamicreports.report.builder.subtotal.SubtotalBuilders;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.ComponentPositionType;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.PageType;
import net.sf.dynamicreports.report.datasource.DRDataSource;
import net.sf.dynamicreports.report.definition.ReportParameters;
import net.sf.dynamicreports.report.exception.DRException;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 *
 * @author JurelP
 */
public class BankManager {

//    private AggregationSubtotalBuilder<Integer> exampleAggregate;
    private List<AccountSummary> accountSummaries;
    private List<Other> othersFunds;
    private List<OnDateFund> onDateFunds;
    private List<AdditionalFund> additionalFunds;
    private Date preparedDate, nextBankingDate;

    private List<SubreportData> subreportData;
    
    private double tfr, actual, prelim, onDate, eDeficit, eLacking, others, additionalFund;

    private final FontBuilder normalFont = stl.font("Times New Roman", false, false, 8);
    private final StyleBuilder normalText = stl.style(normalFont);
    
    
    private SubReportAccounts subReportAccounts;
    private SubReportOtherFunds subReportOtherFunds;
    private SubReportOnDateFunds subReportOnDateFunds;
    private SubReportAdditionalFunds subReportAdditionalFunds;

    private VariableBuilder<BigDecimal> actualSum, preliminarySum, otherFundsSum, onDateFundsSum, additionalFundsSum;

    public BankManager() {
        subreportData = new ArrayList<>();
    }

    public BankManager(List<AccountSummary> accountSummaries, List<Other> othersFunds, List<OnDateFund> onDateFunds, List<AdditionalFund> additionalFunds) {
        this.accountSummaries = accountSummaries;
        this.othersFunds = othersFunds;
        this.onDateFunds = onDateFunds;
        this.additionalFunds = additionalFunds;
    }

    public void setAccountSummaries(List<AccountSummary> accountSummaries) {
        this.accountSummaries = accountSummaries;
    }

    public void setOthersFunds(List<Other> othersFunds) {
        this.othersFunds = othersFunds;
    }

    public void setOnDateFunds(List<OnDateFund> onDateFunds) {
        this.onDateFunds = onDateFunds;
    }

    public void setAdditionalFunds(List<AdditionalFund> additionalFunds) {
        this.additionalFunds = additionalFunds;
    }

    public void setPreparedDate(Date preparedDate) {
        this.preparedDate = preparedDate;
    }

    public void setNextBankingDate(Date nextBankingDate) {
        this.nextBankingDate = nextBankingDate;
    }

    public void generate() {
        System.out.println("Generate Reports...");
        System.out.println(this.accountSummaries);
        System.out.println(this.othersFunds);
        System.out.println(this.onDateFunds);
        System.out.println(this.additionalFunds);
        System.out.println("End Generating Reports...");

        subReportAccounts = new SubReportAccounts(this.accountSummaries, this.preparedDate, this.nextBankingDate);
        subReportOtherFunds = new SubReportOtherFunds(othersFunds);
        subReportOnDateFunds = new SubReportOnDateFunds(onDateFunds);
        subReportAdditionalFunds = new SubReportAdditionalFunds(additionalFunds);
        subreportData.add(new SubreportData(subReportAccounts.getTitle(), subReportAccounts.getColumns(), subReportAccounts.getDataSource()));
        subreportData.add(new SubreportData(subReportOtherFunds.getTitle(), subReportOtherFunds.getColumns(), subReportOtherFunds.getDataSource()));
        subreportData.add(new SubreportData(subReportOnDateFunds.getTitle(), subReportOnDateFunds.getColumns(), subReportOnDateFunds.getDataSource()));
        subreportData.add(new SubreportData(subReportAdditionalFunds.getTitle(), subReportAdditionalFunds.getColumns(), subReportAdditionalFunds.getDataSource()));
    }

    public void build() {

        SubreportBuilder subreportBuilder = cmp.subreport(
                new SubreportExpression()).setDataSource(new SubreportDataSourceExpression());

        JasperReportBuilder report = report();

        try {

//            report.addField("title", String.class);
//            report.addField("columns", List.class);
//            report.addField("data", DRDataSource.class);
//
//            report
//                    .title(cmp.text("Prepare On: "
//                            + Utils.humanDate(preparedDate)
//                            + "\nNext Banking Day: " + Utils.humanDate(nextBankingDate)).setStyle(normalText))
//                    .detail(subreportBuilder.setStyle(normalText), cmp.verticalGap(20))
//                    .setDataSource(subreportData)
//                    .summary(cmp.text("Actual:\t" + Utils.formatDecimal(actual) +
//                            "\nPreliminary:\t" + Utils.formatDecimal(prelim) +
//                            "\nSub-total of Other Funding Needs:\t" + Utils.formatDecimal(others) +
//                            "\nTotal Funding Needs:\t" + Utils.formatDecimal(others) +
//                            "\nBal forwarded:\t" + Utils.formatDecimal(getExcessDeficit()) +
//                            "\nTotal Funding Resource:\t" + Utils.formatDecimal(getTotalFundingResource()) +
//                            "\nExcess/(Deficit):\t" + Utils.formatDecimal(getExcessDeficit()) +
//                            "\nExcess/ (Lacking):\t" + Utils.formatDecimal(getExcessLacking())))
//                    .pageFooter(Templates.footerComponent)
//                    .setTemplate(Templates.reportTemplate)
//                    .show(false);

            report
//                    .title(cmp.text("PLC Report").setStyle(Templates.bold12CenteredStyle))
                    .setPageMargin(margin(40))
                    .setPageFormat(PageType.LETTER)
                    .summary(
                    cmp.verticalList(
                            cmp.verticalGap(10),
                            cmp.horizontalList(
                                    cmp.horizontalGap(60),
                                    cmp.subreport(subReportAccounts.getReport()).setFixedWidth(400),
                                    cmp.horizontalGap(60)
                            ),
                            cmp.verticalGap(20),
                            cmp.horizontalList(
                                    cmp.horizontalGap(60),
                                    cmp.subreport(subReportOtherFunds.getReport()).setFixedWidth(400),
                                    cmp.horizontalGap(60)
                            ),
//                            cmp.subreport(subReportAccounts.getReport()).setFixedWidth(400).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)),
//                            cmp.verticalGap(20),
//                            cmp.subreport(subReportOtherFunds.getReport()).setFixedWidth(400).setStyle(stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER)),
                            cmp.verticalGap(20),
                            
                            cmp.horizontalList(
                                    cmp.horizontalGap(60),
                                    cmp.subreport(subReportOnDateFunds.getReport()),
                                    cmp.horizontalGap(15),
                                    cmp.verticalList(
                                            cmp.horizontalList(
                                                    cmp.text("Bal Forwarded").setStyle(Utils.boldTextBuilder()),
                                                    cmp.text(Utils.formatNegative(getExcessDeficit())).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(Utils.boldTextBuilder())
                                            ),
                                            cmp.subreport(subReportAdditionalFunds.getReport())
                                    ),
                                    cmp.horizontalGap(70)
                                    
                            ),
                            cmp.verticalGap(200),
                            cmp.horizontalList(
                                    cmp.horizontalGap(60),
                                    cmp.text("Total Funding Resource").setStyle(Utils.topBorderBuilder()),
                                    cmp.text(Utils.formatNegative(getTotalFundingResource())).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(Utils.topBorderBuilder()),
                                    cmp.horizontalGap(15),
                                    cmp.text(""),
                                    cmp.text("").setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT),
                                    cmp.horizontalGap(70)
                            ),
                            cmp.horizontalList(
                                    cmp.horizontalGap(60),
                                    cmp.text("Excess / (Deficit)").setStyle(Utils.topBorderBuilder()),
                                    cmp.text(Utils.formatNegative(getExcessDeficit())).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(Utils.topBorderBuilder()),
                                    cmp.horizontalGap(15),
                                    cmp.text("Excess (Lacking)").setStyle(Utils.topBorderBuilder()),
                                    cmp.text(Utils.formatNegative(getExcessLacking())).setHorizontalTextAlignment(HorizontalTextAlignment.RIGHT).setStyle(Utils.topBorderBuilder()),
                                    cmp.horizontalGap(70)
                            )
                    )
            )
                    .show(false);

        } catch (DRException ex) {
            Logger.getLogger(BankManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class SubreportExpression extends AbstractSimpleExpression<JasperReportBuilder> {

        private static final long serialVersionUID = 1L;

        @Override
        public JasperReportBuilder evaluate(ReportParameters reportParameters) {

            JasperReportBuilder report = report();
            String title = reportParameters.getValue("title");
            List<TextColumnBuilder<?>> columns = reportParameters.getValue("columns");
            JRDataSource data = reportParameters.getValue("data");

            SubtotalBuilders sbt = new SubtotalBuilders();
            
            for (int i = 0; i < columns.size(); i++) {
                report.addColumn(columns.get(i));
            }

            report.title(cmp.text(title).setStyle(normalText));
            report.setTemplate(Templates.reportTemplate);
            report.subtotalsAtSummary();
            return report;
        }
    }

    private class SubreportDataSourceExpression extends AbstractSimpleExpression<JRDataSource> {

        private static final long serialVersionUID = 1L;

        @Override
        public JRDataSource evaluate(ReportParameters reportParameters) {

            return reportParameters.getValue("data");

        }
    }

    public void setTfr(double tfr) {
        this.tfr = tfr;
    }

    public void setPrelim(double prelim) {
        this.prelim = prelim;
    }

    public void setOnDate(double onDate) {
        this.onDate = onDate;
    }

    public void seteDeficit(double eDeficit) {
        this.eDeficit = eDeficit;
    }

    public void seteLacking(double eLacking) {
        this.eLacking = eLacking;
    }

    public void setOthers(double others) {
        this.others = others;
    }

    public void setAdditionalFund(double additionalFund) {
        this.additionalFund = additionalFund;
    }

    public void setActual(double actual) {
        this.actual = actual;
    }
    
    public double getTotalFundingResource() {
        return this.prelim + this.onDate;
    }
    
    public double getExcessDeficit() {
        return getTotalFundingResource() - others;
    }
    
    public double getExcessLacking() {
        return getExcessDeficit() + additionalFund;
    }

}
