package com.suken27.humanfactorsjava.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.suken27.humanfactorsjava.model.TeamManager;
import com.suken27.humanfactorsjava.repository.TeamManagerRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private TeamManagerRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("Username cannot be null nor empty.");
        }
        TeamManager user = repository.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("Email '" + username + "' not found in database.");
        }
        return new UserDetailsImpl(user.getEmail(), user.getPassword(), user.getRole().toString());
    }

}