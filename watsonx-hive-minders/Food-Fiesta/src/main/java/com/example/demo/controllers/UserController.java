package com.example.demo.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.entities.User;
import com.example.demo.services.UserServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "User Controller", description = "Endpoints for managing user accounts")
public class UserController
{
	@Autowired
	private UserServices services;

	@PostMapping("/addingUser")
	@Operation(summary = "Add a user", description = "Creates a new user profile manually from the admin panel")
	public String  addUser(@ModelAttribute User user)
	{
		System.out.println(user);
		this.services.addUser(user);
		return "redirect:/admin/services";
	}

	@PostMapping("/register")
	@Operation(summary = "Register as a customer", description = "Self-registration endpoint for new users")
	public String registerUser(@ModelAttribute("userRegistration") User user, Model model)
	{
		this.services.addUser(user);
		return "redirect:/login";
	}

	@GetMapping("/updatingUser/{id}")
	@Operation(summary = "Update a user", description = "Updates an existing customer's account info")
	public String updateUser(@ModelAttribute User user, @PathVariable("id") int id)
	{
		this.services.updateUser(user, id);
		return "redirect:/admin/services";
	}

	@GetMapping("/deleteUser/{id}")
	@Operation(summary = "Delete a user", description = "Removes a customer account by ID")
	public String deleteUser(@PathVariable("id" )int id)
	{
		this.services.deleteUser(id);
		return "redirect:/admin/services";
	}
	


}
