package de.aramar.zoe.data.kamereon.hvac;

import lombok.Getter;

@Getter
public enum HvacCommandEnum {
    START("start"), STOP("cancel");

    private String command;

    private HvacCommandEnum(String command) {
        this.command = command;
    }
}
