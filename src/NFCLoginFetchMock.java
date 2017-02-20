

import java.net.InetAddress;
import java.net.UnknownHostException;

import de.cas.open.commandlineclient.EIMClient;
import de.cas.open.server.api.exceptions.BusinessException;
import de.cas.open.server.api.exceptions.DataLayerException;
import de.cas.open.vsm.server.common.types.FetchNFCLoginRequest;
import de.cas.open.vsm.server.common.types.FetchNFCLoginResponse;


public class NFCLoginFetchMock extends EIMClient {

	public NFCLoginFetchMock() {
		super(new String[] { "-u", "demo/Robert Glaser", "-h",
				"http://localhost:9080/vsm", "-p", "Value" });
	}

	public static void main(final String[] args) throws BusinessException,
			DataLayerException, UnknownHostException {
		new NFCLoginFetchMock().run();
	}

	private void run() throws BusinessException, DataLayerException, UnknownHostException {
		final String ip;
		final InetAddress addr = InetAddress.getLocalHost();
		ip = addr.getHostAddress();

		final FetchNFCLoginRequest request = new FetchNFCLoginRequest(ip);
		final FetchNFCLoginResponse response = (FetchNFCLoginResponse) eimInterface.executeUnauthenticated(request);

		final String password = response.getPassword();
		final String username = response.getUsername();
		System.out.println(password + username);
	}

}

