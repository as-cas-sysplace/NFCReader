

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.smartcardio.CardException;

import de.cas.open.commandlineclient.EIMClient;
import de.cas.open.server.api.exceptions.BusinessException;
import de.cas.open.server.api.exceptions.DataLayerException;
import de.cas.open.vsm.server.common.types.SaveNFCLoginRequest;
import de.cas.open.vsm.server.common.types.SaveNFCLoginResponse;

public class NFCLoginTest extends EIMClient {

	public NFCLoginTest() {
		super(new String[] { "-u", "usr", "-h",
				"url", "-p", "pwd" });
	}

	public static void main(final String[] args) throws BusinessException,
			DataLayerException, CardException, UnknownHostException {
		new NFCLoginTest().run();
	}

	private void run() throws BusinessException, DataLayerException, CardException, UnknownHostException {
		final NFCReader reader = new NFCReader();

		while(reader.existsTerminal()) {
			reader.listen();
			final String uid = reader.getUID();

			final String ip;
//			final InetAddress addr = InetAddress.getLocalHost();
//			ip = addr.getHostAddress();
			ip = "192.168.73.132";

			generateNFCLogins(uid, ip);

			reader.getTerminal().waitForCardAbsent(0);
		}
	}

	private void generateNFCLogins(final String uid, final String ip) throws BusinessException, DataLayerException {
		final SaveNFCLoginRequest saveRequest = new SaveNFCLoginRequest();
		saveRequest.setIdentifier(ip);
		saveRequest.setUser(uid);
		System.out.println(saveRequest);

		final SaveNFCLoginResponse saveResult = (SaveNFCLoginResponse) eimInterface.executeUnauthenticated(saveRequest);
		System.out.println(saveResult);
	}

}
