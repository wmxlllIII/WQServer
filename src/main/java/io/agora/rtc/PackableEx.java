package io.agora.rtc;

public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
