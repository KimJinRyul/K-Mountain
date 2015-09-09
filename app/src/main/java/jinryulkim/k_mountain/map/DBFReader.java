package jinryulkim.k_mountain.map;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * Created by jinryulkim on 15. 9. 8..
 */
public class DBFReader {
    public DBFReader(String s) {
        stream = null;
        fields = null;
        nextRecord = null;
        nFieldCount = 0;
        try {
            init(new FileInputStream(s));
        }
        catch (FileNotFoundException filenotfoundexception) {
            filenotfoundexception.printStackTrace();
        }
    }

    public DBFReader(InputStream inputstream) {
        stream = null;
        fields = null;
        nextRecord = null;
        init(inputstream);
    }

    private void init(InputStream inputstream) {
        try {
            stream = new DataInputStream(inputstream);
            int i = readHeader();
            fields = new DBFField[i];
            int j = 1;
            for (int k = 0; k < i; k++) {
                fields[k] = readFieldHeader();
                if (fields[k] != null) {
                    nFieldCount++;
                    j += fields[k].getLength();
                }
            }

            nextRecord = new byte[j];
            try {
                stream.readFully(nextRecord);
            }
            catch (EOFException eofexception) {
                nextRecord = null;
                stream.close();
            }

            int pos = 0;
            boolean hasBegin = false;
            for (int p = 0; p < j; p++) {
                if (nextRecord[p] == 0X20 || nextRecord[p] == 0X2A) {
                    hasBegin = true;
                    pos = p;
                    break;
                }
            }
            if (pos > 0) {
                byte[] others = new byte[pos];
                stream.readFully(others);

                for (int p = 0; p < j - pos; p++) {
                    nextRecord[p] = nextRecord[p + pos];
                }
                for (int p = 0; p < pos; p++) {
                    nextRecord[j - p - 1] = others[pos - p - 1];
                }
            }

        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private int readHeader() throws IOException {
        byte abyte0[] = new byte[16];
        try {
            stream.readFully(abyte0);
        }
        catch (EOFException eofexception) {
            eofexception.printStackTrace();
        }
        int i = abyte0[8];
        if (i < 0)
            i += 256;
        i += 256 * abyte0[9];
        i = --i / 32;
        i--;
        try {
            stream.readFully(abyte0);
        }
        catch (EOFException eofexception1) {
           eofexception1.printStackTrace();
        }
        return i;
    }

    private DBFField readFieldHeader() throws IOException {

        byte abyte0[] = new byte[16];
        try {
            stream.readFully(abyte0);
        }
        catch (EOFException eofexception) {
            eofexception.printStackTrace();
        }

        if (abyte0[0] == 0X0D || abyte0[0] == 0X00) {
            stream.readFully(abyte0);
            return null;
        }

        StringBuffer stringbuffer = new StringBuffer(10);
        int i = 0;
        for (i = 0; i < 10; i++) {
            if (abyte0[i] == 0)
                break;
        }
        stringbuffer.append(new String(abyte0, 0, i));

        char c = (char) abyte0[11];
        try {
            stream.readFully(abyte0);
        }
        catch (EOFException eofexception1) {
            eofexception1.printStackTrace();
        }

        int j = abyte0[0];
        int k = abyte0[1];
        if (j < 0)
            j += 256;
        if (k < 0)
            k += 256;
        return new DBFField(stringbuffer.toString(), c, j, k);
    }

    public int getFieldCount() {
        return nFieldCount; //fields.length;
    }

    public DBFField getField(int i) {
        return fields[i];
    }

    public boolean hasNextRecord() {
        return nextRecord != null;
    }

    public Object[] nextRecord() {
        if (!hasNextRecord())
            return null;

        Object aobj[] = new Object[nFieldCount];
        int i = 1;
        for (int j = 0; j < aobj.length; j++) {
            int k = fields[j].getLength();
            StringBuffer stringbuffer = new StringBuffer(k);
            stringbuffer.append(new String(nextRecord, i, k));
            aobj[j] = fields[j].parse(stringbuffer.toString());
            i += fields[j].getLength();
        }

        try {
            stream.readFully(nextRecord);
        }
        catch (EOFException eofexception) {
            nextRecord = null;
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
        return aobj;
    }

    public Object[] nextRecord(Charset charset) {
        if (!hasNextRecord())
            return null;

        Object aobj[] = new Object[nFieldCount];
        int i = 1;
        for (int j = 0; j < aobj.length; j++) {
            int k = fields[j].getLength();
            StringBuffer stringbuffer = new StringBuffer(k);
            stringbuffer.append(new String(nextRecord, i, k, charset));
            aobj[j] = fields[j].parse(stringbuffer.toString());
            i += fields[j].getLength();
        }

        try {
            stream.readFully(nextRecord);
        }
        catch (EOFException eofexception) {
            nextRecord = null;
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
        return aobj;
    }

    public void close()  {
        nextRecord = null;
        try {
            stream.close();
        }
        catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }
    private DataInputStream stream;
    private DBFField fields[];
    private byte nextRecord[];
    private int nFieldCount;
}
