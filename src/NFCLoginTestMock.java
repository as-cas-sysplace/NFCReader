

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.cas.open.commandlineclient.EIMClient;
import de.cas.open.server.api.exceptions.BusinessException;
import de.cas.open.server.api.exceptions.DataLayerException;
import de.cas.open.vsm.server.common.types.SaveNFCLoginRequest;
import de.cas.open.vsm.server.common.types.SaveNFCLoginResponse;


public class NFCLoginTestMock extends EIMClient {

	public NFCLoginTestMock() {
		super(new String[] { "-u", "demo/Robert Glaser", "-h",
				"http://localhost:9080/vsm", "-p", "Value" });
	}

	public static void main(final String[] args) throws BusinessException,
			DataLayerException, UnknownHostException {
		new NFCLoginTestMock().run();
	}

	private void run() throws BusinessException, DataLayerException, UnknownHostException {
		final String uid = "AC42CC3F";
		final String ip;
		final InetAddress addr = InetAddress.getLocalHost();
		ip = addr.getHostAddress();

		generateNFCLogins(uid, ip);

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

