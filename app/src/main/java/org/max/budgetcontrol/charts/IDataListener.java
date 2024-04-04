package org.max.budgetcontrol.charts;

import org.max.budgetcontrol.zentypes.Category;
import org.max.budgetcontrol.zentypes.Transaction;

import java.util.List;

public interface IDataListener
{
  void onCategoriesReceived(List<Category> categories);
  void onTransactionsReceived(List<Transaction> transactions);
}
