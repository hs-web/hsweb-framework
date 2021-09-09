package org.hswebframework.web.crud.events;

public enum EntityEventPhase {
    prepare,
    before,
    after;

    public static EntityEventPhase[] all = EntityEventPhase.values();
}