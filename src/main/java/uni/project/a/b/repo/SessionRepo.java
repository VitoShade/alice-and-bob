package uni.project.a.b.repo;


import org.springframework.stereotype.Repository;
import uni.project.a.b.domain.AppSession;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

@Repository
public class SessionRepo {

    private final List<AppSession> sessions = new CopyOnWriteArrayList<>();

    private final AtomicLong counter = new AtomicLong(0);

    public List<AppSession> getAll(){
        return sessions;
    }

    public Optional<AppSession> findById (Long id){
        return sessions.stream().filter(x -> Objects.equals(x.getId(), id)).findFirst();
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



    public AppSession create(String user1, String user2){
        Optional<AppSession> sess = findByUsers(user1, user2);
        if (sess.isEmpty()){
            AppSession session = new AppSession(counter.incrementAndGet(), user1, user2);
            sessions.add(session);
            return session;
        } else {
            return sess.get();
        }
    }

    public void update(AppSession newSession){
        Optional<AppSession> oldSession = findById(newSession.getId());
        if (oldSession.isPresent()){
            delete(oldSession.get().getId());
            counter.incrementAndGet();
            sessions.add(newSession);
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
