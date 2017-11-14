package com.edu.dao;

import com.edu.domain.Customer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CustomerRepository extends PagingAndSortingRepository<Customer, Long> {
    Page<Customer> findAllOrderByName(Pageable pageable);

    Customer findOneByOpenCode(String openCode);

    //TODO: Fix problem below
    //List<Customer> findCustomersByIsActivated();

    @Query("select count(c)>0 from Customer c where c.openCode = ?1")
    boolean isCustomerAlreadyRegistered(String openCode);
}
