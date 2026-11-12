package com.estcomputer.patient.domain.session

/** Taken by [SessionCoordinator.startUnlock] before an unlock, so a lock that arrives meanwhile is noticed. */
public class UnlockTicket internal constructor(internal val lockCount: Long) {
    override fun toString(): String = "UnlockTicket"
}
