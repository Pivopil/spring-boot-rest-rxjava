package io.github.pivopil.rest.services;

import io.github.pivopil.rest.viewmodels.UserView;
import io.github.pivopil.share.entities.impl.Role;
import io.github.pivopil.share.entities.impl.User;
import io.github.pivopil.share.persistence.RoleRepository;
import io.github.pivopil.share.persistence.UserRepository;
import io.github.pivopil.share.throwble.CustomError;
import io.github.pivopil.share.throwble.ExceptionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username);
        if (user == null) {
            throw new UsernameNotFoundException(String.format("User %s does not exist!", username));
        }
        return new UserRepositoryUserDetails(user);
    }

    public Observable<UserView> me(String username) {
        try {
            return Observable.just(userRepository.findByLogin(username)).map(toUserView);
        } catch (Exception e) {
            return Observable.error(new ExceptionAdapter("Error while getting user from database",
                    CustomError.DATABASE, HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    // http://techblog.netflix.com/2013/02/rxjava-netflix-api.html
    public Observable<Iterable<User>> findAllRx() {
        try {
            return ((ArrayList<User>) userRepository.findAll()).stream().map(user -> {

                Map<String, String> userData = new HashMap<>();

                Map<String, String> roleData = new HashMap<>();


                return Observable.zip(Observable.<Map<String, String>>just(userData), Observable.<Map<String, String>just(roleData),

                        (Func2<Map<String, String>, Map<String, String>, Map<String, String>>) (ud, rd) -> {
                            ud.putAll(rd);
                            return ud;
                        }
                );


            });

        } catch (Exception e) {
            return Observable.error(new ExceptionAdapter("Error while getting users from database",
                    CustomError.DATABASE, HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
//        return userRepository.findAll();
    }

    private final static class UserRepositoryUserDetails extends User implements UserDetails {

        private static final long serialVersionUID = 1L;

        private UserRepositoryUserDetails(User user) {
            super(user);
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return getRoles();
        }

        @Override
        public String getUsername() {
            return getLogin();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

    }

    public static rx.functions.Func1<User, UserView> toUserView = user -> {
        UserView userView = new UserView();
        userView.setId(user.getId());
        userView.setName(user.getName());
        userView.setLogin(user.getLogin());
        userView.setRoles(user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toList()));
        return userView;
    };

}
