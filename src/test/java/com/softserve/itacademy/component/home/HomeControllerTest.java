package com.softserve.itacademy.component.home;

import com.softserve.itacademy.model.UserRole;
import com.softserve.itacademy.config.WithMockCustomUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@EnableMethodSecurity
public class HomeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    @WithMockCustomUser(email = "mike@mail.com", role = UserRole.ADMIN)
    public void homeShouldWorkForAdmin() throws Exception {
        mvc.perform(get("/")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(forwardedUrl(null));
    }

    @Test
    public void homeShouldWorkForAnonymousUser() throws Exception {
        mvc.perform(get("/").contentType(MediaType.TEXT_HTML))
                .andExpect(status().isOk());
    }


}
