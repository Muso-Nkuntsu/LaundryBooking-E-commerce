package com.cput.laundryecommercebookingsystem.service;


/*
 * IService.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */

import java.util.List;

public interface IService<T, ID> {
    T create(T t);
    T read(ID id);
    T update(T t);
    boolean delete(ID id);
    List<T> getAll();
}

