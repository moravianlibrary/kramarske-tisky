package cz.trinera.dkt.marc21;

import cz.trinera.dkt.ToolAvailabilityError;
import nu.xom.Document;
import org.yaz4j.Connection;
import org.yaz4j.PrefixQuery;
import org.yaz4j.Record;
import org.yaz4j.ResultSet;
import org.yaz4j.exception.ZoomException;

public class MarcXmlProviderImpl implements MarcXmlProvider {

    private final String host;
    private final int port;
    private final String base;

    public MarcXmlProviderImpl(String host, int port, String base) {
        this.host = host;
        this.port = port;
        this.base = base;
    }

    @Override
    public void checkAvailable() throws ToolAvailabilityError {
        //TODO: implement
    }

    @Override
    public Document getMarcXml(String barcode) {
        try (Connection con = new Connection(host, port)) {
            //con.setSyntax("usmarc");
            con.setSyntax("marc21");
            con.setDatabaseName(base); //TODO: check if this is correct
            con.connect();
            ResultSet set = con.search(new PrefixQuery("find @attr 1=1063 " + barcode));
            Record rec = set.getRecord(0);
            System.out.println(rec.render());
            //TODO: extract marc text from record
            //TODO: convert marc text to marc xml
            return null;
        } catch (ZoomException ze) {
            throw new RuntimeException(ze);
        }
    }
}
