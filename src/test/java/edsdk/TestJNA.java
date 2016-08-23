package edsdk;

import org.junit.Test;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

/**
 * test basic JNA see https://de.wikipedia.org/wiki/Java_Native_Access
 * @author wf
 *
 */
public class TestJNA {

    /**
     * Einfaches Beispiel einer Deklaration und Nutzung einer Dynamischen
     * Programmbibliothek bzw. "shared library".
     */
    public interface CLibrary extends Library {

        CLibrary INSTANCE = (CLibrary) Native.loadLibrary( ( Platform.isWindows()
                                                                                 ? "msvcrt"
                                                                                 : "c" ), CLibrary.class );

        void printf( String format, Object ... args );
    }

    @Test
    public void testJNA() {
        String[] args = { "Welcome", "to", "JNA" };
        CLibrary.INSTANCE.printf( "Hello, World\n" );
        for ( int i = 0; i < args.length; i++ ) {
            CLibrary.INSTANCE.printf( "Argument %d: %s\n", i, args[i] );
        }
    }

}
