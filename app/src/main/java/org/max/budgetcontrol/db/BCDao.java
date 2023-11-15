package org.max.budgetcontrol.db;

import org.max.budgetcontrol.zentypes.Category;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Query;

@Dao
interface BCDao
{
  @Query( "select * from category")
  List<Category> getAllCategories();


}
