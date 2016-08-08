package io.github.pivopil.share.persistence.async;

import com.github.pgasync.Db;
import com.github.pgasync.ResultSet;
import com.github.pgasync.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rx.Observable;

/**
 * Created on 29.07.16.
 */
@Service
public class RoleAsyncPersistence {

    public static final String SELECT_ROLES = "select * from role";

    @Autowired
    private Db postgresAsyncConnectionPool;

    // https://github.com/alaisi/postgres-async-driver
    public Observable<Row> findAllRolesInDBAsRows() {
        return postgresAsyncConnectionPool.queryRows(SELECT_ROLES);
    }

    public Observable<Row> findRolesByUserId(Long userId) {
        return postgresAsyncConnectionPool.queryRows("select * from user_role userToRole inner join role r on userToRole.role_id = r.id where userToRole.user_id = $1", userId);
    }

    public Observable<ResultSet> findAllRolesInDBAsResultSet() {
        return postgresAsyncConnectionPool.querySet(SELECT_ROLES);
    }
}
