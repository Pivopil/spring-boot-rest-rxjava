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
public class UserAsyncService {

    public static final String SELECT_FROM_USER_TABLE = "select * from user_table";

    @Autowired
    private Db postgresAsyncConnectionPool;


    public Observable<Row> findAllUsersInDBAsRows(){
        return postgresAsyncConnectionPool.queryRows(SELECT_FROM_USER_TABLE);
    }

    public Observable<ResultSet> findAllUsersInDBAsResultSet(){
        return postgresAsyncConnectionPool.querySet(SELECT_FROM_USER_TABLE);
    }
}
