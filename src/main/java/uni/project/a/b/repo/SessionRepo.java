package uni.project.a.b.repo;

import uni.project.a.b.domain.AppMessage;
import uni.project.a.b.domain.AppSession;

import java.util.*;
import java.util.stream.Stream;

// if we need to save all in the db we need to extend JpaRepo
public class SessionRepo {

    //TODO: HIGHLY INEFFICIENT SOLUTION, find something better in functional java or replace by simple for loops
    private List<AppSession> sessions = new ArrayList<>();
    private Stream<AppSession> sStream = sessions.stream();


    private Long counter = 0L;

    public Optional<AppSession> findById (Long id){
        return sStream.filter(x -> x.getId() == id).findFirst();
    }

    public Optional<AppSession> findByUsers(Long id1, Long id2){
        return sStream.filter(x ->
                        (Objects.equals(x.getAliceId(), id1) || Objects.equals(x.getAliceId(), id2))
                        && Objects.equals(x.getBobId(), id1) || Objects.equals(x.getBobId(), id2))
                        .findFirst();
    }

    public Stream<AppSession> findByUsers(Long id){
        return sStream.filter(x ->
                        (Objects.equals(x.getAliceId(), id) || Objects.equals(x.getBobId(), id)));
    }


    //TODO: Setting all the keys!
    public void save(Long id1, Long id2){
        if (findByUsers(id1, id2).isEmpty()){
            counter += 1;
            AppSession sess = new AppSession(counter, id1, id2);
            sessions.add(sess);
            sStream = sessions.stream();
        }
    }

    public void delete(long id){
        Optional<AppSession> sess = findById(id);
        if (sess.isPresent()){
            counter -= 1;
            sessions.remove(sess);
            sStream = sessions.stream();
        }
    }
}
