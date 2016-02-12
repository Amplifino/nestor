package com.amplifino.nestor.dot.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.osgi.service.component.annotations.Component;

import com.amplifino.nestor.dot.DotService;

@Component(name="com.amplifino.nestor.dot")
public class DotServiceImpl implements DotService {

	@Override
	public byte[] toSvg(String source) {
		return executeProcess("dot", "-Tsvg", source);
	}

	@Override
	public byte[] toPng(String source) {
		return executeProcess("dot", "-Tpng", source);
	}

	@Override
	public String tred(String source) {
		return new String(executeProcess("tred","",source));
	}

	private byte[] executeProcess(String programName, String parameters, String input) {
        ProcessBuilder builder = new ProcessBuilder(programName, parameters);
        try {
            Process process = builder.start();
            try (OutputStream stdIn = process.getOutputStream()) {
                stdIn.write(input.getBytes());
            }
            try (InputStream stdOut = process.getInputStream()) {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                byte[] data = new byte[1 << 14];  //16K buffer
                int nRead;
                while ((nRead = stdOut.read(data)) != -1) {
                    buffer.write(data, 0, nRead);
                }
                process.waitFor();
                return buffer.toByteArray();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
