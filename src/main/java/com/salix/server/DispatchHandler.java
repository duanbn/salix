package com.salix.server;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import com.salix.core.message.Message;
import com.salix.core.ser.Deserializer;
import com.salix.core.ser.MyDeserializer;
import com.salix.core.ser.MySerializer;
import com.salix.core.ser.Serializer;
import com.salix.server.processor.IProcessor;

public class DispatchHandler extends IoHandlerAdapter {

	private Serializer ser;
	private Deserializer deser;

	private ProcessorLoader pl;

	public DispatchHandler(ProcessorLoader pl) {
		this.ser = MySerializer.getInstance();
		this.deser = MyDeserializer.getInstance();
		this.pl = pl;
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {
		byte[] pkg = (byte[]) message;
		Message in = deser.deser(pkg, Message.class);

		IProcessor processor = pl.get(in.getClass());
		Message out = processor.process(in);

		pkg = ser.ser(out);

		IoBuffer buf = IoBuffer.allocate(4 + pkg.length);
		buf.putInt(pkg.length).put(pkg);
		buf.flip();
		session.write(buf);
	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
		System.out.println("IDLE " + session.getIdleCount(status));
	}
}
