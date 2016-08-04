package io.github.pivopil.rest.services;

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
public class RoleAsyncService {

    public static final String SELECT_ROLES = "select * from role";

    @Autowired
    private Db postgresAsyncConnectionPool;

    // https://github.com/alaisi/postgres-async-driver
    public Observable<Row> findAllRolesInDBAsRows() {
        return postgresAsyncConnectionPool.queryRows(SELECT_ROLES);
    }

    public Observable<ResultSet> findAllRolesInDBAsResultSet() {
        return postgresAsyncConnectionPool.querySet(SELECT_ROLES);
    }


//    public static void main(String... args) {
//        try {
//            findAllRolesInDBAsRows().map(row -> row.getString("name")).toList()
//                    .subscribe(System.out::println);
//        } catch (Exception e) {
//            Observable.error(e).subscribe(System.out::println);
//        } finally {
//            try {
//                postgresAsyncConnectionPool.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }

}
