package com.example.demo.repositories;


import org.springframework.data.repository.CrudRepository;

import java.util.List;
import com.example.demo.entities.Product;

public interface ProductRepository extends CrudRepository<Product,Integer>
{
	public List<Product> findByPnameContainingIgnoreCase(String name);

}