package de.aramar.zoe.data.kamereon.charge;

import lombok.Getter;

@Getter
public enum ChargeCommandEnum {
    START("start"), STOP("cancel");

    private String command;

    private ChargeCommandEnum(String command) {
        this.command = command;
    }
}
