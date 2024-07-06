package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;

import java.util.List;

public interface AddressBookService {

    /**
     * 新增分类
     * @param AddressBook
     */
    void save(AddressBook AddressBook);

    List<AddressBook> list(AddressBook addressBook);

    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    void setDefault(AddressBook addressBook);

    void deleteById(Long id);
}
