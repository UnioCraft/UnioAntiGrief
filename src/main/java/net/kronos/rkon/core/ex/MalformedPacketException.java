package net.kronos.rkon.core.ex;

import java.io.IOException;

@SuppressWarnings("serial")
public class MalformedPacketException extends IOException {
	
	public MalformedPacketException(String message) {
		super(message);
	}
	
}
