package io.github.pivopil.rest.services;

import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import com.github.pgasync.Row;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserAsyncService userAsyncService;

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

    /*
        http://techblog.netflix.com/2013/02/rxjava-netflix-api.html
        https://stackoverflow.com/questions/22240406/rxjava-how-to-compose-multiple-observables-with-dependencies-and-collect-all-re
        https://stackoverflow.com/questions/21890338/when-should-one-use-rxjava-observable-and-when-simple-callback-on-android
        http://blog.danlew.net/2015/06/22/loading-data-from-multiple-sources-with-rxjava/
    */
    public Observable<List<User>> findAllRx() {
        try {
            return userAsyncService.findAllUsersInDBAsRows().map(userRow -> {
                User user = new User();
                user.setId(userRow.getLong("id"));
                user.setName(userRow.getString("name"));
                user.setLogin(userRow.getString("login"));
                Set<Role> roles = new HashSet<>();

                return new User();

//                return Observable.zip(Observable.<User>just(user), Observable.<Set<Role>>just(roles),
//                        (Func2<User, Set<Role>, User) (u, r) -> {
//                            return u.setRoles(r);
//                        }
//                );
            }).toList();

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
