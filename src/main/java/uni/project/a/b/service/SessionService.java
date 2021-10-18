package uni.project.a.b.service;

import uni.project.a.b.domain.AppSession;

import java.util.Optional;
import java.util.stream.Stream;

public interface SessionService {

    Optional<AppSession> getSession(Long id);

    Stream<AppSession> getByUsers(Long id);
    Optional<AppSession> getByUsers(Long id1, Long id2);

    // sure about id?
    void establishSession(Long id1, Long id2);

    //TODO: messsages handler???





}
