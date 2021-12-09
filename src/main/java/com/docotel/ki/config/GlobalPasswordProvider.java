package com.docotel.ki.config;

import com.docotel.ki.model.master.MLookup;
import com.docotel.ki.service.master.LookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class GlobalPasswordProvider implements AuthenticationProvider {
    @Autowired
    private LookupService lookupService;

    @Autowired
    private UserDetailsService userDetailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();

        MLookup lookup = lookupService.findByCodeGroups("PasswordGlobal", "PasswordGlobal");
        String globalPassword = null;
        if(lookup != null) {
            globalPassword = lookup.getName();
        }

        if(globalPassword != null && globalPassword.length() > 0 && passwordEncoder.matches(password, globalPassword)) {
            UserDetails user = userDetailService.loadUserByUsername(username);
            if(user != null) {
                return new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
            }
        }
        throw new BadCredentialsException("External system authentication failed");
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return aClass.equals(UsernamePasswordAuthenticationToken.class);
    }
}
