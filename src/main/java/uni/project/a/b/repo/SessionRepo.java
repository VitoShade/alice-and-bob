package uni.project.a.b.repo;

import org.springframework.stereotype.Repository;
import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

// if we need to save all in the db we need to extend JpaRepo
@Repository
public class SessionRepo {

    //TODO: HIGHLY INEFFICIENT SOLUTION, find something better in functional java or replace by simple for loops
    private final List<AppSession> sessions = new ArrayList<>();

    private final AtomicLong counter = new AtomicLong(0);

    public Optional<AppSession> findById (Long id){
        return sessions.stream().filter(x -> x.getId() == id).findFirst();
    }


    public Optional<AppSession> findByUsers(String user1, String user2){
        return sessions.stream().filter(x ->
                        (Objects.equals(x.getAliceUser(), user1) || Objects.equals(x.getAliceUser(), user2))
                                && Objects.equals(x.getBobUser(), user1) || Objects.equals(x.getBobUser(), user2))
                .findFirst();
    }

    public Stream<AppSession> findByUsers(String username){
        return sessions.stream().filter(x ->
                (Objects.equals(x.getAliceUser(), username) || Objects.equals(x.getBobUser(), username)));
    }




    //TODO: Setting all the keys!

    public void save(String user1, String user2){
        if (findByUsers(user1, user2).isEmpty()){
            AppSession sess = new AppSession(counter.incrementAndGet(), user1, user2);
            sessions.add(sess);
        }
    }

    public void delete(long id){
        Optional<AppSession> sess = findById(id);
        if (sess.isPresent()){
            counter.decrementAndGet();
            sessions.remove(sess.get());
        }
    }



}
