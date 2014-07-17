package com.m1.android.data.http;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

/**
 * 
 * @author zhaozhongyang
 * 
 * @since 2012-5-24上午11:33:21
 */
public class EIMultipartEntity extends MultipartEntity {
	private FileTransferListener transferListener;

	public EIMultipartEntity(FileTransferListener fileTransferListener) {
		this.transferListener = fileTransferListener;
	}

	public EIMultipartEntity(HttpMultipartMode mode, String boundary, Charset charset, FileTransferListener fileTransferListener) {
		super(mode, boundary, charset);
		this.transferListener = fileTransferListener;
	}

	public EIMultipartEntity(HttpMultipartMode mode, FileTransferListener fileTransferListener) {
		super(mode);
		this.transferListener = fileTransferListener;
	}

	@Override
	public void writeTo(final OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.transferListener));
	}

	public static class CountingOutputStream extends FilterOutputStream {
		private final FileTransferListener listener;
		private long transferred;

		public CountingOutputStream(final OutputStream out, final FileTransferListener listener) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
			this.transferred += len;
			if (listener != null) {
				this.listener.transferred(this.transferred);
			}

		}

		@Override
		public void write(int b) throws IOException {
			out.write(b);
			this.transferred++;
			if (listener != null) {
				this.listener.transferred(this.transferred);
			}
		}

	}
}
