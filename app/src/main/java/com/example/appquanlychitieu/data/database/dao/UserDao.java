package com.example.appquanlychitieu.data.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.appquanlychitieu.data.model.User;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    // Lấy user theo email, việc kiểm tra password hash thực hiện ở tầng Java
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    User getUserByEmailForLogin(String email);

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(long id);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);
}
