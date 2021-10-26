package uni.project.a.b.service;

import uni.project.a.b.domain.AppSession;

import java.util.Optional;
import java.util.stream.Stream;

public interface SessionService {

    Optional<AppSession> getSession(Long id);

    Stream<AppSession> getByUsers(String username);
    Optional<AppSession> getByUsers(String user1, String user2);


    void establishSession(String user1, String user2);

    //TODO: messsages handler, delete conversation (using a bean??)





}
