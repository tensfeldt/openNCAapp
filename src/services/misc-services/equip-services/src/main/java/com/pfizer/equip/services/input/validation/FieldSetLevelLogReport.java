package com.pfizer.equip.services.input.validation;

import com.pfizer.equip.services.input.validation.types.ValidationStatusTypes;

public class FieldSetLevelLogReport extends AbstractLogLevelReport {
   private String columnValue;
   private int rowIndex;

   public FieldSetLevelLogReport(int rowIndex, String columnName, String columnValue, String message, ValidationStatusTypes logLevel) {
      this.setRowIndex(rowIndex);
      this.setName(columnName);
      this.setColumnValue(columnValue);
      this.setMessage(message);
      this.setLogLevel(logLevel);
   }

   public FieldSetLevelLogReport() {

   }

   public String getColumnValue() {
      return columnValue;
   }

   public void setColumnValue(String columnValue) {
      this.columnValue = columnValue;
   }

   public int getRowIndex() {
      return rowIndex;
   }

   public void setRowIndex(int rowIndex) {
      this.rowIndex = rowIndex;
   }
}
