package com.example.demo.controllers;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.entities.User;
import com.example.demo.entities.Product;
import com.example.demo.loginCredentials.AdminLogin;
import com.example.demo.services.ProductServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Controller
@Tag(name = "Home Controller", description = "Public pages and navigation")
public class HomeController 
{
	@Autowired
	private ProductServices productServices;
	@GetMapping(value = {"/home", "/"})
	@Operation(summary = "Homepage", description = "Loads the main landing page of the application")
	public String home()
	{
		return "Home";
	}

	@GetMapping("/products")
	@Operation(summary = "Browse Products", description = "Displays the public food menu and dish categories")
	public String products( Model model)
	{ 
		List<Product> allProducts = this.productServices.getAllProducts();
		model.addAttribute("products", allProducts);
		return "Products";
	}

	@GetMapping("/location")
	@Operation(summary = "Locate Us", description = "Displays restaurant addresses and locations")
	public String location()
	{
		return "Locate_us";
	}

	@GetMapping("/about")
	@Operation(summary = "About Us", description = "Displays the company story and mission statement")
	public String about()
	{
		return "About";
	}

	@GetMapping("/login")
	@Operation(summary = "Login Page", description = "Serves the unified login for both admins and customers")
	public String login(Model model)
	{
		model.addAttribute("adminLogin",new AdminLogin());
		return "Login";
	}

	@GetMapping("/userLogin")
	@Operation(summary = "User Login Page", description = "Dedicated login screen for customers")
	public String userLogin(Model model)
	{
		return "UserLogin";
	}

	@GetMapping("/product/search")
	@Operation(summary = "Search Redirect", description = "Redirects to the dashboard if search is accessed directly via GET")
	public String searchRedirect(jakarta.servlet.http.HttpSession session)
	{
		if (session.getAttribute("loggedInUser") != null) {
			return "redirect:/dashboard";
		}
		return "redirect:/userLogin";
	}

	@GetMapping("/register")
	@Operation(summary = "Register Page", description = "Serves the customer registration form")
	public String register(Model model)
	{
		model.addAttribute("userRegistration", new User());
		return "register";
	}
}