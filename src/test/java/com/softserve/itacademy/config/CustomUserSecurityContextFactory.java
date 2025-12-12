package com.softserve.itacademy.config;

import com.softserve.itacademy.service.UserService;
import com.softserve.itacademy.model.User;
import com.softserve.itacademy.config.security.WebAuthenticationToken;
import org.mockito.MockingDetails;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    
    @Autowired(required = false) UserService userService;
    
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        User user = new User();
        user.setId(customUser.id());
        user.setFirstName(customUser.firstName());
        user.setLastName(customUser.lastName());
        user.setEmail(customUser.email());
        user.setPassword(customUser.password());
        user.setRole(customUser.role());
        
        if (userService != null) {
            MockingDetails mockingDetails = Mockito.mockingDetails(userService);
            if (mockingDetails.isMock()) {
                Mockito.when(userService.getCurrentUser()).thenReturn(user);
            }
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        WebAuthenticationToken token = new WebAuthenticationToken(user);

        token.setAuthenticated(true);
        context.setAuthentication(token);

        return context;
    }
}
