package com.redhat.ads.openshift.util;



import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.servlet.http.HttpSession;


/**
 * This class provides a way to get the size of a session object.
 *
 * @author <a href="mailto:fzammetti@omnytex.com">Frank W. Zammetti</a>.
 */
public class SessionSize {


    /**
     * This static initializer block tries to load all the classes this one
     * depends on (those not from standard Java anyway) and prints an error
     * meesage if any cannot be loaded for any reason.
     */
    static {
        try {
            Class.forName("javax.servlet.http.HttpSession");
        } catch (ClassNotFoundException e) {
            System.err.println("SessionSize" +
                    " could not be loaded by classloader because classes it depends" +
                    " on could not be found in the classpath...");
            e.printStackTrace();
        }
    }


    /**
     * Reference to session this object is associated with.
     */
    private HttpSession session;


    /**
     * Collection of reasons why session size could not be determined.
     */
    private ArrayList failureReasons = new ArrayList();


    /**
     * This flag determines if the session size will continue to be calculated
     * even when a non-serializable objects are found.  This can be useful when
     * you know your session won't pass this tests here but want a size, and
     * understand it will always be an understated size, but don't care.
     * This feature was suggested by Wendy Smoak.
     */
    private boolean ignoreNonSerializable;



    /**
     * constructor.
     *
     * @param inSession object this object will be associated with.
     */
    public SessionSize(HttpSession inSession) {

        session = inSession;

    } // End SessionSize().


    /**
     * Mutator for the ignoreNonSerializable field.
     *
     * @param inSetting True to calculate session size regardless of whether
     *                  non-serializable objects are found in it, false
     *                  otherwise (false is the default).
     */
    public void setIgnoreNonSerializable(boolean inSetting) {

        ignoreNonSerializable = inSetting;

    } // End setIgnoreNonSerializable().


    /**
     * This method is used to get the total size of a current, valid HttpSession
     * object it is passed.
     *
     * @return The total size of session in bytes, or -1 if the size could not
     *         be determined.
     */
    public int getSessionSize() {

        Enumeration en   = session.getAttributeNames();
        String      name = null;
        Object      obj  = null;
        String      serialOut;
        String      sizeDelimiter = "size=";
        int         sizeIndex;
        int         objSize;
        int         totalSize = 0;
        while (en.hasMoreElements()) {
            name      = (String)en.nextElement();
            obj       = session.getAttribute(name);
            serialOut = serializiableTest(obj);
            if ((sizeIndex = serialOut.lastIndexOf(sizeDelimiter)) > 0) {
                objSize = Integer.parseInt(serialOut.substring(sizeIndex +
                        sizeDelimiter.length(), serialOut.lastIndexOf(')')));
                totalSize += objSize;
            } else {
                failureReasons.add(serialOut);
            }
        }
        if (!failureReasons.isEmpty() && !ignoreNonSerializable) {
            return -1;
        }
        return totalSize;

    } // End getSessionSize().


    /**
     * When getSessionSize() returns -1, this method can be called to retrieve
     * the collection of reasons it failed.
     *
     * @return Collection of failures why the session size could not be
     *         determiend.
     */
    public List whyFailed() {

        return failureReasons;

    }

    /**
     * This method is used by the getContextSize() method to determine if a
     * given object is serializable.
     *
     * @param  obj The object to test.
     * @return     A response string detailing the outcome of the test.
     */
    public static String serializiableTest(Object obj) {

        String ans = "ok";
        if (obj == null) {
            return "Object is null";
        } else {
            try {
                ByteArrayOutputStream bastream = new ByteArrayOutputStream();
                ObjectOutputStream p = new ObjectOutputStream(bastream);
                p.writeObject(obj);
                ans = "OK (size=" + bastream.size() + ")";
            } catch (NotSerializableException ex) {
                Field[] fields = obj.getClass().getDeclaredFields();
                ans = "NOT SERIALIZABLE (fields=" + String.valueOf(fields) + ")";
                Object fldObj = null;
                if (fields != null && (fields.length != 0)) {
                    StringBuffer sb = new StringBuffer("\n" + ans + "[");
                    for (int i = 0; i < fields.length; i++) {
                        sb.append(fields[i].getName());
                        try {
                            if (obj != null) {
                                fldObj = getFieldWithPrivilege(fields[i], obj);
                            }
                            sb.append("::");
                            if (fldObj == null) {
                                sb.append("<field null>");
                            } else {
                                sb.append(serializiableTest(fldObj));
                            }
                        } catch (IllegalArgumentException aex) {
                            sb.append("::");
                            sb.append("ILLEGAL ARGUMENT EXCEPTION");
                        }
                        if (i != fields.length - 1) {
                            sb.append('\n');
                        }
                    }
                    sb.append("]");
                    ans = sb.toString();
                }
            } catch (IOException ex) {
                ans = "IOEXCEPTION: " + ex.getMessage();
            }
        }
        return obj.getClass().getName() + " is " + ans;

    }

    /**
     * This method is used by the serializiableTest() method to get the
     * needed priveleges on a given field of a given object needed to
     * perform the serializable test.
     *
     * @param  fld The field to get priveleges on.
     * @param  obj The object to test.
     * @return     A Priveleged reference to the field essentially.
     */
    public static Object getFieldWithPrivilege(Field fld, Object obj) {

        final Object obj2 = obj;
        final Field  fld2 = fld;
        return AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        try {
                            return fld2.get(obj2);
                        } catch (IllegalAccessException ex) {
                            return null;
                        }
                    }
                }
        );

    }


}

