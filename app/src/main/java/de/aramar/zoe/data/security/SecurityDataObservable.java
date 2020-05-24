package de.aramar.zoe.data.security;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.BehaviorSubject;

/**
 * Implementation of an Observable for the SecurityData object.
 */
public class SecurityDataObservable {
    private final BehaviorSubject<SecurityData> securityDataBehaviorSubject;

    private static SecurityDataObservable INSTANCE;

    public static Observable<SecurityData> getObservable() {
        if (INSTANCE == null) {
            INSTANCE = new SecurityDataObservable();
        }
        return INSTANCE.securityDataBehaviorSubject.hide();
    }

    public static void publish(SecurityData newSecurityData) {
        if (INSTANCE == null) {
            INSTANCE = new SecurityDataObservable();
        }
        INSTANCE.securityDataBehaviorSubject.onNext(newSecurityData);
    }

    private SecurityDataObservable() {
        this.securityDataBehaviorSubject = BehaviorSubject.createDefault(new SecurityData());
    }
}
