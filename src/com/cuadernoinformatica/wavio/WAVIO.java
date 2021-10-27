package com.cuadernoinformatica.wavio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.BitSet;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

public class WavIO {

	protected static int FREQUENCY = 44100;
	protected static int SAMPLE_SIZE = 8;
	protected static int CHANNELS = 1;
	
	protected static String FILE2WAV = "file2wav";
	protected static String WAV2FILE = "wav2file";
		
	protected ByteArrayOutputStream wavOutput;
	protected long length = 0;
	protected int bitIndex = 0;
	
	public void file2Wav(String file, String wav) throws Exception {
		wavOutput = new ByteArrayOutputStream();
		
		try (FileInputStream fileStream = new FileInputStream(new File(file))) {
			int fileByte;
			while ((fileByte = fileStream.read()) != -1) {
				writeByte(fileByte);
			}
		}
		
		for (int x = 0; x < FREQUENCY; x++) {
			wavOutput.write(127);
			length++;
		}
		
		wavOutput.flush();
		ByteArrayInputStream input = new ByteArrayInputStream(wavOutput.toByteArray());
		AudioFormat format = new AudioFormat(FREQUENCY, SAMPLE_SIZE, CHANNELS, false, true);
		AudioInputStream audio = new AudioInputStream(input, format, length);
		AudioSystem.write(audio, AudioFileFormat.Type.WAVE, new File(wav));
	}	
	
	public void wav2File(String wav, String file) throws Exception {
		AudioInputStream audio = AudioSystem.getAudioInputStream(new File(wav));
		FileOutputStream output = new FileOutputStream(file);
		BitSet bits = new BitSet();
																
		int wavByte;	
		int samples = 0;
		int position = 0;
		
		while (true) {
					
			wavByte = audio.read();
			
			if (position == 0) {
				
				if (wavByte > 200) {
					
					position = 1;
					samples++;
				}
				
			} else {
				
				if (position == 1) {
					
					if (wavByte > 200) {
						
						samples++;
						
					} else if (wavByte < 56) {
						
						position = -1;
						samples++;
					}
					
				} else {
					
					if (wavByte > 200 || wavByte == -1) {
						
						if (samples > 30) {
							
							bits.set(bitIndex, false);
							bitIndex++;
							
						} else {
							
							bits.set(bitIndex, true);
							bitIndex++;
						}
						
						position = 1;
						samples = 1;
						
					} else if (wavByte < 56) {
						
						samples++;
					}
				}
			}
			
			if (wavByte == -1) {
				break;
			}
		}
		
		byte[] bytes = bits.toByteArray();
		int trailingZeros = bitIndex/8 - bytes.length;
		output.write(bytes);
		output.write(new byte[trailingZeros]);
		output.flush();
		output.close();
	}
	
	public void writeByte(int input) throws Exception {
		for (int x = 0; x <= 7; x++) {
			writeBit((input >> x) & 1);
		}
	}
	
	public void writeBit(int input) throws Exception {
		int samples;
		
		if (input == 0) {
			samples = 20;
		} else if(input == 1) {
			samples = 10;
		} else {
			throw new Exception("Invalid bit value: " + input);
		}
		
		for (int x = 0; x < samples; x++) {
			wavOutput.write(228);
		}
		
		for (int x = 0; x < samples; x++) {
			wavOutput.write(28);
		}
		
		length += samples * 2;
		
		bitIndex++;
	}
	
	public static void main(String[] args) throws Exception {
		WavIO file2Wav = new WavIO();
		
		String action = args[0];
		String source = args[1];
		String target = args[2];
				
		if (FILE2WAV.equals(action)) {
			file2Wav.file2Wav(source, target);
		} else if(WAV2FILE.equals(action)) {
			file2Wav.wav2File(source, target);
		} else {
			System.out.println("Invalid action: " + action);
		}
	}
}