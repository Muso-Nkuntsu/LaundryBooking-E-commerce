/**
 * ILaundryService.java
 * Author: Snalo (230541844)
 * Date: 25 July 2026
 */
package com.cput.laundryecommercebookingsystem.service;

import com.cput.laundryecommercebookingsystem.domain.LaundryService;
import java.util.List;

public interface ILaundryService extends IService<LaundryService, String> {
    List<LaundryService> getServicesByName(String name);
}

