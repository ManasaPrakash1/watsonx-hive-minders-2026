package com.example.demo.controllers;

import com.example.demo.entities.User;
import com.example.demo.services.UserServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OAuth2Controller {

    @Autowired
    private UserServices userServices;

    @GetMapping("/oauth2/success")
    public String oauth2Success(@AuthenticationPrincipal OAuth2User principal, HttpSession session) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getAttribute("email");
        String name = principal.getAttribute("name");

        User user = userServices.getUserByEmail(email);

        if (user == null) {
            // New user from Google, create a local account
            user = new User();
            user.setUemail(email);
            user.setUname(name);
            user.setUpassword("OAUTH_USER"); // Placeholder password
            user.setUnumber(0L); // Placeholder
            userServices.addUser(user);
            System.out.println("✅ New Google user registered: " + email);
        }

        // Put user in session so existing dashboard logic works
        session.setAttribute("loggedInUser", user);
        
        return "redirect:/dashboard";
    }
}
