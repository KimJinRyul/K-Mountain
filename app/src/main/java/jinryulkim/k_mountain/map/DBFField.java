package jinryulkim.k_mountain.map;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by jinryulkim on 15. 9. 8..
 */
public class DBFField {

    public DBFField(String s, char c, int i, int j) {
        name = s;
        type = c;
        length = i;
        decimalCount = j;
    }

    public String getName() {
        return name;
    }

    public char getType() {
        return type;
    }

    public int getLength() {
        return length;
    }

    public int getDecimalCount() {
        return decimalCount;
    }

    public String format(Object obj) {
        if (type == 'N' || type == 'F') {
            if (obj == null) {
                obj = new Double(0.0D);
            }
            if (obj instanceof Number) {
                Number number = (Number) obj;
                StringBuffer stringbuffer = new StringBuffer(getLength());
                for (int i = 0; i < getLength(); i++) {
                    stringbuffer.append("#");

                }
                if (getDecimalCount() > 0) {
                    stringbuffer.setCharAt(getLength() - getDecimalCount() - 1, '.');
                }
                DecimalFormat decimalformat = new DecimalFormat(stringbuffer.toString());
                String s1 = decimalformat.format(number);
                int k = getLength() - s1.length();
                StringBuffer stringbuffer2 = new StringBuffer(k);
                for (int l = 0; l < k; l++) {
                    stringbuffer2.append(" ");

                }
                return stringbuffer2 + s1;
            }
        }
        if (type == 'C') {
            if (obj == null) {
                obj = "";
            }
            if (obj instanceof String) {
                String s = (String) obj;
                StringBuffer stringbuffer1 = new StringBuffer(getLength() - s.length());
                for (int j = 0; j < getLength() - s.length(); j++) {
                    stringbuffer1.append(' ');

                }
                return s + stringbuffer1;
            }
        }
        if (type == 'L') {
            if (obj == null) {
                obj = new Boolean(false);
            }
            if (obj instanceof Boolean) {
                Boolean boolean1 = (Boolean) obj;
                return boolean1.booleanValue() ? "Y" : "N";
            }
        }
        if (type == 'D') {
            if (obj == null) {
                obj = new Date();
            }
            if (obj instanceof Date) {
                Date date = (Date) obj;
                SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
                return simpledateformat.format(date);
            }
        }
        return null;
    }

    public Object parse(String s) {
        s = s.trim();
        if (type == 'N' || type == 'F') {
            if (s.equals("")) {
                s = "0";
            }
            try {
                if (getDecimalCount() == 0) {
                    return new Long(s);
                }
                else {
                    return new Double(s);
                }
            }
            catch (NumberFormatException numberformatexception) {
                numberformatexception.printStackTrace();
            }
        }
        if (type == 'C') {
            return s;
        }
        if (type == 'L') {
            if (s.equals("Y") || s.equals("y") || s.equals("T") || s.equals("t")) {
                return new Boolean(true);
            }
            if (s.equals("N") || s.equals("n") || s.equals("F") || s.equals("f")) {
                return new Boolean(false);
            }
        }
        if (type == 'D') {
            SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyyMMdd");
            try {
                if ("".equals(s)) {
                    return null;
                }
                else {
                    return simpledateformat.parse(s);
                }
            }
            catch (ParseException parseexception) {
                parseexception.printStackTrace();
            }
        }

        return null;
    }

    public String toString() {
        return name;
    }

    private String name;
    private char type;
    private int length;
    private int decimalCount;

}
