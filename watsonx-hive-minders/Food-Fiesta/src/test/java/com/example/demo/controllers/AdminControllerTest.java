package com.example.demo.controllers;

import com.example.demo.entities.Admin;
import com.example.demo.entities.Orders;
import com.example.demo.entities.Product;
import com.example.demo.entities.User;
import com.example.demo.services.AdminServices;
import com.example.demo.services.OrderServices;
import com.example.demo.services.ProductServices;
import com.example.demo.services.UserServices;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserServices userServices;
    @MockBean private AdminServices adminServices;
    @MockBean private ProductServices productServices;
    @MockBean private OrderServices orderServices;

    private User alice;
    private Admin admin;
    private Product biryani;

    @BeforeEach
    void setUp() {
        alice = new User();
        alice.setU_id(1);
        alice.setUname("Alice");
        alice.setUemail("alice@example.com");
        alice.setUpassword("secret");
        alice.setUnumber(123456789L);

        admin = new Admin();
        admin.setAdminId(1);
        admin.setAdminName("Super Admin");
        admin.setAdminEmail("admin@foodfiesta.com");
        admin.setAdminPassword("admin123");

        biryani = new Product();
        biryani.setPid(1);
        biryani.setPname("Hyderabadi Chicken Biryani");
        biryani.setPprice(350.0);
        biryani.setPdescription("Authentic biryani");
    }

    // ── POST /adminLogin ─────────────────────────────────────────────────

    @Test
    void adminLogin_validCredentials_setsSessionAndRedirects() throws Exception {
        when(adminServices.validateAdminCredentials("admin@foodfiesta.com", "admin123"))
                .thenReturn(true);

        mockMvc.perform(post("/adminLogin")
                        .param("email", "admin@foodfiesta.com")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"))
                .andExpect(request().sessionAttribute("loggedInAdmin", "admin@foodfiesta.com"));
    }

    @Test
    void adminLogin_invalidCredentials_returnsLoginViewWithError() throws Exception {
        when(adminServices.validateAdminCredentials("admin@foodfiesta.com", "badpass"))
                .thenReturn(false);

        mockMvc.perform(post("/adminLogin")
                        .param("email", "admin@foodfiesta.com")
                        .param("password", "badpass"))
                .andExpect(status().isOk())
                .andExpect(view().name("Login"))
                .andExpect(model().attributeExists("error"));
    }

    @Test
    void adminLogin_unknownEmail_returnsLoginViewWithError() throws Exception {
        when(adminServices.validateAdminCredentials("nobody@foodfiesta.com", "admin123"))
                .thenReturn(false);

        mockMvc.perform(post("/adminLogin")
                        .param("email", "nobody@foodfiesta.com")
                        .param("password", "admin123"))
                .andExpect(status().isOk())
                .andExpect(view().name("Login"))
                .andExpect(model().attributeExists("error"));
    }

    // ── POST /userLogin ──────────────────────────────────────────────────

    @Test
    void userLogin_validCredentials_setsSessionAndRedirects() throws Exception {
        when(userServices.validateLoginCredentials("alice@example.com", "secret"))
                .thenReturn(true);
        when(userServices.getUserByEmail("alice@example.com")).thenReturn(alice);

        mockMvc.perform(post("/userLogin")
                        .param("userEmail", "alice@example.com")
                        .param("userPassword", "secret"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"))
                .andExpect(request().sessionAttribute("loggedInUser", alice));
    }

    @Test
    void userLogin_invalidCredentials_returnsLoginViewWithError() throws Exception {
        when(userServices.validateLoginCredentials("alice@example.com", "wrong"))
                .thenReturn(false);

        mockMvc.perform(post("/userLogin")
                        .param("userEmail", "alice@example.com")
                        .param("userPassword", "wrong"))
                .andExpect(status().isOk())
                .andExpect(view().name("Login"))
                .andExpect(model().attributeExists("error2"));
    }

    // ── GET /dashboard ───────────────────────────────────────────────────

    @Test
    void dashboard_validSession_returnsBuyProductView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        when(orderServices.getOrdersForUser(alice)).thenReturn(List.of());

        mockMvc.perform(get("/dashboard").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("BuyProduct"))
                .andExpect(model().attribute("name", "Alice"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void dashboard_noSession_redirectsToUserLogin() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userLogin"));
    }

    // ── GET /logout ──────────────────────────────────────────────────────

    @Test
    void logout_invalidatesSessionAndRedirectsHome() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        mockMvc.perform(get("/logout").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    // ── GET /admin/services ──────────────────────────────────────────────

    @Test
    void adminServices_validAdminSession_returnsAdminPage() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");

        when(userServices.getAllUser()).thenReturn(List.of(alice));
        when(adminServices.getAll()).thenReturn(List.of(admin));
        when(productServices.getAllProducts()).thenReturn(List.of(biryani));
        when(orderServices.getOrders()).thenReturn(List.of());

        mockMvc.perform(get("/admin/services").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Admin_Page"))
                .andExpect(model().attributeExists("users", "admins", "products", "orders"));
    }

    @Test
    void adminServices_noAdminSession_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/services"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ── POST /product/search ─────────────────────────────────────────────

    @Test
    void productSearch_productFound_addsProductToModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        when(productServices.getProductByName("biryani")).thenReturn(biryani);
        when(orderServices.getOrdersForUser(alice)).thenReturn(List.of());

        mockMvc.perform(post("/product/search")
                        .param("productName", "biryani")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("BuyProduct"))
                .andExpect(model().attribute("product", biryani));
    }

    @Test
    void productSearch_productNotFound_addsMessageToModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        when(productServices.getProductByName("pizza")).thenReturn(null);
        when(orderServices.getOrdersForUser(alice)).thenReturn(List.of());

        mockMvc.perform(post("/product/search")
                        .param("productName", "pizza")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("BuyProduct"))
                .andExpect(model().attributeExists("message"))
                .andExpect(model().attribute("product", nullValue()));
    }

    @Test
    void productSearch_noSession_redirectsToUserLogin() throws Exception {
        mockMvc.perform(post("/product/search")
                        .param("productName", "biryani"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userLogin"));
    }

    // ── POST /product/order ──────────────────────────────────────────────

    @Test
    void productOrder_validSession_calculatesAmountAndReturnsSuccessView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        doNothing().when(orderServices).saveOrder(any(Orders.class));

        mockMvc.perform(post("/product/order")
                        .param("oName", "Hyderabadi Chicken Biryani")
                        .param("oPrice", "350.0")
                        .param("oQuantity", "2")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Order_success"))
                .andExpect(model().attribute("amount", 700.0));

        verify(orderServices, times(1)).saveOrder(any(Orders.class));
    }

    @Test
    void productOrder_quantity1_amountEqualsPriceExact() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        doNothing().when(orderServices).saveOrder(any(Orders.class));

        mockMvc.perform(post("/product/order")
                        .param("oName", "Paneer Butter Masala")
                        .param("oPrice", "280.0")
                        .param("oQuantity", "1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Order_success"))
                .andExpect(model().attribute("amount", 280.0));
    }

    @Test
    void productOrder_noSession_redirectsToUserLogin() throws Exception {
        mockMvc.perform(post("/product/order")
                        .param("oName", "Biryani")
                        .param("oPrice", "350.0")
                        .param("oQuantity", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/userLogin"));
    }

    // ── GET /product/back ────────────────────────────────────────────────

    @Test
    void productBack_redirectsToDashboard() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInUser", alice);

        mockMvc.perform(get("/product/back").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    // ── GET /addAdmin, /addProduct, /addUser ─────────────────────────────

    @Test
    void addAdminPage_returnsAddAdminView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");

        mockMvc.perform(get("/addAdmin").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Add_Admin"));
    }

    @Test
    void addProductPage_returnsAddProductView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");

        mockMvc.perform(get("/addProduct").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Add_Product"));
    }

    @Test
    void addUserPage_returnsAddUserView() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");

        mockMvc.perform(get("/addUser").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Add_User"));
    }

    // ── GET /updateAdmin/{id}, /updateProduct/{id}, /updateUser/{id} ─────

    @Test
    void updateAdminPage_loadsAdminIntoModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        when(adminServices.getAdmin(1)).thenReturn(admin);

        mockMvc.perform(get("/updateAdmin/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Update_Admin"))
                .andExpect(model().attribute("admin", admin));
    }

    @Test
    void updateProductPage_loadsProductIntoModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        when(productServices.getProduct(1)).thenReturn(biryani);

        mockMvc.perform(get("/updateProduct/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Update_Product"))
                .andExpect(model().attribute("product", biryani));
    }

    @Test
    void updateUserPage_loadsUserIntoModel() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        when(userServices.getUser(1)).thenReturn(alice);

        mockMvc.perform(get("/updateUser/1").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("Update_User"))
                .andExpect(model().attribute("user", alice));
    }

    // ── POST /addingAdmin, GET /updatingAdmin, GET /deleteAdmin ──────────

    @Test
    void addingAdmin_savesAdminAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        doNothing().when(adminServices).addAdmin(any(Admin.class));

        mockMvc.perform(post("/addingAdmin")
                        .param("adminName", "New Admin")
                        .param("adminEmail", "new@foodfiesta.com")
                        .param("adminPassword", "pass123")
                        .param("adminNumber", "0600000000")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(adminServices, times(1)).addAdmin(any(Admin.class));
    }

    @Test
    void updatingAdmin_updatesAdminAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        doNothing().when(adminServices).update(any(Admin.class), eq(1));

        mockMvc.perform(get("/updatingAdmin/1")
                        .param("adminName", "Updated Admin")
                        .param("adminEmail", "admin@foodfiesta.com")
                        .param("adminPassword", "newpass")
                        .param("adminNumber", "0600000000")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(adminServices, times(1)).update(any(Admin.class), eq(1));
    }

    @Test
    void deleteAdmin_deletesAdminAndRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("loggedInAdmin", "admin@foodfiesta.com");
        doNothing().when(adminServices).delete(1);

        mockMvc.perform(get("/deleteAdmin/1").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/services"));

        verify(adminServices, times(1)).delete(1);
    }
}
