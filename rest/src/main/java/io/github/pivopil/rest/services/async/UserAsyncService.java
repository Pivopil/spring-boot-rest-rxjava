package io.github.pivopil.rest.services.async;

import io.github.pivopil.share.entities.impl.Role;
import io.github.pivopil.share.entities.impl.User;
import io.github.pivopil.share.persistence.async.RoleAsyncPersistence;
import io.github.pivopil.share.persistence.async.UserAsyncPersistence;
import io.github.pivopil.share.throwble.CustomError;
import io.github.pivopil.share.throwble.ExceptionAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import rx.Observable;
import rx.functions.Func2;

import java.util.HashSet;
import java.util.List;

/**
 * Created on 08.08.16.
 */
@Service
public class UserAsyncService {

    @Autowired
    private UserAsyncPersistence userAsyncPersistence;

    @Autowired
    private RoleAsyncPersistence roleAsyncPersistence;


    /*
        http://techblog.netflix.com/2013/02/rxjava-netflix-api.html
        https://stackoverflow.com/questions/22240406/rxjava-how-to-compose-multiple-observables-with-dependencies-and-collect-all-re
        https://stackoverflow.com/questions/21890338/when-should-one-use-rxjava-observable-and-when-simple-callback-on-android
        http://blog.danlew.net/2015/06/22/loading-data-from-multiple-sources-with-rxjava/
    */
    public Observable<User> findAllRx() {
        try {
            return userAsyncPersistence.findAllUsersInDBAsRows().map(userRow -> {
                User user = new User();
                user.setId(userRow.getLong("id"));
                user.setName(userRow.getString("name"));
                user.setLogin(userRow.getString("login"));

                Observable<List<Role>> listRoles = roleAsyncPersistence.findRolesByUserId(user.getId()).map(roleRow -> {
                    return new Role();
                }).toList();

                Observable<User> userObservable = Observable.just(user);

                return Observable.zip(userObservable, listRoles,
                        (Func2<User, List<Role>, User>) (user1, roleList) -> {
                            user1.setRoles(new HashSet<>(roleList));
                            return user1;
                        }
                );
            }).flatMap(x -> x);

        } catch (Exception e) {
            return Observable.error(new ExceptionAdapter("Error while getting users from database",
                    CustomError.DATABASE, HttpStatus.INTERNAL_SERVER_ERROR, e));
        }
    }


}
