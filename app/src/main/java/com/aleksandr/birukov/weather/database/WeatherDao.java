package com.aleksandr.birukov.weather.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import java.util.List;

@Dao
public interface WeatherDao {

    @Query("SELECT * FROM weatherdb")
    List<WeatherDB> getAll();

    @Query("SELECT * FROM weatherdb WHERE id = :id")
    WeatherDB getById(long id);

    @Insert
    void insert(WeatherDB weatherdb);

    @Update
    void update(WeatherDB weatherdb);

    @Delete
    void delete(WeatherDB weatherdb);

    @Query("DELETE FROM weatherdb")
    void nukeTable();
}
