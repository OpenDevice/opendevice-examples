package opendevice.io.iotcar;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import br.com.criativasoft.opendevice.connection.AbstractConnection;
import br.com.criativasoft.opendevice.connection.AbstractStreamConnection;
import br.com.criativasoft.opendevice.connection.exception.ConnectionException;
import br.com.criativasoft.opendevice.connection.message.Message;
import br.com.criativasoft.opendevice.connection.serialize.DefaultSteamReader;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {


    }

    private class FakeConnection extends AbstractStreamConnection{

        @Override
        public void connect() throws ConnectionException {

        }

        @Override
        public void notifyListeners(Message message) {
            System.out.println(message);
        }
    }
}