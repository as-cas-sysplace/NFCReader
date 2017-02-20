

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
import javax.xml.bind.DatatypeConverter;

public class NFCReader {
		
	private final CardTerminal acrTerminal;
	private CardChannel channel;
	
	
	public NFCReader() throws NullPointerException {
		final TerminalFactory factory = TerminalFactory.getDefault();
		acrTerminal = factory.terminals().getTerminal("ACS ACR122 0");
	}
	
		
	public CardTerminal getTerminal() {
		return acrTerminal;
	}
	
	
	public CardChannel getChannel() {
		return channel;
	}
	
	
	public boolean existsTerminal() {
		return acrTerminal != null;
	}
	
	
	public void listen() throws CardException {
		if (acrTerminal.waitForCardPresent(0)) {
				establishConnection();
		}		
	}
	
	
	public void establishConnection() throws CardException {		
		final Card card = acrTerminal.connect("*");
		channel = card.getBasicChannel();
	}
	
	
	public String[] sendByteCommand(final byte[] cmd)
			throws CardException, IllegalArgumentException {
		final String[] response = new String[2];
		response[0] = "0";
		final CommandAPDU cmdAPDU = new CommandAPDU(cmd);
		final ResponseAPDU respAPDU = channel.transmit(cmdAPDU);
		if (respAPDU.getSW() == 36864) {
			response[0] = "1";
			response[1] = DatatypeConverter.printHexBinary(respAPDU.getData());
		}
		return response;
	}
	
	
	public String getATR() {
		return DatatypeConverter.printHexBinary(channel.getCard().getATR().getBytes());
	}
	
	
	public String getUID() throws IllegalArgumentException, CardException { 
		final byte[] uid = new byte[] {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};
		final String[] response = sendByteCommand(uid);
		return response[1];
	}
	
	
	public boolean loadAuthenticationKey(final String key, final boolean keyNumber)
			throws IllegalArgumentException, CardException {
		if (isValidKey(key)) {
			byte keyNrByte;

			if (keyNumber) {
				keyNrByte = (byte) 0x00;
			} else {
				keyNrByte = (byte) 0x01;
			}		
			
			final byte[] keyBytes = hexStringToBytes(key);
			final byte[] cmdBytes = new byte[] {(byte) 0xFF, (byte) 0x82, (byte) 0x00};
			final byte[] loadKey = ByteBuffer.allocate(cmdBytes.length + keyBytes.length + 2)
					.put(cmdBytes).put(keyNrByte).put((byte) 0x06).put(keyBytes).array();
			final String[] response = sendByteCommand(loadKey);
			if (response[0].equals("1")) {
				return true;
			}
		}
		return false;
	}

	
	public boolean authenticate(final int blockNumber, final boolean keyType, final boolean keyNumber)
			throws IllegalArgumentException, CardException {
		final byte[] cmdAuthenticate = new byte[] {(byte) 0xFF, (byte) 0x86, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x01, (byte) 0x00};
		byte blockNrByte;
		byte keyTypeByte;
		byte keyNrByte;				
		
		if (isValidBlockNumber(blockNumber)) {
			blockNrByte = (byte) blockNumber;
		
			if (keyType) {
				keyTypeByte = (byte) 0x60;
			} else {
				keyTypeByte = (byte) 0x61;
			}
			
			if (keyNumber) {
				keyNrByte = (byte) 0x00;
			} else {
				keyNrByte = (byte) 0x01;
			}		
			
			final byte[] cmd = ByteBuffer.allocate(cmdAuthenticate.length + 3)
					.put(cmdAuthenticate).put(blockNrByte).put(keyTypeByte).put(keyNrByte).array();
			final String[] response = sendByteCommand(cmd);
			if (response[0].equals("1")) {
				return true;
			}
		}
		return false;
	}
	
	
	public String readBlock(final int blockNumber, final int NumberBytesToRead)
			throws IllegalArgumentException, CardException {
		byte blockNrByte;

		if (isValidBlockNumber(blockNumber) && NumberBytesToRead >= 0 && NumberBytesToRead <= 16) {
			blockNrByte = (byte) blockNumber;
			final byte[] cmdRead = new byte[] {(byte) 0xFF, (byte) 0xB0, (byte) 0x00};
			final byte[] cmd = ByteBuffer.allocate(cmdRead.length + 2)
					.put(cmdRead).put(blockNrByte).put((byte) NumberBytesToRead).array();
			final String[] response = sendByteCommand(cmd);
			return response[1];
		}
		return "";
	}
	

	public boolean writeBlock(final int blockNumber, final String data)
			throws IllegalArgumentException, CardException {
		byte blockNrByte;
		
		if (isValidBlockNumber(blockNumber)) {
			blockNrByte = (byte) blockNumber;
			if (isValidData(data)) {
				final byte[] dataBytes = hexStringToBytes(data);
				final byte[] cmdWrite = new byte[] {(byte) 0xFF, (byte) 0xD6, (byte) 0x00};
				final byte[] cmd = ByteBuffer.allocate(cmdWrite.length + 18)
						.put(cmdWrite).put(blockNrByte).put((byte) 0x10).put(dataBytes).array();
				final String[] response = sendByteCommand(cmd);
				
				if (response[0].equals("1")) {
					return true;
				}
			}
		}
		return false;
	}
	
	
	public void deactivateBuzzer() throws IllegalArgumentException, CardException {
		final byte[] cmd = new byte[]{(byte) 0xFF, (byte) 0x00, (byte) 0x52, (byte) 0x00, (byte) 0x00};
		sendByteCommand(cmd);
	}
	
	
	public static boolean isValidKey(final String key) {
		return key != null && key.length() == 12 && Pattern.compile("^([0-9A-Fa-f]{2})+$").matcher(key).matches();
	}
	
	
	public static boolean isValidData(final String data) {
		return data.length() == 32 && Pattern.compile("^([0-9A-Fa-f]{2})+$").matcher(data).matches();
	}
	
	
	public static boolean isValidBlockNumber(final int blockNumber) {
		return 0 <= blockNumber && blockNumber <= 63;
	}
	
	
	public static byte[] hexStringToBytes(final String s) {
		final byte[] data = new byte[s.length()/2];
		for (int i = 0; i < s.length(); i+=2) {
			data[i/2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

}
