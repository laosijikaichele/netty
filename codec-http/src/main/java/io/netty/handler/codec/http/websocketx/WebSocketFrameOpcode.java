package io.netty.handler.codec.http.websocketx;

public enum WebSocketFrameOpcode {

    OPCODE_CONT((byte) 0x0),
    OPCODE_TEXT((byte) 0x1),
    OPCODE_BINARY((byte) 0x2),
    OPCODE_CLOSE((byte) 0x8),
    OPCODE_PING((byte) 0x9),
    OPCODE_PONG((byte) 0xA);

    final byte opcode;

    WebSocketFrameOpcode(byte opcode) {
        this.opcode = opcode;
    }
}
